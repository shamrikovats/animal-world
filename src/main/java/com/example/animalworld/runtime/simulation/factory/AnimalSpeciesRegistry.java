package com.example.animalworld.runtime.simulation.factory;

import com.example.animalworld.runtime.simulation.domain.base.Animal;
import com.example.animalworld.runtime.simulation.domain.base.AnimalSpawnState;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeSpeciesConfig;
import com.example.animalworld.runtime.simulation.domain.species.herbivore.Buffalo;
import com.example.animalworld.runtime.simulation.domain.species.herbivore.Caterpillar;
import com.example.animalworld.runtime.simulation.domain.species.herbivore.Deer;
import com.example.animalworld.runtime.simulation.domain.species.herbivore.Goat;
import com.example.animalworld.runtime.simulation.domain.species.herbivore.Horse;
import com.example.animalworld.runtime.simulation.domain.species.herbivore.Rabbit;
import com.example.animalworld.runtime.simulation.domain.species.herbivore.Sheep;
import com.example.animalworld.runtime.simulation.domain.species.predator.Bear;
import com.example.animalworld.runtime.simulation.domain.species.predator.Boar;
import com.example.animalworld.runtime.simulation.domain.species.predator.Duck;
import com.example.animalworld.runtime.simulation.domain.species.predator.Eagle;
import com.example.animalworld.runtime.simulation.domain.species.predator.Fox;
import com.example.animalworld.runtime.simulation.domain.species.predator.Mouse;
import com.example.animalworld.runtime.simulation.domain.species.predator.Snake;
import com.example.animalworld.runtime.simulation.domain.species.predator.Wolf;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * Реестр concrete-классов животных.
 * Нужен, чтобы создавать правильный runtime-тип по имени вида
 *
 * @author Shamrikova Tatiana
 */
@Component
public class AnimalSpeciesRegistry {
    private final Map<String, AnimalCreator> creators = Map.ofEntries(
            Map.entry("wolf", Wolf::new),
            Map.entry("snake", Snake::new),
            Map.entry("fox", Fox::new),
            Map.entry("bear", Bear::new),
            Map.entry("eagle", Eagle::new),
            Map.entry("horse", Horse::new),
            Map.entry("deer", Deer::new),
            Map.entry("rabbit", Rabbit::new),
            Map.entry("mouse", Mouse::new),
            Map.entry("goat", Goat::new),
            Map.entry("sheep", Sheep::new),
            Map.entry("boar", Boar::new),
            Map.entry("buffalo", Buffalo::new),
            Map.entry("duck", Duck::new),
            Map.entry("caterpillar", Caterpillar::new)
    );

    public Animal create(String speciesName, RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        AnimalCreator creator = creators.get(normalize(speciesName));
        if (creator == null) {
            throw new IllegalArgumentException("Unsupported species for runtime model: " + speciesName);
        }
        return creator.create(configuration, spawnState);
    }

    private String normalize(String speciesName) {
        return speciesName.trim().toLowerCase(Locale.ROOT);
    }

    @FunctionalInterface
    private interface AnimalCreator {
        Animal create(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState);
    }
}
