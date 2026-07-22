
## Table of contents

- [Tech stack](#tech-stack)
- [How to run](#how-to-run)
- [API reference](#api-reference)
- [Technical choices explained](#technical-choices-explained)
- [Time spent](#time-spent)

---

## Tech stack

| Concern            | Choice                                              |
|--------------------|-----------------------------------------------------|
| Language           | Java 21                                             |
| Framework          | Spring Boot 4.1 (Web MVC, Data JPA, Validation)     |
| ORM                | Hibernate (via Spring Data JPA)                     |
| Database (runtime) | PostgreSQL 15 (Docker)                              |
| Database (tests)   | H2, in-memory                                       |
| Migrations         | Liquibase                                           |
| Mapping            | MapStruct (entity ↔ DTO)                            |
| Boilerplate        | Lombok                                              |
| Tests              | JUnit 5, Mockito, AssertJ, Spring MockMvc           |
| Build              | Maven                                               |


---

## How to run

**Prerequisites:** JDK 21+, Docker (with Compose).

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Run the application (Liquibase creates the schema on first boot)
```
The API is then available at `http://localhost:8080`.



## API reference

### Teams — `/api/teams`

| Method | Path                | Description                                                     |
|--------|---------------------|-----------------------------------------------------------------|
| GET    | `/api/teams`        | Paginated, sorted list of teams (each with players). Params: `page` (0), `size` (10), `sortBy` (`name`\|`acronym`\|`budget`, default `name`), `sortDir` (`asc`\|`desc`, default `asc`). |
| GET    | `/api/teams/{id}`   | One team by id, with its players.                               |
| POST   | `/api/teams`        | Create a team. Body below.                                      |
| PUT    | `/api/teams/{id}`   | Update a team (all fields, including the player roster).        |
| DELETE | `/api/teams/{id}`   | Delete a team.                                                  |

**TeamRequest**

```json
{ "name": "OGC Nice", "acronym": "OGCN", "budget": 50000000.00, "playerIds": [1, 2] }
```

`name` (not blank), `acronym` (not blank, max 10 chars), `budget` (not null, positive) are
required. `playerIds` is optional — a list of **existing** player ids to attach.

### Players — `/api/players`

| Method | Path                 | Description                                                    |
|--------|----------------------|----------------------------------------------------------------|
| GET    | `/api/players`       | List all players.                                              |
| GET    | `/api/players/{id}`  | One player by id.                                             |
| POST   | `/api/players`       | Create a player. Body below.                                  |
| PUT    | `/api/players/{id}`  | Update a player (set/change/clear `teamId` to transfer or release). |
| DELETE | `/api/players/{id}`  | Delete a player.                                              |

**PlayerRequest**

```json
{ "name": "Terem Moffi", "position": "FORWARD", "teamId": 1 }
```

`name` (not blank) and `position` (one of `GOALKEEPER`, `DEFENDER`, `MIDFIELDER`,
`FORWARD`) are required. `teamId` is optional (null ⇒ free agent).


---

## Technical choices explained


### 1. Players are referenced by **id**, never created inline

When you create or update a team, the body carries `playerIds` (a list of `Long`), **not**
nested player objects:

```json
{ "name": "OGC Nice", "acronym": "OGCN", "budget": 50000000.00, "playerIds": [1, 2] }
```

The service loads those existing players (`playerRepository.findAllById`) and attaches them
to the team. This is a conscious choice:

- **A player is an independent entity with its own lifecycle.** You create players first
  (`POST /api/players`), then compose teams from them. A team should not silently create
  players as a side effect.
- **It makes the contract unambiguous** and avoids accidental duplicate players.
- If a supplied id doesn't exist, the request fails cleanly with **404** rather than
  quietly inventing a record.

So: **to create a team with players, create the players first, then pass their ids.**
A team with no `playerIds` is created with an empty squad — perfectly valid.


### 2. Pagination & sorting via Spring Data `Pageable`

`GET /api/teams` maps `page` / `size` / `sortBy` / `sortDir` onto a `PageRequest` and calls
`teamRepository.findAll(pageable)`, returning a `Page<TeamResponse>`.

- **Sorting and paging happen in the database** (`ORDER BY … LIMIT … OFFSET …`), not in
  Java — this scales and doesn't load the whole table into memory.
- Using the framework's `findAll(Pageable)` keeps it declarative; the `Page` result also
  ships useful metadata (`totalElements`, `totalPages`, `first`/`last`, …) for the client.
- `sortDir` is validated to `asc`/`desc`; `sortBy` accepts `name`, `acronym`, `budget` as
  required by the assignment.


## Time spent

Roughly **~8 hours**, broken down as:

| Phase                                   | Time    |
|-----------------------------------------|---------|
| Conception / design (model, layering, API shape) | 2 h     |
| Implementation (entities, services, controllers, mappers, migrations) | ~3 h    |
| Tests (unit + integration)              | ~1 h 30 |
| Documentation (this README) & polish    | ~1 h 30 |
