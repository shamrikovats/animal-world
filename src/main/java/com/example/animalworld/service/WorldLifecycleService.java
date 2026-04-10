package com.example.animalworld.service;

import com.example.animalworld.model.WorldStatus;
import com.example.animalworld.model.entity.FeedingRule;
import com.example.animalworld.model.entity.PlantSpeciesConfiguration;
import com.example.animalworld.model.entity.SpeciesConfiguration;
import com.example.animalworld.model.entity.World;
import com.example.animalworld.model.entity.WorldSettings;
import com.example.animalworld.repository.WorldRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Shamrikova Tatiana
 */
@Service
public class WorldLifecycleService {
    private static final Logger log = LoggerFactory.getLogger(WorldLifecycleService.class);

    private final WorldRepository worldRepository;
    private final WorldLookupService worldLookupService;
    private final WorldConfigurationService worldConfigurationService;

    WorldLifecycleService(
            WorldRepository worldRepository,
            WorldLookupService worldLookupService,
            WorldConfigurationService worldConfigurationService
    ) {
        this.worldRepository = worldRepository;
        this.worldLookupService = worldLookupService;
        this.worldConfigurationService = worldConfigurationService;
    }

    @Transactional
    public World create(
            World world,
            WorldSettings settingsPatch,
            List<SpeciesConfiguration> speciesConfigurationPatches,
            List<PlantSpeciesConfiguration> plantConfigurationPatches,
            List<FeedingRule> feedingRulePatches
    ) {
        log.info("Try to create world: {}", world.name());
        World createdWorld = worldRepository.create(world);
        worldConfigurationService.materializeWorldConfiguration(
                createdWorld.id(),
                settingsPatch,
                speciesConfigurationPatches,
                plantConfigurationPatches,
                feedingRulePatches
        );
        return worldLookupService.findById(createdWorld.id());
    }

    @Transactional
    public World updateStatus(Integer worldId, WorldStatus status) {
        log.info("Try to update status of world with id: {} to {}", worldId, status);
        if (status == WorldStatus.ACTIVE) {
            worldConfigurationService.ensureMaterialized(worldId);
        } else {
            worldLookupService.findById(worldId);
        }
        return worldRepository.updateStatus(worldId, status).orElseThrow();
    }
}
