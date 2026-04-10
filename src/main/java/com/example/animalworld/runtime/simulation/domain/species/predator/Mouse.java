package com.example.animalworld.runtime.simulation.domain.species.predator;

import com.example.animalworld.runtime.simulation.domain.base.Animal;
import com.example.animalworld.runtime.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.runtime.simulation.domain.base.Predator;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeSpeciesConfig;

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
