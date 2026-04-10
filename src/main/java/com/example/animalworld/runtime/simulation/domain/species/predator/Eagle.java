package com.example.animalworld.runtime.simulation.domain.species.predator;

import com.example.animalworld.runtime.simulation.domain.base.Animal;
import com.example.animalworld.runtime.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.runtime.simulation.domain.base.Predator;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeSpeciesConfig;

/**
 * Класс орла в runtime-модели.
 *
 * @author Shamrikova Tatiana
 */
public class Eagle extends Predator {
    public Eagle(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        super(configuration, spawnState);
    }

    @Override
    protected Animal createOffspring(AnimalSpawnState spawnState) {
        return new Eagle(configuration(), spawnState);
    }
}
