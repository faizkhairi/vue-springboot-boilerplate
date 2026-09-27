# Contributing

Thanks for considering a contribution to vue-springboot-boilerplate.

## Setup

```bash
cp .env.example .env
cd docker && docker compose up -d postgres mailpit && cd ..

# Backend (needs JWT_SECRET, at least 32 bytes)
cd backend && export JWT_SECRET=$(openssl rand -base64 48) && ./gradlew bootRun

# Frontend, in a second terminal
cd frontend && npm ci && npm run dev
```

## Branching and commits

- Branch off `main`.
- Use [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `chore:`, `docs:`, `ci:`, ...) for commit messages and PR titles.
- Keep PRs focused on one change.

## Before opening a PR

Run the same checks CI runs, in the same order it runs them:

```bash
# Backend
cd backend
./gradlew spotlessCheck
./gradlew compileJava compileTestJava
./gradlew test jacocoTestCoverageVerification
./gradlew build

# Frontend
cd frontend
npm run lint
npm run typecheck
npm test
npm run build
```

- Coverage thresholds (`backend/build.gradle.kts`, `frontend/vitest.config.ts`) are a floor: they may only go up. Do not lower a threshold to make a failing suite pass, add tests instead.
- If your Java changes are not formatted, run `./gradlew spotlessApply` before committing.
- Do not use an em dash (U+2014) or ` -- ` as a dash in any file, including commit messages and PR text. CI's `check-dashes.sh` script fails the `lint` job on either. Use a period, colon, comma, or parentheses instead.
- If you touched `frontend/e2e/`, also run the e2e suite locally: `cd frontend && npm run test:e2e`. `e2e/session.spec.ts` needs the backend running on `:8080` first.
- If you changed a backend dependency, refresh the lockfile: `cd backend && ./gradlew dependencies --write-locks`, and commit the updated `gradle.lockfile`.

## Reporting a security issue

Do not open a public issue for a vulnerability. Follow the private reporting process in [SECURITY.md](SECURITY.md) instead.
