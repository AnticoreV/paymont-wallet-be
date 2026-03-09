# Paymont Wallet Backend

Spring Boot backend for wallet operations (EUR/CZK): wallet creation, balance retrieval, top-up, withdrawal, and transaction history.

## Tech Stack
- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Flyway
- OpenAPI Generator + springdoc
- Gradle

## Prerequisites
- JDK 21
- Docker + Docker Compose (for local DB/app containers)

## Configuration
Default configuration is in `src/main/resources/application.yaml`:
- DB URL: `jdbc:postgresql://localhost:5432/wallet`
- DB user: `wallet_user`
- DB password: `wallet_pass`
- App port: `8080`

You can override these with standard Spring environment variables, for example:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Run Locally

### Option 1: Run with Docker Compose (recommended)
```bash
docker compose up --build
```

This starts:
- `postgres` on `localhost:5432`
- `wallet-app` on `localhost:8080`

### Option 2: Run DB in Docker, app from IDE/terminal
1. Start PostgreSQL:
```bash
docker compose up -d postgres
```
2. Start the app:
```bash
./gradlew bootRun
```

## Database Migrations
Flyway migrations run automatically on startup from:
- `src/main/resources/db/migration/V1__init.sql`
- `src/main/resources/db/migration/V2__added_wallet_snapshots.sql`

## Authentication Model
This project uses a custom request header for authentication:
- `X-User-Email: <email>`

Request is rejected with `401` if:
- header is missing, or
- user with that email does not exist in `users` table.

There is currently no registration endpoint, so add a user manually for local testing.

### Insert a test user
```sql
INSERT INTO users (id, email, full_name, created_at)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  'demo@paymont.local',
  'Demo User',
  now()
)
ON CONFLICT (email) DO NOTHING;
```

Example using Dockerized Postgres:
```bash
docker exec -it wallet-postgres-db psql -U wallet_user -d wallet
```
Then run the SQL above.

## API Documentation
After startup:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Note: runtime endpoints are exposed as `/wallets/...` (no `/api` prefix).

## Tests
Run:
```bash
./gradlew test
```

## Build Jar
```bash
./gradlew clean bootJar
```

## OpenAPI Codegen Notes
API interfaces/models are generated from:
- `src/main/resources/wallet-api.yaml`

Generation is wired in Gradle and runs before Java compilation (`openApiGenerate` -> `compileJava`).
