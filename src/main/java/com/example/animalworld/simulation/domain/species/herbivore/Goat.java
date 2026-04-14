package com.example.animalworld.simulation.domain.species.herbivore;

import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.simulation.domain.base.Herbivore;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;

/**
 * Класс козы в runtime-модели.
 *
 * @author Shamrikova Tatiana
 */
public class Goat extends Herbivore {
    public Goat(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        super(configuration, spawnState);
    }

    @Override
    protected Animal createOffspring(AnimalSpawnState spawnState) {
        return new Goat(configuration(), spawnState);
    }
}
