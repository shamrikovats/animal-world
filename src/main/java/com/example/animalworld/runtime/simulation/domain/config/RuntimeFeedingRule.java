package com.example.animalworld.runtime.simulation.domain.config;

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
