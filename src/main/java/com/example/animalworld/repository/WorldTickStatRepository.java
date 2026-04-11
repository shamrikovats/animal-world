package com.example.animalworld.repository;

import com.example.animalworld.model.entity.WorldTickStat;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author Shamrikova Tatiana
 */
@Repository
public class WorldTickStatRepository {
    private final JdbcClient client;
    private final RowMapper<WorldTickStat> rowMapper = this::mapStat;

    WorldTickStatRepository(JdbcClient client) {
        this.client = client;
    }

    public List<WorldTickStat> findAllByWorldId(Integer worldId) {
        return client.sql("""
                        SELECT world_id,
                               tick_number,
                               alive_predator_count,
                               alive_herbivore_count,
                               total_plant_mass,
                               birth_count,
                               death_count,
                               stats_created_at
                        FROM world_tick_stats
                        WHERE world_id = :worldId
                        ORDER BY tick_number
                        """)
                .param("worldId", worldId)
                .query(rowMapper)
                .list();
    }

    public void save(WorldTickStat stat) {
        client.sql("""
                        INSERT INTO world_tick_stats (
                            world_id,
                            tick_number,
                            alive_predator_count,
                            alive_herbivore_count,
                            total_plant_mass,
                            birth_count,
                            death_count
                        )
                        VALUES (
                            :worldId,
                            :tickNumber,
                            :alivePredatorCount,
                            :aliveHerbivoreCount,
                            :totalPlantMass,
                            :birthCount,
                            :deathCount
                        )
                        ON CONFLICT (world_id, tick_number) DO UPDATE
                        SET alive_predator_count = EXCLUDED.alive_predator_count,
                            alive_herbivore_count = EXCLUDED.alive_herbivore_count,
                            total_plant_mass = EXCLUDED.total_plant_mass,
                            birth_count = EXCLUDED.birth_count,
                            death_count = EXCLUDED.death_count
                        """)
                .param("worldId", stat.worldId())
                .param("tickNumber", stat.tickNumber())
                .param("alivePredatorCount", stat.alivePredatorCount())
                .param("aliveHerbivoreCount", stat.aliveHerbivoreCount())
                .param("totalPlantMass", stat.totalPlantMass())
                .param("birthCount", stat.birthCount())
                .param("deathCount", stat.deathCount())
                .update();
    }

    private WorldTickStat mapStat(ResultSet resultSet, int rowNum) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("stats_created_at");
        return new WorldTickStat(
                resultSet.getInt("world_id"),
                resultSet.getInt("tick_number"),
                resultSet.getInt("alive_predator_count"),
                resultSet.getInt("alive_herbivore_count"),
                resultSet.getDouble("total_plant_mass"),
                resultSet.getInt("birth_count"),
                resultSet.getInt("death_count"),
                createdAt == null ? null : createdAt.toInstant()
        );
    }
}
