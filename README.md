# Spring Boot Hackathon Starter

A minimal reusable Spring Boot starter with H2, shared API infrastructure, static frontend assets, and a provider-swappable AI wrapper.

## What This Starter Includes

* Spring Boot web API with static frontend hosting.
* H2 in-memory database and H2 console for local development.
* Shared response wrapper and consistent error format.
* Global exception handling for validation, malformed JSON, app errors, AI provider errors, and unexpected failures.
* JPA auditing base class and auditing configuration.
* Generic AI module with Ollama, mock fallback, and OpenAI-compatible provider support.
* Minimal starter UI for health and AI wrapper testing.
* Clean module-oriented package structure for adding product features.

## Tech Stack

* Java 17
* Spring Boot
* Maven
* Spring Web MVC
* Spring Data JPA
* H2 Database
* Bean Validation
* Lombok
* Static HTML/CSS/JavaScript
* Ollama or OpenAI-compatible AI provider

## Project Structure

```text
frontend
├── index.html
├── app.js
└── styles.css

server
├── pom.xml
├── mvnw
├── src/main/java/com/app/venus
│   ├── VenusApplication.java
│   ├── modules
│   │   └── ai
│   │       ├── application
│   │       ├── infrastructure
│   │       └── interfaces
│   └── shared
│       ├── auditing
│       ├── exception
│       └── web
└── src/main/resources
    └── application.properties
```

The Maven build copies files from `frontend/` into Spring Boot static resources, so the backend still serves the starter UI at `/`.

## How To Run

```bash
cd server
./mvnw spring-boot:run
```

The app runs on:

```text
http://localhost:8080
```

If port 8080 is busy:

```bash
cd server
SERVER_PORT=8081 ./mvnw spring-boot:run
```

## MVP Backend Mode

This branch intentionally uses a no-auth backend for the MVP. Driver endpoints resolve the current user through `DemoCurrentUserService` instead of a JWT/session principal.

Fixed demo current user:

```text
usr_demo_driver
```

This is deliberate for hackathon/demo speed. Production auth is deferred and should replace `DemoCurrentUserService` with authenticated principal lookup.

## H2 Console

The H2 console is enabled for local development:

```text
http://localhost:8080/h2-console
```

Default connection:

```text
JDBC URL: jdbc:h2:mem:venus
User: username
Password: password
```

## Demo Seed Data

Demo data is enabled by default:

```properties
app.seed.demo-data=true
```

Fresh H2 startup seeds:

* demo driver `usr_demo_driver`
* demo vehicle `veh_demo_vf8`
* three provider station records in Ho Chi Minh City
* connector coverage for Type 2, CCS, and CHAdeMO
* amenities and photo URLs
* confirmed and completed demo orders
* reviews for provider detail pages

Important seeded IDs include:

```text
pvd_p1
pvd_p2
pvd_p3
ord_demo_confirmed
ord_demo_completed_1
ord_demo_completed_2
ord_demo_reviewable
```

Seeding is idempotent across restarts and can be disabled for tests or special local runs:

```bash
APP_SEED_DEMO_DATA=false ./mvnw spring-boot:run
```

## AI Provider Setup

The generic AI endpoint is:

```text
POST /api/ai
```

Request body:

```json
{
  "prompt": "Write a short product idea summary.",
  "systemPrompt": "Optional instruction for generation mode.",
  "mode": "generate"
}
```

Supported modes:

```text
generate
summarize
extract_structured
classify
```

## Ollama Setup

Ollama is the default provider:

```bash
ollama serve
ollama pull gemma3
```

Default config:

```properties
app.ai.provider=ollama
app.ai.default-text-model=gemma3
app.ai.ollama.model=gemma3
app.ai.ollama.base-url=http://localhost:11434
app.ai.timeout-seconds=60
app.ai.fallback-to-mock-on-error=true
```

## Mock Fallback Behavior

When `app.ai.fallback-to-mock-on-error=true`, the app returns a deterministic mock response if the configured provider is unavailable. This keeps local development usable without a running model.

To force mock mode:

```properties
app.ai.provider=mock
```

## OpenAI-Compatible Provider

Set the provider and configure a compatible chat completions endpoint:

```properties
app.ai.provider=openai
app.ai.openai.base-url=https://api.openai.com/v1
app.ai.openai.api-key-env=OPENAI_API_KEY
app.ai.openai.model=gpt-4.1-mini
```

The API key is read from the environment variable named by `app.ai.openai.api-key-env`.

If your key is stored in `server/.env`, load it before running the backend:

```bash
cd server
set -a
source .env
set +a
APP_AI_PROVIDER=openai ./mvnw spring-boot:run
```

## Current Generic Endpoints

```text
GET  /api/health
GET  /api/ai/status
POST /api/ai
```

All API responses use the shared response wrapper.

## Current MVP Product Endpoints

These endpoints are intentionally no-auth for now and operate as the fixed demo driver:

```text
GET   /api/v1/me
PATCH /api/v1/me
GET   /api/v1/me/vehicles
POST  /api/v1/me/vehicles
PATCH /api/v1/me/vehicles/{vehicleId}
DELETE /api/v1/me/vehicles/{vehicleId}

GET   /api/v1/providers
GET   /api/v1/providers/{providerId}
GET   /api/v1/providers/{providerId}/availability

POST  /api/v1/orders
GET   /api/v1/orders/{orderId}
GET   /api/v1/me/orders
PATCH /api/v1/orders/{orderId}/cancel
POST  /api/v1/orders/{orderId}/review
```

Order creation validates provider availability, vehicle ownership, connector compatibility, time range, and slot overlap. Overlapping booking attempts return `409 SLOT_UNAVAILABLE`.

## Frontend Contract Notes

The current Next.js frontend is still mock-driven and has not been wired to these backend APIs yet.

The frontend mock model uses UI-friendly field names:

```text
avatar
connectors
photos
reviews[].author
reviews[].avatar
reviews[].text
reviews[].date
```

The backend follows `API_SPEC.md` names:

```text
avatarUrl
connectorTypes
photoUrls
reviews[].authorName
reviews[].authorAvatarUrl
reviews[].comment
reviews[].createdAt
```

When frontend API integration starts, add a small adapter in the frontend or add harmless alias fields deliberately. No frontend API integration was implemented in this backend task.

## Deferred MVP Work

The following work is intentionally deferred:

* real backend authentication with JWT/session handling
* replacing the fixed demo current user
* provider dashboard APIs and provider-owned station management
* technician workflows
* real payment processing
* automatic order status transitions
* frontend API integration
* production database profile and geospatial indexing

## Building A Product Module

Add new product code under `server/src/main/java/com/app/venus/modules/<module-name>` and keep the same internal shape:

```text
application/     business services and use cases
domain/          domain models when useful
infrastructure/  persistence, clients, provider adapters
interfaces/      REST controllers and DTOs
```

Keep shared cross-cutting code in `shared` only when it is reusable across modules.

## Next Planned Product

EV Charging Space Rental for Grab Drivers.

Future modules may include:

* charging spaces
* reservations
* driver recommendations
* host pricing optimizer
* demand forecast

These modules are intentionally not implemented yet. This cleanup keeps the foundation ready for that pivot.
