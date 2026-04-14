package com.example.animalworld.model.entity;

/**
 * @author Shamrikova Tatiana
 */
public record Species(
        Integer id,
        String name,
        boolean predator,
        boolean herbivore
) {
}
