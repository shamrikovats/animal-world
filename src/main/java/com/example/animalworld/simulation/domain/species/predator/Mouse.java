package com.example.animalworld.simulation.domain.species.predator;

import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.simulation.domain.base.Predator;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;

/**
 * Класс мыши в runtime-модели.
 * В текущей предметной модели мышь может выступать смешанным видом, поэтому хищник
 *
 * @author Shamrikova Tatiana
 */
public class Mouse extends Predator {
    public Mouse(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        super(configuration, spawnState);
    }

    @Override
    protected Animal createOffspring(AnimalSpawnState spawnState) {
        return new Mouse(configuration(), spawnState);
    }
}
