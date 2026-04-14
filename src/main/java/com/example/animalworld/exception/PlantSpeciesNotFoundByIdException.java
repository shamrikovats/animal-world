package com.example.animalworld.exception;

/**
 * @author Shamrikova Tatiana
 */
public class PlantSpeciesNotFoundByIdException extends RuntimeException {
    public PlantSpeciesNotFoundByIdException(Integer id) {
        super("Plant species not found by id: '%s'".formatted(id));
    }
}
