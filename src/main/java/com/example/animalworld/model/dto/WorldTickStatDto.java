package com.example.animalworld.model.dto;

import java.time.Instant;

/**
 * @author Shamrikova Tatiana
 */
public record WorldTickStatDto(
        Integer tickNumber,
        Integer alivePredatorCount,
        Integer aliveHerbivoreCount,
        Double totalPlantMass,
        Integer birthCount,
        Integer deathCount,
        Instant statsCreatedAt
) {
}
