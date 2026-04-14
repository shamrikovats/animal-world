package com.example.animalworld.simulation.domain.species.predator;

import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.simulation.domain.base.Predator;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;

/**
 * Класс волка в runtime-модели.
 *
 * @author Shamrikova Tatiana
 */
public class Wolf extends Predator {
    public Wolf(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        super(configuration, spawnState);
    }

    @Override
    protected Animal createOffspring(AnimalSpawnState spawnState) {
        return new Wolf(configuration(), spawnState);
    }
}
