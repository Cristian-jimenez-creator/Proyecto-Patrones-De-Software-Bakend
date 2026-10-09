# Smart Pool Backend

Spring Boot backend and browser demo for the Smart Pool project.

## Requirements

- JDK 17
- Maven

## Run locally

```powershell
mvn spring-boot:run
```

Open http://localhost:8080 and check http://localhost:8080/health.

Local mode uses an in-memory H2 database. Demo data and camera analysis are simulated; this project does not connect to real cameras or run a real AI model.

## Build

```powershell
mvn -B -DskipTests package
java -jar target/smart-pool-backend-1.0.jar
```

## PostgreSQL / Render

The production profile reads `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` from environment variables. Set these in the hosting provider's secret/environment settings; do not commit credentials. The server listens on `PORT`.
