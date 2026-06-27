# VOLZEN — EV Charging, Anytime

> Airbnb for EV charging. Connect drivers with homeowners who have idle charging stations.

---

## Problem Statement

EV adoption in Vietnam is accelerating, but public charging infrastructure hasn't kept up. Drivers face range anxiety in dense urban areas like Ho Chi Minh City, while thousands of homeowners with private Type 2, CCS, or CHAdeMO chargers sit idle for most of the day. There is no platform that bridges this gap — no way for a driver to discover, book, and navigate to a private charger nearby.

## Solution Overview

VOLZEN is a two-sided marketplace that lets EV drivers find and book private charging slots from homeowner hosts. Drivers open the map, filter by connector type and price, book a time slot, and get turn-by-turn directions to the host's address. Hosts list their station, set availability, and earn passive income. An AI assistant handles support queries so the team doesn't need a support team on day one.

## Features

- **Interactive map** — discover nearby charging stations with live distance sorting
- **Provider profiles** — photos, amenities, connector specs, reviews, and real-time availability
- **Booking flow** — date/time picker, upfront pricing, order confirmation
- **Host dashboard** — manage spots, view bookings, track earnings and analytics
- **Provider onboarding** — guided 3-phase chatbot onboarding with Vietnam business registration form
- **AI support chat** — GPT-powered help assistant at `/help`
- **Light/dark theme** — persisted across sessions

---

## Prerequisites

| Tool | Version |
|---|---|
| Node.js | 20+ |
| Java | 17+ |
| Maven wrapper | included (`./mvnw`) |
| OpenAI API key | for the AI chat feature |

---

## Setup

**1. Clone the repo**

```bash
git clone <repo-url>
cd venus
```

**2. Install frontend dependencies**

```bash
cd frontend
npm install
```

**3. Configure the backend AI key**

Create `server/.env`:

```bash
OPENAI_API_KEY=sk-...
```

No other configuration is required for local development. The backend uses an H2 in-memory database and seeds demo data automatically on startup.

---

## Running Locally

### VS Code Run And Debug

This repo includes checked-in VS Code launch/tasks config under `.vscode/`. After installing frontend dependencies and creating `server/.env`, you can open the Run and Debug panel in VS Code and start:

- `Venus Full Stack` for backend + frontend with the default AI setup
- `Venus Full Stack - OpenAI` for backend + frontend using OpenAI-compatible config

Make sure Java 17+, Node.js 20+, and `OPENAI_API_KEY` in `server/.env` are set up before using the OpenAI debug profile.

**Backend** (runs on port 8080)

```bash
cd server
set -a && source .env && set +a
./mvnw spring-boot:run
```

**Frontend** (runs on port 3000)

```bash
cd frontend
npm run dev
```

