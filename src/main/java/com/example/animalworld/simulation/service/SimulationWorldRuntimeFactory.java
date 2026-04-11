package com.example.animalworld.simulation.service;

import com.example.animalworld.model.entity.BaseLocationRule;
import com.example.animalworld.model.entity.FeedingRule;
import com.example.animalworld.model.entity.PlantSpecies;
import com.example.animalworld.model.entity.PlantSpeciesConfiguration;
import com.example.animalworld.model.entity.Species;
import com.example.animalworld.model.entity.SpeciesConfiguration;
import com.example.animalworld.model.entity.WorldSettings;
import com.example.animalworld.repository.BaseLocationRuleRepository;
import com.example.animalworld.repository.LocationTypeRepository;
import com.example.animalworld.service.ReferenceDataService;
import com.example.animalworld.simulation.domain.config.RuntimeFeedingRule;
import com.example.animalworld.simulation.domain.config.RuntimeFeedingRuleKey;
import com.example.animalworld.simulation.domain.config.RuntimeLocationRule;
import com.example.animalworld.simulation.domain.config.RuntimePlantConfig;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;
import com.example.animalworld.simulation.domain.config.RuntimeWorldSettings;
import com.example.animalworld.simulation.domain.world.LocationType;
import com.example.animalworld.simulation.domain.world.SimulationCell;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Фабрика runtime-мира.
 * Собирает мир, клетки и runtime-конфиги, но не занимается стартовой популяцией.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class SimulationWorldRuntimeFactory {
    private final BaseLocationRuleRepository baseLocationRuleRepository;
    private final LocationTypeRepository locationTypeRepository;
    private final ReferenceDataService referenceDataService;

    SimulationWorldRuntimeFactory(
            BaseLocationRuleRepository baseLocationRuleRepository,
            LocationTypeRepository locationTypeRepository,
            ReferenceDataService referenceDataService
    ) {
        this.baseLocationRuleRepository = baseLocationRuleRepository;
        this.locationTypeRepository = locationTypeRepository;
        this.referenceDataService = referenceDataService;
    }

    public SimulationWorld create(SimulationWorldBootstrapData bootstrapData) {
        Map<Integer, Species> speciesById = referenceDataService.getSpeciesById();
        Map<Integer, PlantSpecies> plantSpeciesById = referenceDataService.getPlantSpeciesById();
        RuntimeWorldSettings runtimeSettings = toRuntimeSettings(bootstrapData.settings());
        Map<Integer, RuntimeSpeciesConfig> speciesConfigsById = toRuntimeSpeciesConfigs(
                bootstrapData.speciesConfigurations(),
                speciesById
        );
        Map<Integer, RuntimePlantConfig> plantConfigsById = toRuntimePlantConfigs(
                bootstrapData.plantConfigurations(),
                plantSpeciesById
        );
        Map<RuntimeFeedingRuleKey, RuntimeFeedingRule> feedingRulesByKey = toRuntimeFeedingRules(
                bootstrapData.feedingRules()
        );
        Map<Integer, Map<LocationType, RuntimeLocationRule>> locationRulesBySpeciesId = toRuntimeLocationRules();
        SimulationCell[][] cells = buildCells(runtimeSettings, bootstrapData.world().id());

        return new SimulationWorld(
                bootstrapData.world().id(),
                bootstrapData.world().name(),
                runtimeSettings,
                cells,
                bootstrapData.currentTick(),
                bootstrapData.world().status(),
                speciesConfigsById,
                plantConfigsById,
                feedingRulesByKey,
                locationRulesBySpeciesId
        );
    }

    private RuntimeWorldSettings toRuntimeSettings(WorldSettings settings) {
        return new RuntimeWorldSettings(
                settings.worldId(),
                settings.tickDuration(),
                settings.height(),
                settings.width(),
                settings.startHungryPercent(),
                settings.startPredatorCounter(),
                settings.startHerbivoreCounter(),
                settings.startPlantsMass()
        );
    }

    private Map<Integer, RuntimeSpeciesConfig> toRuntimeSpeciesConfigs(
            List<SpeciesConfiguration> speciesConfigurations,
            Map<Integer, Species> speciesById
    ) {
        Map<Integer, RuntimeSpeciesConfig> result = new LinkedHashMap<>();
        for (SpeciesConfiguration configuration : speciesConfigurations.stream()
                .sorted(Comparator.comparing(SpeciesConfiguration::speciesId))
                .toList()) {
            Species species = speciesById.get(configuration.speciesId());
            if (species == null) {
                throw new IllegalArgumentException("Species not found for runtime config: " + configuration.speciesId());
            }

            result.put(configuration.speciesId(), new RuntimeSpeciesConfig(
                    configuration.worldId(),
                    configuration.speciesId(),
                    species.name(),
                    species.predator(),
                    species.herbivore(),
                    configuration.maxCoexistCount(),
                    configuration.weight(),
                    configuration.speedCells(),
                    configuration.fullTankWeight(),
                    configuration.minFoodPercent(),
                    configuration.maxChildrenCount(),
                    configuration.pregnancyPeriod(),
                    configuration.lostFoodForTick(),
                    configuration.startCount()
            ));
        }
        return result;
    }

    private Map<Integer, RuntimePlantConfig> toRuntimePlantConfigs(
            List<PlantSpeciesConfiguration> plantConfigurations,
            Map<Integer, PlantSpecies> plantSpeciesById
    ) {
        Map<Integer, RuntimePlantConfig> result = new LinkedHashMap<>();
        for (PlantSpeciesConfiguration configuration : plantConfigurations.stream()
                .sorted(Comparator.comparing(PlantSpeciesConfiguration::plantSpeciesId))
                .toList()) {
            PlantSpecies plantSpecies = plantSpeciesById.get(configuration.plantSpeciesId());
            if (plantSpecies == null) {
                throw new IllegalArgumentException(
                        "Plant species not found for runtime config: " + configuration.plantSpeciesId()
                );
            }

            result.put(configuration.plantSpeciesId(), new RuntimePlantConfig(
                    configuration.worldId(),
                    configuration.plantSpeciesId(),
                    plantSpecies.biologicalName(),
                    configuration.maxRepairSpeed(),
                    configuration.weight(),
                    configuration.startCount()
            ));
        }
        return result;
    }

    private Map<RuntimeFeedingRuleKey, RuntimeFeedingRule> toRuntimeFeedingRules(List<FeedingRule> feedingRules) {
        Map<RuntimeFeedingRuleKey, RuntimeFeedingRule> result = new LinkedHashMap<>();
        for (FeedingRule feedingRule : feedingRules) {
            RuntimeFeedingRuleKey key = new RuntimeFeedingRuleKey(
                    feedingRule.speciesId(),
                    feedingRule.preySpeciesId(),
                    feedingRule.preyPlantSpeciesId()
            );
            result.put(key, new RuntimeFeedingRule(key, feedingRule.foodType(), feedingRule.probability()));
        }
        return result;
    }

    private Map<Integer, Map<LocationType, RuntimeLocationRule>> toRuntimeLocationRules() {
        Map<Integer, Map<LocationType, RuntimeLocationRule>> result = new LinkedHashMap<>();
        for (BaseLocationRule rule : baseLocationRuleRepository.findAll()) {
            LocationType locationType = LocationType.fromDatabaseName(rule.locationName());
            result.computeIfAbsent(rule.speciesId(), ignored -> new LinkedHashMap<>())
                    .put(locationType, new RuntimeLocationRule(rule.speciesId(), locationType, rule.survivalModifier()));
        }
        return result;
    }

    private SimulationCell[][] buildCells(RuntimeWorldSettings settings, Integer worldId) {
        List<LocationType> locationTypes = locationTypeRepository.findAllNames().stream()
                .map(LocationType::fromDatabaseName)
                .toList();
        if (locationTypes.isEmpty()) {
            throw new IllegalStateException("No location types configured in database");
        }

        SimulationCell[][] cells = new SimulationCell[settings.height()][settings.width()];
        for (int y = 0; y < settings.height(); y++) {
            for (int x = 0; x < settings.width(); x++) {
                LocationType locationType = locationTypes.get(Math.floorMod(worldId + x + (y * settings.width()), locationTypes.size()));
                cells[y][x] = new SimulationCell(x, y, locationType);
            }
        }
        return cells;
    }
}
