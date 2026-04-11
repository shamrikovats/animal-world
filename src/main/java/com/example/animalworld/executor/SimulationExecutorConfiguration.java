package com.example.animalworld.executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Конфигурация пулов потоков для симуляции.
 * Один пул отвечает за расписание тактов, второй за параллельную обработку внутри такта.
 *
 * @author Shamrikova Tatiana
 */
@Configuration
public class SimulationExecutorConfiguration {

    @Bean(name = "simulationScheduler", destroyMethod = "shutdown")
    public ScheduledExecutorService simulationScheduler() {
        return Executors.newScheduledThreadPool(
                2,
                threadFactory("simulation-scheduler-")
        );
    }

    @Bean(name = "simulationWorkerPool", destroyMethod = "shutdown")
    public ExecutorService simulationWorkerPool() {
        return Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors()),
                threadFactory("simulation-worker-")
        );
    }

    private ThreadFactory threadFactory(String prefix) {
        AtomicInteger sequence = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + sequence.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }
}
