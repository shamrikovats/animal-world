--liquibase formatted sql

--changeset shamrikova:003-fix-species-configuration-constraints
ALTER TABLE species_configuration
    DROP CONSTRAINT IF EXISTS species_configuration_full_tank_weight_check;

ALTER TABLE species_configuration
    ADD CONSTRAINT species_configuration_full_tank_weight_check
        CHECK (full_tank_weight >= 0);
