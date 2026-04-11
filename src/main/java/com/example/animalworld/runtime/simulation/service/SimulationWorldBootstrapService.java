package com.example.animalworld.runtime.simulation.service;

import com.example.animalworld.model.entity.*;
import com.example.animalworld.repository.BaseLocationRuleRepository;
import com.example.animalworld.repository.LocationTypeRepository;
import com.example.animalworld.runtime.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeFeedingRule;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeFeedingRuleKey;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeLocationRule;
import com.example.animalworld.runtime.simulation.domain.config.RuntimePlantConfig;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeSpeciesConfig;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeWorldSettings;
import com.example.animalworld.runtime.simulation.domain.flora.Plant;
import com.example.animalworld.runtime.simulation.domain.dictionary.Sex;
import com.example.animalworld.runtime.simulation.domain.world.LocationType;
import com.example.animalworld.runtime.simulation.domain.world.SimulationCell;
import com.example.animalworld.runtime.simulation.domain.world.SimulationWorld;
import com.example.animalworld.runtime.simulation.factory.AnimalFactory;
import com.example.animalworld.runtime.simulation.factory.RuntimeIdGenerator;
import com.example.animalworld.service.ReferenceDataService;
import com.example.animalworld.service.WorldConfigurationService;
import com.example.animalworld.service.WorldLookupService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Сервис, который поднимает runtime-мир из данных БД.
 * Он собирает настройки, строит клетки и создает стартовую популяцию животных и растений.
 * TODO Он мне не нравится, когда-нибудь разбить на утилитарные классы
 * @author Shamrikova Tatiana
 */
@Service
public class SimulationWorldBootstrapService {
    private final WorldLookupService worldLookupService;
    private final WorldConfigurationService worldConfigurationService;
    private final ReferenceDataService referenceDataService;
    private final BaseLocationRuleRepository baseLocationRuleRepository;
    private final LocationTypeRepository locationTypeRepository;
    private final AnimalFactory animalFactory;
    private final RuntimeIdGenerator runtimeIdGenerator;

    SimulationWorldBootstrapService(
            WorldLookupService worldLookupService,
            WorldConfigurationService worldConfigurationService,
            ReferenceDataService referenceDataService,
            BaseLocationRuleRepository baseLocationRuleRepository,
            LocationTypeRepository locationTypeRepository,
            AnimalFactory animalFactory,
            RuntimeIdGenerator runtimeIdGenerator
    ) {
        this.worldLookupService = worldLookupService;
        this.worldConfigurationService = worldConfigurationService;
        this.referenceDataService = referenceDataService;
        this.baseLocationRuleRepository = baseLocationRuleRepository;
        this.locationTypeRepository = locationTypeRepository;
        this.animalFactory = animalFactory;
        this.runtimeIdGenerator = runtimeIdGenerator;
    }

