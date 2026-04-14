package com.example.animalworld.simulation.engine.phase;

import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.world.SimulationCell;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.executor.SimulationParallelSupport;
import com.example.animalworld.simulation.engine.TickMetrics;
import org.springframework.stereotype.Service;

/**
 * Фаза голода и смерти животных.
 * Уменьшает сытость и удаляет из клетки животных, которые не пережили такт.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class SurvivalPhaseService {
    private final SimulationParallelSupport parallelSupport;

    SurvivalPhaseService(SimulationParallelSupport parallelSupport) {
        this.parallelSupport = parallelSupport;
    }

    public void execute(SimulationWorld world, TickMetrics metrics) {
        parallelSupport.forEachCell(world, cell -> processCell(cell, metrics));
    }

    private void processCell(SimulationCell cell, TickMetrics metrics) {
        cell.withLock(() -> {
            for (Animal animal : cell.animalsSnapshot()) {
                if (!animal.ateThisTick()) {
                    animal.loseSatietyForTick();
                }

                if (animal.isAlive()
                        && !animal.ateThisTick()
                        && animal.foodPercent() < animal.configuration().minFoodPercent()) {
                    animal.markDead();
                }

                if (!animal.isAlive()) {
                    cell.removeAnimal(animal);
                    metrics.incrementDeaths(animal.speciesName(), 1);
                }
            }
        });
    }
}
