package com.example.animalworld.simulation.domain.dictionary;

/**
 * Направление перемещения животного по клеткам мира(по сути по декартовой системе XY).

 *
 * @author Shamrikova Tatiana
 */
public enum MovementDirection {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0),
    STAY(0, 0);

    private final int deltaX;
    private final int deltaY;

    MovementDirection(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public int deltaX() {
        return deltaX;
    }

    public int deltaY() {
        return deltaY;
    }
}
