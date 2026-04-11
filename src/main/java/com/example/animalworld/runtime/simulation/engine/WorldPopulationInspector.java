package com.example.animalworld.runtime.simulation.engine;

import com.example.animalworld.runtime.simulation.domain.base.Herbivore;
import com.example.animalworld.runtime.simulation.domain.base.Predator;
import com.example.animalworld.runtime.simulation.domain.flora.Plant;
import com.example.animalworld.runtime.simulation.domain.world.SimulationCell;
import com.example.animalworld.runtime.simulation.domain.world.SimulationWorld;
import org.springframework.stereotype.Component;

/**
 * Сервис подсчета текущего состояния мира.
 *
 * @author Shamrikova Tatiana
 */
@Component
public class WorldPopulationInspector {
    public WorldPopulationSnapshot inspect(SimulationWorld world) {
        int totalAnimals = 0;
        int predatorCount = 0;
        int herbivoreCount = 0;
        double totalPlantMass = 0;

        for (SimulationCell cell : world.allCells()) {
            var animals = cell.animalsSnapshot();
            var plants = cell.plantsSnapshot();

            totalAnimals += animals.size();
            predatorCount += (int) animals.stream()
                    .filter(Predator.class::isInstance)
                    .count();
            herbivoreCount += (int) animals.stream()
                    .filter(Herbivore.class::isInstance)
                    .count();
            totalPlantMass += plants.stream()
                    .filter(Plant::isAlive)
                    .mapToDouble(Plant::currentMass)
                    .sum();
        }

        return new WorldPopulationSnapshot(
                totalAnimals,
                predatorCount,
                herbivoreCount,
                totalPlantMass
        );
    }
}
