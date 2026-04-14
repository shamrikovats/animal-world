package com.example.animalworld.simulation.service;

import com.example.animalworld.model.dto.WorldRuntimeSummaryDto;
import com.example.animalworld.scheduler.SimulationWorldRegistry;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.simulation.engine.WorldPopulationInspector;
import com.example.animalworld.simulation.engine.WorldPopulationSnapshot;
import org.springframework.stereotype.Service;

/**
 * Сервис короткой сводки runtime-мира.
 * Он убирает ручные подсчеты из контроллера и держит одну точку сборки summary.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class SimulationWorldSummaryService {
    private final SimulationWorldRegistry registry;
    private final SimulationWorldBootstrapService bootstrapService;
    private final WorldPopulationInspector worldPopulationInspector;

    SimulationWorldSummaryService(
            SimulationWorldRegistry registry,
            SimulationWorldBootstrapService bootstrapService,
            WorldPopulationInspector worldPopulationInspector
    ) {
        this.registry = registry;
        this.bootstrapService = bootstrapService;
        this.worldPopulationInspector = worldPopulationInspector;
    }

    public WorldRuntimeSummaryDto build(Integer worldId) {
        SimulationWorld world = registry.findWorld(worldId).orElseGet(() -> bootstrapService.bootstrap(worldId));
        WorldPopulationSnapshot snapshot = worldPopulationInspector.inspect(world);

        return new WorldRuntimeSummaryDto(
                world.worldId(),
                world.worldName(),
                world.status(),
                world.currentTick(),
                world.height(),
                world.width(),
                snapshot.totalAnimals(),
                world.totalPlants()
        );
    }
}
