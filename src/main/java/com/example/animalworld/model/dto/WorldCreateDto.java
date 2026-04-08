package com.example.animalworld.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Shamrikova Tatiana
 */
public record WorldCreateDto(
        @NotBlank
        String name,
        @NotBlank
        String description
) {
}
