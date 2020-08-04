-- !Ups

CREATE TABLE IF NOT EXISTS projects (
    id SERIAL NOT NULL PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE,
    ts timestamp NOT NULL,
    isdeleted timestamp
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

-- !Downs

DROP TABLE tasks;
DROP TABLE projects;