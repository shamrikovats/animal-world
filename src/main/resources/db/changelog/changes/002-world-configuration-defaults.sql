--liquibase formatted sql

--changeset shamrikova:002-world-configuration-defaults
ALTER TABLE species_configuration
    ADD COLUMN IF NOT EXISTS start_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE world_plant_species
    ADD COLUMN IF NOT EXISTS start_count INTEGER NOT NULL DEFAULT 100;

ALTER TABLE IF EXISTS feeding_species_rules RENAME TO feeding_species_rules_legacy;

CREATE TABLE IF NOT EXISTS feeding_species_rules
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    world_id             INTEGER NOT NULL,
    species_id           INTEGER NOT NULL,
    prey_species_id      INTEGER,
    prey_plant_species_id INTEGER,
    probability          INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (world_id) REFERENCES world (id) ON DELETE CASCADE,
    FOREIGN KEY (species_id) REFERENCES species (id) ON DELETE CASCADE,
    FOREIGN KEY (prey_species_id) REFERENCES species (id) ON DELETE CASCADE,
    FOREIGN KEY (prey_plant_species_id) REFERENCES plant_species (id) ON DELETE CASCADE,
    CHECK (probability >= 0 AND probability <= 100),
    CHECK (
        (prey_species_id IS NOT NULL AND prey_plant_species_id IS NULL)
        OR
        (prey_species_id IS NULL AND prey_plant_species_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_world_feeding_rule_species
    ON feeding_species_rules (world_id, species_id, prey_species_id)
    WHERE prey_species_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_world_feeding_rule_plant
    ON feeding_species_rules (world_id, species_id, prey_plant_species_id)
    WHERE prey_plant_species_id IS NOT NULL;

INSERT INTO feeding_species_rules (world_id, species_id, prey_species_id, probability)
SELECT world_id, species_id, prey_species_id, probability
FROM feeding_species_rules_legacy
WHERE prey_species_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS default_world_settings
(
    id                     INTEGER PRIMARY KEY,
    tick_duration          INTEGER NOT NULL DEFAULT 1000,
    height                 INTEGER NOT NULL DEFAULT 10,
    width                  INTEGER NOT NULL DEFAULT 10,
    start_hungry_percent   INTEGER NOT NULL DEFAULT 10,
    start_predator_counter INTEGER NOT NULL DEFAULT 5,
    start_herbivore_counter INTEGER NOT NULL DEFAULT 30,
    start_plants_mass      INTEGER NOT NULL DEFAULT 400,
    CHECK (id = 1),
    CHECK (tick_duration > 0),
    CHECK (height > 0),
    CHECK (width > 0),
    CHECK (start_hungry_percent >= 0 AND start_hungry_percent <= 100),
    CHECK (start_predator_counter >= 0),
    CHECK (start_herbivore_counter >= 0),
    CHECK (start_plants_mass >= 0)
);

CREATE TABLE IF NOT EXISTS default_species_configuration
(
    species_id          INTEGER PRIMARY KEY,
    max_coexist_count   INTEGER NOT NULL,
    weight              DOUBLE PRECISION NOT NULL,
    speed_cells         INTEGER NOT NULL,
    full_tank_weight    DOUBLE PRECISION NOT NULL,
    min_food_percent    INTEGER NOT NULL DEFAULT 20,
    max_children_count  INTEGER NOT NULL DEFAULT 1,
    pregnancy_period    INTEGER NOT NULL DEFAULT 1,
    lost_food_for_tick  INTEGER NOT NULL DEFAULT 15,
    start_count         INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (species_id) REFERENCES species (id) ON DELETE CASCADE,
    CHECK (max_coexist_count >= 0),
    CHECK (weight >= 0),
    CHECK (speed_cells >= 0),
    CHECK (full_tank_weight >= 0),
    CHECK (min_food_percent >= 0 AND min_food_percent <= 100),
    CHECK (max_children_count > 0),
    CHECK (pregnancy_period > 0),
    CHECK (lost_food_for_tick >= 0),
    CHECK (start_count >= 0)
);

CREATE TABLE IF NOT EXISTS default_plant_species_configuration
(
    plant_species_id   INTEGER PRIMARY KEY,
    max_repair_speed   INTEGER NOT NULL DEFAULT 3,
    weight             INTEGER NOT NULL DEFAULT 1,
    start_count        INTEGER NOT NULL DEFAULT 400,
    FOREIGN KEY (plant_species_id) REFERENCES plant_species (id) ON DELETE CASCADE,
    CHECK (max_repair_speed >= 0),
    CHECK (weight > 0),
    CHECK (start_count >= 0)
);

CREATE TABLE IF NOT EXISTS default_feeding_species_rules
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    species_id           INTEGER NOT NULL,
    prey_species_id      INTEGER,
    prey_plant_species_id INTEGER,
    probability          INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (species_id) REFERENCES species (id) ON DELETE CASCADE,
    FOREIGN KEY (prey_species_id) REFERENCES species (id) ON DELETE CASCADE,
    FOREIGN KEY (prey_plant_species_id) REFERENCES plant_species (id) ON DELETE CASCADE,
    CHECK (probability >= 0 AND probability <= 100),
    CHECK (
        (prey_species_id IS NOT NULL AND prey_plant_species_id IS NULL)
        OR
        (prey_species_id IS NULL AND prey_plant_species_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_default_feeding_rule_species
    ON default_feeding_species_rules (species_id, prey_species_id)
    WHERE prey_species_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_default_feeding_rule_plant
    ON default_feeding_species_rules (species_id, prey_plant_species_id)
    WHERE prey_plant_species_id IS NOT NULL;

INSERT INTO species (name, is_predator, is_herbivore)
VALUES ('Wolf', TRUE, FALSE),
       ('Snake', TRUE, FALSE),
       ('Fox', TRUE, FALSE),
       ('Bear', TRUE, FALSE),
       ('Eagle', TRUE, FALSE),
       ('Horse', FALSE, TRUE),
       ('Deer', FALSE, TRUE),
       ('Rabbit', FALSE, TRUE),
       ('Mouse', TRUE, TRUE),
       ('Goat', FALSE, TRUE),
       ('Sheep', FALSE, TRUE),
       ('Boar', TRUE, TRUE),
       ('Buffalo', FALSE, TRUE),
       ('Duck', TRUE, TRUE),
       ('Caterpillar', FALSE, TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO plant_species (biological_name, description)
VALUES ('Plant', 'Default island plant species')
ON CONFLICT (biological_name) DO NOTHING;

INSERT INTO default_world_settings (
    id,
    tick_duration,
    height,
    width,
    start_hungry_percent,
    start_predator_counter,
    start_herbivore_counter,
    start_plants_mass
)
VALUES (1, 1000, 10, 10, 10, 5, 30, 400)
ON CONFLICT (id) DO UPDATE
SET tick_duration = EXCLUDED.tick_duration,
    height = EXCLUDED.height,
    width = EXCLUDED.width,
    start_hungry_percent = EXCLUDED.start_hungry_percent,
    start_predator_counter = EXCLUDED.start_predator_counter,
    start_herbivore_counter = EXCLUDED.start_herbivore_counter,
    start_plants_mass = EXCLUDED.start_plants_mass;

INSERT INTO default_species_configuration (
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
SELECT s.id,
       cfg.max_coexist_count,
       cfg.weight,
       cfg.speed_cells,
       cfg.full_tank_weight,
       cfg.min_food_percent,
       cfg.max_children_count,
       cfg.pregnancy_period,
       cfg.lost_food_for_tick,
       cfg.start_count
FROM (
    VALUES
        ('Wolf', 30, 50.0, 3, 8.0, 20, 2, 3, 18, 3),
        ('Snake', 30, 15.0, 1, 3.0, 20, 2, 3, 18, 3),
        ('Fox', 30, 8.0, 2, 2.0, 20, 3, 2, 15, 4),
        ('Bear', 5, 500.0, 2, 80.0, 20, 1, 4, 12, 1),
        ('Eagle', 20, 6.0, 3, 1.0, 20, 2, 2, 18, 3),
        ('Horse', 20, 400.0, 4, 60.0, 20, 1, 4, 12, 4),
        ('Deer', 20, 300.0, 4, 50.0, 20, 2, 4, 12, 6),
        ('Rabbit', 150, 2.0, 2, 0.45, 20, 4, 2, 18, 24),
        ('Mouse', 500, 0.05, 1, 0.01, 20, 5, 2, 20, 30),
        ('Goat', 140, 60.0, 3, 10.0, 20, 2, 3, 15, 8),
        ('Sheep', 140, 70.0, 3, 15.0, 20, 2, 3, 15, 8),
        ('Boar', 50, 400.0, 2, 50.0, 20, 3, 3, 15, 6),
        ('Buffalo', 10, 700.0, 3, 100.0, 20, 1, 5, 11, 3),
        ('Duck', 200, 1.0, 4, 0.15, 20, 4, 2, 18, 18),
        ('Caterpillar', 1000, 0.01, 0, 0.0, 20, 6, 1, 5, 50)
) AS cfg(name, max_coexist_count, weight, speed_cells, full_tank_weight, min_food_percent, max_children_count, pregnancy_period, lost_food_for_tick, start_count)
JOIN species s ON s.name = cfg.name
ON CONFLICT (species_id) DO UPDATE
SET max_coexist_count = EXCLUDED.max_coexist_count,
    weight = EXCLUDED.weight,
    speed_cells = EXCLUDED.speed_cells,
    full_tank_weight = EXCLUDED.full_tank_weight,
    min_food_percent = EXCLUDED.min_food_percent,
    max_children_count = EXCLUDED.max_children_count,
    pregnancy_period = EXCLUDED.pregnancy_period,
    lost_food_for_tick = EXCLUDED.lost_food_for_tick,
    start_count = EXCLUDED.start_count;

INSERT INTO default_plant_species_configuration (
    plant_species_id,
    max_repair_speed,
    weight,
    start_count
)
SELECT id, 3, 1, 400
FROM plant_species
WHERE biological_name = 'Plant'
ON CONFLICT (plant_species_id) DO UPDATE
SET max_repair_speed = EXCLUDED.max_repair_speed,
    weight = EXCLUDED.weight,
    start_count = EXCLUDED.start_count;

INSERT INTO default_feeding_species_rules (species_id, prey_species_id, prey_plant_species_id, probability)
SELECT predator.id, prey.id, NULL, rules.probability
FROM (
    VALUES
        ('Wolf', 'Horse', 10),
        ('Wolf', 'Deer', 15),
        ('Wolf', 'Rabbit', 60),
        ('Wolf', 'Mouse', 80),
        ('Wolf', 'Goat', 60),
        ('Wolf', 'Sheep', 70),
        ('Wolf', 'Boar', 15),
        ('Wolf', 'Buffalo', 10),
        ('Wolf', 'Duck', 40),
        ('Snake', 'Fox', 15),
        ('Snake', 'Rabbit', 20),
        ('Snake', 'Mouse', 40),
        ('Snake', 'Duck', 10),
        ('Fox', 'Rabbit', 70),
        ('Fox', 'Mouse', 90),
        ('Fox', 'Duck', 60),
        ('Fox', 'Caterpillar', 40),
        ('Bear', 'Snake', 80),
        ('Bear', 'Horse', 40),
        ('Bear', 'Deer', 80),
        ('Bear', 'Rabbit', 80),
        ('Bear', 'Mouse', 90),
        ('Bear', 'Goat', 70),
        ('Bear', 'Sheep', 70),
        ('Bear', 'Boar', 50),
        ('Bear', 'Buffalo', 20),
        ('Bear', 'Duck', 10),
        ('Eagle', 'Fox', 10),
        ('Eagle', 'Rabbit', 90),
        ('Eagle', 'Mouse', 90),
        ('Eagle', 'Duck', 80),
        ('Mouse', 'Caterpillar', 90),
        ('Boar', 'Mouse', 50),
        ('Boar', 'Caterpillar', 90),
        ('Duck', 'Caterpillar', 90)
) AS rules(predator_name, prey_name, probability)
JOIN species predator ON predator.name = rules.predator_name
JOIN species prey ON prey.name = rules.prey_name
ON CONFLICT DO NOTHING;

INSERT INTO default_feeding_species_rules (species_id, prey_species_id, prey_plant_species_id, probability)
SELECT eater.id, NULL, plant.id, 100
FROM (
    VALUES
        ('Horse'),
        ('Deer'),
        ('Rabbit'),
        ('Mouse'),
        ('Goat'),
        ('Sheep'),
        ('Boar'),
        ('Buffalo'),
        ('Duck'),
        ('Caterpillar')
) AS eaters(name)
JOIN species eater ON eater.name = eaters.name
JOIN plant_species plant ON plant.biological_name = 'Plant'
ON CONFLICT DO NOTHING;
