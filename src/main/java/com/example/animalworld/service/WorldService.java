package com.example.animalworld.service;

import com.example.animalworld.exception.WorldNotFoundByIdException;
import com.example.animalworld.model.WorldStatus;
import com.example.animalworld.model.entity.World;
import com.example.animalworld.repository.WorldRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Shamrikova Tatiana
 */
@Service
public class WorldService {
    private static final Logger log = LoggerFactory.getLogger(WorldService.class);
    private final WorldRepository repository;

    WorldService(WorldRepository repository) {
        this.repository = repository;
    }

    public World create(World entity) {
        log.info("Try to create world: {}", entity);
        return repository.create(entity);
    }

    public World updateStatus(Integer id, WorldStatus status) {
        log.info("Try to update status of world with id: {} to {}", id, status);
        return repository.updateStatus(id, status)
                .orElseThrow(() -> new WorldNotFoundByIdException(id));
    }

    public World findById(Integer id) {
        log.info("Try to find world with id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new WorldNotFoundByIdException(id));
    }

    public List<World> getAll() {
        log.info("Try to get all worlds");
        return repository.getAll();
    }

}
