package com.example.animalworld.runtime.simulation.domain.base;

import com.example.animalworld.model.FoodType;

/**
 * Общий контракт для всего, что можно слопать в runtime-модели.
 * Сейчас это животные и растения.
 *
 * @author Shamrikova Tatiana
 */
public interface Consumable {
    FoodType foodType();

    boolean isAlive();

    double consume();
}
