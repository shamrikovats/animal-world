package com.example.animalworld.runtime.simulation.domain.config;

import com.example.animalworld.runtime.simulation.domain.world.LocationType;

/**
 * Runtime-правило по локациям для вида.
 *
 * @author Shamrikova Tatiana
 */
public record RuntimeLocationRule(
        Integer speciesId,
        LocationType locationType,
        int survivalModifier
) {
    public boolean forbidden() {
        return survivalModifier < 0;
    }
}
