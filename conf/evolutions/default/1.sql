-- !Ups

CREATE TABLE IF NOT EXISTS projects (
    id SERIAL NOT NULL PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE,
    ts timestamp NOT NULL,
    isdeleted timestamp,
    lastactivity timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS tasks (
    id SERIAL NOT NULL PRIMARY KEY,
    project_id INT NOT NULL,
    ts timestamp NOT NULL,
    duration bigint NOT NULL,
    volume INT,
    description varchar(255),
    isdeleted timestamp,
    CONSTRAINT fk_project
        FOREIGN KEY (project_id)
            REFERENCES projects(id)
);

CREATE TABLE IF NOT EXISTS users (
    uuid UUID NOT NULL PRIMARY KEY
);

INSERT INTO users VALUES ('74c8c172-e167-4754-9cbf-0d48aefd6167');
INSERT INTO users VALUES ('536347fb-4c28-4aa0-a765-1a8b87eb1a86');

-- !Downs

DROP TABLE tasks;
DROP TABLE projects;