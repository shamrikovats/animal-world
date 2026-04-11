package com.example.animalworld.simulation.factory;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Генератор runtime идентификаторов для животных и растений.
 * Нужен, чтобы объекты в памяти имели стабильный уникальный идентификатор независимо от БД.
 *
 * @author Shamrikova Tatiana
 */
@Component
public class RuntimeIdGenerator {
    private final AtomicLong sequence = new AtomicLong(1);

    public long nextId() {
        return sequence.getAndIncrement();
    }
}