Open [http://localhost:3000](http://localhost:3000).

**Verify the backend is up:**

```bash
curl http://localhost:8080/api/health
```

**Verify AI is connected:**

```bash
curl http://localhost:8080/api/ai/status
# "status": "Connected" confirms OpenAI is live
```

If the OpenAI key is missing the backend falls back to mock mode — all features work except the `/help` chatbot.

---

## User Guide

### As a Driver

1. **Land on the homepage** → click **Get started** to sign up or **Sign in**
2. **Explore** → the map view shows all nearby stations; click a pin or card to open the provider profile
3. **Book** → choose a date and time slot on the provider detail page, confirm the order
4. **Navigate** → after booking, the route page gives turn-by-turn directions to the charger
5. **Get help** → click **Help** in the nav to chat with the AI assistant

### As a Host

1. **Sign up** and select the **Provider** role
2. Complete the **Provider Onboarding** chatbot (3 phases: business info → station details → Vietnam Biz Reg form)
3. Access the **Host Dashboard** at `/host/spots` to manage your listings
4. View incoming bookings at `/host/bookings` and earnings at `/host/financial`

### H2 Database Console (dev only)

```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:venus
User:     username
Password: password
```

### Demo Seed Data

The backend seeds demo data on every fresh startup. Key IDs:

| Type | ID |
|---|---|
| Driver | `usr_demo_driver` |
| Vehicle | `veh_demo_vf8` |
| Stations | `pvd_p1`, `pvd_p2`, `pvd_p3` |
| Orders | `ord_demo_confirmed`, `ord_demo_completed_1` |

---

## Tech Stack

### Frontend

| | |
|---|---|
| Framework | Next.js 16 (App Router, React 19) |
| Styling | Tailwind v4 (CSS-first, no config file) |
| Components | ShadCN/ui |
| Maps | Leaflet (dynamic import, SSR-disabled) |
| Fonts | DM Sans + Instrument Serif (Google Fonts) |
| Theme | CSS custom properties, `data-theme` toggle, localStorage persistence |

### Backend

| | |
|---|---|
| Runtime | Java 17 + Spring Boot |
| API | Spring Web MVC (REST) |
| Database | H2 in-memory (JPA / Hibernate) |
| AI | OpenAI-compatible client; Ollama fallback; mock fallback |
| Build | Maven wrapper (`./mvnw`) |

### Architecture Notes

```
frontend/          Next.js app (App Router)
  app/             Pages and routes
  components/      Shared UI components
  lib/             API helpers and mock data

server/
  src/main/java/com/app/venus/
    modules/       Feature modules (ai, advisor, order, provider, review)
      <module>/
        application/    Services and use cases
        domain/         Domain models
        infrastructure/ Persistence and external clients
        interfaces/     REST controllers and DTOs
    shared/        Cross-cutting: CORS, exception handling, response wrapper
  src/main/resources/
    application.properties
```

Each feature module is self-contained. New modules drop into `modules/<name>` and follow the same four-package layout. Shared utilities live in `shared/` only when genuinely reusable.

**Auth note:** Auth is stubbed for the MVP. All endpoints resolve the current user from a fixed demo identity (`usr_demo_driver`). Production auth would replace `DemoCurrentUserService` with JWT/session principal lookup.

### Seeded Demo Data

Seeding includes a demo driver, demo vehicle, Ho Chi Minh City provider stations, connector coverage, amenities, photo URLs, demo orders, and reviews for provider detail pages.

Important seeded IDs include:

```text
usr_demo_driver
veh_demo_vf8
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
app.ai.timeout-seconds=20
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
POST  /api/v1/advisor/chat
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

## Volzen Advisor

Volzen Advisor is a source-grounded EV charging platform consultant for the Vietnam MVP. It is not a generic chatbot. It should answer short practical questions about Volzen boundaries, charging-site readiness, host onboarding, connector fit, and proxy demand signals.

Advisor endpoint:

```text
POST /api/v1/advisor/chat
```

Example request:

```json
{
  "message": "Is District 7 promising for a charging location?",
  "locationContext": {
    "district": "District 7",
    "city": "Ho Chi Minh City"
  }
}
```

Example response shape:

```json
{
  "answer": "Short grounded answer.",
  "sourceIds": ["VOLZEN-LOCATION-SIGNALS-001"],
  "retrievedSources": [],
  "grounded": true,
  "needsProfessionalReview": true,
  "dataAsOf": "2026-06-28",
  "provider": "openai",
  "unsupportedReason": null
}
```

Advisor rules:

* Unsupported general chat returns `I don't have a verified answer for that yet.`
* Grounded answers must include `sourceIds`.
* Legal, electrical, safety, site-readiness, and demand/profitability answers should use `needsProfessionalReview=true` when review is needed.
* Location signals are proxy estimates only, not verified demand facts.
* Responses must not expose raw prompts, provider secrets, or hidden retrieved snippets.

Knowledge files live in:

```text
server/src/main/resources/advisor/knowledge/
```

Current formats are small curated markdown files with `id`, `sourceId`, `claimType`, `note`, and `Sources` metadata, plus `location-signals.json` for manually curated demo location signals. Every external legal/market claim needs source metadata, and internal assumptions must be labelled `internal/pilot`.

Advisor provider config:

```properties
app.advisor.provider=openai
app.advisor.openai.model=gpt-4.1-mini
app.advisor.openai.vector-store-id=
app.advisor.timeout-seconds=20
app.advisor.request-provider-override-enabled=false
```

OpenAI is the default advisor provider and reuses the server-side OpenAI-compatible setup. The API key is read only from the server environment through `OPENAI_API_KEY` or the configured generic AI key env var.

Ollama can be used for local/demo advisor runs by setting:

```properties
app.advisor.provider=ollama
app.ai.provider=ollama
app.ai.ollama.model=gemma3
```

Ollama advisor answers use local curated retrieval only. They should not be treated as live web knowledge.

Future frontend integration should add a chat bubble or advisor panel that calls only the backend endpoint. The frontend should send optional location context from the selected provider/site/district, display the short answer and source IDs, show a professional-review caution when `needsProfessionalReview=true`, and show unsupported fallback clearly. It should not show hidden prompts, raw retrieved snippets, provider internals, or call OpenAI directly.

Deferred production improvements:

* managed knowledge ingestion workflow
* richer source review process
* hosted vector-store administration
* analytics for unanswered questions
* real auth and per-user chat history

## Deferred MVP Work

The following work is intentionally deferred:

* real backend authentication with JWT/session handling
* replacing the fixed demo current user
* technician workflows
* real payment processing
* automatic order status transitions
* production database profile and geospatial indexing
