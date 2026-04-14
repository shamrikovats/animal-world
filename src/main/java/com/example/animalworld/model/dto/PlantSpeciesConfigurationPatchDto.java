package com.example.animalworld.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * @author Shamrikova Tatiana
 */
public record PlantSpeciesConfigurationPatchDto(
        @NotNull
        Integer plantSpeciesId,
        @PositiveOrZero
        Integer maxRepairSpeed,
        @Positive
        Integer weight,
        @PositiveOrZero
        Integer startCount
) {
}
