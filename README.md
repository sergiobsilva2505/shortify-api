# 🔗 Shortify API

📌 Project idea: <https://roadmap.sh/projects/url-shortening-service>

[![CI](https://github.com/sergiobsilva2505/shortify-api/actions/workflows/ci.yml/badge.svg)](https://github.com/sergiobsilva2505/shortify-api/actions/workflows/ci.yml)

RESTful URL shortening API with full CRUD and access statistics.

## ✨ Features

- Create a short URL from a long URL
- Retrieve a short URL's data (each retrieval counts as an access)
- Update the original URL of an existing code
- Delete a short URL
- Get access statistics (number of times it was accessed)
- Interactive documentation (Swagger UI / OpenAPI) and operational endpoints (Actuator)

## 🧱 Architecture

The project follows a **hexagonal architecture (ports & adapters)**, keeping business rules isolated from frameworks
and infrastructure.
The dependency rule always points inward: **adapters → application → domain**.

```
domain/
  model/            → ShortUrl (pure domain model), ShortCodeFormat
  exception/        → InvalidUrlException, ShortUrlNotFoundException, ShortCodeAlreadyExistsException

application/
  port/in/          → ShortUrlUseCase (use cases)
  port/out/         → ShortUrlRepositoryPort, ShortCodeGeneratorPort
  service/          → ShortUrlService (implements the use cases)

adapter/
  in/web/           → controllers, DTOs, mapper and GlobalExceptionHandler
  out/persistence/  → ShortUrlPersistenceAdapter, JPA entity, Spring Data repository, mapper
  out/codegen/      → Base62ShortCodeGenerator

config/             → OpenApiConfig
```

### Why hexagonal?

For a small CRUD, a layered design would be enough, so this is a deliberate trade-off:

- **Open variation points:** the project statement allows MySQL *or* MongoDB, and the code generator or a cache in front
  of reads are likely to change. Each is a new adapter behind a port, not a change to the use cases.
- **Fast, infrastructure-free tests:** business rules (collision retries, URL validation, not-found handling) are tested
  with mocked ports, with no Spring context or database.
- **Local changes:** changing the routes and response shapes only touched the web adapter.
- **Enforced, not just documented:** `HexagonalArchitectureTest` (ArchUnit) fails the build if the domain depends on
  frameworks or if the dependency rule is broken.
- **Costs:** more types and mapping (DTO ↔ domain ↔ JPA entity) and extra indirection.

Details and alternatives in [ADR 0001](docs/adr/0001-hexagonal-architecture.md).

## 🛠️ Stack

- Java 21
- Spring Boot 4.1.2 (Web, Validation, Data JPA, Actuator)
- MySQL
- Liquibase (schema versioning)
- springdoc-openapi 3.2.0 (OpenAPI / Swagger UI)
- JUnit 5 + Mockito
- Testcontainers (MySQL) for integration tests
- JaCoCo (coverage)

## 📡 Endpoints

### API

| Method | Route                   | Description                                                            | Responses           |
|--------|-------------------------|------------------------------------------------------------------------|---------------------|
| POST   | `/shorten`              | Creates a short URL (body `{"url": "..."}`)                            | `201`, `400`        |
| GET    | `/shorten/{code}`       | Returns the short URL data and **increments the access counter**       | `200`, `404`        |
| PUT    | `/shorten/{code}`       | Updates the original URL (body `{"url": "..."}`)                       | `200`, `400`, `404` |
| DELETE | `/shorten/{code}`       | Deletes the short URL                                                  | `204`, `404`        |
| GET    | `/shorten/{code}/stats` | Returns the short URL data with `accessCount` (does not count as access) | `200`, `404`        |

Rules: `{code}` is **7 alphanumeric characters** (Base62); the URL must be `http` or `https` and at most **2048
characters** long.
Errors use the `application/problem+json` format (`ProblemDetail`) with a `timestamp` and, for validation errors, an
`errors[]` list (`field` and `message`).

### Documentation

| Method | Route                    | Description                 |
|--------|--------------------------|-----------------------------|
| GET    | `/swagger-ui/index.html` | Swagger UI                  |
| GET    | `/v3/api-docs`           | OpenAPI specification (JSON) |

### Operations (Actuator)

| Method | Route                        | Description                        |
|--------|------------------------------|------------------------------------|
| GET    | `/actuator/health`           | Application health (includes the database) |
| GET    | `/actuator/health/liveness`  | Liveness probe                     |
| GET    | `/actuator/health/readiness` | Readiness probe                    |
| GET    | `/actuator/info`             | Application information            |
| GET    | `/actuator/metrics`          | Metrics                            |

Only `health`, `info` and `metrics` are exposed; health details are hidden (`show-details: never`).

### Example

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/some/long/url"}'
```

```json
{
  "id": 1,
  "url": "https://example.com/some/long/url",
  "shortCode": "aB3dE9x",
  "createdAt": "2026-10-25T12:00:00Z",
  "updatedAt": "2026-10-25T12:00:00Z"
}
```

## 🚀 Running

### With Docker Compose

```bash
# Only the MySQL database (run the API from your IDE or with ./mvnw spring-boot:run)
docker compose up -d db

# Database + API in containers (prod profile)
docker compose --profile app up --build
```

If host port 3306 is already in use, set `DB_PORT` (e.g. `DB_PORT=3307 docker compose up -d db`) and point `DB_URL` to
that port.

### Without Docker

Requirements: Java 21 and a reachable MySQL. By default the API uses `localhost:3306`, database `shortifyapiDb`, user
`root` with no password.

```bash
./mvnw spring-boot:run
```

### Configuration

| Variable                 | Default                                                                     |
|--------------------------|-----------------------------------------------------------------------------|
| `DB_URL`                 | `jdbc:mysql://localhost:3306/shortifyapiDb?useSSL=false&serverTimezone=UTC` |
| `DB_USER`                | `root`                                                                      |
| `DB_PASSWORD`            | _(empty)_                                                                   |
| `SPRING_PROFILES_ACTIVE` | _(none; the Docker image uses `prod`)_                                      |

The **`prod`** profile disables Swagger UI and `/v3/api-docs`, exposes only `health` and `info` in Actuator, and turns
off SQL logging.
Since the API has no authentication, keep `/actuator` and the documentation away from public networks.

Liquibase migrations run automatically on startup.
With the application running (default profile), open Swagger UI at <http://localhost:8080/swagger-ui/index.html> and
the health endpoint at <http://localhost:8080/actuator/health>.

## 🧪 Tests

Integration tests use Testcontainers, so **Docker** must be running.

```bash
./mvnw test
```

A single MySQL container is shared by all integration test classes (`support/IntegrationTestSupport`) and can be reused
between runs with `testcontainers.reuse.enable=true` in `~/.testcontainers.properties`.
The JaCoCo coverage report is generated at `target/site/jacoco` (with `./mvnw verify`).
