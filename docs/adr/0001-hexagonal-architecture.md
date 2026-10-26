# ADR 0001 — Adopt hexagonal architecture (ports & adapters)

- **Status:** Accepted
- **Date:** 2026-10-26

## Context

Shortify API implements the [URL Shortening Service](https://roadmap.sh/projects/url-shortening-service) project:
create, retrieve, update and delete short URLs, plus access statistics.

On its own this is a small CRUD, and a conventional layered architecture (controller → service → repository) would be
enough to deliver it. Still, the project statement leaves several decisions open, and they are exactly the ones most
likely to change:

- **Storage:** the statement suggests MySQL *or* MongoDB.
- **Short code generation:** random Base62 today; hashing, a sequence or a distributed ID generator are realistic
  alternatives.
- **Delivery mechanism:** a REST API today, with a redirect endpoint, a minimal front end or a cache in front of the
  read path as natural next steps.

The project is also meant to demonstrate how to keep business rules independent of frameworks.

## Decision

Structure the code as a hexagonal architecture:

```
domain/       pure business model and exceptions (no framework dependencies)
application/  use cases (ports in) and the contracts they need from the outside (ports out)
adapter/      web (in), persistence and code generation (out), each implementing or using a port
```

The dependency rule is **adapters → application → domain**, never the other way around. It is enforced by
`HexagonalArchitectureTest` (ArchUnit), which fails the build if, for example, the domain imports Spring or JPA, the
application layer imports an adapter, or an inbound adapter depends on an outbound one.

## Consequences

### Benefits

- **Business rules are tested without infrastructure.** `ShortUrlServiceTest` covers code-collision retries, URL
  validation and not-found handling with mocked ports: no Spring context, no database, milliseconds to run.
  Persistence and HTTP concerns are tested separately in the adapter and integration tests.
- **Changes stay local.** Moving the routes from `/api/shorten` to `/shorten` and reshaping the responses (dropping
  `shortUrl`, returning the full object from `/stats`) touched only the web adapter and its tests; `domain/` and
  `application/` were unchanged. Removing the redirect endpoint reached the application layer only to delete the use-case
  methods that had become unused.
- **Infrastructure details do not leak.** The persistence adapter translates `DataIntegrityViolationException` into the
  domain's `ShortCodeAlreadyExistsException` and updates only the URL and timestamp of a managed entity so that a
  concurrent access-counter increment is not overwritten. The service knows nothing about either detail.
- **Swappable parts.** A different database (for example MongoDB), a different code generator or a cache are new
  adapters behind `ShortUrlRepositoryPort` / `ShortCodeGeneratorPort`, not changes to the use cases.
- **The rule is executable.** Architectural drift is caught by the build instead of by code review.

### Costs

- **More types and mapping.** A request goes through DTO → domain model → JPA entity and back, with a mapper at each
  boundary, plus one interface per port. For three or four operations this is more code than a layered design.
- **Indirection.** Following a call means jumping through an interface to its single implementation.
- **Pragmatic exception.** `ShortUrlService` still uses Spring's `@Service` and `@Transactional`. This keeps transaction
  handling simple; the ArchUnit rules allow these two stereotypes but forbid web, persistence and serialization
  frameworks in the application layer.

### When this would not be worth it

If the service were to stay a throw-away CRUD with a single database and no further evolution, the extra structure would
not pay for itself and a layered design would be the better choice. The decision is justified here by the open
variation points above and by the goal of showing the technique, not by the size of the problem.

## Alternatives considered

- **Classic layered architecture (controller → service → repository).** Simpler and faster to write, but the service
  would depend directly on the JPA repository and entity, making the business rules harder to test in isolation and the
  storage harder to replace.
- **Layered architecture with a single shared entity.** The previous state of this project (one class serving as JPA
  entity and domain model). It worked, but coupled the business model to the persistence mapping.
