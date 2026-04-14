package com.example.animalworld.simulation.factory;

import com.example.animalworld.simulation.domain.base.Animal;
import com.example.animalworld.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;
import org.springframework.stereotype.Component;

/**
 * Фабрика создания runtime-животных.
 * Она получает runtime-конфиг вида и создает нужный класс животного.
 *
 * @author Shamrikova Tatiana
 */
@Component
public class AnimalFactory {
    private final AnimalSpeciesRegistry registry;

    AnimalFactory(AnimalSpeciesRegistry registry) {
        this.registry = registry;
    }

    public Animal create(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        return registry.create(configuration.speciesName(), configuration, spawnState);
    }
}
