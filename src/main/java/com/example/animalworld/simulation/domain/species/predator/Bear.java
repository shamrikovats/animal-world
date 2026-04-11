package com.example.animalworld.simulation.domain.species.predator;

import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.simulation.domain.base.Predator;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;

/**
 * Класс медведя в runtime-модели.
 *
 * @author Shamrikova Tatiana
 */
public class Bear extends Predator {
    public Bear(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        super(configuration, spawnState);
    }

    @Override
    protected Animal createOffspring(AnimalSpawnState spawnState) {
        return new Bear(configuration(), spawnState);
    }
}
