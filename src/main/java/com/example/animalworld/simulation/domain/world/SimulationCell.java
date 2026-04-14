package com.example.animalworld.simulation.domain.world;

import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;
import com.example.animalworld.simulation.domain.flora.Plant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Одна клетка мира в памяти.
 * Хранит координаты, тип локации, животных, растения и lock для многопоточки.
 *
 * @author Shamrikova Tatiana
 */
public class SimulationCell {
    private final int x;
    private final int y;
    private final LocationType locationType;
    private final Map<Integer, List<Animal>> animalsBySpecies = new HashMap<>();
    private final List<Plant> plants = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public SimulationCell(int x, int y, LocationType locationType) {
        this.x = x;
        this.y = y;
        this.locationType = locationType;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public LocationType locationType() {
        return locationType;
    }

    public ReentrantLock lock() {
        return lock;
    }

    public void withLock(Runnable action) {
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    public <T> T withLock(Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    public boolean canAcceptAnimal(RuntimeSpeciesConfig configuration) {
        return animalsBySpecies.getOrDefault(configuration.speciesId(), List.of()).size() < configuration.maxCoexistCount();
    }

    public void addAnimal(Animal animal) {
        animalsBySpecies.computeIfAbsent(animal.speciesId(), ignored -> new ArrayList<>()).add(animal);
    }

    public void removeAnimal(Animal animal) {
        List<Animal> animals = animalsBySpecies.get(animal.speciesId());
        if (animals == null) {
            return;
        }
        animals.remove(animal);
        if (animals.isEmpty()) {
            animalsBySpecies.remove(animal.speciesId());
        }
    }

    public void addPlant(Plant plant) {
        plants.add(plant);
    }

    public void removePlant(Plant plant) {
        plants.remove(plant);
    }

    public int plantCount(Integer plantSpeciesId) {
        return (int) plants.stream()
                .filter(plant -> plant.plantSpeciesId().equals(plantSpeciesId))
                .count();
    }

    public List<Plant> plantsSnapshot() {
        return List.copyOf(plants);
    }

    public List<Animal> animalsSnapshot() {
        return animalsBySpecies.values().stream()
                .flatMap(Collection::stream)
                .toList();
    }

    public Map<Integer, List<Animal>> animalsBySpeciesSnapshot() {
        Map<Integer, List<Animal>> snapshot = new HashMap<>();
        animalsBySpecies.forEach((speciesId, animals) -> snapshot.put(speciesId, List.copyOf(animals)));
        return Map.copyOf(snapshot);
    }
}
