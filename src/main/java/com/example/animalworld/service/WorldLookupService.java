package com.example.animalworld.service;

import com.example.animalworld.exception.WorldNotFoundByIdException;
import com.example.animalworld.model.entity.World;
import com.example.animalworld.model.entity.WorldTickStat;
import com.example.animalworld.repository.WorldRepository;
import com.example.animalworld.repository.WorldTickStatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Shamrikova Tatiana
 */
@Service
public class WorldLookupService {
    private final WorldRepository worldRepository;
    private final WorldTickStatRepository worldTickStatRepository;

    WorldLookupService(WorldRepository worldRepository, WorldTickStatRepository worldTickStatRepository) {
        this.worldRepository = worldRepository;
        this.worldTickStatRepository = worldTickStatRepository;
    }

    public World findById(Integer id) {
        return worldRepository.findById(id)
                .orElseThrow(() -> new WorldNotFoundByIdException(id));
    }

    public List<World> getAll() {
        return worldRepository.getAll();
    }

    public List<WorldTickStat> getStats(Integer worldId) {
        findById(worldId);
        return worldTickStatRepository.findAllByWorldId(worldId);
    }
}
