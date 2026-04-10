package com.example.animalworld.controller;

import com.example.animalworld.mapper.WorldConfigurationMapper;
import com.example.animalworld.model.dto.WorldSettingsDto;
import com.example.animalworld.model.dto.WorldSettingsPatchDto;
import com.example.animalworld.service.WorldConfigurationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * @author Shamrikova Tatiana
 */
@RestController
@RequestMapping("/api/worlds/{worldId}/settings")
public class WorldSettingsController {
    private final WorldConfigurationMapper mapper;
    private final WorldConfigurationService service;

    WorldSettingsController(WorldConfigurationMapper mapper, WorldConfigurationService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @GetMapping
    public WorldSettingsDto getWorldSettings(@PathVariable Integer worldId) {
        return mapper.toDto(service.getSettings(worldId));
    }

    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.PATCH})
    public WorldSettingsDto updateWorldSettings(
            @PathVariable Integer worldId,
            @Valid @RequestBody WorldSettingsPatchDto dto
    ) {
        return mapper.toDto(service.updateSettings(worldId, mapper.toEntity(worldId, dto)));
    }
}
