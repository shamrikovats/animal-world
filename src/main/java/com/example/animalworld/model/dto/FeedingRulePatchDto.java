package com.example.animalworld.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * @author Shamrikova Tatiana
 */
public record FeedingRulePatchDto(
        @NotNull
        Integer speciesId,
        Integer preySpeciesId,
        Integer preyPlantSpeciesId,
        @NotNull
        @Min(0)
        @Max(100)
        Integer probability
) {
}
