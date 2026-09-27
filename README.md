# Vue + Spring Boot Boilerplate

Full-stack monorepo template: Vue 3 + Vite frontend, Spring Boot backend. JWT auth, PostgreSQL, Flyway, springdoc OpenAPI, Docker Compose. No third-party SaaS accounts required.

## Features

- **Backend**: Spring Boot 4.1.1 (Java 25), Spring Security 7 with a stateless JWT filter, Spring Data JPA, Flyway migrations, Spring Mail + Thymeleaf email templates, springdoc-openapi (Swagger UI)
- **Frontend**: Vue 3, Vite, Pinia, Vue Router, Tailwind CSS 4, a small set of Shadcn-vue-style UI components (button, card); Axios API client
- **Security hardening**: CSP and other security headers on every response, a per-IP rate limiter on `/api/auth/**`, a required `JWT_SECRET` with a minimum length the app enforces at startup
- **Testing**: Backend JUnit 5 + Testcontainers (PostgreSQL) with a JaCoCo coverage floor; frontend Vitest (unit, with a coverage floor) and Playwright (e2e)
- **Docker**: Compose file for PostgreSQL + Mailpit, or the full stack (backend, frontend, PostgreSQL, Mailpit)
- **CI**: lint, typecheck, test, build (Node 22 and 24), dependency audit, secret scanning, and an end-to-end Playwright job, all in GitHub Actions
- **Dependabot**: automated dependency update pull requests for npm, Gradle, and GitHub Actions

## Quick Start

Create a new project from this template, either:

```bash
npx degit faizkhairi/vue-springboot-boilerplate my-app
```

or:

```bash
gh repo create my-app --template faizkhairi/vue-springboot-boilerplate --clone
```

or click **Use this template** on GitHub and clone the resulting repository.

### Prerequisites

