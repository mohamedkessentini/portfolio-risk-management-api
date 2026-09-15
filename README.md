# Portfolio Risk Management API

A REST API for managing investment portfolios, financial instruments, transactions, and computing
portfolio valuation and basic risk-exposure metrics — built with Java, Spring Boot, and PostgreSQL,
containerized with Docker, and wired into a CI/CD pipeline and Kubernetes deployment.

This is a personal learning/portfolio project, not a production trading system. It is designed to be
realistic and defensible in a technical interview, not to simulate a real brokerage.

## Overview

The API models a simplified portfolio management workflow:

1. Create **portfolios** (a named account owned by someone, in a base currency).
2. Register **instruments** (equities, bonds, ETFs, cash) with a current market price.
3. Record **transactions** (BUY/SELL) against a portfolio. Each transaction automatically updates the
   portfolio's **positions** using the weighted-average-cost method.
4. Query a portfolio's **valuation**: total market value, cost basis, unrealized P&L, allocation by
   asset class, and concentration risk (the largest position's share of the portfolio).

## Features

- CRUD + search/pagination for portfolios and instruments
- Transaction recording with real business rules (can't sell more than you hold; average cost
  recalculated on every buy)
- Portfolio valuation and risk-exposure endpoint (allocation by instrument type, top-holding
  concentration, unrealized P&L)
- Bean Validation on all inputs with structured, consistent error responses
- Centralized exception handling (`404` not found, `409` duplicate, `422` business rule violation,
  `400` validation errors)
- Externalized configuration via Spring profiles (`dev`, `docker`, `test`)
- Database schema managed with Flyway migrations (no `ddl-auto: update` in any profile)
- OpenAPI/Swagger UI documentation
- Unit tests (Mockito) for business logic + integration tests (Testcontainers + real PostgreSQL)
- Docker, Docker Compose, GitLab CI/CD pipeline, Jenkins pipeline, Kubernetes manifests

## Architecture

```
Controller  →  Service  →  Repository  →  PostgreSQL
   ↑              ↑
  DTOs      Business rules
(validation)  (@Transactional)
```

- **`web.controller`** — REST endpoints. Only talk to services, never to repositories directly.
- **`web.dto`** — request/response records, decoupled from JPA entities.
- **`web.mapper`** — small classes that convert entities to response DTOs.
- **`service`** — business logic and transaction boundaries (`@Transactional`).
- **`repository`** — Spring Data JPA interfaces.
- **`domain`** — JPA entities.
- **`exception`** — custom exceptions + `@RestControllerAdvice` global handler.
- **`config`** — cross-cutting configuration (OpenAPI).

Entities are never returned directly from controllers — every response goes through a DTO. This
avoids leaking lazy-loading proxies over JSON and keeps the API contract independent from the schema.

## Tech Stack

| Layer          | Technology                                             |
|----------------|---------------------------------------------------------|
| Language       | Java 21                                                 |
| Framework      | Spring Boot 3.3 (Web, Data JPA, Validation, Actuator)   |
| Database       | PostgreSQL 16                                           |
| Migrations     | Flyway                                                  |
| Build          | Maven                                                   |
| Testing        | JUnit 5, Mockito, AssertJ, Testcontainers               |
| API docs       | springdoc-openapi (Swagger UI)                          |
| Containers     | Docker, Docker Compose                                  |
| CI/CD          | GitLab CI/CD, Jenkins                                   |
| Orchestration  | Kubernetes                                              |

## Project Structure

```
portfolio-risk-management-api/
├── src/main/java/com/kossentini/portfolio/
│   ├── domain/          # JPA entities + enums
│   ├── repository/      # Spring Data JPA repositories
│   ├── service/         # Business logic
│   ├── web/
│   │   ├── controller/  # REST controllers
│   │   ├── dto/         # Request/response records
│   │   └── mapper/      # Entity <-> DTO mapping
│   ├── exception/       # Custom exceptions + global handler
│   └── config/          # OpenAPI config
├── src/main/resources/
│   ├── application.yml (+ -dev, -docker)
│   └── db/migration/    # Flyway SQL migrations
├── src/test/java/...    # Unit tests (service) + integration tests (Testcontainers)
├── k8s/                 # Kubernetes manifests
├── Dockerfile
├── docker-compose.yml
├── .gitlab-ci.yml
├── Jenkinsfile
├── CV_DESCRIPTION.md
└── INTERVIEW_PREPARATION.md
```

## Prerequisites

- Java 21 (Temurin recommended)
- Maven 3.9+
- Docker Desktop (with Kubernetes enabled, for the K8s section)
- PostgreSQL 16 (or just use Docker Compose — see below)

## Installation

```bash
git clone <your-repo-url>
cd portfolio-risk-management-api
mvn -DskipTests package
```

## Configuration

Copy `.env.example` to `.env` and adjust as needed (used by Docker Compose):

```bash
cp .env.example .env
```

| Variable      | Description                          | Default                                      |
|---------------|---------------------------------------|-----------------------------------------------|
| `DB_URL`      | JDBC URL (used outside Docker)        | `jdbc:postgresql://localhost:5432/portfolio_db` |
| `DB_USERNAME` | Database user                         | `portfolio_user`                              |
| `DB_PASSWORD` | Database password                     | `portfolio_pass`                              |
| `DB_NAME`     | Database name (Docker Compose only)   | `portfolio_db`                                |
| `SERVER_PORT` | HTTP port                             | `8080`                                        |

## Running locally

Start a local PostgreSQL on port 5432 (or point `DB_URL` at the one started by Docker Compose below,
which listens on 5433 by default to avoid clashing with any other local Postgres), then:

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Docker

Build the image standalone:

```bash
docker build -t portfolio-risk-management-api .
```

## Docker Compose

Runs the API and PostgreSQL together. Host ports are 5433 (Postgres) and 8081 (API) by default —
overridable via `DB_PORT`/`API_PORT` in `.env` — to avoid clashing with a Postgres/app already
running locally on 5432/8080:

```bash
docker compose up --build
```

- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- PostgreSQL: `localhost:5433`

Stop and remove containers:

```bash
docker compose down
```

## API Documentation

Once the app is running, interactive API docs are at:

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

### Key endpoints

| Method | Path                                          | Description                          |
|--------|------------------------------------------------|---------------------------------------|
| POST   | `/api/portfolios`                              | Create a portfolio                    |
| GET    | `/api/portfolios?name=&page=&size=`             | Search/list portfolios                |
| GET    | `/api/portfolios/{id}`                          | Get a portfolio                       |
| GET    | `/api/portfolios/{id}/valuation`                | Valuation + risk exposure             |
| POST   | `/api/instruments`                              | Create an instrument                  |
| GET    | `/api/instruments?type=&search=&page=&size=`    | Search/list instruments               |
| PATCH  | `/api/instruments/{id}/price`                   | Update market price                   |
| POST   | `/api/portfolios/{portfolioId}/transactions`    | Record a BUY/SELL transaction         |
| GET    | `/api/portfolios/{portfolioId}/transactions?...`| Search a portfolio's transactions     |

## Testing

```bash
# Unit tests only (fast, no Docker required)
mvn -Dtest='!*IntegrationTest' test

# Integration tests (spins up a real PostgreSQL via Testcontainers - requires Docker running)
mvn -Dtest='*IntegrationTest' test

# Everything
mvn test
```

> **Windows + Docker Desktop note**: recent Docker Desktop versions block direct named-pipe access
> for non-CLI clients (including Testcontainers) unless you enable
> *Settings → Advanced → "Allow the default Docker socket to be used"*. Without it, integration
> tests fail with `Could not find a valid Docker environment` even though `docker` commands work
> fine. This doesn't affect CI (GitLab's `docker:dind` service and Jenkins-on-Linux both use a
> direct Unix socket, not a Windows named pipe).

