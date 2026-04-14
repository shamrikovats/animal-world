package com.example.animalworld.simulation.domain.config;

import com.example.animalworld.model.FoodType;

/**
 * Runtime-представление правила питания.
 *
 * @author Shamrikova Tatiana
 */
public record RuntimeFeedingRule(
        RuntimeFeedingRuleKey key,
        FoodType foodType,
        Integer probability
) {
}
