# Portfolio Risk Management API

A Spring Boot REST API for managing investment portfolios, instruments and transactions, with
valuation and risk-exposure calculations. Built to be a realistic backend project I can actually
defend in an interview — not a toy CRUD app, but not pretending to be a real trading system either.

## What it does

- Create portfolios, register instruments (equities, bonds, ETFs, cash) with a market price
- Record BUY/SELL transactions against a portfolio — this updates the position automatically using
  weighted-average cost
- Get a portfolio's valuation: market value, cost basis, unrealized P&L, allocation by asset type,
  and concentration (how much of the portfolio sits in the largest holding)
- Search, filter and paginate everything
- Proper validation and error handling, not just happy-path code

## Stack

Java 21, Spring Boot 3 (Web, Data JPA, Validation, Actuator), PostgreSQL, Flyway for migrations,
Maven, JUnit 5 + Mockito + Testcontainers for tests, springdoc-openapi for the Swagger docs, Docker
+ Docker Compose, GitLab CI/CD, Jenkins, Kubernetes.

## Architecture

Standard layered setup: `Controller -> Service -> Repository -> PostgreSQL`. Controllers only talk
to services, DTOs are separate from JPA entities (I never return entities directly — avoids the
whole lazy-loading-proxy-in-JSON mess), and business rules live in the service layer under
`@Transactional`.

```
src/main/java/com/kossentini/portfolio/
├── domain/          entities + enums
├── repository/       Spring Data JPA
├── service/          business logic
├── web/
│   ├── controller/
│   ├── dto/
│   └── mapper/
├── exception/         custom exceptions + global handler
└── config/
```

## Running it locally

You'll need Java 21 and Maven 3.9+.

```bash
mvn -DskipTests package
```

Point it at a Postgres instance (port 5432 by default, or use the one from Docker Compose below —
that one runs on 5433 to avoid clashing with a local Postgres install), then:

```bash
mvn spring-boot:run
```

API on `http://localhost:8080`, Swagger UI at `/swagger-ui.html`.

## Docker

```bash
docker compose up --build
```

This spins up the API and Postgres together. API ends up on `8081` and Postgres on `5433` (not the
defaults 8080/5432) since I already had other stuff running on those ports locally — override with
`API_PORT`/`DB_PORT` in `.env` if you need different ones.

## Tests

```bash
mvn -Dtest='!*IntegrationTest' test   # unit tests, no Docker needed
mvn -Dtest='*IntegrationTest' test    # integration tests, spins up real Postgres via Testcontainers
mvn test                               # both
```

One gotcha if you're on Windows with Docker Desktop: recent versions block direct named-pipe access
for anything that isn't the Docker CLI, which breaks Testcontainers even though `docker` commands
work fine. Fix is in Settings → Advanced → "Allow the default Docker socket to be used". Doesn't
affect CI since GitLab's `docker:dind` and Jenkins-on-Linux use a real Unix socket.

## CI/CD

`.gitlab-ci.yml` runs build → test (unit + integration in parallel) → package → docker build, with
the registry push and Kubernetes deploy steps set to manual (they need credentials that aren't on
GitLab's free shared runners).

There's also a `Jenkinsfile` if you want to run it through Jenkins instead — spin one up locally
with:

```bash
docker run -d --name jenkins -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

then set up JDK 21 + Maven 3.9 as global tools (named `jdk21`/`maven3` to match the Jenkinsfile) and
point a Pipeline job at this repo.

## Kubernetes

Manifests are in `k8s/`. Needs a local cluster (enable Kubernetes in Docker Desktop, or Minikube).

```bash
docker build -t portfolio-risk-management-api:latest .
kubectl apply -f k8s/
kubectl -n portfolio-app get pods
```

API is reachable on `http://localhost:30080` via the NodePort service.

## Notes to self / things I'd add next

- Multi-currency support (right now it assumes everything's in the portfolio's base currency)
- Auth — no login system yet, so there's no concept of "your" portfolios
- Realized P&L on sells (only unrealized is calculated currently)
- Rate limiting on the transaction endpoint
