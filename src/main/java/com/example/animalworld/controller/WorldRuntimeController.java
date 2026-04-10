package com.example.animalworld.controller;

import com.example.animalworld.model.dto.WorldRuntimeSummaryDto;
import com.example.animalworld.runtime.simulation.domain.world.SimulationWorld;
import com.example.animalworld.runtime.simulation.service.SimulationWorldBootstrapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для ручной проверки runtime-слоя через REST.
 * Он не запускает симуляцию, а только собирает мир в памяти и возвращает короткую сводку.
 *
 * @author Shamrikova Tatiana
 */
@RestController
@RequestMapping("/api/worlds/{worldId}/runtime")
public class WorldRuntimeController {
    private final SimulationWorldBootstrapService bootstrapService;

    WorldRuntimeController(SimulationWorldBootstrapService bootstrapService) {
        this.bootstrapService = bootstrapService;
    }

    @GetMapping("/summary")
    public WorldRuntimeSummaryDto getRuntimeSummary(@PathVariable Integer worldId) {
        SimulationWorld world = bootstrapService.bootstrap(worldId);
        int totalAnimals = world.allCells().stream()
                .mapToInt(cell -> cell.animalsSnapshot().size())
                .sum();
        int totalPlants = world.allCells().stream()
                .mapToInt(cell -> cell.plantsSnapshot().size())
                .sum();

        return new WorldRuntimeSummaryDto(
                world.worldId(),
                world.worldName(),
                world.status(),
                world.currentTick(),
                world.height(),
                world.width(),
                totalAnimals,
                totalPlants
        );
    }
}
