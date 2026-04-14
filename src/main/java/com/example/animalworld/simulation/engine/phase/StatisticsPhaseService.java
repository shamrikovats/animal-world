package com.example.animalworld.simulation.engine.phase;

import com.example.animalworld.model.entity.WorldTickStat;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.simulation.engine.TickMetrics;
import com.example.animalworld.simulation.engine.WorldPopulationSnapshot;
import com.example.animalworld.simulation.metrics.SimulationMetricsPublisher;
import com.example.animalworld.service.WorldTickStatService;
import org.springframework.stereotype.Service;

/**
 * Фаза сбора статистики по миру.
 * Считает агрегаты после такта и сохраняет их в таблицу world_tick_stats.
 * TODO накуралесила по типам, потом разобраться, никакого приведения к скобках!
 *
 * @author Shamrikova Tatiana
 */
@Service
public class StatisticsPhaseService {
    private final WorldTickStatService worldTickStatService;
    private final SimulationMetricsPublisher simulationMetricsPublisher;

    StatisticsPhaseService(
            WorldTickStatService worldTickStatService,
            SimulationMetricsPublisher simulationMetricsPublisher
    ) {
        this.worldTickStatService = worldTickStatService;
        this.simulationMetricsPublisher = simulationMetricsPublisher;
    }

    public WorldPopulationSnapshot execute(
            SimulationWorld world,
            TickMetrics metrics,
            long tickNumber,
            WorldPopulationSnapshot snapshot
    ) {

        worldTickStatService.save(new WorldTickStat(
                world.worldId(),
                (int) tickNumber,
                snapshot.predatorCount(),
                snapshot.herbivoreCount(),
                snapshot.totalPlantMass(),
                metrics.births(),
                metrics.deaths(),
                null
        ));
        simulationMetricsPublisher.recordTick(world, tickNumber, snapshot, metrics);

        return snapshot;
    }
}
