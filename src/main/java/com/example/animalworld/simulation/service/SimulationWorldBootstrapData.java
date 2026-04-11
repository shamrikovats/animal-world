package com.example.animalworld.simulation.service;

import com.example.animalworld.model.entity.FeedingRule;
import com.example.animalworld.model.entity.PlantSpeciesConfiguration;
import com.example.animalworld.model.entity.SpeciesConfiguration;
import com.example.animalworld.model.entity.World;
import com.example.animalworld.model.entity.WorldSettings;

import java.util.List;

/**
 * Данные, которых достаточно, чтобы собрать runtime-мир.
 *
 * @author Shamrikova Tatiana
 */
public record SimulationWorldBootstrapData(
        World world,
        WorldSettings settings,
        long currentTick,
        List<SpeciesConfiguration> speciesConfigurations,
        List<PlantSpeciesConfiguration> plantConfigurations,
        List<FeedingRule> feedingRules
) {
}
