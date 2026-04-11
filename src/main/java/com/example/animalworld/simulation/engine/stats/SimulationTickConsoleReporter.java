package com.example.animalworld.simulation.engine.stats;

import com.example.animalworld.simulation.domain.world.SimulationWorld;
import com.example.animalworld.simulation.engine.TickMetrics;
import com.example.animalworld.simulation.engine.WorldPopulationSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Короткий вывод статистики тика в консоль.
 * Нужен для ручного наблюдения за миром без прямого запроса в БД.
 *
 * @author Shamrikova Tatiana
 */
@Service
public class SimulationTickConsoleReporter {
    private static final Logger log = LoggerFactory.getLogger(SimulationTickConsoleReporter.class);
    private static final Map<String, String> SPECIES_ICONS = createSpeciesIcons();

    public void print(SimulationWorld world, long tickNumber, WorldPopulationSnapshot snapshot, TickMetrics metrics) {
        Map<String, Integer> countsBySpecies = countAnimalsBySpecies(world);
        StringBuilder builder = new StringBuilder();
        builder.append(System.lineSeparator())
                .append("World ").append(world.worldId())
                .append(" tick ").append(tickNumber).append(System.lineSeparator())
                .append("Predators=").append(snapshot.predatorCount())
                .append(" Herbivores=").append(snapshot.herbivoreCount())
                .append(" PlantsMass=").append(Math.round(snapshot.totalPlantMass()))
                .append(" Births=").append(metrics.births())
                .append(" Deaths=").append(metrics.deaths())
                .append(System.lineSeparator());

        appendPopulationBlock(builder, countsBySpecies);
        appendSpeciesDeltaBlock(builder, "Births", metrics.birthsBySpecies());
        appendSpeciesDeltaBlock(builder, "Deaths", metrics.deathsBySpecies());

        log.info(builder.toString());
    }

    private Map<String, Integer> countAnimalsBySpecies(SimulationWorld world) {
        Map<String, Integer> result = new LinkedHashMap<>();
        world.allCells().forEach(cell -> cell.animalsSnapshot().forEach(animal ->
                result.merge(animal.speciesName(), 1, Integer::sum)
        ));
        return result;
    }

    private static Map<String, String> createSpeciesIcons() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("Buffalo", "🐃");
        result.put("Bear", "🐻");
        result.put("Horse", "🐎");
        result.put("Deer", "🦌");
        result.put("Boar", "🐗");
        result.put("Sheep", "🐑");
        result.put("Goat", "🐐");
        result.put("Wolf", "🐺");
        result.put("Snake", "🐍");
        result.put("Fox", "🦊");
        result.put("Eagle", "🦅");
        result.put("Rabbit", "🐇");
        result.put("Duck", "🦆");
        result.put("Mouse", "🐁");
        result.put("Caterpillar", "🐛");
        return result;
    }

    private void appendPopulationBlock(StringBuilder builder, Map<String, Integer> countsBySpecies) {
        for (String speciesName : SPECIES_ICONS.keySet()) {
            appendSpeciesLine(builder, speciesName, countsBySpecies.getOrDefault(speciesName, 0), "x");
        }
    }

    private void appendSpeciesDeltaBlock(StringBuilder builder, String title, Map<String, Integer> valuesBySpecies) {
        boolean hasAnyPositiveValue = valuesBySpecies.values().stream().anyMatch(value -> value != null && value > 0);
        if (!hasAnyPositiveValue) {
            return;
        }

        builder.append(title).append(":").append(System.lineSeparator());
        for (Map.Entry<String, String> entry : SPECIES_ICONS.entrySet()) {
            int count = valuesBySpecies.getOrDefault(entry.getKey(), 0);
            if (count <= 0) {
                continue;
            }

            appendSpeciesLine(builder, entry.getKey(), count, "+");
        }
    }

    private void appendSpeciesLine(StringBuilder builder, String speciesName, int count, String countPrefix) {
        String icon = SPECIES_ICONS.getOrDefault(speciesName, "");
        builder.append(icon)
                .append(" ")
                .append(speciesName)
                .append(" ")
                .append(countPrefix)
                .append(count)
                .append(" ")
                .append(icon.repeat(Math.max(0, count)))
                .append(System.lineSeparator());
    }
}
