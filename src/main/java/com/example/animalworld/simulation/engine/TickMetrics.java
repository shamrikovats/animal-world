package com.example.animalworld.simulation.engine;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Счетчики одного такта симуляции.
 *
 * @author Shamrikova Tatiana
 */
public class TickMetrics {
    private final AtomicInteger births = new AtomicInteger();
    private final AtomicInteger deaths = new AtomicInteger();
    private final Map<String, AtomicInteger> birthsBySpecies = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> deathsBySpecies = new ConcurrentHashMap<>();

    public void incrementBirths(int value) {
        births.addAndGet(value);
    }

    public void incrementBirths(String speciesName, int value) {
        incrementBirths(value);
        birthsBySpecies.computeIfAbsent(speciesName, ignored -> new AtomicInteger()).addAndGet(value);
    }

    public void incrementDeaths(int value) {
        deaths.addAndGet(value);
    }

    public void incrementDeaths(String speciesName, int value) {
        incrementDeaths(value);
        deathsBySpecies.computeIfAbsent(speciesName, ignored -> new AtomicInteger()).addAndGet(value);
    }

    public int births() {
        return births.get();
    }

    public int deaths() {
        return deaths.get();
    }

    public Map<String, Integer> birthsBySpecies() {
        return birthsBySpecies.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry ->
                        entry.getValue().get()));
    }

    public Map<String, Integer> deathsBySpecies() {
        return deathsBySpecies.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry ->
                        entry.getValue().get()));
    }
}
