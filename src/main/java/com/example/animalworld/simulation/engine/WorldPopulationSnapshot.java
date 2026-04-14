package com.example.animalworld.simulation.engine;

/**
 * Снимок населения мира после такта.
 *
 * @author Shamrikova Tatiana
 */
public record WorldPopulationSnapshot(
        int totalAnimals,
        int predatorCount,
        int herbivoreCount,
        double totalPlantMass
) {
}
