package com.example.animalworld.scheduler;

import com.example.animalworld.model.WorldStatus;
import com.example.animalworld.repository.WorldRepository;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.simulation.engine.WorldPopulationSnapshot;
import com.example.animalworld.job.WorldTickJob;
import com.example.animalworld.simulation.service.SimulationWorldBootstrapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Сервис запуска и остановки runtime-миров.
 * Следит, чтобы один мир не запускался дважды, и управляет задачами тактов.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class WorldSimulationScheduler {
    private static final Logger log = LoggerFactory.getLogger(WorldSimulationScheduler.class);

    private final ScheduledExecutorService scheduler;
    private final SimulationWorldBootstrapService bootstrapService;
    private final WorldTickJob worldTickJob;
    private final SimulationWorldRegistry registry;
    private final WorldRepository worldRepository;

    WorldSimulationScheduler(
            @Qualifier("simulationScheduler") ScheduledExecutorService scheduler,
            SimulationWorldBootstrapService bootstrapService,
            WorldTickJob worldTickJob,
            SimulationWorldRegistry registry,
            WorldRepository worldRepository
    ) {
        this.scheduler = scheduler;
        this.bootstrapService = bootstrapService;
        this.worldTickJob = worldTickJob;
        this.registry = registry;
        this.worldRepository = worldRepository;
    }

    public synchronized void startWorld(Integer worldId) {
        if (registry.isRunning(worldId)) {
            return;
        }

        SimulationWorld world = bootstrapService.bootstrap(worldId);
        RunningWorldContext context = new RunningWorldContext(
                world,
                scheduler.scheduleWithFixedDelay(
                        () -> runTick(world),
                        0,
                        world.settings().tickDuration(),
                        TimeUnit.MILLISECONDS
                )
        );

        RunningWorldContext existing = registry.putIfAbsent(worldId, context);
        if (existing != context) {
            context.scheduledTask().cancel(false);
        }
    }

    public synchronized void stopWorld(Integer worldId) {
        registry.remove(worldId).ifPresent(context -> context.scheduledTask().cancel(false));
    }

    private void runTick(SimulationWorld world) {
        try {
            WorldPopulationSnapshot snapshot = worldTickJob.run(world);
            if (snapshot.totalAnimals() == 0) {
                log.info("World {} stopped automatically because no animals are alive", world.worldId());
                stopWorld(world.worldId());
                worldRepository.updateStatus(world.worldId(), WorldStatus.INACTIVE);
            }
        } catch (Exception exception) {
            log.error("Simulation tick failed for world {}", world.worldId(), exception);
            stopWorld(world.worldId());
            worldRepository.updateStatus(world.worldId(), WorldStatus.INACTIVE);
        }
    }
}
