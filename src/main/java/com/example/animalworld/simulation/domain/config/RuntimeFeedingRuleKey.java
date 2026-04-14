package com.example.animalworld.simulation.domain.config;

/**
 * Ключ правила питания в runtime-модели.
 * По нему удобно искать вероятность поедания конкретной жертвы конкретным видом.
 *
 * @author Shamrikova Tatiana
 */
public record RuntimeFeedingRuleKey(
        Integer predatorSpeciesId,
        Integer preySpeciesId,
        Integer preyPlantSpeciesId
) {
}
