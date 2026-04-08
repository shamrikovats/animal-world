package com.example.animalworld.mapper;

import com.example.animalworld.model.dto.WorldCreateDto;
import com.example.animalworld.model.dto.WorldDto;
import com.example.animalworld.model.entity.World;
import org.springframework.stereotype.Component;

/**
 * @author Shamrikova Tatiana
 */
@Component
public class WorldMapper {

    public World toEntity(WorldCreateDto dto) {
        return new World(
                null,
                dto.name(),
                dto.description(),
                null,
                null
        );
    }

    public WorldDto toDto(World entity) {
        return new WorldDto(
                entity.id(),
                entity.name(),
                entity.description(),
                entity.status()
        );
    }

}