- JDK 25 (Gradle itself comes from the committed wrapper, so no Gradle install is needed)
- Node.js 22.13+ (see `.nvmrc`; 24 is what this repo pins and what CI's default build job uses)
- Docker, for local PostgreSQL and Mailpit

### Run locally

```bash
cd my-app
cp .env.example .env
```

Export a JWT secret (at least 32 bytes). The backend needs it, and Docker Compose refuses to read the compose file without it, even when you only start the database:

```bash
export JWT_SECRET=$(openssl rand -base64 48)
```

Start PostgreSQL and Mailpit:

```bash
cd docker
docker compose up -d postgres mailpit
```

Run the backend in the same shell:

```bash
cd backend
./gradlew bootRun
```

Run the frontend:

```bash
cd frontend
npm ci
npm run dev
```

Open [http://localhost:5173](http://localhost:5173). Swagger UI is at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). Development emails land in Mailpit at [http://localhost:8025](http://localhost:8025).

## Environment Variables

All variables are documented with placeholders in `.env.example`.

| Variable | Required | Purpose |
|----------|----------|---------|
| `DATABASE_URL` | Yes | PostgreSQL JDBC connection string |
| `DATABASE_USER` | Yes | PostgreSQL username |
| `DATABASE_PASSWORD` | Yes | PostgreSQL password |
| `JWT_SECRET` | Yes | Signs JWTs; must be at least 32 bytes. The app fails fast at startup if it is missing or too short. Generate with `openssl rand -base64 48` |
| `SMTP_HOST` | Yes | SMTP server host (Mailpit locally, any provider in production) |
| `SMTP_PORT` | Yes | SMTP server port |
| `SMTP_USER` | Optional | SMTP username (empty for Mailpit) |
| `SMTP_PASS` | Optional | SMTP password (empty for Mailpit) |
| `SMTP_FROM` | Yes | Sender address for outgoing email |
| `APP_URL` | Yes | Base URL used in email links |
| `CORS_ALLOWED_ORIGINS` | Optional | Comma-separated list of origins allowed to call the API. Default: `http://localhost:5173,http://localhost:4173,http://localhost:3000` |
| `TRUSTED_PROXY_COUNT` | Optional | Reverse proxies in front of the API that append to `X-Forwarded-For` (default `1`); sets which entry the auth rate limiter trusts as the client IP |
| `VITE_API_BASE_URL` | Optional | Frontend API base URL. Empty means relative `/api`, which the Vite dev and preview servers proxy to `http://localhost:8080` |

## Scripts

**Backend** (run from `backend/`):

| Command | Description |
|---------|-------------|
| `./gradlew bootRun` | Run the API |
| `./gradlew build` | Compile, test, and package the API |
| `./gradlew test` | Run the test suite |
| `./gradlew spotlessCheck` / `./gradlew spotlessApply` | Check or apply Java formatting |
| `./gradlew jacocoTestCoverageVerification` | Enforce the coverage floor |
| `./gradlew dependencies --write-locks` | Refresh `gradle.lockfile` after a dependency change |

**Frontend** (run from `frontend/`):

| Command | Description |
|---------|-------------|
| `npm run dev` | Start the Vite dev server |
| `npm run build` | Type-check and build for production |
| `npm run preview` | Preview the production build |
| `npm run lint` | Run ESLint |
| `npm run typecheck` | Run `vue-tsc` |
| `npm test` | Run Vitest with coverage |
| `npm run test:e2e` | Run the Playwright e2e suite |

## Testing

### Backend

```bash
cd backend
./gradlew test jacocoTestCoverageVerification
```

JUnit 5 tests run against a real PostgreSQL container via Testcontainers. JaCoCo enforces a coverage floor of 70% line and 55% branch coverage (`backend/build.gradle.kts`); these are measured floors, not targets, so they only move up as coverage improves.

### Frontend

```bash
cd frontend
npm test
```

Vitest runs with a coverage floor enforced in `frontend/vitest.config.ts` (currently 30% lines and statements, 15% branches, 10% functions; also floors, not targets).

### End-to-end (Playwright)

```bash
cd frontend
npm run test:e2e
```

`e2e/session.spec.ts` needs the Spring Boot API running on `:8080` (registration and login), so start the backend first. The other specs do not call the API. Playwright's config starts the frontend preview server itself when one is not already running.

### Continuous Integration

`.github/workflows/ci.yml` runs on every push to `main` and every pull request:

| Job | What it does |
|-----|---------------|
| `lint` | `./gradlew spotlessCheck`, `npm run lint`, and a check for em dashes (`.github/scripts/check-dashes.sh`) |
| `typecheck` | `./gradlew compileJava compileTestJava`, `npm run typecheck` |
| `test` | Backend suite with Testcontainers and the JaCoCo floor, Vitest with its coverage floor |
| `build` | Backend jar and frontend build, on Node 22 and Node 24 |
| `audit` | `npm audit --audit-level=high`, and `osv-scanner` against `backend/gradle.lockfile` |
| `secrets` | gitleaks secret scan |
| `e2e` | Boots the API jar against a Postgres and Mailpit service container, builds the frontend, then runs Playwright against Chromium |

`.github/workflows/dependency-submission.yml` submits the Gradle dependency graph to GitHub's dependency graph, and `.github/dependabot.yml` opens automated update pull requests for npm, Gradle, and GitHub Actions.

## Project Structure

```
backend/                    # Spring Boot API (Java 25, Gradle)
├── src/main/java/          # Application code
├── src/main/resources/     # application.yml, Flyway migrations
├── src/test/               # JUnit 5 + Testcontainers tests
└── Dockerfile

frontend/                   # Vue 3 + Vite SPA
├── src/                    # Components, views, stores, router
├── e2e/                    # Playwright e2e specs
└── Dockerfile

packages/
└── api-client/             # Optional OpenAPI-generated TypeScript client (not wired into the frontend)

docker/
└── docker-compose.yml      # PostgreSQL, Mailpit, and optionally backend + frontend
```

## Security

- **Headers and CSP**: `backend/src/main/java/com/app/boilerplate/config/SecurityConfig.java` sets a Content-Security-Policy plus `X-Content-Type-Options`, `Referrer-Policy`, `X-Frame-Options: DENY`, `Permissions-Policy`, and HSTS on every response.
- **Rate limiting**: `RateLimitFilter` limits `/api/auth/**` to 5 requests per minute per client IP per path, responding `429` with a `Retry-After` header. It reads the client IP as the `X-Forwarded-For` entry `TRUSTED_PROXY_COUNT` hops from the right, not the spoofable leftmost entry. It is in-memory and per instance: a multi-instance deployment needs a shared store (for example Redis) to enforce one limit across instances.
- **JWT secret**: `JWT_SECRET` has no default and must be at least 32 bytes; the app fails fast at startup otherwise.
- **Actuator**: only `/actuator/health` is exposed. The mail health indicator is disabled on purpose, so an SMTP outage does not mark the API unhealthy.
- **Secret scanning**: gitleaks runs in CI on every push and pull request.
- **Dependency auditing**: `npm audit --audit-level=high` and `osv-scanner` against the locked Gradle dependency graph (`backend/gradle.lockfile`) run in CI; Dependabot opens update pull requests.
- **Dependency locking**: Gradle dependency locking is enabled (`backend/gradle.lockfile`). Refresh it after changing a dependency with `./gradlew dependencies --write-locks`.
- Report vulnerabilities as described in [SECURITY.md](SECURITY.md).

## Deployment

There is no bundled production deployment target or CD pipeline; this is a starting point, not a hosted service.

**Backend**: `backend/Dockerfile` builds the API with the committed Gradle wrapper on Java 25 and runs it as a non-root user. Build and run it with:

```bash
cd backend
docker build -t vue-springboot-backend .
docker run -p 8080:8080 -e JWT_SECRET=... -e DATABASE_URL=... vue-springboot-backend
```

**Frontend**: `frontend/Dockerfile` builds the Vite app and serves the static output with nginx. `VITE_API_BASE_URL` is a build argument, so it must be set at image build time, not at container start.

**Full stack locally**: `docker/docker-compose.yml` runs PostgreSQL, Mailpit, the backend, and the frontend together:

```bash
export JWT_SECRET=$(openssl rand -base64 48)
cd docker
docker compose up -d
```

Before deploying anywhere else: apply Flyway migrations against the target database (they run automatically on backend startup), set every environment variable listed above, and confirm `/actuator/health` returns `200` after the deploy.

## Tech Stack

| Layer | Technology | External Account? |
|-------|-----------|-------------------|
| Backend framework | Spring Boot 4.1.1 (Java 25) | No |
| Security | Spring Security 7, JWT (jjwt 0.13.0) | No |
| Database | PostgreSQL 16 + Flyway | No |
| API docs | springdoc-openapi 3.1.1 (Swagger UI) | No |
| Email | Spring Mail + Thymeleaf, Mailpit (dev) | No |
| Frontend framework | Vue 3, Vite 8 | No |
| State / routing | Pinia 4, Vue Router 5 | No |
| Styling | Tailwind CSS 4 | No |
| Testing | JUnit 5 + Testcontainers, JaCoCo, Vitest, Playwright | No |
| Build | Gradle 9.8.0 (wrapper), npm | No |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for local setup, the checks CI runs, and commit conventions.

## License

[MIT](LICENSE)

Author: Faiz Khairi ([faizkhairi.github.io](https://faizkhairi.github.io), [@faizkhairi](https://github.com/faizkhairi))
