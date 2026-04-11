package com.example.animalworld.simulation.domain.config;

/**
 * Настройки мира в runtime-формате.
 * Это отдельный объект, чтобы память симуляции была отделена от бд.
 *
 * @author Shamrikova Tatiana
 */
public record RuntimeWorldSettings(
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
