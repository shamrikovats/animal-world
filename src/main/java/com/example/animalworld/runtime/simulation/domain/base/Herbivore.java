package com.example.animalworld.runtime.simulation.domain.base;

import com.example.animalworld.runtime.simulation.domain.config.RuntimeSpeciesConfig;

/**
 * Базовый класс для травоядных животных.
 *
 * @author Shamrikova Tatiana
 */
public abstract class Herbivore extends Animal {
    protected Herbivore(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        super(configuration, spawnState);
    }
}
