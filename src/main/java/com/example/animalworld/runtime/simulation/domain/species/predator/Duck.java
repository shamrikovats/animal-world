package com.example.animalworld.runtime.simulation.domain.species.predator;

import com.example.animalworld.runtime.simulation.domain.base.Animal;
import com.example.animalworld.runtime.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.runtime.simulation.domain.base.Predator;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeSpeciesConfig;

/**
 * Класс утки в runtime-модели.
 * В текущем справочнике утка может быть не только травоядной, поэтому она идет через ветку хищников.
 *
 * @author Shamrikova Tatiana
 */
public class Duck extends Predator {
    public Duck(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        super(configuration, spawnState);
    }

    @Override
    protected Animal createOffspring(AnimalSpawnState spawnState) {
        return new Duck(configuration(), spawnState);
    }
}
