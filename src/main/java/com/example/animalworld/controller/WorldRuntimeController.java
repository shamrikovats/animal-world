package com.example.animalworld.controller;

import com.example.animalworld.model.dto.WorldRuntimeSummaryDto;
import com.example.animalworld.simulation.service.SimulationWorldSummaryService;
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
    private final SimulationWorldSummaryService simulationWorldSummaryService;

    WorldRuntimeController(SimulationWorldSummaryService simulationWorldSummaryService) {
        this.simulationWorldSummaryService = simulationWorldSummaryService;
    }

    @GetMapping("/summary")
    public WorldRuntimeSummaryDto getRuntimeSummary(@PathVariable Integer worldId) {
        return simulationWorldSummaryService.build(worldId);
    }
}
