-- !Ups

CREATE TABLE Projects (
    id SERIAL NOT NULL PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE,
    ts timestamp NOT NULL,
    isdeleted timestamp
);

-- !Downs

DROP TABLE Projects;