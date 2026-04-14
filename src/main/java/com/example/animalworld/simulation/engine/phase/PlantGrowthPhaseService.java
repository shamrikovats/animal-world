package com.example.animalworld.simulation.engine.phase;

import com.example.animalworld.simulation.domain.config.RuntimePlantConfig;
import com.example.animalworld.simulation.domain.flora.Plant;
import com.example.animalworld.simulation.domain.world.SimulationCell;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.executor.SimulationParallelSupport;
import com.example.animalworld.simulation.factory.RuntimeIdGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Фаза роста растений.
 * Восстанавливает существующие растения и может добавить новое растение в клетку, если вид там исчез.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class PlantGrowthPhaseService {
    // TODO вынести это в конфиг, сейчас это хардкод потому что не успеваю переработать модель
    private static final int MIN_GROWTH_CHANCE_PERCENT = 10;
    private static final int MAX_GROWTH_CHANCE_PERCENT = 100;
    private static final int REPAIR_SPEED_FACTOR = 15;
    private static final int DEFICIT_FACTOR = 10;

    private final SimulationParallelSupport parallelSupport;
    private final RuntimeIdGenerator runtimeIdGenerator;

    PlantGrowthPhaseService(SimulationParallelSupport parallelSupport, RuntimeIdGenerator runtimeIdGenerator) {
        this.parallelSupport = parallelSupport;
        this.runtimeIdGenerator = runtimeIdGenerator;
    }

    public void execute(SimulationWorld world) {
        parallelSupport.forEachCell(world, cell -> growInCell(cell, world));
    }

    private void growInCell(SimulationCell cell, SimulationWorld world) {
        cell.withLock(() -> {
            List<Plant> plants = cell.plantsSnapshot();
            for (Plant plant : plants) {
                plant.regrow();
            }

            for (RuntimePlantConfig configuration : world.plantConfigsById().values()) {
                int targetPlantsPerCell = targetPlantsPerCell(configuration, world);
                int currentPlantCount = cell.plantCount(configuration.plantSpeciesId());
                if (currentPlantCount < targetPlantsPerCell && shouldGrow(configuration, targetPlantsPerCell - currentPlantCount)) {
                    cell.addPlant(new Plant(runtimeIdGenerator.nextId(), configuration));
                }
            }
        });
    }

    private boolean shouldGrow(RuntimePlantConfig configuration, int deficit) {
        int growthChancePercent = Math.min(
                MAX_GROWTH_CHANCE_PERCENT,
                Math.max(
                        MIN_GROWTH_CHANCE_PERCENT,
                        configuration.maxRepairSpeed() * REPAIR_SPEED_FACTOR + (deficit * DEFICIT_FACTOR)
                )
        );
        return ThreadLocalRandom.current().nextInt(100) < growthChancePercent;
    }

    private int targetPlantsPerCell(RuntimePlantConfig configuration, SimulationWorld world) {
        int totalCells = world.height() * world.width();
        int desiredPlantCount = Math.max(configuration.startCount(), world.settings().startPlantsMass());
        return Math.max(1, (int) Math.ceil((double) desiredPlantCount / totalCells));
    }
}
