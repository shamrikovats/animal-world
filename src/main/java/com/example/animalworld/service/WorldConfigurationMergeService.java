package com.example.animalworld.service;

import com.example.animalworld.model.entity.FeedingRule;
import com.example.animalworld.model.entity.PlantSpeciesConfiguration;
import com.example.animalworld.model.entity.SpeciesConfiguration;
import com.example.animalworld.model.entity.WorldSettings;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Shamrikova Tatiana
 */
@Component
public class WorldConfigurationMergeService {

    public WorldSettings mergeSettings(WorldSettings base, WorldSettings patch, Integer worldId) {
        if (patch == null) {
            return withWorldId(base, worldId);
        }

        return new WorldSettings(
                worldId,
                patch.tickDuration() == null ? base.tickDuration() : patch.tickDuration(),
                patch.height() == null ? base.height() : patch.height(),
                patch.width() == null ? base.width() : patch.width(),
                patch.startHungryPercent() == null ? base.startHungryPercent() : patch.startHungryPercent(),
                patch.startPredatorCounter() == null ? base.startPredatorCounter() : patch.startPredatorCounter(),
                patch.startHerbivoreCounter() == null ? base.startHerbivoreCounter() : patch.startHerbivoreCounter(),
                patch.startPlantsMass() == null ? base.startPlantsMass() : patch.startPlantsMass()
        );
    }

    public List<SpeciesConfiguration> mergeSpeciesConfigurations(
            List<SpeciesConfiguration> defaults,
            List<SpeciesConfiguration> patches,
            Integer worldId
    ) {
        Map<Integer, SpeciesConfiguration> merged = defaults.stream()
                .collect(Collectors.toMap(SpeciesConfiguration::speciesId, configuration -> withWorldId(configuration, worldId)));

        if (patches == null || patches.isEmpty()) {
            return sortSpeciesConfigurations(merged.values());
        }

        for (SpeciesConfiguration patch : patches) {
            SpeciesConfiguration base = Optional.ofNullable(merged.get(patch.speciesId()))
                    .orElseThrow(() -> new IllegalArgumentException("Default species configuration not found for speciesId: %s".formatted(patch.speciesId())));
            merged.put(patch.speciesId(), mergeSpeciesConfiguration(base, patch, worldId));
        }

        return sortSpeciesConfigurations(merged.values());
    }

    public SpeciesConfiguration mergeSpeciesConfiguration(
            SpeciesConfiguration base,
            SpeciesConfiguration patch,
            Integer worldId
    ) {
        return new SpeciesConfiguration(
                worldId,
                base.speciesId(),
                patch.maxCoexistCount() == null ? base.maxCoexistCount() : patch.maxCoexistCount(),
                patch.weight() == null ? base.weight() : patch.weight(),
                patch.speedCells() == null ? base.speedCells() : patch.speedCells(),
                patch.fullTankWeight() == null ? base.fullTankWeight() : patch.fullTankWeight(),
                patch.minFoodPercent() == null ? base.minFoodPercent() : patch.minFoodPercent(),
                patch.maxChildrenCount() == null ? base.maxChildrenCount() : patch.maxChildrenCount(),
                patch.pregnancyPeriod() == null ? base.pregnancyPeriod() : patch.pregnancyPeriod(),
                patch.lostFoodForTick() == null ? base.lostFoodForTick() : patch.lostFoodForTick(),
                patch.startCount() == null ? base.startCount() : patch.startCount()
        );
    }

    public List<PlantSpeciesConfiguration> mergePlantConfigurations(
            List<PlantSpeciesConfiguration> defaults,
            List<PlantSpeciesConfiguration> patches,
            Integer worldId
    ) {
        Map<Integer, PlantSpeciesConfiguration> merged = defaults.stream()
                .collect(Collectors.toMap(
                        PlantSpeciesConfiguration::plantSpeciesId,
                        configuration -> withWorldId(configuration, worldId)
                ));

        if (patches == null || patches.isEmpty()) {
            return sortPlantConfigurations(merged.values());
        }

        for (PlantSpeciesConfiguration patch : patches) {
            PlantSpeciesConfiguration base = Optional.ofNullable(merged.get(patch.plantSpeciesId()))
                    .orElseThrow(() -> new IllegalArgumentException("Default plant configuration not found for plantSpeciesId: %s".formatted(patch.plantSpeciesId())));
            merged.put(patch.plantSpeciesId(), mergePlantConfiguration(base, patch, worldId));
        }

        return sortPlantConfigurations(merged.values());
    }

