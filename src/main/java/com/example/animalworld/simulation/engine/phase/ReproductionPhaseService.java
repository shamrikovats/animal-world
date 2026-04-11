package com.example.animalworld.simulation.engine.phase;

import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;
import com.example.animalworld.simulation.domain.dictionary.Sex;
import com.example.animalworld.simulation.domain.world.SimulationCell;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.executor.SimulationParallelSupport;
import com.example.animalworld.simulation.engine.TickMetrics;
import com.example.animalworld.simulation.factory.RuntimeIdGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Фаза размножения животных.
 * Если в клетке есть самец и самка одного вида, самка может забеременеть и потом родить потомство.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class ReproductionPhaseService {
    private static final int REPRODUCTION_FOOD_THRESHOLD_PERCENT = 60;

    private final SimulationParallelSupport parallelSupport;
    private final RuntimeIdGenerator runtimeIdGenerator;

    ReproductionPhaseService(
            SimulationParallelSupport parallelSupport,
            RuntimeIdGenerator runtimeIdGenerator
    ) {
        this.parallelSupport = parallelSupport;
        this.runtimeIdGenerator = runtimeIdGenerator;
    }

    public void execute(SimulationWorld world, TickMetrics metrics) {
        parallelSupport.forEachCell(world, cell -> reproduceInCell(cell, metrics));
    }

    private void reproduceInCell(SimulationCell cell, TickMetrics metrics) {
        cell.withLock(() -> {
            Map<Integer, List<Animal>> animalsBySpecies = cell.animalsBySpeciesSnapshot();
            for (List<Animal> animals : animalsBySpecies.values()) {
                if (animals.isEmpty()) {
                    continue;
                }

                RuntimeSpeciesConfig configuration = animals.getFirst().configuration();
                List<Animal> aliveAnimals = animals.stream().filter(Animal::isAlive).toList();
                boolean hasMale = aliveAnimals.stream().anyMatch(this::isReadyMale);
                if (!hasMale) {
                    continue;
                }

                int capacityLeft = configuration.maxCoexistCount() - aliveAnimals.size();
                if (capacityLeft <= 0) {
                    continue;
                }

                List<Animal> females = aliveAnimals.stream()
                        .filter(animal -> animal.sex() == Sex.FEMALE)
                        .collect(Collectors.toList());

                for (Animal female : females) {
                    if (female.pregnant()) {
                        female.decrementPregnancyTick();
                        if (female.pregnancyRemainingTicks() == 0
                                && capacityLeft > 0
                                && shouldReproduceThisTick()
                                && isReadyFemale(female)) {
                            int offspringCount = Math.min(capacityLeft, averageOffspringCount(configuration));
                            List<Animal> offspring = female.reproduce(offspringCount, runtimeIdGenerator::nextId);
                            for (Animal newborn : offspring) {
                                cell.addAnimal(newborn);
                            }
                            capacityLeft -= offspring.size();
                            metrics.incrementBirths(configuration.speciesName(), offspring.size());
                        }
                    } else if (isReadyFemale(female) && shouldReproduceThisTick()) {
                        female.impregnate();
                    }
                }
            }
        });
    }

    private int averageOffspringCount(RuntimeSpeciesConfig configuration) {
        if (configuration.maxChildrenCount() == null || configuration.maxChildrenCount() <= 1) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil(configuration.maxChildrenCount() / 2.0));
    }

    private boolean shouldReproduceThisTick() {
        return ThreadLocalRandom.current().nextInt(100) < 35;
    }

    private boolean isReadyMale(Animal animal) {
        return animal.sex() == Sex.MALE
                && animal.foodPercent() >= reproductionFoodThreshold(animal);
    }

    private boolean isReadyFemale(Animal animal) {
        return animal.sex() == Sex.FEMALE
                && animal.foodPercent() >= reproductionFoodThreshold(animal);
    }

    private int reproductionFoodThreshold(Animal animal) {
        return Math.max(REPRODUCTION_FOOD_THRESHOLD_PERCENT, animal.configuration().minFoodPercent());
    }
}