## CI/CD

### GitLab CI/CD (`.gitlab-ci.yml`)

Pipeline stages: `build` → `test` (unit + integration in parallel) → `package` → `docker-build` →
`docker-push` (manual) → `deploy` (manual, to Kubernetes).

To run it: push this repository to a GitLab project with CI/CD enabled. `docker-push` and `deploy-k8s`
are manual jobs since they need a container registry / cluster credentials configured as CI/CD
variables (`$CI_REGISTRY_*`, `KUBECONFIG`).

### Jenkins (`Jenkinsfile`)

Run Jenkins locally:

```bash
docker run -d --name jenkins -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

Then configure JDK 21 and Maven 3.9 under *Manage Jenkins → Tools* (names `jdk21` and `maven3` to
match the `Jenkinsfile`), and create a Pipeline job pointing at this repository.

Stages: Checkout → Build → Unit Tests → Integration Tests (skipped if Docker isn't reachable from the
agent) → Package → Docker Build.

## Kubernetes

Manifests live in `k8s/`. Requires a local cluster (Docker Desktop → Settings → Kubernetes →
*Enable Kubernetes*, or Minikube).

```bash
# Build the image so the local cluster can find it
docker build -t portfolio-risk-management-api:latest .

kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/api-deployment.yaml
kubectl apply -f k8s/api-service.yaml

kubectl -n portfolio-app get pods
```

The API is reachable at `http://localhost:30080` (NodePort). See [k8s/secret.yaml](k8s/secret.yaml)
for how to create your own secret instead of using the example values.

## Screenshots

Verified end-to-end via `docker compose up --build`: portfolio creation, instrument creation, a BUY
transaction, and the resulting valuation (market value, cost basis, unrealized P&L, allocation) all
returned correct values through the live API, and Swagger UI rendered all endpoints at
`http://localhost:8081/swagger-ui/index.html`. See
[INTERVIEW_PREPARATION.md](INTERVIEW_PREPARATION.md) for example request/response payloads.

## Future Improvements

- Multi-currency support with FX conversion for cross-currency portfolios
- Authentication/authorization (Spring Security + JWT) to scope portfolios to a user
- Historical price time series and portfolio performance-over-time (instead of point-in-time valuation)
- Realized P&L tracking on sells (currently only unrealized P&L is computed)
- Rate limiting and idempotency keys on the transaction-recording endpoint

## What I Learned

- Structuring a Spring Boot service around DTOs instead of exposing JPA entities directly, and why
  that matters for both API stability and avoiding `LazyInitializationException`.
- Implementing the weighted-average-cost method for position tracking and covering it with unit tests
  that pin down the exact rounding behavior.
- Using Testcontainers to run integration tests against a real PostgreSQL instance instead of mocking
  the database, catching issues (like Flyway migration mismatches) that mocks can't.
- Writing a Kubernetes deployment with liveness/readiness probes backed by Spring Boot Actuator's
  health groups, and the difference between the two probe types.
- Structuring a GitLab CI/CD pipeline with parallel test jobs and manual gates for steps that need
  credentials (registry push, cluster deploy) not available on shared runners.
