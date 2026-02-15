# @repo/api-client

Auto-generated TypeScript Axios client from Spring Boot OpenAPI specification.

## Contract-First Design

1. **Backend generates OpenAPI spec** → Spring Boot with springdoc-openapi generates `openapi.json`
2. **openapi-generator-cli produces TypeScript client** → Generates Axios-based TypeScript API client
3. **Frontend imports typed API client** → Zero manual typing, type-safe API calls

## Setup

```bash
cd packages/api-client
npm install
```

## Generate Client

### Prerequisites

1. Backend must be running or have generated the OpenAPI spec:
   ```bash
   cd backend
   ./gradlew bootRun
   # OpenAPI spec available at http://localhost:8080/v3/api-docs
   ```

2. Or generate the spec without running the server:
   ```bash
   cd backend
   ./gradlew generateOpenApiDocs
   # Creates backend/build/openapi.json
   ```

### Generate TypeScript Client

```bash
cd packages/api-client
npm run generate
```

This will:
- Read `backend/build/openapi.json`
- Generate TypeScript interfaces and Axios API classes in `src/generated/`

## Usage in Frontend

```ts
import { AuthApi, Configuration } from '@repo/api-client'
import type { LoginRequest, TokenResponse } from '@repo/api-client'

// Configure base URL
const config = new Configuration({
  basePath: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
})

// Create API instance
const authApi = new AuthApi(config)

// Make type-safe API calls
const response = await authApi.login({
  email: 'user@example.com',
  password: 'password123'
})

const tokens: TokenResponse = response.data
```

## Workflow

1. **Add new endpoint in Spring Boot**
   ```java
   @PostMapping("/api/users")
   public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request) {
     // implementation
   }
   ```

2. **Regenerate OpenAPI spec**
   ```bash
   cd backend
   ./gradlew generateOpenApiDocs
   ```

3. **Regenerate TypeScript client**
   ```bash
   cd packages/api-client
   npm run generate
   ```

4. **Use in frontend with full type safety**
   ```ts
   import { UsersApi } from '@repo/api-client'
   const usersApi = new UsersApi(config)
   await usersApi.createUser({ name: 'John', email: 'john@example.com' })
   ```

## Benefits

- ✅ **Zero manual typing** — DTOs auto-generated from backend
- ✅ **Type safety** — Catch API contract mismatches at compile time
- ✅ **Single source of truth** — Backend defines the contract
- ✅ **Automatic updates** — Regenerate when backend changes
