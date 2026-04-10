package com.example.animalworld.runtime.simulation.domain.base;

import com.example.animalworld.runtime.simulation.domain.config.RuntimeSpeciesConfig;

/**
 * Базовый класс для хищников.
 *
 * @author Shamrikova Tatiana
 */
public abstract class Predator extends Animal {
    protected Predator(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        super(configuration, spawnState);
    }
}
