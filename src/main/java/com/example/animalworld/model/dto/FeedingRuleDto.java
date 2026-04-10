package com.example.animalworld.model.dto;

import com.example.animalworld.model.FoodType;

/**
 * @author Shamrikova Tatiana
 */
public record FeedingRuleDto(
        Integer speciesId,
        String speciesName,
        FoodType foodType,
        Integer preySpeciesId,
        String preySpeciesName,
        Integer preyPlantSpeciesId,
        String preyPlantSpeciesName,
        Integer probability
) {
}
