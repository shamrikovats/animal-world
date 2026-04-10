package com.example.animalworld.runtime.simulation.domain.species.herbivore;

import com.example.animalworld.runtime.simulation.domain.base.Animal;
import com.example.animalworld.runtime.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.runtime.simulation.domain.base.Herbivore;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeSpeciesConfig;

/**
 * Класс буйвола в runtime-модели.
 *
 * @author Shamrikova Tatiana
 */
public class Buffalo extends Herbivore {
    public Buffalo(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        super(configuration, spawnState);
    }

    @Override
    protected Animal createOffspring(AnimalSpawnState spawnState) {
        return new Buffalo(configuration(), spawnState);
    }
}
