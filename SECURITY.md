# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please report it responsibly.

**Do NOT open a public GitHub issue for security vulnerabilities.**

Instead, please email **ifaizkhairi@gmail.com** with:

1. A description of the vulnerability
2. Steps to reproduce the issue
3. Any potential impact

You will receive acknowledgment within 48 hours and a detailed response within 5 business days.

## Supported Versions

| Version | Supported |
|---------|-----------|
| Latest  | Yes       |

## Security Best Practices

When using this boilerplate, ensure you:

- Never commit `.env` files or secrets to version control
- Generate a strong `JWT_SECRET` (minimum 256-bit / 32 bytes)
- Use HTTPS in production
- Keep dependencies updated (`./gradlew dependencyUpdates`, `pnpm audit`)
- Validate all user inputs with `@Valid` annotations (backend) and Zod schemas (frontend)
- Use Flyway migrations for all schema changes (never `ddl-auto: create`)
- Configure CORS appropriately for your deployment environment
