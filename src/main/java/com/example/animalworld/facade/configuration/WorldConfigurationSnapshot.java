package com.example.animalworld.facade.configuration;

import com.example.animalworld.model.entity.FeedingRule;
import com.example.animalworld.model.entity.PlantSpeciesConfiguration;
import com.example.animalworld.model.entity.SpeciesConfiguration;
import com.example.animalworld.model.entity.WorldSettings;

import java.util.List;

/**
 * Полный снимок конфигурации мира.
 * Нужен, чтобы не таскать по коду настройки и три списка отдельными аргументами.
 *
 * @author Shamrikova Tatiana
 */
public record WorldConfigurationSnapshot(
        WorldSettings settings,
        List<SpeciesConfiguration> speciesConfigurations,
        List<PlantSpeciesConfiguration> plantConfigurations,
        List<FeedingRule> feedingRules
) {
    public boolean isComplete() {
        return settings != null
                && speciesConfigurations != null
                && !speciesConfigurations.isEmpty()
                && plantConfigurations != null
                && !plantConfigurations.isEmpty()
                && feedingRules != null
                && !feedingRules.isEmpty();
    }
}
