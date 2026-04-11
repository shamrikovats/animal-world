package com.example.animalworld.runtime.simulation.engine.phase;

import com.example.animalworld.model.entity.WorldTickStat;
import com.example.animalworld.repository.WorldTickStatRepository;
import com.example.animalworld.runtime.simulation.domain.world.SimulationWorld;
import com.example.animalworld.runtime.simulation.engine.TickMetrics;
import com.example.animalworld.runtime.simulation.engine.WorldPopulationInspector;
import com.example.animalworld.runtime.simulation.engine.WorldPopulationSnapshot;
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
    private final WorldTickStatRepository worldTickStatRepository;
    private final WorldPopulationInspector worldPopulationInspector;

    StatisticsPhaseService(
            WorldTickStatRepository worldTickStatRepository,
            WorldPopulationInspector worldPopulationInspector
    ) {
        this.worldTickStatRepository = worldTickStatRepository;
        this.worldPopulationInspector = worldPopulationInspector;
    }

    public WorldPopulationSnapshot execute(
            SimulationWorld world,
            TickMetrics metrics,
            long tickNumber,
            WorldPopulationSnapshot snapshot
    ) {

        worldTickStatRepository.save(new WorldTickStat(
                world.worldId(),
                (int) tickNumber,
                snapshot.predatorCount(),
                snapshot.herbivoreCount(),
                snapshot.totalPlantMass(),
                metrics.births(),
                metrics.deaths(),
                null
        ));

        return snapshot;
    }
}
