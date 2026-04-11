package com.example.animalworld.executor;

import com.example.animalworld.runtime.simulation.domain.world.SimulationWorld;
import com.example.animalworld.runtime.simulation.domain.world.SimulationCell;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Вспомогательный сервис для параллельной обработки мира по строкам.
 * Нужен, чтобы фазы тика не дублировали код работы с worker pool.
 *
 * @author Shamrikova Tatiana
 */
@Component
public class SimulationParallelSupport {
    private final ExecutorService workerPool;

    SimulationParallelSupport(@Qualifier("simulationWorkerPool") ExecutorService workerPool) {
        this.workerPool = workerPool;
    }

    public void forEachRow(SimulationWorld world, RowAction action) {
        List<Callable<Void>> tasks = new ArrayList<>(world.height());
        for (int row = 0; row < world.height(); row++) {
            final int currentRow = row;
            tasks.add(() -> {
                action.process(currentRow);
                return null;
            });
        }

        try {
            List<Future<Void>> futures = workerPool.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to process simulation tasks in parallel", exception);
        }
    }

    public void forEachCell(SimulationWorld world, CellAction action) {
        forEachRow(world, row -> {
            for (int x = 0; x < world.width(); x++) {
                action.process(world.cellAt(x, row));
            }
        });
    }

    @FunctionalInterface
    public interface RowAction {
        void process(int row);
    }

    @FunctionalInterface
    public interface CellAction {
        void process(SimulationCell cell);
    }
}
