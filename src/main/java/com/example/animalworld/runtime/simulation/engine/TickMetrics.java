package com.example.animalworld.runtime.simulation.engine;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Счетчики одного такта симуляции.
 *
 * @author Shamrikova Tatiana
 */
public class TickMetrics {
    private final AtomicInteger births = new AtomicInteger();
    private final AtomicInteger deaths = new AtomicInteger();

    public void incrementBirths(int value) {
        births.addAndGet(value);
    }

    public void incrementDeaths(int value) {
        deaths.addAndGet(value);
    }

    public int births() {
        return births.get();
    }

    public int deaths() {
        return deaths.get();
    }
}
