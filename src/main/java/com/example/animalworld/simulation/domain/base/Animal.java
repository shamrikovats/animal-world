package com.example.animalworld.simulation.domain.base;

import com.example.animalworld.model.FoodType;
import com.example.animalworld.simulation.domain.dictionary.MovementDirection;
import com.example.animalworld.simulation.domain.dictionary.Sex;
import com.example.animalworld.simulation.domain.config.RuntimeSpeciesConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;

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
    private boolean ateThisTick;
    private int pregnancyRemainingTicks;
    private double satiety;

    protected Animal(RuntimeSpeciesConfig configuration, AnimalSpawnState spawnState) {
        this.runtimeId = spawnState.runtimeId();
        this.configuration = configuration;
        this.sex = spawnState.sex();
        this.alive = spawnState.alive();
        this.pregnant = spawnState.pregnant();
        this.ateThisTick = false;
        this.pregnancyRemainingTicks = spawnState.pregnancyRemainingTicks();
        this.satiety = Math.min(spawnState.satiety(), configuration.effectiveFullTankWeight());
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
        double fullTankWeight = configuration.effectiveFullTankWeight();
        return Math.max(0, ((fullTankWeight - satiety) / fullTankWeight) * 100);
    }

    public double foodPercent() {
        return Math.max(0, 100 - hungerPercent());
    }

    public boolean needsFood() {
        return alive && satiety < configuration.effectiveFullTankWeight();
    }

    public boolean ateThisTick() {
        return ateThisTick;
    }

    public void prepareForTick() {
        ateThisTick = false;
    }

    public boolean eat(Consumable consumable) {
        if (!alive || consumable == null || !consumable.isAlive() || consumable.foodType() == null) {
            return false;
        }

        double nutrition = consumable.consume();
        if (nutrition <= 0) {
            return false;
        }

        satiety = Math.min(configuration.effectiveFullTankWeight(), satiety + nutrition);
        ateThisTick = true;
        return true;
    }

    public List<Animal> reproduce(int offspringCount, LongSupplier runtimeIdSupplier) {
        if (!alive || sex != Sex.FEMALE || !pregnant || pregnancyRemainingTicks > 0) {
            return List.of();
        }

        pregnant = false;
        List<Animal> offspring = new ArrayList<>(offspringCount);
        for (int i = 0; i < offspringCount; i++) {
            AnimalSpawnState offspringState = new AnimalSpawnState(
                    runtimeIdSupplier.getAsLong(),
                    randomSex(),
                    configuration.effectiveFullTankWeight() * 0.5,
                    true,
                    false,
                    0
            );
            offspring.add(createOffspring(offspringState));
        }
        return List.copyOf(offspring);
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
        ateThisTick = false;
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
        double satietyLoss = configuration.effectiveFullTankWeight() * configuration.lostFoodForTick() / 100.0;
        satiety = Math.max(0, satiety - satietyLoss);
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
