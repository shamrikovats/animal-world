package com.example.animalworld.repository;

import com.example.animalworld.model.entity.PlantSpeciesConfiguration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * @author Shamrikova Tatiana
 */
@Repository
public class PlantSpeciesConfigurationRepository {
    private final JdbcClient client;
    private final RowMapper<PlantSpeciesConfiguration> rowMapper = this::mapConfiguration;

    PlantSpeciesConfigurationRepository(JdbcClient client) {
        this.client = client;
    }

    public List<PlantSpeciesConfiguration> findAllDefaults() {
        return client.sql("""
                        SELECT CAST(NULL AS INTEGER) AS world_id,
                               plant_species_id,
                               max_repair_speed,
                               weight,
                               start_count
                        FROM default_plant_species_configuration
                        ORDER BY plant_species_id
                        """)
                .query(rowMapper)
                .list();
    }

    public List<PlantSpeciesConfiguration> findAllByWorldId(Integer worldId) {
        return client.sql("""
                        SELECT world_id,
                               plant_species_id,
                               max_repair_speed,
                               weight,
                               start_count
                        FROM world_plant_species
                        WHERE world_id = :worldId
                        ORDER BY plant_species_id
                        """)
                .param("worldId", worldId)
                .query(rowMapper)
                .list();
    }

    public Optional<PlantSpeciesConfiguration> findByWorldIdAndPlantSpeciesId(Integer worldId, Integer plantSpeciesId) {
        return client.sql("""
                        SELECT world_id,
                               plant_species_id,
                               max_repair_speed,
                               weight,
                               start_count
                        FROM world_plant_species
                        WHERE world_id = :worldId
                          AND plant_species_id = :plantSpeciesId
                        """)
                .param("worldId", worldId)
                .param("plantSpeciesId", plantSpeciesId)
                .query(rowMapper)
                .optional();
    }

    public void replaceAllForWorld(Integer worldId, List<PlantSpeciesConfiguration> configurations) {
        client.sql("DELETE FROM world_plant_species WHERE world_id = :worldId")
                .param("worldId", worldId)
                .update();
        configurations.forEach(this::upsert);
    }

    public void upsert(PlantSpeciesConfiguration configuration) {
        client.sql("""
                        INSERT INTO world_plant_species (
                            world_id,
                            plant_species_id,
                            max_repair_speed,
                            weight,
                            start_count
                        )
                        VALUES (
                            :worldId,
                            :plantSpeciesId,
                            :maxRepairSpeed,
                            :weight,
                            :startCount
                        )
                        ON CONFLICT (world_id, plant_species_id) DO UPDATE
                        SET max_repair_speed = EXCLUDED.max_repair_speed,
                            weight = EXCLUDED.weight,
                            start_count = EXCLUDED.start_count
                        """)
                .param("worldId", configuration.worldId())
                .param("plantSpeciesId", configuration.plantSpeciesId())
                .param("maxRepairSpeed", configuration.maxRepairSpeed())
                .param("weight", configuration.weight())
                .param("startCount", configuration.startCount())
                .update();
    }

    private PlantSpeciesConfiguration mapConfiguration(ResultSet resultSet, int rowNum) throws SQLException {
        return new PlantSpeciesConfiguration(
                resultSet.getObject("world_id", Integer.class),
                resultSet.getInt("plant_species_id"),
                resultSet.getInt("max_repair_speed"),
                resultSet.getInt("weight"),
                resultSet.getInt("start_count")
        );
    }
}
