# vue-springboot-boilerplate: AI Development Guide

## Overview

Monorepo: Spring Boot 4.1.1 (Java 25) backend + Vue 3 + Vite frontend. JWT auth (jjwt 0.13.0), PostgreSQL, Flyway, Spring Mail + Thymeleaf, springdoc-openapi. Frontend: Pinia, Vue Router, Tailwind CSS 4, a small set of Shadcn-vue-style UI components.

## Quick Start

```bash
# Backend (needs JWT_SECRET, at least 32 bytes)
cd backend && export JWT_SECRET=$(openssl rand -base64 48) && ./gradlew bootRun

# Frontend
cd frontend && npm ci && npm run dev

# Infra only (Postgres + Mailpit)
cd docker && docker compose up -d postgres mailpit
```

- Backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html (spec at /api-docs)
- Frontend: http://localhost:5173
- Mailpit: http://localhost:8025

## Architecture

- **backend**: Gradle 9.8.0 wrapper, Java 25 toolchain. `SecurityConfig` wires the JWT filter and `RateLimitFilter` (5 req/min per client IP per path on `/api/auth/**`, in-memory). `User` entity, `UserRepository`, auth controller for register/login/refresh. `JwtService` issues access and refresh tokens. Email via Spring Mail + Thymeleaf templates. Flyway migrations in `src/main/resources/db/migration/`. `JWT_SECRET` is required (no default) and must be at least 32 bytes; the app fails fast at startup otherwise.
- **frontend**: Vite, Vue 3, Pinia, Vue Router. Auth store; Axios client. Vitest (unit, coverage floor enforced), Playwright (e2e in `e2e/`; `session.spec.ts` needs the API on `:8080`).
- **docker**: `docker/docker-compose.yml` runs PostgreSQL + Mailpit, or the full stack (adds backend and nginx-served frontend).
- **packages/api-client**: an OpenAPI-generated TypeScript client. It is not imported by the frontend today; treat it as optional tooling, not a required build step.

## Conventions

- **Backend**: REST under `/api/*`. Auth endpoints: `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`. Use `@Valid` and DTOs for request validation. Spotless (`palantirJavaFormat`) enforces formatting; run `./gradlew spotlessApply` before committing.
- **Frontend**: `VITE_API_BASE_URL` is optional; empty means relative `/api`, proxied to `http://localhost:8080` by the Vite dev and preview servers.
- **DB**: Flyway for all schema changes. Never use `ddl-auto: create` (the app uses `ddl-auto: validate`).
- **No em dashes**: this is a public repo. `.github/scripts/check-dashes.sh` fails CI on an em dash (U+2014) or ` -- ` used as a dash in Markdown prose.

## Key Files

- backend: `SecurityConfig.java`, `RateLimitFilter.java`, `JwtService.java`, `GlobalExceptionHandler.java`, `ErrorResponse.java`, `application.yml`
- frontend: `main.ts`, `router/`, `stores/`, `vite.config.ts` (dev/preview `/api` proxy)

## Pre-Push Build Verification

Run the same checks CI runs before pushing:

```bash
# Backend
cd backend && ./gradlew spotlessCheck build

# Frontend
cd frontend && npm run lint && npm run typecheck && npm test && npm run build
```

**Path aliases**: the Vite production build (Rollup) does not read TypeScript `paths` from `tsconfig.app.json`. Path aliases like `@/` must be configured in both `tsconfig.app.json` and `vite.config.ts` (`resolve.alias`). Missing the Vite alias causes `Cannot resolve @/components/...` errors only during production builds.

## Shadcn-vue Component Pattern

Components use barrel exports. When importing sub-components:

```typescript
// Import from the index.ts barrel
import { Card, CardHeader, CardTitle } from '@/components/ui/card'
```

Each component directory has an `index.ts` that re-exports its sub-components.
