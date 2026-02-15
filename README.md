# Vue + Spring Boot Boilerplate

Full-stack monorepo: Vue 3 + Vite frontend, Spring Boot 3.3 (Java 21) backend. JWT auth, PostgreSQL, Flyway, OpenAPI, Docker Compose.

## Features

- **Backend**: Spring Boot 3.3, Spring Security + JWT (access + refresh), Spring Data JPA, Flyway, Spring Mail + Thymeleaf, springdoc-openapi
- **Frontend**: Vue 3, Vite, Pinia, Vue Router, Tailwind; Axios with 401 refresh retry
- **Testing**: Backend JUnit 5 + Testcontainers (PostgreSQL); Frontend Vitest (unit), Playwright (e2e)
- **Docker**: PostgreSQL + Mailpit; optional backend + frontend Dockerfiles and Compose services

## Quick Start

### Backend

```bash
cd backend
# Ensure Java 21 and Gradle are installed, or use ./gradlew
./gradlew bootRun
# Or: export DATABASE_URL=jdbc:postgresql://localhost:5432/vue_springboot && ./gradlew bootRun
```

- API: http://localhost:8080  
- Swagger UI: http://localhost:8080/swagger-ui.html  

### Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

- App: http://localhost:5173  

### Docker (full stack or infra only)

**Option A — PostgreSQL + Mailpit only** (run backend/frontend on host):

```bash
cd docker
docker compose up -d postgres mailpit
# Backend: DATABASE_URL=jdbc:postgresql://localhost:5432/vue_springboot
# Mailpit UI: http://localhost:8025
```

**Option B — Full stack** (backend + frontend in containers):

```bash
cd docker
docker compose up -d
# Frontend: http://localhost (port 80)
# Backend API: http://localhost:8080
# Mailpit UI: http://localhost:8025
```

## Environment

**Backend** (application.yml / env):

- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`
- `JWT_SECRET` (min 256-bit for HS256)
- `SMTP_HOST`, `SMTP_PORT` (dev: localhost:1025 for Mailpit)
- `APP_URL`, `SMTP_FROM` (for email links and sender)

**Frontend**:

- `VITE_API_BASE_URL` — backend base URL (e.g. http://localhost:8080)

## Testing

- **Backend**: `cd backend && ./gradlew test` (JUnit 5 + Testcontainers PostgreSQL). CI runs build with `-x test`; run tests locally.
- **Frontend**: `cd frontend && npm run test` (Vitest), `npm run test:e2e` (Playwright; run dev server or set `PLAYWRIGHT_BASE_URL`).

## Project Structure

```
backend/     — Spring Boot (Java 21, Gradle), Dockerfile
frontend/    — Vue 3 + Vite, Pinia, Vue Router, Dockerfile, Vitest, Playwright
docker/      — docker-compose (PostgreSQL, Mailpit, optional backend + frontend)
```

## License

MIT
