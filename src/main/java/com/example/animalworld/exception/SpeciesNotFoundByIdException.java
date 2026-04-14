package com.example.animalworld.exception;

/**
 * @author Shamrikova Tatiana
 */
public class SpeciesNotFoundByIdException extends RuntimeException {
    public SpeciesNotFoundByIdException(Integer id) {
        super("Species not found by id: '%s'".formatted(id));
    }
}
