package com.example.animalworld.repository;

import com.example.animalworld.model.entity.SpeciesConfiguration;
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
public class SpeciesConfigurationRepository {
    private final JdbcClient client;
    private final RowMapper<SpeciesConfiguration> rowMapper = this::mapConfiguration;

    SpeciesConfigurationRepository(JdbcClient client) {
        this.client = client;
    }

    public List<SpeciesConfiguration> findAllDefaults() {
        return client.sql("""
                        SELECT CAST(NULL AS INTEGER) AS world_id,
                               species_id,
                               max_coexist_count,
                               weight,
                               speed_cells,
                               full_tank_weight,
                               min_food_percent,
                               max_children_count,
                               pregnancy_period,
                               lost_food_for_tick,
                               start_count
                        FROM default_species_configuration
                        ORDER BY species_id
                        """)
                .query(rowMapper)
                .list();
    }

    public List<SpeciesConfiguration> findAllByWorldId(Integer worldId) {
        return client.sql("""
                        SELECT world_id,
                               species_id,
                               max_coexist_count,
                               weight,
                               speed_cells,
                               full_tank_weight,
                               min_food_percent,
                               max_children_count,
                               pregnancy_period,
                               lost_food_for_tick,
                               start_count
                        FROM species_configuration
                        WHERE world_id = :worldId
                        ORDER BY species_id
                        """)
                .param("worldId", worldId)
                .query(rowMapper)
                .list();
    }

    public Optional<SpeciesConfiguration> findByWorldIdAndSpeciesId(Integer worldId, Integer speciesId) {
        return client.sql("""
                        SELECT world_id,
                               species_id,
                               max_coexist_count,
                               weight,
                               speed_cells,
                               full_tank_weight,
                               min_food_percent,
                               max_children_count,
                               pregnancy_period,
                               lost_food_for_tick,
                               start_count
                        FROM species_configuration
                        WHERE world_id = :worldId
                          AND species_id = :speciesId
                        """)
                .param("worldId", worldId)
                .param("speciesId", speciesId)
                .query(rowMapper)
                .optional();
    }

    public void replaceAllForWorld(Integer worldId, List<SpeciesConfiguration> configurations) {
        client.sql("DELETE FROM species_configuration WHERE world_id = :worldId")
                .param("worldId", worldId)
                .update();
        configurations.forEach(this::upsert);
    }

    public void upsert(SpeciesConfiguration configuration) {
        client.sql("""
                        INSERT INTO species_configuration (
                            world_id,
                            species_id,
                            max_coexist_count,
                            weight,
                            speed_cells,
                            full_tank_weight,
                            min_food_percent,
                            max_children_count,
                            pregnancy_period,
                            lost_food_for_tick,
                            start_count
                        )
                        VALUES (
                            :worldId,
                            :speciesId,
                            :maxCoexistCount,
                            :weight,
                            :speedCells,
                            :fullTankWeight,
                            :minFoodPercent,
                            :maxChildrenCount,
                            :pregnancyPeriod,
                            :lostFoodForTick,
                            :startCount
                        )
                        ON CONFLICT (world_id, species_id) DO UPDATE
                        SET max_coexist_count = EXCLUDED.max_coexist_count,
                            weight = EXCLUDED.weight,
                            speed_cells = EXCLUDED.speed_cells,
                            full_tank_weight = EXCLUDED.full_tank_weight,
                            min_food_percent = EXCLUDED.min_food_percent,
                            max_children_count = EXCLUDED.max_children_count,
                            pregnancy_period = EXCLUDED.pregnancy_period,
                            lost_food_for_tick = EXCLUDED.lost_food_for_tick,
                            start_count = EXCLUDED.start_count
                        """)
                .param("worldId", configuration.worldId())
                .param("speciesId", configuration.speciesId())
                .param("maxCoexistCount", configuration.maxCoexistCount())
                .param("weight", configuration.weight())
                .param("speedCells", configuration.speedCells())
                .param("fullTankWeight", configuration.fullTankWeight())
                .param("minFoodPercent", configuration.minFoodPercent())
                .param("maxChildrenCount", configuration.maxChildrenCount())
                .param("pregnancyPeriod", configuration.pregnancyPeriod())
                .param("lostFoodForTick", configuration.lostFoodForTick())
                .param("startCount", configuration.startCount())
                .update();
    }

    private SpeciesConfiguration mapConfiguration(ResultSet resultSet, int rowNum) throws SQLException {
        return new SpeciesConfiguration(
                resultSet.getObject("world_id", Integer.class),
                resultSet.getInt("species_id"),
                resultSet.getInt("max_coexist_count"),
                resultSet.getDouble("weight"),
                resultSet.getInt("speed_cells"),
                resultSet.getDouble("full_tank_weight"),
                resultSet.getInt("min_food_percent"),
                resultSet.getInt("max_children_count"),
                resultSet.getInt("pregnancy_period"),
                resultSet.getInt("lost_food_for_tick"),
                resultSet.getInt("start_count")
        );
    }
}
