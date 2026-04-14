package com.example.animalworld.simulation.engine.phase;

import com.example.animalworld.model.FoodType;
import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.config.RuntimeFeedingRule;
import com.example.animalworld.simulation.domain.flora.Plant;
import com.example.animalworld.simulation.domain.world.SimulationCell;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.executor.SimulationParallelSupport;
import com.example.animalworld.simulation.engine.TickMetrics;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Фаза питания животных.
 * Находит подходящую еду в клетке и применяет вероятность поедания из feeding rules мира.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class FeedingPhaseService {
    private final SimulationParallelSupport parallelSupport;

    FeedingPhaseService(SimulationParallelSupport parallelSupport) {
        this.parallelSupport = parallelSupport;
    }

    public void execute(SimulationWorld world, TickMetrics metrics) {
        Map<Integer, List<RuntimeFeedingRule>> rulesByPredator = world.feedingRulesByKey().values().stream()
                .collect(Collectors.groupingBy(rule -> rule.key().predatorSpeciesId()));

        parallelSupport.forEachCell(world, cell -> feedCell(cell, rulesByPredator, metrics));
    }

    private void feedCell(SimulationCell cell, Map<Integer, List<RuntimeFeedingRule>> rulesByPredator, TickMetrics metrics) {
        cell.withLock(() -> {
            List<Animal> animals = cell.animalsSnapshot();
            List<Plant> plants = cell.plantsSnapshot();
            for (Animal animal : animals) {
                if (!animal.isAlive()) {
                    continue;
                }

                List<RuntimeFeedingRule> rules = rulesByPredator.get(animal.speciesId());
                if (rules == null || rules.isEmpty()) {
                    continue;
                }

                while (animal.needsFood() && tryEatOnce(animal, cell, animals, plants, rules, metrics)) {
                    // Животное продолжает есть, пока не насытится или пока в клетке есть подходящая еда.
                }
            }
        });
    }

    private boolean tryEatOnce(
            Animal animal,
            SimulationCell cell,
            List<Animal> animals,
            List<Plant> plants,
            List<RuntimeFeedingRule> rules,
            TickMetrics metrics
    ) {
        for (RuntimeFeedingRule rule : rules) {
            if (!canEat(rule)) {
                continue;
            }

            if (rule.foodType() == FoodType.PLANT && tryEatPlant(animal, cell, plants, rule)) {
                return true;
            }
            if (rule.foodType() == FoodType.SPECIES && tryEatAnimal(animal, cell, animals, rule, metrics)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryEatPlant(Animal animal, SimulationCell cell, List<Plant> plants, RuntimeFeedingRule rule) {
        for (Plant plant : plants) {
            if (!plant.isAlive() || !plant.plantSpeciesId().equals(rule.key().preyPlantSpeciesId())) {
                continue;
            }
            if (animal.eat(plant)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryEatAnimal(
            Animal hunter,
            SimulationCell cell,
            List<Animal> animals,
            RuntimeFeedingRule rule,
            TickMetrics metrics
    ) {
        for (Animal prey : animals) {
            if (!prey.isAlive()
                    || prey == hunter
                    || !prey.speciesId().equals(rule.key().preySpeciesId())) {
                continue;
            }
            if (hunter.eat(prey)) {
                cell.removeAnimal(prey);
                metrics.incrementDeaths(prey.speciesName(), 1);
                return true;
            }
        }
        return false;
    }

    private boolean canEat(RuntimeFeedingRule rule) {
        return ThreadLocalRandom.current().nextInt(100) < rule.probability();
    }
}