    public SimulationWorld bootstrap(Integer worldId) {
        World world = worldLookupService.findById(worldId);
        WorldSettings settings = worldConfigurationService.getSettings(worldId);
        List<SpeciesConfiguration> speciesConfigurations = worldConfigurationService.getSpeciesConfigurations(worldId);
        List<PlantSpeciesConfiguration> plantConfigurations = worldConfigurationService.getPlantConfigurations(worldId);
        List<FeedingRule> feedingRules = worldConfigurationService.getFeedingRules(worldId);

        Map<Integer, Species> speciesById = referenceDataService.getSpeciesById();
        Map<Integer, PlantSpecies> plantSpeciesById = referenceDataService.getPlantSpeciesById();

        RuntimeWorldSettings runtimeSettings = toRuntimeSettings(settings);
        Map<Integer, RuntimeSpeciesConfig> speciesConfigsById = toRuntimeSpeciesConfigs(speciesConfigurations, speciesById);
        Map<Integer, RuntimePlantConfig> plantConfigsById = toRuntimePlantConfigs(plantConfigurations, plantSpeciesById);
        Map<RuntimeFeedingRuleKey, RuntimeFeedingRule> feedingRulesByKey = toRuntimeFeedingRules(feedingRules);
        Map<Integer, Map<LocationType, RuntimeLocationRule>> locationRulesBySpeciesId = toRuntimeLocationRules();

        List<LocationType> locationTypes = locationTypeRepository.findAllNames().stream()
                .map(LocationType::fromDatabaseName)
                .toList();
        SimulationCell[][] cells = buildCells(runtimeSettings, locationTypes, worldId);

        SimulationWorld simulationWorld = new SimulationWorld(
                world.id(),
                world.name(),
                runtimeSettings,
                cells,
                currentTick(worldId),
                world.status(),
                speciesConfigsById,
                plantConfigsById,
                feedingRulesByKey,
                locationRulesBySpeciesId
        );

        populateAnimals(simulationWorld);
        populatePlants(simulationWorld);
        return simulationWorld;
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
                    .put(
                            locationType,
                            new RuntimeLocationRule(
                                    rule.speciesId(),
                                    locationType,
                                    rule.survivalModifier()
                            )
                    );
        }
        return result;
    }

    private SimulationCell[][] buildCells(RuntimeWorldSettings settings, List<LocationType> locationTypes, Integer worldId) {
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

    private void populateAnimals(SimulationWorld simulationWorld) {
        List<SimulationCell> cells = simulationWorld.allCells();
        for (RuntimeSpeciesConfig configuration : simulationWorld.speciesConfigsById().values()) {
            Random random = new Random(seed(simulationWorld.worldId(), configuration.speciesId()));
            for (int i = 0; i < configuration.startCount(); i++) {
                SimulationCell cell = selectCellForAnimal(cells, configuration, random);
                AnimalSpawnState spawnState = new AnimalSpawnState(
                        runtimeIdGenerator.nextId(),
                        random.nextBoolean() ? Sex.MALE : Sex.FEMALE,
                        initialSatiety(simulationWorld.settings(), configuration),
                        true,
                        false,
                        0
                );
                cell.addAnimal(animalFactory.create(configuration, spawnState));
            }
        }
    }

    private void populatePlants(SimulationWorld simulationWorld) {
        List<SimulationCell> cells = simulationWorld.allCells();
        List<RuntimePlantConfig> plantConfigs = new ArrayList<>(simulationWorld.plantConfigsById().values());
        plantConfigs.sort(Comparator.comparing(RuntimePlantConfig::plantSpeciesId));

        int fallbackPlantCount = Math.max(1, simulationWorld.settings().startPlantsMass());
        for (RuntimePlantConfig configuration : plantConfigs) {
            int plantCount = configuration.startCount() > 0 ? configuration.startCount() : fallbackPlantCount;
            int targetPlantsPerCell = targetPlantsPerCell(plantCount, cells.size());
            Random random = new Random(seed(simulationWorld.worldId(), configuration.plantSpeciesId()));
            for (int i = 0; i < plantCount; i++) {
                SimulationCell cell = selectCellForPlant(cells, configuration, targetPlantsPerCell, random);
                cell.addPlant(new Plant(runtimeIdGenerator.nextId(), configuration));
            }
        }
    }

    private SimulationCell selectCellForAnimal(
            List<SimulationCell> cells,
            RuntimeSpeciesConfig configuration,
            Random random
    ) {
        int startIndex = random.nextInt(cells.size());
        for (int offset = 0; offset < cells.size(); offset++) {
            SimulationCell cell = cells.get((startIndex + offset) % cells.size());
            cell.lock().lock();
            try {
                if (cell.canAcceptAnimal(configuration)) {
                    return cell;
                }
            } finally {
                cell.lock().unlock();
            }
        }

        throw new IllegalStateException("No free cell capacity for species: " + configuration.speciesName());
    }

    private double initialSatiety(RuntimeWorldSettings worldSettings, RuntimeSpeciesConfig configuration) {
        return configuration.effectiveFullTankWeight() * (100 - worldSettings.startHungryPercent()) / 100.0;
    }

    private SimulationCell selectCellForPlant(
            List<SimulationCell> cells,
            RuntimePlantConfig configuration,
            int targetPlantsPerCell,
            Random random
    ) {
        int startIndex = random.nextInt(cells.size());
        for (int offset = 0; offset < cells.size(); offset++) {
            SimulationCell cell = cells.get((startIndex + offset) % cells.size());
            if (cell.plantCount(configuration.plantSpeciesId()) < targetPlantsPerCell) {
                return cell;
            }
        }
        return cells.get(random.nextInt(cells.size()));
    }

    private int targetPlantsPerCell(int totalPlantCount, int cellCount) {
        return Math.max(1, (int) Math.ceil((double) totalPlantCount / cellCount));
    }

    private long seed(Integer worldId, Integer entityId) {
        return ((long) worldId * 1_000_003L) + entityId;
    }

    private long currentTick(Integer worldId) {
        return worldLookupService.getStats(worldId).stream()
                .mapToLong(WorldTickStat::tickNumber)
                .max()
                .orElse(0);
    }
}
