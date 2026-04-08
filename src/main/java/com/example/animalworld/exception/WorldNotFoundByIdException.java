package com.example.animalworld.exception;

/**
 * @author Shamrikova Tatiana
 */
public class WorldNotFoundByIdException extends RuntimeException {
    static final String ERROR_MESSAGE = "World not found by id: '%s'";
    public WorldNotFoundByIdException(Integer id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
