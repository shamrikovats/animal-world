package com.example.animalworld.simulation.domain.world;

import java.util.Locale;

/**
 * Тип локации клетки в runtime-мире.
 * Значения приходят из БД и приводятся к enum для удобной работы в коде.
 *
 * @author Shamrikova Tatiana
 */
public enum LocationType {
    FOREST,
    FIELD,
    MOUNTAIN,
    RIVER,
    SWAMP;

    public static LocationType fromDatabaseName(String name) {
        return LocationType.valueOf(name.trim().toUpperCase(Locale.ROOT));
    }
}
