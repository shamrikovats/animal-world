package com.example.animalworld.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * @author Shamrikova Tatiana
 */
public record SpeciesConfigurationPatchDto(
        @NotNull
        Integer speciesId,
        @PositiveOrZero
        Integer maxCoexistCount,
        @DecimalMin("0.0")
        Double weight,
        @PositiveOrZero
        Integer speedCells,
        @DecimalMin("0.0")
        Double fullTankWeight,
        @Min(0)
        @Max(100)
        Integer minFoodPercent,
        @Positive
        Integer maxChildrenCount,
        @Positive
        Integer pregnancyPeriod,
        @PositiveOrZero
        Integer lostFoodForTick,
        @PositiveOrZero
        Integer startCount
) {
}
