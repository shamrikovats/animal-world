package com.example.animalworld.simulation.engine.phase;

import com.example.animalworld.model.FoodType;
import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.config.RuntimeFeedingRule;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;
import com.example.animalworld.simulation.domain.flora.Plant;
import com.example.animalworld.simulation.domain.world.SimulationCell;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.executor.SimulationParallelSupport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Фаза передвижения животных.
 * Сначала считает намерения перемещения, потом отдельно применяет их безопасно.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class MovementPhaseService {
    private final SimulationParallelSupport parallelSupport;

    MovementPhaseService(SimulationParallelSupport parallelSupport) {
        this.parallelSupport = parallelSupport;
    }

    public void execute(SimulationWorld world) {
        Map<Integer, List<RuntimeFeedingRule>> rulesByPredator = world.feedingRulesByKey().values().stream()
                .collect(Collectors.groupingBy(rule -> rule.key().predatorSpeciesId()));
        ConcurrentLinkedQueue<MoveIntent> intents = new ConcurrentLinkedQueue<>();
        parallelSupport.forEachCell(world, cell -> collectIntents(world, cell, intents, rulesByPredator));

        List<MoveIntent> orderedIntents = new ArrayList<>(intents);
        orderedIntents.sort(Comparator.comparingLong(intent -> intent.animal().runtimeId()));

        for (MoveIntent intent : orderedIntents) {
            applyIntent(intent);
        }
    }

    private void collectIntents(
            SimulationWorld world,
            SimulationCell sourceCell,
            ConcurrentLinkedQueue<MoveIntent> intents,
            Map<Integer, List<RuntimeFeedingRule>> rulesByPredator
    ) {
        for (Animal animal : sourceCell.animalsSnapshot()) {
            if (!animal.isAlive()) {
                continue;
            }

            SimulationCell targetCell = chooseTargetCell(world, sourceCell, animal, rulesByPredator);
            if (targetCell != sourceCell) {
                intents.add(new MoveIntent(animal, sourceCell, targetCell));
            }
        }
    }

    private SimulationCell chooseTargetCell(
            SimulationWorld world,
            SimulationCell sourceCell,
            Animal animal,
            Map<Integer, List<RuntimeFeedingRule>> rulesByPredator
    ) {
        RuntimeSpeciesConfig configuration = animal.configuration();
        if (configuration.speedCells() == null || configuration.speedCells() <= 0) {
            return sourceCell;
        }

        List<CandidateCell> candidates = collectCandidateCells(world, sourceCell, animal, rulesByPredator);
        if (candidates.isEmpty()) {
            return sourceCell;
        }

        double bestScore = candidates.stream()
                .mapToDouble(CandidateCell::score)
                .max()
                .orElse(Double.NEGATIVE_INFINITY);
        List<SimulationCell> bestCells = candidates.stream()
                .filter(candidate -> Double.compare(candidate.score(), bestScore) == 0)
                .map(CandidateCell::cell)
                .toList();
        return bestCells.get(ThreadLocalRandom.current().nextInt(bestCells.size()));
    }

    private void applyIntent(MoveIntent intent) {
        if (!intent.animal().isAlive()) {
            return;
        }
        if (!intent.targetCell().canAcceptAnimal(intent.animal().configuration())) {
            return;
        }

        intent.sourceCell().removeAnimal(intent.animal());
        intent.targetCell().addAnimal(intent.animal());
    }

    private List<CandidateCell> collectCandidateCells(
            SimulationWorld world,
            SimulationCell sourceCell,
            Animal animal,
            Map<Integer, List<RuntimeFeedingRule>> rulesByPredator
    ) {
        List<CandidateCell> candidates = new ArrayList<>();
        int speed = animal.configuration().speedCells();

        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                int distance = Math.abs(sourceCell.x() - x) + Math.abs(sourceCell.y() - y);
                if (distance == 0 || distance > speed) {
                    continue;
                }

                SimulationCell targetCell = world.cellAt(x, y);
                if (!world.canEnter(animal.speciesId(), targetCell.locationType())) {
                    continue;
                }
                if (!targetCell.canAcceptAnimal(animal.configuration())) {
                    continue;
                }

                candidates.add(new CandidateCell(
                        targetCell,
                        scoreTargetCell(world, animal, targetCell, distance, rulesByPredator.getOrDefault(animal.speciesId(), List.of()))
                ));
            }
        }

        return candidates;
    }

    private double scoreTargetCell(
            SimulationWorld world,
            Animal animal,
            SimulationCell targetCell,
            int distance,
            List<RuntimeFeedingRule> feedingRules
    ) {
        int sameSpeciesCount = targetCell.animalsBySpeciesSnapshot()
                .getOrDefault(animal.speciesId(), List.of())
                .size();
        int locationModifier = world.survivalModifier(animal.speciesId(), targetCell.locationType());

        return foodScore(world, targetCell, feedingRules)
                + (locationModifier * 100.0)
                - (sameSpeciesCount * 5.0)
                - distance;
    }

    private double foodScore(
            SimulationWorld world,
            SimulationCell targetCell,
            List<RuntimeFeedingRule> feedingRules
    ) {
        List<Animal> animals = targetCell.animalsSnapshot();
        List<Plant> plants = targetCell.plantsSnapshot();
        double score = 0;

        for (RuntimeFeedingRule rule : feedingRules) {
            double probabilityFactor = rule.probability() / 100.0;
            if (rule.foodType() == FoodType.PLANT && rule.key().preyPlantSpeciesId() != null) {
                score += plants.stream()
                        .filter(Plant::isAlive)
                        .filter(plant -> plant.plantSpeciesId().equals(rule.key().preyPlantSpeciesId()))
                        .mapToDouble(Plant::currentMass)
                        .sum() * probabilityFactor;
            }
            if (rule.foodType() == FoodType.SPECIES && rule.key().preySpeciesId() != null) {
                score += animals.stream()
                        .filter(Animal::isAlive)
                        .filter(prey -> prey.speciesId().equals(rule.key().preySpeciesId()))
                        .mapToDouble(prey -> world.speciesConfigsById().get(prey.speciesId()).weight())
                        .sum() * probabilityFactor;
            }
        }

        return score;
    }

    private record MoveIntent(
            Animal animal,
            SimulationCell sourceCell,
            SimulationCell targetCell
    ) {
    }

    private record CandidateCell(
            SimulationCell cell,
            double score
    ) {
    }
}
