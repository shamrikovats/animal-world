package com.example.animalworld.repository;

import com.example.animalworld.model.WorldStatus;
import com.example.animalworld.model.entity.World;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * @author Shamrikova Tatiana
 */
@Repository
public class WorldRepository {
    private final JdbcClient client;
    private final RowMapper<World> worldRowMapper = this::mapWorld;

    WorldRepository(JdbcClient client) {
        this.client = client;
    }

    public World create(World entity) {
        return client.sql("""
                        INSERT INTO world (name, description)
                        VALUES (:name, :description)
                        RETURNING id, name, description, created_at, status
                        """
                )
                .param("name", entity.name())
                .param("description", entity.description())
                .query(worldRowMapper)
                .single();
    }

    public List<World> getAll() {
        return client.sql("""
                        SELECT id, name, description, created_at, status
                        FROM world
                        ORDER BY id
                        """)
                .query(worldRowMapper)
                .list();
    }

    public Optional<World> findById(Integer id) {
        return client.sql("""
                        SELECT id, name, description, created_at, status
                        FROM world
                        WHERE id = :id
                        """)
                .param("id", id)
                .query(worldRowMapper)
                .optional();
    }

    public Optional<World> updateStatus(Integer id, WorldStatus status) {
        return client.sql("""
                        UPDATE world
                        SET status = :status
                        WHERE id = :id
                        RETURNING id, name, description, created_at, status
                        """)
                .param("id", id)
                .param("status", status.name())
                .query(worldRowMapper)
                .optional();
    }

    private World mapWorld(ResultSet resultSet, int rowNum) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new World(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                createdAt == null ? null : createdAt.toInstant(),
                WorldStatus.valueOf(resultSet.getString("status"))
        );
    }
}
