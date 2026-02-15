/**
 * API Client Package
 *
 * Auto-generated TypeScript Axios client from Spring Boot OpenAPI spec
 *
 * ## Usage
 *
 * ```ts
 * import { AuthApi, Configuration } from '@repo/api-client'
 *
 * const config = new Configuration({
 *   basePath: import.meta.env.VITE_API_BASE_URL
 * })
 *
 * const authApi = new AuthApi(config)
 * const response = await authApi.login({ email, password })
 * ```
 *
 * ## Regenerating Client
 *
 * 1. Start backend: `cd backend && ./gradlew bootRun`
 * 2. Generate OpenAPI spec: `cd backend && ./gradlew generateOpenApiDocs`
 * 3. Generate TypeScript client: `cd packages/api-client && npm run generate`
 */

export * from './generated/api'
export * from './generated/models'
export { Configuration } from './generated/configuration'
