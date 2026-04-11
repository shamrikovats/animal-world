package com.example.animalworld.simulation.engine;

import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.simulation.engine.phase.*;
import com.example.animalworld.simulation.engine.stats.SimulationTickConsoleReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Оркестратор одного такта симуляции.
 * Вызывает фазы в стабильном порядке и сохраняет статистику после завершения такта.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class SimulationTickEngine {
    private static final Logger log = LoggerFactory.getLogger(SimulationTickEngine.class);

    private final MovementPhaseService movementPhaseService;
    private final FeedingPhaseService feedingPhaseService;
    private final ReproductionPhaseService reproductionPhaseService;
    private final SurvivalPhaseService survivalPhaseService;
    private final PlantGrowthPhaseService plantGrowthPhaseService;
    private final StatisticsPhaseService statisticsPhaseService;
    private final WorldPopulationInspector worldPopulationInspector;
    private final SimulationTickConsoleReporter simulationTickConsoleReporter;

    SimulationTickEngine(
            MovementPhaseService movementPhaseService,
            FeedingPhaseService feedingPhaseService,
            ReproductionPhaseService reproductionPhaseService,
            SurvivalPhaseService survivalPhaseService,
            PlantGrowthPhaseService plantGrowthPhaseService,
            StatisticsPhaseService statisticsPhaseService,
            WorldPopulationInspector worldPopulationInspector,
            SimulationTickConsoleReporter simulationTickConsoleReporter
    ) {
        this.movementPhaseService = movementPhaseService;
        this.feedingPhaseService = feedingPhaseService;
        this.reproductionPhaseService = reproductionPhaseService;
        this.survivalPhaseService = survivalPhaseService;
        this.plantGrowthPhaseService = plantGrowthPhaseService;
        this.statisticsPhaseService = statisticsPhaseService;
        this.worldPopulationInspector = worldPopulationInspector;
        this.simulationTickConsoleReporter = simulationTickConsoleReporter;
    }

    public WorldPopulationSnapshot executeTick(SimulationWorld world) {
        TickMetrics metrics = new TickMetrics();
        prepareAnimalsForTick(world);
        int populationBeforeTick = worldPopulationInspector.inspect(world).totalAnimals();
        feedingPhaseService.execute(world, metrics);
        movementPhaseService.execute(world);
        reproductionPhaseService.execute(world, metrics);
        survivalPhaseService.execute(world, metrics);
        WorldPopulationSnapshot populationBeforePlantGrowth = worldPopulationInspector.inspect(world);

        long tickNumber = world.advanceTick();
        WorldPopulationSnapshot populationAfterTick = statisticsPhaseService.execute(
                world,
                metrics,
                tickNumber,
                populationBeforePlantGrowth
        );
        simulationTickConsoleReporter.print(world, tickNumber, populationAfterTick, metrics);
        validatePopulationBalance(world, tickNumber, populationBeforeTick, populationAfterTick.totalAnimals(), metrics);
        plantGrowthPhaseService.execute(world);
        return populationAfterTick;
    }

    private void validatePopulationBalance(
            SimulationWorld world,
            long tickNumber,
            int populationBeforeTick,
            int populationAfterTick,
            TickMetrics metrics
    ) {
        int expectedPopulation = populationBeforeTick + metrics.births() - metrics.deaths();
        if (expectedPopulation != populationAfterTick) {
            log.warn(
                    "Population mismatch after tick {} for world {}. before={}, births={}, deaths={}, expected={}, actual={}",
                    tickNumber,
                    world.worldId(),
                    populationBeforeTick,
                    metrics.births(),
                    metrics.deaths(),
                    expectedPopulation,
                    populationAfterTick
            );
        }
    }

    private void prepareAnimalsForTick(SimulationWorld world) {
        for (var cell : world.allCells()) {
            for (var animal : cell.animalsSnapshot()) {
                animal.prepareForTick();
            }
        }
    }
}
