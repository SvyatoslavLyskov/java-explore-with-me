CREATE TABLE IF NOT EXISTS users
(
    user_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    user_email  VARCHAR(254) NOT NULL,
    user_name   VARCHAR(250) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS categories
(
    category_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    category_name   VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS locations
(
    location_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    lat         FLOAT NOT NULL,
    lon         FLOAT NOT NULL
);

CREATE TABLE IF NOT EXISTS events
(
    event_id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    annotation          VARCHAR(2000) NOT NULL,
    category_id         BIGINT NOT NULL REFERENCES categories (category_id),
    description         VARCHAR(7000) NOT NULL,
    event_date          TIMESTAMP NOT NULL,
    location_id         BIGINT NOT NULL REFERENCES locations (location_id),
    paid                BOOLEAN NOT NULL,
    participant_limit   INT NOT NULL,
    request_moderation  BOOLEAN NOT NULL,
    title               VARCHAR(120) NOT NULL,
    created_on          TIMESTAMP NOT NULL,
    initiator_id        BIGINT NOT NULL REFERENCES users (user_id),
    state               VARCHAR(9) NOT NULL,
    published_on        TIMESTAMP,
    confirmed_requests  BIGINT
);

CREATE TABLE IF NOT EXISTS requests
(
    request_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    created         TIMESTAMP NOT NULL,
    event_id        BIGINT NOT NULL REFERENCES events (event_id),
    requester_id    BIGINT NOT NULL REFERENCES users (user_id),
    status          VARCHAR(9) NOT NULL,

    CONSTRAINT uq_request UNIQUE(event_id, requester_id)
);

CREATE TABLE IF NOT EXISTS compilations
(
    compilation_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    pinned              BOOLEAN NOT NULL,
    compilation_title   VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS compilations_events
(
    compilation_id  BIGINT REFERENCES compilations (compilation_id),
    event_id        BIGINT REFERENCES events (event_id),

    PRIMARY KEY (compilation_id, event_id)
);