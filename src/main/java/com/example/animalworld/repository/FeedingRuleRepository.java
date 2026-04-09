package com.example.animalworld.repository;

import com.example.animalworld.model.FoodType;
import com.example.animalworld.model.entity.FeedingRule;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Shamrikova Tatiana
 */
@Repository
public class FeedingRuleRepository {
    private final JdbcClient client;
    private final RowMapper<FeedingRule> rowMapper = this::mapRule;

    FeedingRuleRepository(JdbcClient client) {
        this.client = client;
    }

    public List<FeedingRule> findAllDefaults() {
        return client.sql("""
                        SELECT id,
                               CAST(NULL AS INTEGER) AS world_id,
                               species_id,
                               prey_species_id,
                               prey_plant_species_id,
                               probability
                        FROM default_feeding_species_rules
                        ORDER BY species_id, prey_species_id NULLS LAST, prey_plant_species_id NULLS LAST
                        """)
                .query(rowMapper)
                .list();
    }

    public List<FeedingRule> findAllByWorldId(Integer worldId) {
        return client.sql("""
                        SELECT id,
                               world_id,
                               species_id,
                               prey_species_id,
                               prey_plant_species_id,
                               probability
                        FROM feeding_species_rules
                        WHERE world_id = :worldId
                        ORDER BY species_id, prey_species_id NULLS LAST, prey_plant_species_id NULLS LAST
                        """)
                .param("worldId", worldId)
                .query(rowMapper)
                .list();
    }

    public void replaceAllForWorld(Integer worldId, List<FeedingRule> rules) {
        client.sql("DELETE FROM feeding_species_rules WHERE world_id = :worldId")
                .param("worldId", worldId)
                .update();
        rules.forEach(this::insertWorldRule);
    }

    public void upsertWorldRule(FeedingRule rule) {
        deleteWorldRule(rule.worldId(), rule.speciesId(), rule.preySpeciesId(), rule.preyPlantSpeciesId());
        insertWorldRule(rule);
    }

    private void insertWorldRule(FeedingRule rule) {
        client.sql("""
                        INSERT INTO feeding_species_rules (
                            world_id,
                            species_id,
                            prey_species_id,
                            prey_plant_species_id,
                            probability
                        )
                        VALUES (
                            :worldId,
                            :speciesId,
                            :preySpeciesId,
                            :preyPlantSpeciesId,
                            :probability
                        )
                        """)
                .param("worldId", rule.worldId())
                .param("speciesId", rule.speciesId())
                .param("preySpeciesId", rule.preySpeciesId())
                .param("preyPlantSpeciesId", rule.preyPlantSpeciesId())
                .param("probability", rule.probability())
                .update();
    }

    private void deleteWorldRule(Integer worldId, Integer speciesId, Integer preySpeciesId, Integer preyPlantSpeciesId) {
        if (preySpeciesId != null) {
            client.sql("""
                            DELETE FROM feeding_species_rules
                            WHERE world_id = :worldId
                              AND species_id = :speciesId
                              AND prey_species_id = :preySpeciesId
                            """)
                    .param("worldId", worldId)
                    .param("speciesId", speciesId)
                    .param("preySpeciesId", preySpeciesId)
                    .update();
            return;
        }

        client.sql("""
                        DELETE FROM feeding_species_rules
                        WHERE world_id = :worldId
                          AND species_id = :speciesId
                          AND prey_plant_species_id = :preyPlantSpeciesId
                        """)
                .param("worldId", worldId)
                .param("speciesId", speciesId)
                .param("preyPlantSpeciesId", preyPlantSpeciesId)
                .update();
    }

    private FeedingRule mapRule(ResultSet resultSet, int rowNum) throws SQLException {
        Integer preySpeciesId = resultSet.getObject("prey_species_id", Integer.class);
        Integer preyPlantSpeciesId = resultSet.getObject("prey_plant_species_id", Integer.class);
        FoodType foodType = preySpeciesId != null ? FoodType.SPECIES : FoodType.PLANT;

        return new FeedingRule(
                resultSet.getLong("id"),
                resultSet.getObject("world_id", Integer.class),
                resultSet.getInt("species_id"),
                preySpeciesId,
                preyPlantSpeciesId,
                foodType,
                resultSet.getInt("probability")
        );
    }
}
