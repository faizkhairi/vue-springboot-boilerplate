# vue-springboot-boilerplate — AI Development Guide

## Overview

Monorepo: Spring Boot 3.3 (Java 21) backend + Vue 3 + Vite frontend. JWT auth (jjwt), PostgreSQL, Flyway, Spring Mail + Thymeleaf, springdoc-openapi. Frontend: Pinia, Vue Router, Shadcn-vue + Tailwind.

## Quick Start

```bash
# Backend
cd backend && ./gradlew bootRun
# Set DATABASE_URL if not using default localhost PostgreSQL

# Frontend
cd frontend && pnpm install && pnpm dev

# Docker (optional)
cd docker && docker compose up -d
```

- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:5173
- Mailpit: http://localhost:8025

## Architecture

- **backend**: Gradle, Java 21. SecurityConfig (JWT filter), User entity, UserRepository, AuthController (register, login, refresh). JwtService issues access + refresh tokens (type claim). EmailService + Thymeleaf (welcome, password-reset, verify-email). Flyway in `db/migration/`. JWT secret via `app.jwt.secret` (min 256 bits).
- **frontend**: Vite, Vue 3, Pinia, Vue Router. Auth store; API client (Axios) with Bearer and 401 → refresh then retry. Login/Register/Dashboard. Vitest (unit), Playwright (e2e in `e2e/`).
- **docker**: PostgreSQL + Mailpit; optional backend + frontend services (Dockerfiles in backend/ and frontend/).

## Conventions

- **Backend**: REST under `/api/*`. Auth: `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh` (body: refreshToken). TokenResponse has accessToken, refreshToken. Use `@Valid` and DTOs for validation.
- **Frontend**: `VITE_API_BASE_URL` for API base. Store tokens in memory or secure storage; send `Authorization: Bearer <accessToken>`.
- **DB**: Flyway for migrations; do not use `ddl-auto: create`.

## Key Files

- backend: `BoilerplateApplication.java`, `SecurityConfig.java`, `JwtService.java`, `AuthController.java`, `User.java`, `V1__create_users.sql`
- frontend: `main.ts`, router, stores, views (Login, Register, Dashboard)
