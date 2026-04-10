package com.example.animalworld.controller;

import com.example.animalworld.mapper.WorldConfigurationMapper;
import com.example.animalworld.model.dto.WorldTickStatDto;
import com.example.animalworld.service.WorldLookupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Shamrikova Tatiana
 */
@RestController
@RequestMapping("/api/worlds/{worldId}/stats")
public class WorldStatsController {
    private final WorldConfigurationMapper mapper;
    private final WorldLookupService service;

    WorldStatsController(WorldConfigurationMapper mapper, WorldLookupService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @GetMapping
    public List<WorldTickStatDto> getWorldStats(@PathVariable Integer worldId) {
        return service.getStats(worldId).stream()
                .map(mapper::toDto)
                .toList();
    }
}
