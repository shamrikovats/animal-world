package com.example.animalworld.scheduler;

import com.example.animalworld.simulation.domain.world.SimulationWorld;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр активных runtime-миров.
 * Нужен, чтобы не запускать один и тот же мир дважды и уметь быстро найти уже работающий runtime.
 *
 * @author Shamrikova Tatiana
 */
@Component
public class SimulationWorldRegistry {
    private final ConcurrentHashMap<Integer, RunningWorldContext> runningWorlds = new ConcurrentHashMap<>();

    public boolean isRunning(Integer worldId) {
        return runningWorlds.containsKey(worldId);
    }

    public Optional<RunningWorldContext> findContext(Integer worldId) {
        return Optional.ofNullable(runningWorlds.get(worldId));
    }

    public Optional<SimulationWorld> findWorld(Integer worldId) {
        return findContext(worldId).map(RunningWorldContext::world);
    }

    public RunningWorldContext putIfAbsent(Integer worldId, RunningWorldContext context) {
        RunningWorldContext existing = runningWorlds.putIfAbsent(worldId, context);
        return existing == null ? context : existing;
    }

    public Optional<RunningWorldContext> remove(Integer worldId) {
        return Optional.ofNullable(runningWorlds.remove(worldId));
    }
}
