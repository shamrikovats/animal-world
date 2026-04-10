package com.example.animalworld.service;

import com.example.animalworld.model.FoodType;
import com.example.animalworld.model.entity.FeedingRule;
import com.example.animalworld.model.entity.PlantSpeciesConfiguration;
import com.example.animalworld.model.entity.PlantSpecies;
import com.example.animalworld.model.entity.Species;
import com.example.animalworld.model.entity.SpeciesConfiguration;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Shamrikova Tatiana
 */
@Service
public class WorldConfigurationValidationService {
    private final ReferenceDataService referenceDataService;

    WorldConfigurationValidationService(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public void validateSpeciesConfigurations(List<SpeciesConfiguration> configurations) {
        if (configurations == null || configurations.isEmpty()) {
            return;
        }

        Map<Integer, Species> speciesById = referenceDataService.getSpeciesById();
        Set<Integer> seen = new HashSet<>();
        for (SpeciesConfiguration configuration : configurations) {
            if (!speciesById.containsKey(configuration.speciesId())) {
                throw new com.example.animalworld.exception.SpeciesNotFoundByIdException(configuration.speciesId());
            }
            if (!seen.add(configuration.speciesId())) {
                throw new IllegalArgumentException(
                        "Duplicate species configuration for speciesId: %s".formatted(configuration.speciesId())
                );
            }
        }
    }

    public void validatePlantConfigurations(List<PlantSpeciesConfiguration> configurations) {
        if (configurations == null || configurations.isEmpty()) {
            return;
        }

        Map<Integer, PlantSpecies> plantSpeciesById = referenceDataService.getPlantSpeciesById();
        Set<Integer> seen = new HashSet<>();
        for (PlantSpeciesConfiguration configuration : configurations) {
            if (!plantSpeciesById.containsKey(configuration.plantSpeciesId())) {
                throw new com.example.animalworld.exception.PlantSpeciesNotFoundByIdException(
                        configuration.plantSpeciesId()
                );
            }
            if (!seen.add(configuration.plantSpeciesId())) {
                throw new IllegalArgumentException(
                        "Duplicate plant configuration for plantSpeciesId: %s".formatted(configuration.plantSpeciesId())
                );
            }
        }
    }

    public void validateFeedingRules(List<FeedingRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return;
        }

        Map<Integer, Species> speciesById = referenceDataService.getSpeciesById();
        Map<Integer, PlantSpecies> plantSpeciesById = referenceDataService.getPlantSpeciesById();
        Set<FeedingRule.Key> seen = new HashSet<>();

        for (FeedingRule rule : rules) {
            if (!speciesById.containsKey(rule.speciesId())) {
                throw new com.example.animalworld.exception.SpeciesNotFoundByIdException(rule.speciesId());
            }

            boolean hasSpeciesPrey = rule.preySpeciesId() != null;
            boolean hasPlantPrey = rule.preyPlantSpeciesId() != null;
            if (hasSpeciesPrey == hasPlantPrey) {
                throw new IllegalArgumentException("Exactly one prey type must be specified for feeding rule");
            }

            if (hasSpeciesPrey) {
                if (!speciesById.containsKey(rule.preySpeciesId())) {
                    throw new com.example.animalworld.exception.SpeciesNotFoundByIdException(rule.preySpeciesId());
                }
                if (rule.foodType() != FoodType.SPECIES) {
                    throw new IllegalArgumentException("Food type must be SPECIES when preySpeciesId is used");
                }
            } else {
                if (!plantSpeciesById.containsKey(rule.preyPlantSpeciesId())) {
                    throw new com.example.animalworld.exception.PlantSpeciesNotFoundByIdException(
                            rule.preyPlantSpeciesId()
                    );
                }
                if (rule.foodType() != FoodType.PLANT) {
                    throw new IllegalArgumentException("Food type must be PLANT when preyPlantSpeciesId is used");
                }
            }

            if (!seen.add(rule.key())) {
                throw new IllegalArgumentException(
                        "Duplicate feeding rule for speciesId=%s, preySpeciesId=%s, preyPlantSpeciesId=%s"
                                .formatted(rule.speciesId(), rule.preySpeciesId(), rule.preyPlantSpeciesId())
                );
            }
        }
    }

    public void validateSpeciesPath(Integer pathSpeciesId, Integer bodySpeciesId) {
        if (!Objects.equals(pathSpeciesId, bodySpeciesId)) {
            throw new IllegalArgumentException("Path variable speciesId must match request body speciesId");
        }
    }

    public void validatePlantPath(Integer pathPlantSpeciesId, Integer bodyPlantSpeciesId) {
        if (!Objects.equals(pathPlantSpeciesId, bodyPlantSpeciesId)) {
            throw new IllegalArgumentException("Path variable plantSpeciesId must match request body plantSpeciesId");
        }
    }
}
