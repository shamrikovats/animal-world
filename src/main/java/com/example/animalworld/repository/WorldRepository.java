package com.example.animalworld.repository;

import com.example.animalworld.model.WorldStatus;
import com.example.animalworld.model.entity.World;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Shamrikova Tatiana
 */
@Repository
public class WorldRepository {
    private final JdbcClient client;

    WorldRepository(JdbcClient client) {
        this.client = client;
    }

    public World create(World entity) {
        return client.sql("""
                        INSERT INTO world (name, description)
                        VALUES (:name, :description)
                        RETURNING *
                        """
                )
                .param("name",entity.name())
                .param("description", entity.description())
                .query(World.class)
                .single();
    }

    public List<World> getAll() {
        return client.sql("""
                        SELECT * FROM world
                        """)
                .query(World.class)
                .list();
    }

    public Optional<World> findById(Integer id) {
        return client.sql("""
                        SELECT * FROM world WHERE id = :id
                        """)
                .param("id", id)
                .query(World.class)
                .optional();
    }

    public Optional<World> updateStatus(Integer id, WorldStatus status) {
        return client.sql("""
                        UPDATE world
                        SET status = :status WHERE id = :id
                        RETURNING *
                        """)
                .param("id", id)
                .param("status", status.name())
                .query(World.class)
                .optional();
    }



}
