package com.example.animalworld.model.entity;

import java.time.Instant;

/**
 * @author Shamrikova Tatiana
 */
public record WorldTickStat(
        Integer worldId,
        Integer tickNumber,
        Integer alivePredatorCount,
        Integer aliveHerbivoreCount,
        Double totalPlantMass,
        Integer birthCount,
        Integer deathCount,
        Instant statsCreatedAt
) {
}
