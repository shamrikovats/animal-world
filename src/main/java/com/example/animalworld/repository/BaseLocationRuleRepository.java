package com.example.animalworld.repository;

import com.example.animalworld.model.entity.BaseLocationRule;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Репозиторий правил по типам локаций.
 * Читает из БД ограничения и базовые предпочтения к местности.
 *
 * @author Shamrikova Tatiana
 */
@Repository
public class BaseLocationRuleRepository {
    private final JdbcClient client;
    private final RowMapper<BaseLocationRule> rowMapper = this::mapRule;

    BaseLocationRuleRepository(JdbcClient client) {
        this.client = client;
    }

    public List<BaseLocationRule> findAll() {
        return client.sql("""
                        SELECT blr.species_id,
                               lt.name AS location_name,
                               blr.survival_modifier
                        FROM base_location_rules blr
                        JOIN location_types lt ON lt.id = blr.location_id
                        ORDER BY blr.species_id, lt.name
                        """)
                .query(rowMapper)
                .list();
    }

    private BaseLocationRule mapRule(ResultSet resultSet, int rowNum) throws SQLException {
        return new BaseLocationRule(
                resultSet.getInt("species_id"),
                resultSet.getString("location_name"),
                resultSet.getInt("survival_modifier")
        );
    }
}
