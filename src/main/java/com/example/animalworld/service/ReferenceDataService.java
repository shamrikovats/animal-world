package com.example.animalworld.service;

import com.example.animalworld.exception.PlantSpeciesNotFoundByIdException;
import com.example.animalworld.exception.SpeciesNotFoundByIdException;
import com.example.animalworld.model.entity.PlantSpecies;
import com.example.animalworld.model.entity.Species;
import com.example.animalworld.repository.ReferenceDataRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Shamrikova Tatiana
 */
@Service
public class ReferenceDataService {
    private final ReferenceDataRepository repository;

    ReferenceDataService(ReferenceDataRepository repository) {
        this.repository = repository;
    }

    public Map<Integer, Species> getSpeciesById() {
        return repository.getSpeciesById();
    }

    public Map<Integer, PlantSpecies> getPlantSpeciesById() {
        return repository.getPlantSpeciesById();
    }

    public void ensureSpeciesExists(Integer speciesId) {
        if (!getSpeciesById().containsKey(speciesId)) {
            throw new SpeciesNotFoundByIdException(speciesId);
        }
    }

    public void ensurePlantSpeciesExists(Integer plantSpeciesId) {
        if (!getPlantSpeciesById().containsKey(plantSpeciesId)) {
            throw new PlantSpeciesNotFoundByIdException(plantSpeciesId);
        }
    }
}