    public PlantSpeciesConfiguration mergePlantConfiguration(
            PlantSpeciesConfiguration base,
            PlantSpeciesConfiguration patch,
            Integer worldId
    ) {
        return new PlantSpeciesConfiguration(
                worldId,
                base.plantSpeciesId(),
                patch.maxRepairSpeed() == null ? base.maxRepairSpeed() : patch.maxRepairSpeed(),
                patch.weight() == null ? base.weight() : patch.weight(),
                patch.startCount() == null ? base.startCount() : patch.startCount()
        );
    }

    public List<FeedingRule> mergeFeedingRules(
            List<FeedingRule> defaults,
            List<FeedingRule> patches,
            Integer worldId
    ) {
        Map<FeedingRuleKey, FeedingRule> merged = defaults.stream()
                .collect(Collectors.toMap(this::toKey, rule -> withWorldId(rule, worldId)));

        if (patches == null || patches.isEmpty()) {
            return sortFeedingRules(merged.values());
        }

        for (FeedingRule patch : patches) {
            merged.put(toKey(patch), new FeedingRule(
                    null,
                    worldId,
                    patch.speciesId(),
                    patch.preySpeciesId(),
                    patch.preyPlantSpeciesId(),
                    patch.foodType(),
                    patch.probability()
            ));
        }

        return sortFeedingRules(merged.values());
    }

    public List<SpeciesConfiguration> sortSpeciesConfigurations(Collection<SpeciesConfiguration> configurations) {
        return configurations.stream()
                .sorted(Comparator.comparing(SpeciesConfiguration::speciesId))
                .toList();
    }

    public List<PlantSpeciesConfiguration> sortPlantConfigurations(Collection<PlantSpeciesConfiguration> configurations) {
        return configurations.stream()
                .sorted(Comparator.comparing(PlantSpeciesConfiguration::plantSpeciesId))
                .toList();
    }

    public List<FeedingRule> sortFeedingRules(Collection<FeedingRule> rules) {
        return rules.stream()
                .sorted(Comparator
                        .comparing(FeedingRule::speciesId)
                        .thenComparing(rule -> Optional.ofNullable(rule.preySpeciesId()).orElse(Integer.MAX_VALUE))
                        .thenComparing(rule -> Optional.ofNullable(rule.preyPlantSpeciesId()).orElse(Integer.MAX_VALUE)))
                .toList();
    }

    private WorldSettings withWorldId(WorldSettings settings, Integer worldId) {
        return new WorldSettings(
                worldId,
                settings.tickDuration(),
                settings.height(),
                settings.width(),
                settings.startHungryPercent(),
                settings.startPredatorCounter(),
                settings.startHerbivoreCounter(),
                settings.startPlantsMass()
        );
    }

    private SpeciesConfiguration withWorldId(SpeciesConfiguration configuration, Integer worldId) {
        return new SpeciesConfiguration(
                worldId,
                configuration.speciesId(),
                configuration.maxCoexistCount(),
                configuration.weight(),
                configuration.speedCells(),
                configuration.fullTankWeight(),
                configuration.minFoodPercent(),
                configuration.maxChildrenCount(),
                configuration.pregnancyPeriod(),
                configuration.lostFoodForTick(),
                configuration.startCount()
        );
    }

    private PlantSpeciesConfiguration withWorldId(PlantSpeciesConfiguration configuration, Integer worldId) {
        return new PlantSpeciesConfiguration(
                worldId,
                configuration.plantSpeciesId(),
                configuration.maxRepairSpeed(),
                configuration.weight(),
                configuration.startCount()
        );
    }

    private FeedingRule withWorldId(FeedingRule rule, Integer worldId) {
        return new FeedingRule(
                rule.id(),
                worldId,
                rule.speciesId(),
                rule.preySpeciesId(),
                rule.preyPlantSpeciesId(),
                rule.foodType(),
                rule.probability()
        );
    }

    private FeedingRuleKey toKey(FeedingRule rule) {
        return new FeedingRuleKey(rule.speciesId(), rule.preySpeciesId(), rule.preyPlantSpeciesId());
    }

    private record FeedingRuleKey(
            Integer speciesId,
            Integer preySpeciesId,
            Integer preyPlantSpeciesId
    ) {
    }
}
