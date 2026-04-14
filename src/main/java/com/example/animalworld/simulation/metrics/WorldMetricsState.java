package com.example.animalworld.simulation.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Текущее in-memory состояние метрик одного мира.
 * Нужен как простой контейнер под gauge/counter значения без логики симуляции.
 *
 * @author Shamrikova Tatiana
 */
public class WorldMetricsState {
    private final AtomicInteger running = new AtomicInteger();
    private final AtomicLong currentTick = new AtomicLong();
    private final AtomicInteger predatorsAlive = new AtomicInteger();
    private final AtomicInteger herbivoresAlive = new AtomicInteger();
    private final DoubleAdder plantMass = new DoubleAdder();
    private final AtomicInteger birthsLastTick = new AtomicInteger();
    private final AtomicInteger deathsLastTick = new AtomicInteger();

    public AtomicInteger running() {
        return running;
    }

    public AtomicLong currentTick() {
        return currentTick;
    }

    public AtomicInteger predatorsAlive() {
        return predatorsAlive;
    }

    public AtomicInteger herbivoresAlive() {
        return herbivoresAlive;
    }

    public DoubleAdder plantMass() {
        return plantMass;
    }

    public AtomicInteger birthsLastTick() {
        return birthsLastTick;
    }

    public AtomicInteger deathsLastTick() {
        return deathsLastTick;
    }
}
