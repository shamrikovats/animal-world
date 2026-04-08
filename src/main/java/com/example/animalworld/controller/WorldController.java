package com.example.animalworld.controller;

import com.example.animalworld.mapper.WorldMapper;
import com.example.animalworld.model.dto.WorldCreateDto;
import com.example.animalworld.model.dto.WorldDto;
import com.example.animalworld.model.dto.WorldStatusUpdateDto;
import com.example.animalworld.model.entity.World;
import com.example.animalworld.service.WorldService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * @author Shamrikova Tatiana
 */
@RestController
@RequestMapping("/api/worlds")
public class WorldController {
    private final WorldMapper mapper;
    private final WorldService service;

    WorldController(WorldMapper mapper, WorldService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WorldDto> createWorld(@Valid @RequestBody WorldCreateDto dto) {
        World entity = mapper.toEntity(dto);
        World createdWorld = service.create(entity);
        WorldDto responseBody = mapper.toDto(createdWorld);
        return ResponseEntity
                .created(URI.create("/api/worlds/" + responseBody.id()))
                .body(responseBody);
    }

    @PatchMapping("/{id}/status")
    public WorldDto updateWorldStatus(
            @PathVariable Integer id,
            @Valid @RequestBody WorldStatusUpdateDto dto
    ) {
        World world = service.updateStatus(id, dto.status());
        return mapper.toDto(world);
    }

    @GetMapping
    public Iterable<WorldDto> findAllWorlds() {
        return service.getAll().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public WorldDto findWorldById(@PathVariable Integer id) {
        return mapper.toDto(service.findById(id));
    }
}
