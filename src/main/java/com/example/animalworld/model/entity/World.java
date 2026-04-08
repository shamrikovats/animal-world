package com.example.animalworld.model.entity;

import com.example.animalworld.model.WorldStatus;

import java.time.Instant;

/**
 * @author Shamrikova Tatiana
 */
public record World(
        Integer id,
        String name,
        String description,
        Instant createdAt,
        WorldStatus status
) {
}
