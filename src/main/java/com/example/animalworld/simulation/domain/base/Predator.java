package com.example.animalworld.simulation.domain.base;

import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;

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
