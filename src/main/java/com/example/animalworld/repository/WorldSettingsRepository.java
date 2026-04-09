package com.example.animalworld.repository;

import com.example.animalworld.model.entity.WorldSettings;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * @author Shamrikova Tatiana
 */
@Repository
public class WorldSettingsRepository {
    private final JdbcClient client;
    private final RowMapper<WorldSettings> settingsRowMapper = this::mapSettings;

    WorldSettingsRepository(JdbcClient client) {
        this.client = client;
    }

    public WorldSettings findDefault() {
        return client.sql("""
                        SELECT CAST(NULL AS INTEGER) AS world_id,
                               tick_duration,
                               height,
                               width,
                               start_hungry_percent,
                               start_predator_counter,
                               start_herbivore_counter,
                               start_plants_mass
                        FROM default_world_settings
                        WHERE id = 1
                        """)
                .query(settingsRowMapper)
                .single();
    }

    public Optional<WorldSettings> findByWorldId(Integer worldId) {
        return client.sql("""
                        SELECT world_id,
                               tick_duration,
                               height,
                               width,
                               start_hungry_percent,
                               start_predator_counter,
                               start_herbivore_counter,
                               start_plants_mass
                        FROM world_settings
                        WHERE world_id = :worldId
                        """)
                .param("worldId", worldId)
                .query(settingsRowMapper)
                .optional();
    }

    public void upsert(WorldSettings settings) {
        client.sql("""
                        INSERT INTO world_settings (
                            world_id,
                            tick_duration,
                            height,
                            width,
                            start_hungry_percent,
                            start_predator_counter,
                            start_herbivore_counter,
                            start_plants_mass
                        )
                        VALUES (
                            :worldId,
                            :tickDuration,
                            :height,
                            :width,
                            :startHungryPercent,
                            :startPredatorCounter,
                            :startHerbivoreCounter,
                            :startPlantsMass
                        )
                        ON CONFLICT (world_id) DO UPDATE
                        SET tick_duration = EXCLUDED.tick_duration,
                            height = EXCLUDED.height,
                            width = EXCLUDED.width,
                            start_hungry_percent = EXCLUDED.start_hungry_percent,
                            start_predator_counter = EXCLUDED.start_predator_counter,
                            start_herbivore_counter = EXCLUDED.start_herbivore_counter,
                            start_plants_mass = EXCLUDED.start_plants_mass
                        """)
                .param("worldId", settings.worldId())
                .param("tickDuration", settings.tickDuration())
                .param("height", settings.height())
                .param("width", settings.width())
                .param("startHungryPercent", settings.startHungryPercent())
                .param("startPredatorCounter", settings.startPredatorCounter())
                .param("startHerbivoreCounter", settings.startHerbivoreCounter())
                .param("startPlantsMass", settings.startPlantsMass())
                .update();
    }

    private WorldSettings mapSettings(ResultSet resultSet, int rowNum) throws SQLException {
        Integer worldId = resultSet.getObject("world_id", Integer.class);
        return new WorldSettings(
                worldId,
                resultSet.getInt("tick_duration"),
                resultSet.getInt("height"),
                resultSet.getInt("width"),
                resultSet.getInt("start_hungry_percent"),
                resultSet.getInt("start_predator_counter"),
                resultSet.getInt("start_herbivore_counter"),
                resultSet.getInt("start_plants_mass")
        );
    }
}
