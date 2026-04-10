package com.example.animalworld.runtime.simulation.factory;

import com.example.animalworld.runtime.simulation.domain.base.Animal;
import com.example.animalworld.runtime.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeSpeciesConfig;
import org.springframework.stereotype.Component;

/**
 * Фабрика создания runtime-животных.
 * Она получает runtime-конфиг вида и создает нужный concrete-класс животного.
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
