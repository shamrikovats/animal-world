package com.example.animalworld.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * @author Shamrikova Tatiana
 */
public record WorldCreateDto(
        @NotBlank
        String name,
        @NotBlank
        String description,
        @Valid
        WorldSettingsPatchDto settings,
        List<@Valid SpeciesConfigurationPatchDto> speciesConfigurations,
        List<@Valid FeedingRulePatchDto> feedingRules,
        List<@Valid PlantSpeciesConfigurationPatchDto> plantConfigurations
) {
}
