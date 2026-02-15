/**
 * Auto-generated model exports
 *
 * This file will be replaced when running `npm run generate`
 */

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
}

export interface RefreshRequest {
  refreshToken: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
}
