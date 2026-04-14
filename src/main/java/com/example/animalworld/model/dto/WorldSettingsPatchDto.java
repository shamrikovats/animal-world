package com.example.animalworld.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * @author Shamrikova Tatiana
 */
public record WorldSettingsPatchDto(
        @Positive
        Integer tickDuration,
        @Positive
        Integer height,
        @Positive
        Integer width,
        @Min(0)
        @Max(100)
        Integer startHungryPercent,
        @PositiveOrZero
        Integer startPredatorCounter,
        @PositiveOrZero
        Integer startHerbivoreCounter,
        @PositiveOrZero
        Integer startPlantsMass
) {
}
