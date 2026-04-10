package com.example.animalworld.runtime.simulation.domain.config;

/**
 * Конфиг растения для конкретного мира в runtime-виде.
 * Сюда bootstrap складывает значения из БД, чтобы симуляция не зависела от entity-слоя.
 *
 * @author Shamrikova Tatiana
 */
public record RuntimePlantConfig(
        Integer worldId,
        Integer plantSpeciesId,
        String biologicalName,
        Integer maxRepairSpeed,
        Integer weight,
        Integer startCount
) {
}
