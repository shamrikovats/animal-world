package com.example.animalworld.simulation.domain.flora;

import com.example.animalworld.model.FoodType;
import com.example.animalworld.simulation.domain.base.Consumable;
import com.example.animalworld.simulation.domain.config.RuntimePlantConfig;

/**
 * Runtime-объект растения в клетке мира.
 * Хранит текущее состояние растения в памяти и умеет отдавать массу при поедании.
 *
 * @author Shamrikova Tatiana
 */
public class Plant implements Consumable {
    private final long runtimeId;
    private final RuntimePlantConfig configuration;

    private int currentMass;
    private boolean alive;

    public Plant(long runtimeId, RuntimePlantConfig configuration) {
        this.runtimeId = runtimeId;
        this.configuration = configuration;
        this.currentMass = configuration.weight();
        this.alive = true;
    }

    public long runtimeId() {
        return runtimeId;
    }

    public Integer plantSpeciesId() {
        return configuration.plantSpeciesId();
    }

    public String biologicalName() {
        return configuration.biologicalName();
    }

    public RuntimePlantConfig configuration() {
        return configuration;
    }

    public int currentMass() {
        return currentMass;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public FoodType foodType() {
        return FoodType.PLANT;
    }

    @Override
    public double consume() {
        if (!alive) {
            return 0;
        }

        int consumedMass = currentMass;
        currentMass = 0;
        alive = false;
        return consumedMass;
    }

    public void regrow() {
        if (!alive) {
            alive = true;
        }
        currentMass = Math.min(configuration.weight(), currentMass + configuration.maxRepairSpeed());
    }
}
