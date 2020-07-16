-- !Ups

CREATE TABLE Projects (
    id SERIAL NOT NULL PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE
);

INSERT INTO Projects (name) VALUES ('test_project1');
INSERT INTO Projects (name) VALUES ('test_project2');

-- !Downs

DROP TABLE Projects;