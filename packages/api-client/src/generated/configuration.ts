/**
 * Auto-generated configuration
 *
 * This file will be replaced when running `npm run generate`
 * For now, this is a placeholder to allow imports without errors
 */

export interface ConfigurationParameters {
  basePath?: string
  baseOptions?: any
  accessToken?: string | Promise<string> | ((name?: string, scopes?: string[]) => string) | ((name?: string, scopes?: string[]) => Promise<string>)
}

export class Configuration {
  basePath?: string
  baseOptions?: any
  accessToken?: string | Promise<string> | ((name?: string, scopes?: string[]) => string) | ((name?: string, scopes?: string[]) => Promise<string>)

  constructor(param: ConfigurationParameters = {}) {
    this.basePath = param.basePath
    this.baseOptions = param.baseOptions
    this.accessToken = param.accessToken
  }
}
