package com.example.animalworld.repository;

import com.example.animalworld.model.entity.PlantSpecies;
import com.example.animalworld.model.entity.Species;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Shamrikova Tatiana
 */
@Repository
public class ReferenceDataRepository {
    private final JdbcClient client;
    private final RowMapper<Species> speciesRowMapper = this::mapSpecies;
    private final RowMapper<PlantSpecies> plantSpeciesRowMapper = this::mapPlantSpecies;

    ReferenceDataRepository(JdbcClient client) {
        this.client = client;
    }

    public Map<Integer, Species> getSpeciesById() {
        List<Species> species = client.sql("""
                        SELECT id, name, is_predator, is_herbivore
                        FROM species
                        ORDER BY id
                        """)
                .query(speciesRowMapper)
                .list();

        return species.stream()
                .collect(Collectors.toMap(Species::id, Function.identity()));
    }

    public Map<Integer, PlantSpecies> getPlantSpeciesById() {
        List<PlantSpecies> plants = client.sql("""
                        SELECT id, biological_name
                        FROM plant_species
                        ORDER BY id
                        """)
                .query(plantSpeciesRowMapper)
                .list();

        return plants.stream()
                .collect(Collectors.toMap(PlantSpecies::id, Function.identity()));
    }

    private Species mapSpecies(ResultSet resultSet, int rowNum) throws SQLException {
        return new Species(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getBoolean("is_predator"),
                resultSet.getBoolean("is_herbivore")
        );
    }

    private PlantSpecies mapPlantSpecies(ResultSet resultSet, int rowNum) throws SQLException {
        return new PlantSpecies(
                resultSet.getInt("id"),
                resultSet.getString("biological_name")
        );
    }
}
