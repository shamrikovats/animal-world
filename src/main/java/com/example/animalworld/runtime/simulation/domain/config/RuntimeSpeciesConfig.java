package com.example.animalworld.runtime.simulation.domain.config;

/**
 * Конфиг животного вида для конкретного мира в runtime-виде.
 * Тут лежат все числа, которые нужны движку симуляции во время работы.
 *
 * @author Shamrikova Tatiana
 */
public record RuntimeSpeciesConfig(
        Integer worldId,
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
