package com.example.animalworld.controller;

import com.example.animalworld.mapper.WorldConfigurationMapper;
import com.example.animalworld.model.dto.WorldTickStatDto;
import com.example.animalworld.facade.WorldLookupService;
import com.example.animalworld.service.WorldTickStatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Shamrikova Tatiana
 */
@RestController
@RequestMapping("/api/worlds/{worldId}/stats")
public class WorldStatsController {
    private final WorldConfigurationMapper mapper;
    private final WorldLookupService worldLookupService;
    private final WorldTickStatService worldTickStatService;

    WorldStatsController(
            WorldConfigurationMapper mapper,
            WorldLookupService worldLookupService,
            WorldTickStatService worldTickStatService
    ) {
        this.mapper = mapper;
        this.worldLookupService = worldLookupService;
        this.worldTickStatService = worldTickStatService;
    }

    @GetMapping
    public List<WorldTickStatDto> getWorldStats(@PathVariable Integer worldId) {
        worldLookupService.findById(worldId);
        return worldTickStatService.getAllByWorldId(worldId).stream()
                .map(mapper::toDto)
                .toList();
    }
}
