package com.example.animalworld.model.dto;

/**
 * @author Shamrikova Tatiana
 */
public record SpeciesConfigurationDto(
        Integer speciesId,
        String speciesName,
        Integer maxCoexistCount,
        Double weight,
        Integer speedCells,
        Double fullTankWeight,
        Integer minFoodPercent,
        Integer maxChildrenCount,
        Integer pregnancyPeriod,
        Integer lostFoodForTick,
        Integer startCount
) {
}
