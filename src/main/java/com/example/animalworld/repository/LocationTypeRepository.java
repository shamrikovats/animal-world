package com.example.animalworld.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для чтения типов локаций из БД.
 * Нужен bootstrap-слою, чтобы собрать runtime-клетки мира с понятным типом местности.
 *
 * @author Shamrikova Tatiana
 */
@Repository
public class LocationTypeRepository {
    private final JdbcClient client;

    LocationTypeRepository(JdbcClient client) {
        this.client = client;
    }

    public List<String> findAllNames() {
        return client.sql("""
                        SELECT name
                        FROM location_types
                        ORDER BY id
                        """)
                .query(String.class)
                .list();
    }
}
