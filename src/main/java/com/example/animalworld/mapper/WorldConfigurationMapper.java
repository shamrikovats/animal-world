package com.example.animalworld.mapper;

import com.example.animalworld.model.entity.*;
import com.example.animalworld.model.dto.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Shamrikova Tatiana
 */
@Component
public class WorldConfigurationMapper {

    public WorldSettingsDto toDto(WorldSettings entity) {
        return new WorldSettingsDto(
                entity.tickDuration(),
                entity.height(),
                entity.width(),
                entity.startHungryPercent(),
                entity.startPredatorCounter(),
                entity.startHerbivoreCounter(),
                entity.startPlantsMass()
        );
    }

    public SpeciesConfigurationDto toDto(SpeciesConfiguration entity, Map<Integer, Species> speciesById) {
        Species species = speciesById.get(entity.speciesId());
        return new SpeciesConfigurationDto(
                entity.speciesId(),
                species == null ? null : species.name(),
                entity.maxCoexistCount(),
                entity.weight(),
                entity.speedCells(),
                entity.fullTankWeight(),
                entity.minFoodPercent(),
                entity.maxChildrenCount(),
                entity.pregnancyPeriod(),
                entity.lostFoodForTick(),
                entity.startCount()
        );
    }

    public PlantSpeciesConfigurationDto toDto(
            PlantSpeciesConfiguration entity,
            Map<Integer, PlantSpecies> plantSpeciesById
    ) {
        PlantSpecies plantSpecies = plantSpeciesById.get(entity.plantSpeciesId());
        return new PlantSpeciesConfigurationDto(
                entity.plantSpeciesId(),
                plantSpecies == null ? null : plantSpecies.biologicalName(),
                entity.maxRepairSpeed(),
                entity.weight(),
                entity.startCount()
        );
    }

    public FeedingRuleDto toDto(
            FeedingRule entity,
            Map<Integer, Species> speciesById,
            Map<Integer, PlantSpecies> plantSpeciesById
    ) {
        Species predator = speciesById.get(entity.speciesId());
        Species preySpecies = entity.preySpeciesId() == null ? null : speciesById.get(entity.preySpeciesId());
        PlantSpecies preyPlant = entity.preyPlantSpeciesId() == null ? null : plantSpeciesById.get(entity.preyPlantSpeciesId());

        return new FeedingRuleDto(
                entity.speciesId(),
                predator == null ? null : predator.name(),
                entity.foodType(),
                entity.preySpeciesId(),
                preySpecies == null ? null : preySpecies.name(),
                entity.preyPlantSpeciesId(),
                preyPlant == null ? null : preyPlant.biologicalName(),
                entity.probability()
        );
    }

    public WorldTickStatDto toDto(WorldTickStat entity) {
        return new WorldTickStatDto(
                entity.tickNumber(),
                entity.alivePredatorCount(),
                entity.aliveHerbivoreCount(),
                entity.totalPlantMass(),
                entity.birthCount(),
                entity.deathCount(),
                entity.statsCreatedAt()
        );
    }

    public SpeciesConfiguration toEntity(Integer worldId, SpeciesConfigurationPatchDto dto) {
        return new SpeciesConfiguration(
                worldId,
                dto.speciesId(),
                dto.maxCoexistCount(),
                dto.weight(),
                dto.speedCells(),
                dto.fullTankWeight(),
                dto.minFoodPercent(),
                dto.maxChildrenCount(),
                dto.pregnancyPeriod(),
                dto.lostFoodForTick(),
                dto.startCount()
        );
    }

    public PlantSpeciesConfiguration toEntity(Integer worldId, PlantSpeciesConfigurationPatchDto dto) {
        return new PlantSpeciesConfiguration(
                worldId,
                dto.plantSpeciesId(),
                dto.maxRepairSpeed(),
                dto.weight(),
                dto.startCount()
        );
    }

    public FeedingRule toEntity(Integer worldId, FeedingRulePatchDto dto) {
        return new FeedingRule(
                null,
                worldId,
                dto.speciesId(),
                dto.preySpeciesId(),
                dto.preyPlantSpeciesId(),
                dto.preySpeciesId() == null ? com.example.animalworld.model.FoodType.PLANT : com.example.animalworld.model.FoodType.SPECIES,
                dto.probability()
        );
    }

    public WorldSettings toEntity(Integer worldId, WorldSettingsPatchDto dto) {
        return new WorldSettings(
                worldId,
                dto.tickDuration(),
                dto.height(),
                dto.width(),
                dto.startHungryPercent(),
                dto.startPredatorCounter(),
                dto.startHerbivoreCounter(),
                dto.startPlantsMass()
        );
    }

    public java.util.List<SpeciesConfiguration> toSpeciesEntities(Integer worldId, java.util.List<SpeciesConfigurationPatchDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream().map(dto -> toEntity(worldId, dto)).collect(Collectors.toList());
    }

    public java.util.List<PlantSpeciesConfiguration> toPlantEntities(Integer worldId, java.util.List<PlantSpeciesConfigurationPatchDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream().map(dto -> toEntity(worldId, dto)).collect(Collectors.toList());
    }

    public java.util.List<FeedingRule> toFeedingEntities(Integer worldId, java.util.List<FeedingRulePatchDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream().map(dto -> toEntity(worldId, dto)).collect(Collectors.toList());
    }
}
