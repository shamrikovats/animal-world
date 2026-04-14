package com.example.animalworld.model.dto;

/**
 * @author Shamrikova Tatiana
 */
public record PlantSpeciesConfigurationDto(
        Integer plantSpeciesId,
        String plantSpeciesName,
        Integer maxRepairSpeed,
        Integer weight,
        Integer startCount
) {
}
