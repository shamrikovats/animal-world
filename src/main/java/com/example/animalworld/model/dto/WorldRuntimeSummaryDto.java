package com.example.animalworld.model.dto;

import com.example.animalworld.model.WorldStatus;

/**
 * Короткая сводка runtime-мира для ручной проверки через API.
 * Нужна, чтобы можно было поднять мир в памяти и увидеть базовое состояние без отладчика.
 *
 * @author Shamrikova Tatiana
 */
public record WorldRuntimeSummaryDto(
        Integer worldId,
        String worldName,
        WorldStatus status,
        long currentTick,
        int height,
        int width,
        int totalAnimals,
        int totalPlants
) {
}
