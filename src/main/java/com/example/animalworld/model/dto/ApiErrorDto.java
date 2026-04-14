package com.example.animalworld.model.dto;

import java.time.Instant;

/**
 * @author Shamrikova Tatiana
 */
public record ApiErrorDto(
        Instant timestamp,
        int status,
        String error,
        String message
) {
}
