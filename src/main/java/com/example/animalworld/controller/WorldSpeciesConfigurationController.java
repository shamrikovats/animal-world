package com.example.animalworld.controller;

import com.example.animalworld.mapper.WorldConfigurationMapper;
import com.example.animalworld.model.dto.SpeciesConfigurationDto;
import com.example.animalworld.model.dto.SpeciesConfigurationPatchDto;
import com.example.animalworld.model.entity.Species;
import com.example.animalworld.service.ReferenceDataService;
import com.example.animalworld.facade.WorldConfigurationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Shamrikova Tatiana
 */
@RestController
@RequestMapping("/api/worlds/{worldId}/species-configurations")
public class WorldSpeciesConfigurationController {
    private final WorldConfigurationMapper mapper;
    private final WorldConfigurationService configurationService;
    private final ReferenceDataService referenceDataService;

    WorldSpeciesConfigurationController(
            WorldConfigurationMapper mapper,
            WorldConfigurationService configurationService,
            ReferenceDataService referenceDataService
    ) {
        this.mapper = mapper;
        this.configurationService = configurationService;
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public List<SpeciesConfigurationDto> getSpeciesConfigurations(@PathVariable Integer worldId) {
        Map<Integer, Species> speciesById = referenceDataService.getSpeciesById();
        return configurationService.getSpeciesConfigurations(worldId).stream()
                .map(configuration -> mapper.toDto(configuration, speciesById))
                .toList();
    }

    @RequestMapping(path = "/{speciesId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public SpeciesConfigurationDto updateSpeciesConfiguration(
            @PathVariable Integer worldId,
            @PathVariable Integer speciesId,
            @Valid @RequestBody SpeciesConfigurationPatchDto dto
    ) {
        Map<Integer, Species> speciesById = referenceDataService.getSpeciesById();
        return mapper.toDto(
                configurationService.updateSpeciesConfiguration(worldId, speciesId, mapper.toEntity(worldId, dto)),
                speciesById
        );
    }
}
