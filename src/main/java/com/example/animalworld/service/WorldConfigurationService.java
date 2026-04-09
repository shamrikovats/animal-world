package com.example.animalworld.service;

import com.example.animalworld.model.entity.FeedingRule;
import com.example.animalworld.model.entity.PlantSpeciesConfiguration;
import com.example.animalworld.model.entity.SpeciesConfiguration;
import com.example.animalworld.model.entity.WorldSettings;
import com.example.animalworld.repository.FeedingRuleRepository;
import com.example.animalworld.repository.PlantSpeciesConfigurationRepository;
import com.example.animalworld.repository.SpeciesConfigurationRepository;
import com.example.animalworld.repository.WorldSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Shamrikova Tatiana
 */
@Service
public class WorldConfigurationService {
    private final WorldLookupService worldLookupService;
    private final WorldSettingsRepository worldSettingsRepository;
    private final SpeciesConfigurationRepository speciesConfigurationRepository;
    private final PlantSpeciesConfigurationRepository plantSpeciesConfigurationRepository;
    private final FeedingRuleRepository feedingRuleRepository;
    private final WorldConfigurationMergeService mergeService;
    private final WorldConfigurationValidationService validationService;

    WorldConfigurationService(
            WorldLookupService worldLookupService,
            WorldSettingsRepository worldSettingsRepository,
            SpeciesConfigurationRepository speciesConfigurationRepository,
            PlantSpeciesConfigurationRepository plantSpeciesConfigurationRepository,
            FeedingRuleRepository feedingRuleRepository,
            WorldConfigurationMergeService mergeService,
            WorldConfigurationValidationService validationService
    ) {
        this.worldLookupService = worldLookupService;
        this.worldSettingsRepository = worldSettingsRepository;
        this.speciesConfigurationRepository = speciesConfigurationRepository;
        this.plantSpeciesConfigurationRepository = plantSpeciesConfigurationRepository;
        this.feedingRuleRepository = feedingRuleRepository;
        this.mergeService = mergeService;
        this.validationService = validationService;
    }

    @Transactional
    public void materializeWorldConfiguration(
            Integer worldId,
            WorldSettings settingsPatch,
            List<SpeciesConfiguration> speciesConfigurationPatches,
            List<PlantSpeciesConfiguration> plantConfigurationPatches,
            List<FeedingRule> feedingRulePatches
    ) {
        worldLookupService.findById(worldId);
        validationService.validateSpeciesConfigurations(speciesConfigurationPatches);
        validationService.validatePlantConfigurations(plantConfigurationPatches);
        validationService.validateFeedingRules(feedingRulePatches);

        WorldSettings settings = mergeService.mergeSettings(
                worldSettingsRepository.findDefault(),
                settingsPatch,
                worldId
        );
        List<SpeciesConfiguration> speciesConfigurations = mergeService.mergeSpeciesConfigurations(
                speciesConfigurationRepository.findAllDefaults(),
                speciesConfigurationPatches,
                worldId
        );
        List<PlantSpeciesConfiguration> plantConfigurations = mergeService.mergePlantConfigurations(
                plantSpeciesConfigurationRepository.findAllDefaults(),
                plantConfigurationPatches,
                worldId
        );
        List<FeedingRule> feedingRules = mergeService.mergeFeedingRules(
                feedingRuleRepository.findAllDefaults(),
                feedingRulePatches,
                worldId
        );

        saveConfiguration(worldId, settings, speciesConfigurations, plantConfigurations, feedingRules);
    }

    @Transactional
    public void ensureMaterialized(Integer worldId) {
        worldLookupService.findById(worldId);

        WorldSettings existingSettings = worldSettingsRepository.findByWorldId(worldId).orElse(null);
        List<SpeciesConfiguration> existingSpecies = speciesConfigurationRepository.findAllByWorldId(worldId);
        List<PlantSpeciesConfiguration> existingPlants = plantSpeciesConfigurationRepository.findAllByWorldId(worldId);
        List<FeedingRule> existingFeedingRules = feedingRuleRepository.findAllByWorldId(worldId);

        if (existingSettings != null
                && !existingSpecies.isEmpty()
                && !existingPlants.isEmpty()
                && !existingFeedingRules.isEmpty()) {
            return;
        }

        materializeWorldConfiguration(
                worldId,
                existingSettings,
                existingSpecies,
                existingPlants,
                existingFeedingRules
        );
    }

