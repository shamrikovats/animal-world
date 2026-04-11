package com.example.animalworld.simulation.domain.base;

import com.example.animalworld.simulation.domain.dictionary.Sex;

/**
 * Начальное состояние животного при создании runtime-объекта.
 *
 * @author Shamrikova Tatiana
 */
public record AnimalSpawnState(
        long runtimeId,
        Sex sex,
        double satiety,
        boolean alive,
        boolean pregnant,
        int pregnancyRemainingTicks
) {
}
