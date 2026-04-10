package com.example.animalworld.runtime.simulation.domain.base;

import com.example.animalworld.model.FoodType;
import com.example.animalworld.runtime.simulation.domain.dictionary.MovementDirection;
import com.example.animalworld.runtime.simulation.domain.dictionary.Sex;
import com.example.animalworld.runtime.simulation.domain.config.RuntimeSpeciesConfig;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Базовый runtime-класс любого животного в симуляции.
 * Здесь лежит общее состояние животного в памяти и простая базовая логика
 * еды, размножения, движения и смерти.
 *
 * @author Shamrikova Tatiana
 */
public abstract class Animal implements Consumable {
    private final long runtimeId;
    private final RuntimeSpeciesConfig configuration;
    private final Sex sex;

    private boolean alive;
    private boolean pregnant;
    private int pregnancyRemainingTicks;
    private double satiety;

    protected Animal(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        this.runtimeId = spawnState.runtimeId();
        this.configuration = configuration;
        this.sex = spawnState.sex();
        this.alive = spawnState.alive();
        this.pregnant = spawnState.pregnant();
        this.pregnancyRemainingTicks = spawnState.pregnancyRemainingTicks();
        this.satiety = Math.min(spawnState.satiety(), configuration.fullTankWeight());
    }

    public long runtimeId() {
        return runtimeId;
    }

    public Integer speciesId() {
        return configuration.speciesId();
    }

    public String speciesName() {
        return configuration.speciesName();
    }

    public RuntimeSpeciesConfig configuration() {
        return configuration;
    }

    public Sex sex() {
        return sex;
    }

    public boolean alive() {
        return alive;
    }

    public boolean pregnant() {
        return pregnant;
    }

    public int pregnancyRemainingTicks() {
        return pregnancyRemainingTicks;
    }

    public double satiety() {
        return satiety;
    }

    public double hungerPercent() {
        double fullTankWeight = configuration.fullTankWeight();
        if (fullTankWeight <= 0) {
            return 0;
        }
        return Math.max(0, ((fullTankWeight - satiety) / fullTankWeight) * 100);
    }

    public boolean eat(Consumable consumable) {
        if (!alive || consumable == null || !consumable.isAlive() || consumable.foodType() == null) {
            return false;
        }

        double nutrition = consumable.consume();
        if (nutrition <= 0) {
            return false;
        }

        satiety = Math.min(configuration.fullTankWeight(), satiety + nutrition);
        return true;
    }

    public Optional<Animal> reproduce(long offspringRuntimeId) {
        if (!alive || sex != Sex.FEMALE || !pregnant || pregnancyRemainingTicks > 0) {
            return Optional.empty();
        }

        pregnant = false;
        AnimalSpawnState offspringState = new AnimalSpawnState(
                offspringRuntimeId,
                randomSex(),
                configuration.fullTankWeight() * 0.5,
                true,
                false,
                0
        );
        return Optional.of(createOffspring(offspringState));
    }

    public MovementDirection chooseMoveDirection(List<MovementDirection> availableDirections) {
        if (!alive || configuration.speedCells() == null || configuration.speedCells() <= 0) {
            return MovementDirection.STAY;
        }
        if (availableDirections == null || availableDirections.isEmpty()) {
            return MovementDirection.STAY;
        }

        return availableDirections.get(ThreadLocalRandom.current().nextInt(availableDirections.size()));
    }

    public void markDead() {
        alive = false;
        satiety = 0;
        pregnant = false;
        pregnancyRemainingTicks = 0;
    }

    public void impregnate() {
        if (sex == Sex.FEMALE && alive) {
            pregnant = true;
            pregnancyRemainingTicks = configuration.pregnancyPeriod();
        }
    }

    public void decrementPregnancyTick() {
        if (pregnancyRemainingTicks > 0) {
            pregnancyRemainingTicks--;
        }
    }

    public void loseSatietyForTick() {
        satiety = Math.max(0, satiety - configuration.lostFoodForTick());
        if (satiety <= 0) {
            markDead();
        }
    }

    @Override
    public FoodType foodType() {
        return FoodType.SPECIES;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public double consume() {
        if (!alive) {
            return 0;
        }

        double nutrition = configuration.weight();
        markDead();
        return nutrition;
    }

    protected abstract Animal createOffspring(AnimalSpawnState spawnState);

    protected Sex randomSex() {
        return ThreadLocalRandom.current().nextBoolean() ? Sex.MALE : Sex.FEMALE;
    }
}
