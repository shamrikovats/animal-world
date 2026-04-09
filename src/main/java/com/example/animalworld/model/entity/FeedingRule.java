package com.example.animalworld.model.entity;

import com.example.animalworld.model.FoodType;

/**
 * @author Shamrikova Tatiana
 */
public record FeedingRule(
        Long id,
        Integer worldId,
        Integer speciesId,
        Integer preySpeciesId,
        Integer preyPlantSpeciesId,
        FoodType foodType,
        Integer probability
) {
}
