CREATE TABLE IF NOT EXISTS users
(
    id            BIGINT PRIMARY KEY,
    user_name     VARCHAR(255),
    first_name    VARCHAR(255),
    last_name     VARCHAR(255),
    registered_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exercise
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL UNIQUE,
    description  TEXT,
    video_url    VARCHAR(255),
    image_url    VARCHAR(255),
    muscle_group VARCHAR(50)  NOT NULL
);

CREATE TABLE IF NOT EXISTS exercise_result
(
    id                    BIGSERIAL PRIMARY KEY,
    exercise_id           BIGINT NOT NULL,
    weight                DOUBLE PRECISION,
    number_of_sets        SMALLINT,
    number_of_repetitions SMALLINT,
    comment               TEXT,
    date                  DATE,
    user_id               BIGINT NOT NULL,
    CONSTRAINT fk_exercise FOREIGN KEY (exercise_id) REFERENCES exercise (id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
