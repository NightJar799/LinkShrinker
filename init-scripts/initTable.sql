-- DROP DATABASE IF EXISTS mini_lu;
-- CREATE DATABASE mini_lu;

-- \c mini_lu;

CREATE SCHEMA IF NOT EXISTS mil;

SET search_path TO mil;

CREATE TABLE mil.users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(75) NOT NULL,  
    name VARCHAR(100) NOT NULL
);


CREATE TABLE mil.links (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    link VARCHAR(250) NOT NULL,
    short_link VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_user_link FOREIGN KEY (user_id) REFERENCES mil.users(id) ON DELETE CASCADE
);

CREATE TABLE mil.user_metrics (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    link_id BIGINT NOT NULL,
    ip VARCHAR(45),
    user_agent VARCHAR(500),
    referer VARCHAR(300),
    time_stamp VARCHAR(50),
    short_link VARCHAR(50),
    country VARCHAR(100),
    device_type VARCHAR(50),
    city VARCHAR(100),
    device VARCHAR(100),
    agent VARCHAR(400),
    os VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_link_metrics FOREIGN KEY (link_id) REFERENCES mil.links(id) ON DELETE CASCADE
);