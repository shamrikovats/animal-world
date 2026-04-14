package com.example.animalworld.simulation.domain.base;

import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;

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
