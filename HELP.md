# Базовые данные для проекта
Чтобы поднять локально требуется докер, терпение и вера в человечество

### Реализация потоковой обработки

Суть работы с потоками сводится к двум принципам:

1. На уровне миров WorldSimulationScheduler запускает для каждого worldId отдельную scheduled-задачу через 
scheduleWithFixedDelay(...).

Логика:
- один мир = одна периодическая задача, то есть мы не можем в одном мира запустить параллельных тасок, которые
запутаются и дадут неконсистентность.
- разные миры могут существовать параллельно и по сути количество их не ограничено (разве что архитектурой)
- один и тот же мир не должен запускаться дважды, потому что это контролирует SimulationWorldRegistry
- startWorld() и stopWorld() синхронизированы, чтобы не было гонок
- scheduleWithFixedDelay запускает следующий тик только после завершения предыдущего

2. На уровне одного тика SimulationTickEngine вызывает фазы последовательно. По сути это некое подобие паттерна SAGA.
Внутри фаз мир обрабатывается параллельно через worker pool, который задан в SimulationExecutorConfig.
Обеспечивается это через SimulationParallelSupport:
- мир режется по строкам и на каждую строку создается задача
- задачи идут в simulationWorkerPool
- invokeAll() ждет завершения всех задач только потом начинается следующая фаза

Безопасность на уровне клеток обеспечивается черех SimulationCel, который хранит ReentrantLock
фазы мутаций используют cell.withLock(...):
- содержимое клетки мутирует атомарно в рамках одной операции
- две worker-задачи не могут одновременно изменять одну и ту же клетку

### Реализация фаз мира

#### Feeding 
Животное сначала пытается поесть в своей текущей клетке. Для него берутся правила питания мира feeding rules.
Дальше оно перебирает доступную еду:
- если правило на растение, ищет подходящее живое растение
- если правило на животное, ищет подходящую живую добычу

Вероятность успеха берется из правила. Если поедание удалось:
- хищник или травоядное получает массу съеденной пищи в сытость
- добыча удаляется из клетки, если это животное
- растение считается съеденным по логике самого растения
- в метрики тика идет смерть, если была съедена жертва

Животное ест не один раз, а повторяет попытки, пока не насытится или в клетке не закончится подходящая еда

#### Movement
После еды животное выбирает, куда двигаться. Сначала для всех животных параллельно считаются намерения перемещения.

Правила движений животного:
- не двигается дальше speed_cells
- не идет в запрещенную локацию
- не идет в клетку, где уже нет места для его вида

Клетки-кандидаты оцениваются по score:
- сколько в клетке потенциальной еды
- какой modifier у location type
- сколько уже своих животных в этой клетке
- насколько клетка далеко

Потом выбирается лучшая клетка, а если лучших несколько, одна из них случайно.

#### Reproduction
Размножение идет внутри клетки среди животных одного вида.

Логика такая:
- должен быть живой самец нужного вида
- самка должна быть достаточно сыта
- если самка не беременна, она может забеременеть с вероятностью
- если самка беременна, у нее уменьшается счетчик беременности
- когда срок дошел до нуля, она может родить потомство

Количество потомков ограничивается по max_children_count и свободным местом в клетке по max_coexist_count

После рождения потомки сразу добавляются в клетку и записываются в метрики тика.

#### Survival
Это фаза голода и смерти. Вытащила отдельно для упрощения классов по сути.

Для каждого животного:
- если оно не ело в этом тике, у него уменьшается сытость по lost_food_for_tick
- если после этого процент сытости стал ниже min_food_percent, животное умирает
- мертвое животное удаляется из клетки
- смерть пишется в метрики

То есть тут фиксируется смерть именно от голода и общего истощения.

#### Statistics
После основных фаз мир собирает snapshot состояния:
- сколько живых хищников
- сколько живых травоядных
- какая суммарная масса растений
- сколько было рождений
- сколько было смертей

Этот snapshot:
- сохраняется в world_tick_stats
- печатается в консоль (тут с добавлением деталей по животным и иконками животных)
- отправляется в метрики Prometheus для дашика в Grafana

#### Plant Growth
После сохранения статистики растения восстанавливаются.

Логика такая:
- каждое существующее растение делает regrow()
- если растений какого-то вида в клетке меньше целевого количества, может вырасти новое растение
- вероятность роста зависит от max_repair_speed и дефицита растений в клетке

Это сделано так, чтобы растения не стояли мертвыми навсегда и не мгновенно возвращались одинаково в каждой клетке

### Как тестировать

1. Поднять проект локально (сначала поднять все из docker-compose.yml)
2. Проверить, что все сервисы запустились
3. Открыть локально swagger http://localhost:8080/swagger-ui.html
4. Cоздать мир с минимальным запросом (мир + описание)
5. Активировать мир (status = ACTIVE)
6. Проверить, что в логах пошла статистика по миру. Также можно покидать запросы в
http://localhost:8080/api/worlds/8/runtime/summary
7. Проверить, что данные полетели в графану (в worldId выбрать свой мир)
http://localhost:3000/d/animalworld-simulation/animalworld-simulation?orgId=1&refresh=5s&from=now-15m&to=now&var-worldId=7&var-worldId=8


### Архитектура приложения (можно открыть в https://editor.plantuml.com/)

@startuml
title AnimalWorld - Архитектура приложения

skinparam backgroundColor lightgrey
skinparam shadowing true
skinparam packageStyle rectangle
skinparam componentStyle rectangle
skinparam ArrowColor #4b5563
skinparam PackageBorderColor #111827
skinparam PackageFontColor #111827
skinparam ComponentBorderColor #374151
skinparam ComponentFontColor #111827

actor "Client\nSwagger / REST" as Client

rectangle "API Layer" as API
rectangle "Application Layer\nfacades + services" as APP
database "PostgreSQL" as DB

rectangle "Simulation Runtime" as RUNTIME
rectangle "Simulation Engine\nphases" as ENGINE
rectangle "Scheduler & Executors\nworld start/stop + pools" as SCHEDULER

rectangle "Actuator Metrics" as ACTUATOR
rectangle "Prometheus" as PROM
rectangle "Grafana" as GRAFANA

Client --> API : REST requests
API --> APP : use cases
APP --> DB : read/write config and state

APP --> SCHEDULER : start/stop world
SCHEDULER --> RUNTIME : build and keep running worlds
SCHEDULER --> ENGINE : execute next tick

RUNTIME --> ENGINE : world state
ENGINE --> RUNTIME : update animals, plants,\nworld tick state
ENGINE --> DB : save tick statistics

ENGINE --> ACTUATOR : publish business metrics
ACTUATOR --> PROM : expose /actuator/prometheus
PROM --> GRAFANA : datasource for dashboards

note right of API
Контроллер. По сути все методы,
которые видны в сваггере.
end note

note right of APP
По сути обеспечение конфигов по миру
и всего, что делает рест.
end note

note right of RUNTIME
Хранение миров в памяти с деталями вне конфигов
в том числе состояние клеток и животных
текущее состояние мира тоже тут.
end note

note right of ENGINE
Обеспечение логики тика
Тут происходит проход по фазам
и обеспечивается многопоточка
end note

note right of SCHEDULER
Запускается по переходу мира в ACTIVE,
Останавливает джобы по переходу в INACTIVE
По сути тут все управление тиками и задачами.
end note

note right of GRAFANA
Отображение графиков по текущему состоянию мира.
end note

@enduml