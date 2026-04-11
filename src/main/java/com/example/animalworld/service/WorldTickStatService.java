package com.example.animalworld.service;

import com.example.animalworld.model.entity.WorldTickStat;
import com.example.animalworld.repository.WorldTickStatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис статистики по тактам мира.
 * Через него читаем и сохраняем world_tick_stats, чтобы контроллеры и движок не ходили прямо в репозиторий.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class WorldTickStatService {
    private final WorldTickStatRepository repository;

    WorldTickStatService(WorldTickStatRepository repository) {
        this.repository = repository;
    }

    public List<WorldTickStat> getAllByWorldId(Integer worldId) {
        return repository.findAllByWorldId(worldId);
    }

    public void save(WorldTickStat stat) {
        repository.save(stat);
    }
}
