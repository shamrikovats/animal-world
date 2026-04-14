# Docker Observability

## Как поднять все сервисы

```bash
docker compose up
```

Поднимутся сервисы:
- `postgres` на `localhost:5432`
- `prometheus` на `http://localhost:9090`
- `grafana` на `http://localhost:3000`

Само приложение запускается локально:

```text
http://localhost:8080
```

Локальный инстанс приложения:
- принимает все REST-запросы
- печатает тики в консоль
- отдает метрики в `/actuator/prometheus`

Prometheus и Grafana только читают эти метрики.

## Данные для входа в Grafana

- `login`: `admin`
- `password`: `admin`

## Что уже настроено автоматически

- datasource `Prometheus`
- dashboard `AnimalWorld Simulation`

## Полезные URL

- Spring Boot actuator prometheus:

```text
http://localhost:8080/actuator/prometheus
```

- Prometheus targets:

```text
http://localhost:9090/targets
```

- Grafana dashboards:

```text
http://localhost:3000/dashboards
```

## Основные бизнес-метрики

- `animalworld_worlds_running_total`
- `animalworld_world_running{world_id="..."}`
- `animalworld_world_current_tick{world_id="..."}`
- `animalworld_world_predators_alive{world_id="..."}`
- `animalworld_world_herbivores_alive{world_id="..."}`
- `animalworld_world_plant_mass{world_id="..."}`
- `animalworld_world_births_last_tick{world_id="..."}`
- `animalworld_world_deaths_last_tick{world_id="..."}`
- `animalworld_simulation_ticks_total{world_id="..."}`
- `animalworld_simulation_births_total{world_id="..."}`
- `animalworld_simulation_deaths_total{world_id="..."}`
- `animalworld_world_auto_stops_total`
