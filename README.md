# onlinePM
REST API application to manage time of projects and tasks

Starting application: 

- download repo
- type: sbt run

To run tests type: sbt test

Default config details(possible to change in conf):
- database - h2-inMemory(mode=Postgres), no additional setup required, database clears after every restart of application
- authentication - application provides jwt authentication for example users added to database in evolutions schema
- tokens - generated tokens for example users, for testing

API documentation with example requests(curl, postman) available at: https://documenter.getpostman.com/view/12502118/TVCZaWqu



