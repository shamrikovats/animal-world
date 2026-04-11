package com.example.animalworld.simulation.service;

import com.example.animalworld.simulation.domain.world.SimulationWorld;
import org.springframework.stereotype.Service;

/**
 * Сервис, который поднимает runtime-мир из данных БД.
 * Он только оркестрирует загрузку данных, сбор runtime-мира и стартовое заселение.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class SimulationWorldBootstrapService {
    private final SimulationWorldBootstrapDataLoader bootstrapDataLoader;
    private final SimulationWorldRuntimeFactory simulationWorldRuntimeFactory;
    private final SimulationWorldPopulationInitializer simulationWorldPopulationInitializer;

    SimulationWorldBootstrapService(
            SimulationWorldBootstrapDataLoader bootstrapDataLoader,
            SimulationWorldRuntimeFactory simulationWorldRuntimeFactory,
            SimulationWorldPopulationInitializer simulationWorldPopulationInitializer
    ) {
        this.bootstrapDataLoader = bootstrapDataLoader;
        this.simulationWorldRuntimeFactory = simulationWorldRuntimeFactory;
        this.simulationWorldPopulationInitializer = simulationWorldPopulationInitializer;
    }

    public SimulationWorld bootstrap(Integer worldId) {
        SimulationWorldBootstrapData bootstrapData = bootstrapDataLoader.load(worldId);
        SimulationWorld simulationWorld = simulationWorldRuntimeFactory.create(bootstrapData);
        simulationWorldPopulationInitializer.populate(simulationWorld);
        return simulationWorld;
    }
}
