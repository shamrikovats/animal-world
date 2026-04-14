package com.example.animalworld.simulation.domain.species.predator;

import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.simulation.domain.base.Predator;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;

/**
 * Класс кабана в runtime-модели.
 * В этой модели он идет через ветку хищников, потому что вид умеет есть не только растения.
 *
 * @author Shamrikova Tatiana
 */
public class Boar extends Predator {
    public Boar(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        super(configuration, spawnState);
    }

    @Override
    protected Animal createOffspring(AnimalSpawnState spawnState) {
        return new Boar(configuration(), spawnState);
    }
}
