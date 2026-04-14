package com.example.animalworld.simulation.metrics;

import com.example.animalworld.scheduler.SimulationWorldRegistry;
import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.simulation.engine.TickMetrics;
import com.example.animalworld.simulation.engine.WorldPopulationSnapshot;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Публикация бизнес-метрик симуляции в Micrometer.
 * Здесь держим только связку между runtime-миром и метриками, без доменной логики.
 *
 * @author Shamrikova Tatiana
 */
@Component
public class SimulationMetricsPublisher {
    private final MeterRegistry meterRegistry;
    private final SimulationWorldRegistry simulationWorldRegistry;
    private final ConcurrentHashMap<Integer, WorldMetricsState> worldMetrics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Counter> tickCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Counter> birthCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Counter> deathCounters = new ConcurrentHashMap<>();
    private final Counter autoStopCounter;

    SimulationMetricsPublisher(MeterRegistry meterRegistry, SimulationWorldRegistry simulationWorldRegistry) {
        this.meterRegistry = meterRegistry;
        this.simulationWorldRegistry = simulationWorldRegistry;
        this.autoStopCounter = Counter.builder("animalworld_world_auto_stops")
                .description("How many worlds were stopped automatically")
                .register(meterRegistry);

        Gauge.builder("animalworld_worlds_running_total", simulationWorldRegistry, SimulationWorldRegistry::runningWorldCount)
                .description("Currently running worlds")
                .register(meterRegistry);
    }

    public void markWorldStarted(SimulationWorld world) {
        WorldMetricsState state = stateFor(world.worldId());
        state.running().set(1);
        state.currentTick().set(world.currentTick());
    }

    public void markWorldStopped(Integer worldId) {
        stateFor(worldId).running().set(0);
    }

    public void markWorldAutoStopped(Integer worldId) {
        markWorldStopped(worldId);
        autoStopCounter.increment();
    }

    public void recordTick(
            SimulationWorld world,
            long tickNumber,
            WorldPopulationSnapshot snapshot,
            TickMetrics metrics
    ) {
        Integer worldId = world.worldId();
        WorldMetricsState state = stateFor(worldId);
        state.running().set(1);
        state.currentTick().set(tickNumber);
        state.predatorsAlive().set(snapshot.predatorCount());
        state.herbivoresAlive().set(snapshot.herbivoreCount());
        state.plantMass().reset();
        state.plantMass().add(snapshot.totalPlantMass());
        state.birthsLastTick().set(metrics.births());
        state.deathsLastTick().set(metrics.deaths());

        tickCounters.computeIfAbsent(worldId, this::registerTickCounter).increment();
        birthCounters.computeIfAbsent(worldId, this::registerBirthCounter).increment(metrics.births());
        deathCounters.computeIfAbsent(worldId, this::registerDeathCounter).increment(metrics.deaths());
    }

    private WorldMetricsState stateFor(Integer worldId) {
        return worldMetrics.computeIfAbsent(worldId, this::registerWorldMeters);
    }

    private WorldMetricsState registerWorldMeters(Integer worldId) {
        WorldMetricsState state = new WorldMetricsState();
        String worldIdTag = String.valueOf(worldId);

        Gauge.builder("animalworld_world_running", state.running(), AtomicInteger::get)
                .description("World running flag")
                .tag("world_id", worldIdTag)
                .register(meterRegistry);
        Gauge.builder("animalworld_world_current_tick", state.currentTick(), AtomicLong::get)
                .description("Current tick of world")
                .tag("world_id", worldIdTag)
                .register(meterRegistry);
        Gauge.builder("animalworld_world_predators_alive", state.predatorsAlive(), AtomicInteger::get)
                .description("Alive predators in world")
                .tag("world_id", worldIdTag)
                .register(meterRegistry);
        Gauge.builder("animalworld_world_herbivores_alive", state.herbivoresAlive(), AtomicInteger::get)
                .description("Alive herbivores in world")
                .tag("world_id", worldIdTag)
                .register(meterRegistry);
        Gauge.builder("animalworld_world_plant_mass", state.plantMass(), DoubleAdder::sum)
                .description("Current plant mass in world")
                .tag("world_id", worldIdTag)
                .register(meterRegistry);
        Gauge.builder("animalworld_world_births_last_tick", state.birthsLastTick(), AtomicInteger::get)
                .description("Birth count on last tick")
                .tag("world_id", worldIdTag)
                .register(meterRegistry);
        Gauge.builder("animalworld_world_deaths_last_tick", state.deathsLastTick(), AtomicInteger::get)
                .description("Death count on last tick")
                .tag("world_id", worldIdTag)
                .register(meterRegistry);

        return state;
    }

    private Counter registerTickCounter(Integer worldId) {
        return Counter.builder("animalworld_simulation_ticks")
                .description("Completed simulation ticks")
                .tag("world_id", String.valueOf(worldId))
                .register(meterRegistry);
    }

    private Counter registerBirthCounter(Integer worldId) {
        return Counter.builder("animalworld_simulation_births")
                .description("Total births in simulation")
                .tag("world_id", String.valueOf(worldId))
                .register(meterRegistry);
    }

    private Counter registerDeathCounter(Integer worldId) {
        return Counter.builder("animalworld_simulation_deaths")
                .description("Total deaths in simulation")
                .tag("world_id", String.valueOf(worldId))
                .register(meterRegistry);
    }
}
