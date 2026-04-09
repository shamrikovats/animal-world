package com.example.animalworld.controller;

import com.example.animalworld.mapper.WorldConfigurationMapper;
import com.example.animalworld.model.dto.PlantSpeciesConfigurationDto;
import com.example.animalworld.model.dto.PlantSpeciesConfigurationPatchDto;
import com.example.animalworld.model.entity.PlantSpecies;
import com.example.animalworld.service.ReferenceDataService;
import com.example.animalworld.service.WorldConfigurationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Shamrikova Tatiana
 */
@RestController
@RequestMapping("/api/worlds/{worldId}/plant-configurations")
public class WorldPlantConfigurationController {
    private final WorldConfigurationMapper mapper;
    private final WorldConfigurationService configurationService;
    private final ReferenceDataService referenceDataService;

    WorldPlantConfigurationController(
            WorldConfigurationMapper mapper,
            WorldConfigurationService configurationService,
            ReferenceDataService referenceDataService
    ) {
        this.mapper = mapper;
        this.configurationService = configurationService;
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public List<PlantSpeciesConfigurationDto> getPlantConfigurations(@PathVariable Integer worldId) {
        Map<Integer, PlantSpecies> plantSpeciesById = referenceDataService.getPlantSpeciesById();
        return configurationService.getPlantConfigurations(worldId).stream()
                .map(configuration -> mapper.toDto(configuration, plantSpeciesById))
                .toList();
    }

    @RequestMapping(path = "/{plantSpeciesId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public PlantSpeciesConfigurationDto updatePlantConfiguration(
            @PathVariable Integer worldId,
            @PathVariable Integer plantSpeciesId,
            @Valid @RequestBody PlantSpeciesConfigurationPatchDto dto
    ) {
        Map<Integer, PlantSpecies> plantSpeciesById = referenceDataService.getPlantSpeciesById();
        return mapper.toDto(
                configurationService.updatePlantConfiguration(worldId, plantSpeciesId, mapper.toEntity(worldId, dto)),
                plantSpeciesById
        );
    }
}
