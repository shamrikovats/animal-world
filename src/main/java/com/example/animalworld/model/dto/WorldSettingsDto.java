package com.example.animalworld.model.dto;

/**
 * @author Shamrikova Tatiana
 */
public record WorldSettingsDto(
        Integer tickDuration,
        Integer height,
        Integer width,
        Integer startHungryPercent,
        Integer startPredatorCounter,
        Integer startHerbivoreCounter,
        Integer startPlantsMass
) {
}
