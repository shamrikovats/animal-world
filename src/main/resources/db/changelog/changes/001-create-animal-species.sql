--liquibase formatted sql

--changeset shamrikova:001-create-species
CREATE TABLE IF NOT EXISTS species
(
    id           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         VARCHAR(50) NOT NULL UNIQUE,
    is_predator  BOOLEAN     NOT NULL DEFAULT FALSE,
    is_herbivore BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS world
(
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'))
);

CREATE TABLE IF NOT EXISTS location_types
(
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS plant_species
(
    id              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    biological_name VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS world_settings
(
    world_id                INTEGER PRIMARY KEY,
    tick_duration           INTEGER NOT NULL DEFAULT 1000,
    height                  INTEGER NOT NULL DEFAULT 10,
    width                   INTEGER NOT NULL DEFAULT 10,
    start_hungry_percent    INTEGER NOT NULL DEFAULT 20,
    start_predator_counter  INTEGER NOT NULL DEFAULT 10,
    start_herbivore_counter INTEGER NOT NULL DEFAULT 20,
    start_plants_mass       INTEGER   NOT NULL DEFAULT 100,
    FOREIGN KEY (world_id) REFERENCES world (id) ON DELETE CASCADE,
    CHECK (tick_duration > 0),
    CHECK (height > 0),
    CHECK (width > 0),
    CHECK (start_hungry_percent >= 0 AND start_hungry_percent <= 100),
    CHECK (start_predator_counter >= 0),
    CHECK (start_herbivore_counter >= 0),
    CHECK (start_plants_mass >= 0)
);

CREATE TABLE IF NOT EXISTS species_configuration
(
    world_id           INTEGER NOT NULL,
    species_id         INTEGER NOT NULL,
    max_coexist_count  INTEGER NOT NULL DEFAULT 10,
    weight             FLOAT   NOT NULL DEFAULT 20,
    speed_cells        INTEGER NOT NULL DEFAULT 3,
    full_tank_weight   INTEGER NOT NULL DEFAULT 100,
    min_food_percent   INTEGER NOT NULL DEFAULT 20,
    max_children_count INTEGER NOT NULL DEFAULT 1,
    pregnancy_period   INTEGER NOT NULL DEFAULT 1,
    lost_food_for_tick INTEGER NOT NULL DEFAULT 40,
    PRIMARY KEY (world_id, species_id),
    FOREIGN KEY (world_id) REFERENCES world (id) ON DELETE CASCADE,
    FOREIGN KEY (species_id) REFERENCES species (id) ON DELETE CASCADE,
    CHECK (max_coexist_count >= 0),
    CHECK (weight > 0),
    CHECK (speed_cells >= 0),
    CHECK (full_tank_weight >= 0),
    CHECK (min_food_percent >= 0 AND min_food_percent <= 100),
    CHECK (max_children_count > 0),
    CHECK (pregnancy_period > 0),
    CHECK (lost_food_for_tick >= 0)
);

CREATE TABLE IF NOT EXISTS world_plant_species
(
    world_id              INTEGER NOT NULL,
    plant_species_id      INTEGER NOT NULL,
    max_repair_speed      INTEGER NOT NULL DEFAULT 1,
    weight                INTEGER   NOT NULL DEFAULT 1,
    PRIMARY KEY (world_id, plant_species_id),
    FOREIGN KEY (world_id) REFERENCES world (id) ON DELETE CASCADE,
    FOREIGN KEY (plant_species_id) REFERENCES plant_species (id) ON DELETE CASCADE,
    CHECK (max_repair_speed >= 0),
    CHECK (weight > 0)
);

CREATE TABLE IF NOT EXISTS base_location_rules
(
    location_id       INTEGER NOT NULL,
    species_id         INTEGER NOT NULL,
    survival_modifier FLOAT   NOT NULL DEFAULT 1,
    PRIMARY KEY (location_id, species_id),
    FOREIGN KEY (location_id) REFERENCES location_types (id) ON DELETE CASCADE,
    FOREIGN KEY (species_id) REFERENCES species (id) ON DELETE CASCADE,
    CHECK (survival_modifier IN (-1, 0, 1))
);

CREATE TABLE IF NOT EXISTS feeding_species_rules
(
    world_id        INTEGER NOT NULL,
    species_id      INTEGER NOT NULL,
    prey_species_id INTEGER NOT NULL,
    probability     INTEGER   NOT NULL DEFAULT 0,
    PRIMARY KEY (world_id, species_id, prey_species_id),
    FOREIGN KEY (world_id) REFERENCES world (id) ON DELETE CASCADE,
    FOREIGN KEY (species_id) REFERENCES species (id) ON DELETE CASCADE,
    FOREIGN KEY (prey_species_id) REFERENCES species (id) ON DELETE CASCADE,
    CHECK (species_id <> prey_species_id),
    CHECK (probability >= 0 AND probability <= 100)
);

CREATE TABLE IF NOT EXISTS world_tick_stats
(
    world_id              INTEGER   NOT NULL,
    tick_number           INTEGER   NOT NULL,
    alive_predator_count  INTEGER   NOT NULL DEFAULT 0,
    alive_herbivore_count INTEGER   NOT NULL DEFAULT 0,
    total_plant_mass      FLOAT     NOT NULL DEFAULT 0,
    birth_count           INTEGER   NOT NULL DEFAULT 0,
    death_count           INTEGER   NOT NULL DEFAULT 0,
    stats_created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (world_id, tick_number),
    FOREIGN KEY (world_id) REFERENCES world (id) ON DELETE CASCADE,
    CHECK (tick_number >= 0),
    CHECK (alive_predator_count >= 0),
    CHECK (alive_herbivore_count >= 0),
    CHECK (total_plant_mass >= 0),
    CHECK (birth_count >= 0),
    CHECK (death_count >= 0)
);

INSERT INTO location_types (name, description)
VALUES ('FOREST', 'Forest location'),
       ('FIELD', 'Field location'),
       ('MOUNTAIN', 'Mountain location'),
       ('RIVER', 'River location'),
       ('SWAMP', 'Swamp location')
ON CONFLICT (name) DO NOTHING;
