-- Initial schema for the OGC Nice football API (v1.0).
-- The 'ogcnice' database itself is created by docker-compose (POSTGRES_DB),
-- so this changeset only creates the tables inside it.

CREATE TABLE team (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    acronym VARCHAR(10) NOT NULL,
    budget DECIMAL(15, 2) NOT NULL
);

CREATE TABLE player (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    position VARCHAR(50) NOT NULL,
    team_id BIGINT,
    CONSTRAINT fk_player_team FOREIGN KEY (team_id) REFERENCES team (id)
);
