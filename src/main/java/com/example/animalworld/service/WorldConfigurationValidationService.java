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
            requireSpeciesExists(configuration.speciesId(), speciesById);
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
            requirePlantSpeciesExists(configuration.plantSpeciesId(), plantSpeciesById);
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
        Set<FeedingRuleKey> seen = new HashSet<>();

        for (FeedingRule rule : rules) {
            requireSpeciesExists(rule.speciesId(), speciesById);

            boolean hasSpeciesPrey = rule.preySpeciesId() != null;
            boolean hasPlantPrey = rule.preyPlantSpeciesId() != null;
            if (hasSpeciesPrey == hasPlantPrey) {
                throw new IllegalArgumentException("Exactly one prey type must be specified for feeding rule");
            }

            if (hasSpeciesPrey) {
                requireSpeciesExists(rule.preySpeciesId(), speciesById);
                if (rule.foodType() != FoodType.SPECIES) {
                    throw new IllegalArgumentException("Food type must be SPECIES when preySpeciesId is used");
                }
            } else {
                requirePlantSpeciesExists(rule.preyPlantSpeciesId(), plantSpeciesById);
                if (rule.foodType() != FoodType.PLANT) {
                    throw new IllegalArgumentException("Food type must be PLANT when preyPlantSpeciesId is used");
                }
            }

            if (!seen.add(new FeedingRuleKey(rule.speciesId(), rule.preySpeciesId(), rule.preyPlantSpeciesId()))) {
                throw new IllegalArgumentException(
                        "Duplicate feeding rule for speciesId=%s, preySpeciesId=%s, preyPlantSpeciesId=%s"
                                .formatted(rule.speciesId(), rule.preySpeciesId(), rule.preyPlantSpeciesId())
                );
            }
        }
    }

    public void validateSpeciesPath(Integer pathSpeciesId, Integer bodySpeciesId) {
        if (!pathSpeciesId.equals(bodySpeciesId)) {
            throw new IllegalArgumentException("Path variable speciesId must match request body speciesId");
        }
    }

    public void validatePlantPath(Integer pathPlantSpeciesId, Integer bodyPlantSpeciesId) {
        if (!pathPlantSpeciesId.equals(bodyPlantSpeciesId)) {
            throw new IllegalArgumentException("Path variable plantSpeciesId must match request body plantSpeciesId");
        }
    }

    private void requireSpeciesExists(Integer speciesId, Map<Integer, Species> speciesById) {
        if (!speciesById.containsKey(speciesId)) {
            referenceDataService.ensureSpeciesExists(speciesId);
        }
    }

    private void requirePlantSpeciesExists(Integer plantSpeciesId, Map<Integer, PlantSpecies> plantSpeciesById) {
        if (!plantSpeciesById.containsKey(plantSpeciesId)) {
            referenceDataService.ensurePlantSpeciesExists(plantSpeciesId);
        }
    }

    private record FeedingRuleKey(
            Integer speciesId,
            Integer preySpeciesId,
            Integer preyPlantSpeciesId
    ) {
    }
}
