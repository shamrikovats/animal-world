package com.example.animalworld.model.dto;

import com.example.animalworld.model.WorldStatus;
import jakarta.validation.constraints.NotNull;

/**
 * @author Shamrikova Tatiana
 */
public record WorldStatusUpdateDto(
        @NotNull
        WorldStatus status
) {
}
