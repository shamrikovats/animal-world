package com.example.animalworld.model.entity;

/**
 * @author Shamrikova Tatiana
 */
public record SpeciesConfiguration(
        Integer worldId,
        Integer speciesId,
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
