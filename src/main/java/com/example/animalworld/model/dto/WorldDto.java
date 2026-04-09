package com.example.animalworld.model.dto;

import com.example.animalworld.model.WorldStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * @author Shamrikova Tatiana
 */
public record WorldDto(
        @NotNull
        Integer id,
        String name,
        String description,
        @NotNull
        Instant createdAt,
        @NotNull
        WorldStatus status
) {
}
