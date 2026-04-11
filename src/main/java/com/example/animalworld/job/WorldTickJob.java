package com.example.animalworld.job;

import com.example.animalworld.runtime.simulation.domain.world.SimulationWorld;
import com.example.animalworld.runtime.simulation.engine.SimulationTickEngine;
import org.springframework.stereotype.Component;

/**
 * Одна job выполнения такта мира.
 * Нужна, чтобы scheduler управлял только расписанием, а логика одного тика была выделена отдельно.
 *
 * @author Shamrikova Tatiana
 */
@Component
public class WorldTickJob {
    private final SimulationTickEngine tickEngine;

    WorldTickJob(SimulationTickEngine tickEngine) {
        this.tickEngine = tickEngine;
    }

    public void run(SimulationWorld world) {
        tickEngine.executeTick(world);
    }
}