    public WorldSettings getSettings(Integer worldId) {
        ensureMaterialized(worldId);
        return worldSettingsRepository.findByWorldId(worldId).orElseThrow();
    }

    @Transactional
    public WorldSettings updateSettings(Integer worldId, WorldSettings settingsPatch) {
        ensureMaterialized(worldId);
        WorldSettings merged = mergeService.mergeSettings(getSettings(worldId), settingsPatch, worldId);
        worldSettingsRepository.upsert(merged);
        return merged;
    }

    public List<SpeciesConfiguration> getSpeciesConfigurations(Integer worldId) {
        ensureMaterialized(worldId);
        return speciesConfigurationRepository.findAllByWorldId(worldId);
    }

    @Transactional
    public SpeciesConfiguration updateSpeciesConfiguration(Integer worldId, Integer speciesId, SpeciesConfiguration patch) {
        ensureMaterialized(worldId);
        validationService.validateSpeciesPath(speciesId, patch.speciesId());
        validationService.validateSpeciesConfigurations(List.of(patch));

        SpeciesConfiguration current = speciesConfigurationRepository.findByWorldIdAndSpeciesId(worldId, speciesId).orElseThrow();
        SpeciesConfiguration merged = mergeService.mergeSpeciesConfiguration(current, patch, worldId);
        speciesConfigurationRepository.upsert(merged);
        return merged;
    }

    public List<PlantSpeciesConfiguration> getPlantConfigurations(Integer worldId) {
        ensureMaterialized(worldId);
        return plantSpeciesConfigurationRepository.findAllByWorldId(worldId);
    }

    @Transactional
    public PlantSpeciesConfiguration updatePlantConfiguration(
            Integer worldId,
            Integer plantSpeciesId,
            PlantSpeciesConfiguration patch
    ) {
        ensureMaterialized(worldId);
        validationService.validatePlantPath(plantSpeciesId, patch.plantSpeciesId());
        validationService.validatePlantConfigurations(List.of(patch));

        PlantSpeciesConfiguration current = plantSpeciesConfigurationRepository
                .findByWorldIdAndPlantSpeciesId(worldId, plantSpeciesId)
                .orElseThrow();
        PlantSpeciesConfiguration merged = mergeService.mergePlantConfiguration(current, patch, worldId);
        plantSpeciesConfigurationRepository.upsert(merged);
        return merged;
    }

    public List<FeedingRule> getFeedingRules(Integer worldId) {
        ensureMaterialized(worldId);
        return feedingRuleRepository.findAllByWorldId(worldId);
    }

    @Transactional
    public List<FeedingRule> updateFeedingRules(Integer worldId, List<FeedingRule> rules) {
        ensureMaterialized(worldId);
        validationService.validateFeedingRules(rules);

        Map<FeedingRuleKey, FeedingRule> currentRules = feedingRuleRepository.findAllByWorldId(worldId).stream()
                .collect(Collectors.toMap(this::toKey, Function.identity()));

        for (FeedingRule rule : rules) {
            FeedingRuleKey key = toKey(rule);
            FeedingRule existing = currentRules.get(key);
            FeedingRule worldRule = new FeedingRule(
                    existing == null ? null : existing.id(),
                    worldId,
                    rule.speciesId(),
                    rule.preySpeciesId(),
                    rule.preyPlantSpeciesId(),
                    rule.foodType(),
                    rule.probability()
            );
            feedingRuleRepository.upsertWorldRule(worldRule);
            currentRules.put(key, worldRule);
        }

        return feedingRuleRepository.findAllByWorldId(worldId);
    }

    private void saveConfiguration(
            Integer worldId,
            WorldSettings settings,
            List<SpeciesConfiguration> speciesConfigurations,
            List<PlantSpeciesConfiguration> plantConfigurations,
            List<FeedingRule> feedingRules
    ) {
        worldSettingsRepository.upsert(settings);
        speciesConfigurationRepository.replaceAllForWorld(worldId, speciesConfigurations);
        plantSpeciesConfigurationRepository.replaceAllForWorld(worldId, plantConfigurations);
        feedingRuleRepository.replaceAllForWorld(worldId, feedingRules);
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
