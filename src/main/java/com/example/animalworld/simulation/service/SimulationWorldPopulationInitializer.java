package com.example.animalworld.simulation.service;

import com.example.animalworld.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.simulation.domain.config.RuntimePlantConfig;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;
import com.example.animalworld.simulation.domain.dictionary.Sex;
import com.example.animalworld.simulation.domain.flora.Plant;
import com.example.animalworld.simulation.domain.world.SimulationCell;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.simulation.factory.AnimalFactory;
import com.example.animalworld.simulation.factory.RuntimeIdGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Заселение runtime-мира стартовой популяцией.
 * Отвечает только за животных и растения после того, как каркас мира уже создан.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class SimulationWorldPopulationInitializer {
    private final AnimalFactory animalFactory;
    private final RuntimeIdGenerator runtimeIdGenerator;

    SimulationWorldPopulationInitializer(AnimalFactory animalFactory, RuntimeIdGenerator runtimeIdGenerator) {
        this.animalFactory = animalFactory;
        this.runtimeIdGenerator = runtimeIdGenerator;
    }

    public void populate(SimulationWorld simulationWorld) {
        populateAnimals(simulationWorld);
        populatePlants(simulationWorld);
    }

    private void populateAnimals(SimulationWorld simulationWorld) {
        List<SimulationCell> cells = simulationWorld.allCells();
        for (RuntimeSpeciesConfig configuration : simulationWorld.speciesConfigsById().values()) {
            Random random = new Random(seed(simulationWorld.worldId(), configuration.speciesId()));
            for (int i = 0; i < configuration.startCount(); i++) {
                SimulationCell cell = selectCellForAnimal(cells, configuration, random);
                AnimalSpawnState spawnState = new AnimalSpawnState(
                        runtimeIdGenerator.nextId(),
                        random.nextBoolean() ? Sex.MALE : Sex.FEMALE,
                        initialSatiety(simulationWorld, configuration),
                        true,
                        false,
                        0
                );
                cell.addAnimal(animalFactory.create(configuration, spawnState));
            }
        }
    }

    private void populatePlants(SimulationWorld simulationWorld) {
        List<SimulationCell> cells = simulationWorld.allCells();
        List<RuntimePlantConfig> plantConfigs = new ArrayList<>(simulationWorld.plantConfigsById().values());
        plantConfigs.sort(Comparator.comparing(RuntimePlantConfig::plantSpeciesId));

        int fallbackPlantCount = Math.max(1, simulationWorld.settings().startPlantsMass());
        for (RuntimePlantConfig configuration : plantConfigs) {
            int plantCount = configuration.startCount() > 0 ? configuration.startCount() : fallbackPlantCount;
            int targetPlantsPerCell = Math.max(1, (int) Math.ceil((double) plantCount / cells.size()));
            Random random = new Random(seed(simulationWorld.worldId(), configuration.plantSpeciesId()));
            for (int i = 0; i < plantCount; i++) {
                SimulationCell cell = selectCellForPlant(cells, configuration, targetPlantsPerCell, random);
                cell.addPlant(new Plant(runtimeIdGenerator.nextId(), configuration));
            }
        }
    }

    private SimulationCell selectCellForAnimal(
            List<SimulationCell> cells,
            RuntimeSpeciesConfig configuration,
            Random random
    ) {
        int startIndex = random.nextInt(cells.size());
        for (int offset = 0; offset < cells.size(); offset++) {
            SimulationCell cell = cells.get((startIndex + offset) % cells.size());
            cell.lock().lock();
            try {
                if (cell.canAcceptAnimal(configuration)) {
                    return cell;
                }
            } finally {
                cell.lock().unlock();
            }
        }

        throw new IllegalStateException("No free cell capacity for species: " + configuration.speciesName());
    }

    private SimulationCell selectCellForPlant(
            List<SimulationCell> cells,
            RuntimePlantConfig configuration,
            int targetPlantsPerCell,
            Random random
    ) {
        int startIndex = random.nextInt(cells.size());
        for (int offset = 0; offset < cells.size(); offset++) {
            SimulationCell cell = cells.get((startIndex + offset) % cells.size());
            if (cell.plantCount(configuration.plantSpeciesId()) < targetPlantsPerCell) {
                return cell;
            }
        }
        return cells.get(random.nextInt(cells.size()));
    }

    private double initialSatiety(SimulationWorld simulationWorld, RuntimeSpeciesConfig configuration) {
        return configuration.effectiveFullTankWeight()
                * (100 - simulationWorld.settings().startHungryPercent()) / 100.0;
    }

    private long seed(Integer worldId, Integer entityId) {
        return ((long) worldId * 1_000_003L) + entityId;
    }
}
