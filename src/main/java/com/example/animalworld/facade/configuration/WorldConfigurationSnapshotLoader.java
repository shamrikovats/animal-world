package com.example.animalworld.facade.configuration;

import com.example.animalworld.model.entity.FeedingRule;
import com.example.animalworld.model.entity.PlantSpeciesConfiguration;
import com.example.animalworld.model.entity.SpeciesConfiguration;
import com.example.animalworld.model.entity.WorldSettings;
import com.example.animalworld.repository.FeedingRuleRepository;
import com.example.animalworld.repository.PlantSpeciesConfigurationRepository;
import com.example.animalworld.repository.SpeciesConfigurationRepository;
import com.example.animalworld.repository.WorldSettingsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Загрузка конфигурации мира из репозиториев.
 * Он только читает текущее или дефолтное состояние и собирает его в один объект.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class WorldConfigurationSnapshotLoader {
    private final WorldSettingsRepository worldSettingsRepository;
    private final SpeciesConfigurationRepository speciesConfigurationRepository;
    private final PlantSpeciesConfigurationRepository plantSpeciesConfigurationRepository;
    private final FeedingRuleRepository feedingRuleRepository;

    WorldConfigurationSnapshotLoader(
            WorldSettingsRepository worldSettingsRepository,
            SpeciesConfigurationRepository speciesConfigurationRepository,
            PlantSpeciesConfigurationRepository plantSpeciesConfigurationRepository,
            FeedingRuleRepository feedingRuleRepository
    ) {
        this.worldSettingsRepository = worldSettingsRepository;
        this.speciesConfigurationRepository = speciesConfigurationRepository;
        this.plantSpeciesConfigurationRepository = plantSpeciesConfigurationRepository;
        this.feedingRuleRepository = feedingRuleRepository;
    }

    public WorldConfigurationSnapshot loadDefaults() {
        return new WorldConfigurationSnapshot(
                worldSettingsRepository.findDefault(),
                speciesConfigurationRepository.findAllDefaults(),
                plantSpeciesConfigurationRepository.findAllDefaults(),
                feedingRuleRepository.findAllDefaults()
        );
    }

    public WorldConfigurationSnapshot loadForWorld(Integer worldId) {
        WorldSettings settings = worldSettingsRepository.findByWorldId(worldId).orElse(null);
        List<SpeciesConfiguration> speciesConfigurations = speciesConfigurationRepository.findAllByWorldId(worldId);
        List<PlantSpeciesConfiguration> plantConfigurations =
                plantSpeciesConfigurationRepository.findAllByWorldId(worldId);
        List<FeedingRule> feedingRules = feedingRuleRepository.findAllByWorldId(worldId);

        return new WorldConfigurationSnapshot(
                settings,
                speciesConfigurations,
                plantConfigurations,
                feedingRules
        );
    }
}
