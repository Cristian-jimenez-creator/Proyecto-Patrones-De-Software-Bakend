# Pool Safety Backend

Backend for a pool safety monitoring platform. It evaluates configured operational risk
conditions (camera offline, timeouts, unacknowledged alerts, manual signals) and requires
human action for critical alerts. It does not detect drowning and does not use artificial intelligence.

## Stack

Java 21, Spring Boot 3.5, Maven, PostgreSQL 16, Spring Security, Spring Data JPA, Flyway,
WebSocket, Actuator, springdoc OpenAPI, JUnit 5, Testcontainers, ArchUnit.

## Layers

`presentation` -> `application` -> `domain` <- `infrastructure`. The rules are enforced by `ArchitectureTest`.

## Run locally

```bash
cp .env.example .env            # then edit DB_PASSWORD
docker compose up -d            # starts PostgreSQL
set -a; source .env; set +a     # exports the variables to the current shell
mvn spring-boot:run
```

Health check: `GET http://localhost:8080/actuator/health`
OpenAPI UI: `http://localhost:8080/swagger-ui.html`

## Tests

```bash
mvn verify
```

Integration tests use Testcontainers and are skipped automatically when Docker is not available.
