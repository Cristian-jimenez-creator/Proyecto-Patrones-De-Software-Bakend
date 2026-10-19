# Smart Pool API

Spring Boot REST backend for the Smart Pool Security web application.

## Run locally

Requires JDK 17 and Maven. Run `mvn spring-boot:run` from this directory. By default it uses an in-memory H2 database. Enable the login flow with `SMARTPOOL_AUTH_ENABLED=true`; configure `SMARTPOOL_AUTH_USERNAME` and an admin password of at least 16 characters.

The frontend runs separately on `http://localhost:5500`. CORS allows that local origin by default.
