-- Initial schema creation for shotlog application

-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

-- Create user_roles join table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create shootingplace table
CREATE TABLE shootingplace (
    id BIGSERIAL PRIMARY KEY,
    club VARCHAR(255),
    location VARCHAR(255)
);

-- Create sessions table
CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_date DATE,
    session_time TIME,
    shooting_place_id BIGINT NOT NULL,
    session_type VARCHAR(50),
    decimal_scoring BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (shooting_place_id) REFERENCES shootingplace(id) ON DELETE RESTRICT
);

-- Create series table
CREATE TABLE series (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    series_number INTEGER NOT NULL,
    test_shot BOOLEAN NOT NULL,
    FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
    UNIQUE (session_id, series_number)
);

-- Create shot table
CREATE TABLE shot (
    id BIGSERIAL PRIMARY KEY,
    series_id BIGINT NOT NULL,
    shot_number INTEGER NOT NULL,
    value DECIMAL(4,1),
    FOREIGN KEY (series_id) REFERENCES series(id) ON DELETE CASCADE,
    UNIQUE (series_id, shot_number)
);

-- Insert default roles
INSERT INTO roles (name) VALUES ('USER');
INSERT INTO roles (name) VALUES ('ADMIN');