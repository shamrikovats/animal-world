package com.example.animalworld.model.entity;

/**
 * Правило пригодности типа локации для конкретного вида.
 * Нужен, чтобы симуляция понимала, куда виду можно идти, а куда нельзя.
 *
 * @author Shamrikova Tatiana
 */
public record BaseLocationRule(
        Integer speciesId,
        String locationName,
        Integer survivalModifier
) {
}
