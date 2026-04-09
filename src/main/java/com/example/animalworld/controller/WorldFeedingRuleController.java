package com.example.animalworld.controller;

import com.example.animalworld.mapper.WorldConfigurationMapper;
import com.example.animalworld.model.dto.FeedingRuleDto;
import com.example.animalworld.model.dto.FeedingRulePatchDto;
import com.example.animalworld.model.entity.PlantSpecies;
import com.example.animalworld.model.entity.Species;
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
@RequestMapping("/api/worlds/{worldId}/feeding-rules")
public class WorldFeedingRuleController {
    private final WorldConfigurationMapper mapper;
    private final WorldConfigurationService configurationService;
    private final ReferenceDataService referenceDataService;

    WorldFeedingRuleController(
            WorldConfigurationMapper mapper,
            WorldConfigurationService configurationService,
            ReferenceDataService referenceDataService
    ) {
        this.mapper = mapper;
        this.configurationService = configurationService;
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public List<FeedingRuleDto> getFeedingRules(@PathVariable Integer worldId) {
        Map<Integer, Species> speciesById = referenceDataService.getSpeciesById();
        Map<Integer, PlantSpecies> plantSpeciesById = referenceDataService.getPlantSpeciesById();
        return configurationService.getFeedingRules(worldId).stream()
                .map(rule -> mapper.toDto(rule, speciesById, plantSpeciesById))
                .toList();
    }

    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.PATCH})
    public List<FeedingRuleDto> updateFeedingRules(
            @PathVariable Integer worldId,
            @RequestBody List<@Valid FeedingRulePatchDto> dtos
    ) {
        Map<Integer, Species> speciesById = referenceDataService.getSpeciesById();
        Map<Integer, PlantSpecies> plantSpeciesById = referenceDataService.getPlantSpeciesById();
        return configurationService.updateFeedingRules(worldId, mapper.toFeedingEntities(worldId, dtos)).stream()
                .map(rule -> mapper.toDto(rule, speciesById, plantSpeciesById))
                .toList();
    }
}
