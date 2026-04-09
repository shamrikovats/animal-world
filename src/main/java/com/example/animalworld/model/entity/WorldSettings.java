package com.example.animalworld.model.entity;

/**
 * @author Shamrikova Tatiana
 */
public record WorldSettings(
        Integer worldId,
        Integer tickDuration,
        Integer height,
        Integer width,
        Integer startHungryPercent,
        Integer startPredatorCounter,
        Integer startHerbivoreCounter,
        Integer startPlantsMass
) {
}
