package com.example.animalworld.model.entity;

/**
 * @author Shamrikova Tatiana
 */
public record PlantSpeciesConfiguration(
        Integer worldId,
        Integer plantSpeciesId,
        Integer maxRepairSpeed,
        Integer weight,
        Integer startCount
) {
}
