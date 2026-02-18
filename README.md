# Vue + Spring Boot Boilerplate

Full-stack monorepo: Vue 3 + Vite frontend, Spring Boot 3.3 (Java 21) backend. JWT auth, PostgreSQL, Flyway, OpenAPI, Docker Compose. Zero external account dependencies.

## Quick Create

```bash
# Create project from template
gh repo create my-app --template faizkhairi/vue-springboot-boilerplate --private --clone
cd my-app
```

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

## API Documentation

The backend uses **springdoc-openapi** to automatically generate OpenAPI 3.0 documentation from Spring annotations.

**Access Swagger UI:**

1. Start the backend: `cd backend && ./gradlew bootRun`
2. Navigate to: **http://localhost:8080/swagger-ui.html**
3. Explore all available endpoints with request/response schemas

**OpenAPI JSON Spec:**

- Available at: http://localhost:8080/v3/api-docs
- Use this spec for client code generation (e.g., TypeScript Axios client via `openapi-generator-cli`)

**Key Endpoints:**

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/login` | POST | Authenticate with email/password, returns JWT tokens |
| `/api/auth/register` | POST | Create new user account |
| `/api/auth/refresh` | POST | Refresh access token using refresh token |
| `/api/users/me` | GET | Get authenticated user profile |

**Authentication:**

Most endpoints require a valid JWT access token. In Swagger UI:

1. Use `/api/auth/login` to get tokens
2. Copy the `accessToken` from the response
3. Click "Authorize" button at the top
4. Enter: `Bearer {accessToken}`
5. All subsequent requests will include the token

## Logging

**Backend (SLF4J + Logback):**

- Logs are written to `logs/application.log` (rolling daily, 30-day retention)
- Audit events (auth, security) are logged to `logs/audit.log` (90-day retention)
- Log levels: DEBUG (dev), INFO (prod) — configured in `logback-spring.xml`

**Frontend (Structured Logger):**

- Development: logs to browser console with timestamps
- Production: logs can be sent to backend or third-party service (see `src/utils/logger.ts`)

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

[MIT](LICENSE)

## Author

**Faiz Khairi** — [faizkhairi.github.io](https://faizkhairi.github.io) — [@faizkhairi](https://github.com/faizkhairi)
