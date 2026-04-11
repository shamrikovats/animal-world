package com.example.animalworld.simulation.domain.world;

import com.example.animalworld.model.WorldStatus;
import com.example.animalworld.simulation.domain.config.RuntimeFeedingRule;
import com.example.animalworld.simulation.domain.config.RuntimeFeedingRuleKey;
import com.example.animalworld.simulation.domain.config.RuntimeLocationRule;
import com.example.animalworld.simulation.domain.config.RuntimePlantConfig;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;
import com.example.animalworld.simulation.domain.config.RuntimeWorldSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Runtime-мир целиком.
 * Здесь лежит матрица клеток, настройки, текущий такт и ссылки на конфиги, которые нужны движку симуляции.
 *
 * @author Shamrikova Tatiana
 */
public class SimulationWorld {
    private final Integer worldId;
    private final String worldName;
    private final RuntimeWorldSettings settings;
    private final SimulationCell[][] cells;
    private final Map<Integer, RuntimeSpeciesConfig> speciesConfigsById;
    private final Map<Integer, RuntimePlantConfig> plantConfigsById;
    private final Map<RuntimeFeedingRuleKey, RuntimeFeedingRule> feedingRulesByKey;
    private final Map<Integer, Map<LocationType, RuntimeLocationRule>> locationRulesBySpeciesId;
    private final AtomicLong currentTick = new AtomicLong();

    private WorldStatus status;

    public SimulationWorld(
            Integer worldId,
            String worldName,
            RuntimeWorldSettings settings,
            SimulationCell[][] cells,
            long currentTick,
            WorldStatus status,
            Map<Integer, RuntimeSpeciesConfig> speciesConfigsById,
            Map<Integer, RuntimePlantConfig> plantConfigsById,
            Map<RuntimeFeedingRuleKey, RuntimeFeedingRule> feedingRulesByKey,
            Map<Integer, Map<LocationType, RuntimeLocationRule>> locationRulesBySpeciesId
    ) {
        this.worldId = worldId;
        this.worldName = worldName;
        this.settings = settings;
        this.cells = cells;
        this.status = status;
        this.currentTick.set(currentTick);
        this.speciesConfigsById = Map.copyOf(speciesConfigsById);
        this.plantConfigsById = Map.copyOf(plantConfigsById);
        this.feedingRulesByKey = Map.copyOf(feedingRulesByKey);
        this.locationRulesBySpeciesId = Map.copyOf(locationRulesBySpeciesId);
    }

    public Integer worldId() {
        return worldId;
    }

    public String worldName() {
        return worldName;
    }

    public RuntimeWorldSettings settings() {
        return settings;
    }

    public int height() {
        return cells.length;
    }

    public int width() {
        return cells.length == 0 ? 0 : cells[0].length;
    }

    public SimulationCell cellAt(int x, int y) {
        return cells[y][x];
    }

    public List<SimulationCell> allCells() {
        List<SimulationCell> result = new ArrayList<>(height() * width());
        for (SimulationCell[] row : cells) {
            for (SimulationCell cell : row) {
                result.add(cell);
            }
        }
        return result;
    }

    public Map<Integer, RuntimeSpeciesConfig> speciesConfigsById() {
        return speciesConfigsById;
    }

    public Map<Integer, RuntimePlantConfig> plantConfigsById() {
        return plantConfigsById;
    }

    public Map<RuntimeFeedingRuleKey, RuntimeFeedingRule> feedingRulesByKey() {
        return feedingRulesByKey;
    }

    public boolean canEnter(Integer speciesId, LocationType locationType) {
        RuntimeLocationRule rule = locationRulesBySpeciesId
                .getOrDefault(speciesId, Map.of())
                .get(locationType);
        return rule == null || !rule.forbidden();
    }

    public int survivalModifier(Integer speciesId, LocationType locationType) {
        RuntimeLocationRule rule = locationRulesBySpeciesId
                .getOrDefault(speciesId, Map.of())
                .get(locationType);
        return rule == null ? 0 : rule.survivalModifier();
    }

    public long currentTick() {
        return currentTick.get();
    }

    public long advanceTick() {
        return currentTick.incrementAndGet();
    }

    public WorldStatus status() {
        return status;
    }

    public void setStatus(WorldStatus status) {
        this.status = status;
    }
}
