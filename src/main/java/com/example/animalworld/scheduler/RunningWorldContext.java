package com.example.animalworld.scheduler;

import com.example.animalworld.simulation.domain.world.SimulationWorld;

import java.util.concurrent.ScheduledFuture;

/**
 * Контекст запущенного мира.
 * Хранит runtime-мир и задачи, чтобы мир можно было остановить корректно.
 *
 * @author Shamrikova Tatiana
 */
public record RunningWorldContext(
        SimulationWorld world,
        ScheduledFuture<?> scheduledTask
) {
}
