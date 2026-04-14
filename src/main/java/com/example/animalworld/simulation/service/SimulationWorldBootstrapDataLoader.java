package com.example.animalworld.simulation.service;

import com.example.animalworld.facade.WorldConfigurationService;
import com.example.animalworld.facade.WorldLookupService;
import com.example.animalworld.model.entity.World;
import com.example.animalworld.model.entity.WorldSettings;
import com.example.animalworld.model.entity.WorldTickStat;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Загрузка данных для bootstrap мира.
 * Он только читает нужные данные из БД и отдает их одним объектом.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class SimulationWorldBootstrapDataLoader {
    private final WorldLookupService worldLookupService;
    private final WorldConfigurationService worldConfigurationService;

    SimulationWorldBootstrapDataLoader(
            WorldLookupService worldLookupService,
            WorldConfigurationService worldConfigurationService
    ) {
        this.worldLookupService = worldLookupService;
        this.worldConfigurationService = worldConfigurationService;
    }

    public SimulationWorldBootstrapData load(Integer worldId) {
        World world = worldLookupService.findById(worldId);
        WorldSettings settings = worldConfigurationService.getSettings(worldId);

        return new SimulationWorldBootstrapData(
                world,
                settings,
                currentTick(worldId),
                worldConfigurationService.getSpeciesConfigurations(worldId),
                worldConfigurationService.getPlantConfigurations(worldId),
                worldConfigurationService.getFeedingRules(worldId)
        );
    }

    private long currentTick(Integer worldId) {
        List<WorldTickStat> stats = worldLookupService.getStats(worldId);
        return stats.stream()
                .mapToLong(WorldTickStat::tickNumber)
                .max()
                .orElse(0);
    }
}
