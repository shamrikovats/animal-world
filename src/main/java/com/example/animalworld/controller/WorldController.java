package com.example.animalworld.controller;

import com.example.animalworld.mapper.WorldConfigurationMapper;
import com.example.animalworld.mapper.WorldMapper;
import com.example.animalworld.model.dto.WorldCreateDto;
import com.example.animalworld.model.dto.WorldDto;
import com.example.animalworld.model.dto.WorldStatusUpdateDto;
import com.example.animalworld.model.entity.World;
import com.example.animalworld.facade.WorldLifecycleService;
import com.example.animalworld.facade.WorldLookupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * @author Shamrikova Tatiana
 */
@RestController
@RequestMapping("/api/worlds")
public class WorldController {
    private final WorldMapper mapper;
    private final WorldConfigurationMapper configurationMapper;
    private final WorldLifecycleService lifecycleService;
    private final WorldLookupService lookupService;

    WorldController(
            WorldMapper mapper,
            WorldConfigurationMapper configurationMapper,
            WorldLifecycleService lifecycleService,
            WorldLookupService lookupService
    ) {
        this.mapper = mapper;
        this.configurationMapper = configurationMapper;
        this.lifecycleService = lifecycleService;
        this.lookupService = lookupService;
    }

    @PostMapping
    public ResponseEntity<WorldDto> createWorld(@Valid @RequestBody WorldCreateDto dto) {
        World createdWorld = lifecycleService.create(
                mapper.toEntity(dto),
                dto.settings() == null ? null : configurationMapper.toEntity(null, dto.settings()),
                configurationMapper.toSpeciesEntities(null, dto.speciesConfigurations()),
                configurationMapper.toPlantEntities(null, dto.plantConfigurations()),
                configurationMapper.toFeedingEntities(null, dto.feedingRules())
        );
        WorldDto responseBody = mapper.toDto(createdWorld);
        return ResponseEntity.created(URI.create("/api/worlds/" + responseBody.id())).body(responseBody);
    }

    @GetMapping
    public List<WorldDto> findAllWorlds() {
        return lookupService.getAll().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public WorldDto findWorldById(@PathVariable Integer id) {
        return mapper.toDto(lookupService.findById(id));
    }

    @PatchMapping("/{id}/status")
    public WorldDto updateWorldStatus(
            @PathVariable Integer id,
            @Valid @RequestBody WorldStatusUpdateDto dto
    ) {
        return mapper.toDto(lifecycleService.updateStatus(id, dto.status()));
    }
}
