/**
 * Placeholder AuthApi
 *
 * This will be auto-generated from the OpenAPI spec
 */

import type { AxiosInstance, AxiosPromise } from 'axios'
import type { Configuration } from '../configuration'
import type { LoginRequest, RegisterRequest, RefreshRequest, TokenResponse } from '../models'

export class AuthApi {
  protected basePath = 'http://localhost:8080'
  protected axios: AxiosInstance

  constructor(configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    this.basePath = basePath || configuration?.basePath || this.basePath
    // Use provided axios or create default
    this.axios = axios || require('axios').default
  }

  public login(loginRequest: LoginRequest): AxiosPromise<TokenResponse> {
    return this.axios.post(`${this.basePath}/api/auth/login`, loginRequest)
  }

  public register(registerRequest: RegisterRequest): AxiosPromise<TokenResponse> {
    return this.axios.post(`${this.basePath}/api/auth/register`, registerRequest)
  }

  public refresh(refreshRequest: RefreshRequest): AxiosPromise<TokenResponse> {
    return this.axios.post(`${this.basePath}/api/auth/refresh`, refreshRequest)
  }
}
