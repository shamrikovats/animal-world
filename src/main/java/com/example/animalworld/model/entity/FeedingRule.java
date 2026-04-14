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
    public Key key() {
        return new Key(speciesId, preySpeciesId, preyPlantSpeciesId);
    }

    public FeedingRule withWorldId(Integer worldId) {
        return new FeedingRule(
                id,
                worldId,
                speciesId,
                preySpeciesId,
                preyPlantSpeciesId,
                foodType,
                probability
        );
    }

    public record Key(
            Integer speciesId,
            Integer preySpeciesId,
            Integer preyPlantSpeciesId
    ) {
    }
}
