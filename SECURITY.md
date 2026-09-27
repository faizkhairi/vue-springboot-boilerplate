# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please report it responsibly.

**Do NOT open a public GitHub issue for security vulnerabilities.**

Instead, email **ifaizkhairi@gmail.com** with:

1. A description of the vulnerability
2. Steps to reproduce the issue
3. Any potential impact

You will receive acknowledgment within 48 hours and a detailed response within 5 business days.

## Supported Versions

Only the latest commit on `main` is supported. There are no maintained release branches.

## Built-in Protections

This boilerplate ships with, and CI enforces:

- **Security headers and CSP** on every response (`backend/src/main/java/com/app/boilerplate/config/SecurityConfig.java`): Content-Security-Policy, `X-Content-Type-Options`, `Referrer-Policy`, `X-Frame-Options: DENY`, `Permissions-Policy`, and HSTS.
- **Rate limiting** on `/api/auth/**` (`RateLimitFilter`): 5 requests per minute per client IP per path, returning `429` with a `Retry-After` header. It keys on the `X-Forwarded-For` entry `TRUSTED_PROXY_COUNT` hops from the right, not the spoofable leftmost entry. It is in-memory and per instance: use a shared store (for example Redis) behind a load balancer with more than one instance.
- **Required JWT secret**: `JWT_SECRET` has no default and must be at least 32 bytes (256 bits); the app fails fast at startup if it is missing or too short.
- **Password hashing** with BCrypt (strength 12).
- **Actuator surface**: only `/actuator/health` is exposed. The mail health indicator is disabled on purpose, so an SMTP outage does not mark the API unhealthy.
- **Dependency locking**: Gradle dependency locking is enabled (`backend/gradle.lockfile`), pinning the resolved dependency graph.
- **Secret scanning**: gitleaks runs in CI on every push and pull request.
- **Dependency auditing**: `npm audit --audit-level=high` and `osv-scanner` against `backend/gradle.lockfile` run in CI, and Dependabot opens automated dependency update pull requests.

## Security Best Practices

When using this boilerplate, ensure you:

- Never commit `.env` files or secrets to version control
- Generate a strong `JWT_SECRET` (`openssl rand -base64 48`)
- Use HTTPS in production
- Keep dependencies updated (`npm audit`, `./gradlew dependencies --write-locks`, or let Dependabot open the PR)
- Swap the in-memory rate limiter for a shared store before scaling past one instance
- Set `TRUSTED_PROXY_COUNT` to match your actual reverse proxy chain
- Set `CORS_ALLOWED_ORIGINS` to only the origins your deployment actually serves
