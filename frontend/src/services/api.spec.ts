import { describe, it, expect, beforeEach, vi, type Mock } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

type Fn = (...args: unknown[]) => unknown
type MockedAxiosInstance = Mock & {
  post: Mock
  interceptors: {
    request: { use: (fulfilled: Fn, rejected?: Fn) => void }
    response: { use: (fulfilled: Fn, rejected?: Fn) => void }
  }
}

const { requestHandlers, responseHandlers, instanceFn, postMock } = vi.hoisted(() => {
  const requestHandlers: { fulfilled: Fn; rejected?: Fn }[] = []
  const responseHandlers: { fulfilled: Fn; rejected?: Fn }[] = []
  const postMock = vi.fn()
  const instanceFn: MockedAxiosInstance = Object.assign(vi.fn(), {
    post: postMock,
    interceptors: {
      request: {
        use: (fulfilled: Fn, rejected?: Fn) => requestHandlers.push({ fulfilled, rejected }),
      },
      response: {
        use: (fulfilled: Fn, rejected?: Fn) => responseHandlers.push({ fulfilled, rejected }),
      },
    },
  })
  return { requestHandlers, responseHandlers, instanceFn, postMock }
})

vi.mock('axios', () => ({
  default: { create: () => instanceFn },
}))

import { useAuthStore } from '../stores/auth'
import '../services/api'

interface FakeConfig {
  url?: string
  headers: Record<string, string>
  _retry?: boolean
}

function makeError(status: number, config: FakeConfig) {
  return { response: { status }, config }
}

describe('api client wrapper', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    instanceFn.mockReset()
    postMock.mockReset()
    Object.defineProperty(window, 'location', {
      value: { href: '' },
      writable: true,
      configurable: true,
    })
  })

  it('registers exactly one request and one response interceptor', () => {
    expect(requestHandlers).toHaveLength(1)
    expect(responseHandlers).toHaveLength(1)
  })

  it('attaches the bearer token to requests when authenticated', () => {
    const auth = useAuthStore()
    auth.setTokens('access-token', 'refresh-token')
    const config = { headers: {} as Record<string, string> }
    const result = requestHandlers[0]!.fulfilled(config) as FakeConfig
    expect(result.headers.Authorization).toBe('Bearer access-token')
  })

  it('leaves requests untouched when unauthenticated', () => {
    const config = { headers: {} as Record<string, string> }
    const result = requestHandlers[0]!.fulfilled(config) as FakeConfig
    expect(result.headers.Authorization).toBeUndefined()
  })

  it('passes through non-401 errors unchanged', async () => {
    const err = makeError(500, { headers: {} })
    await expect(responseHandlers[0]!.rejected!(err)).rejects.toBe(err)
  })

  it('clears tokens and redirects when the refresh request itself returns 401', async () => {
    const auth = useAuthStore()
    auth.setTokens('access-token', 'refresh-token')
    const err = makeError(401, { url: '/api/auth/refresh', headers: {} })

    await expect(responseHandlers[0]!.rejected!(err)).rejects.toBe(err)
    expect(auth.accessToken).toBeNull()
    expect(window.location.href).toBe('/login')
  })

  it('clears tokens and redirects when a retried request still 401s', async () => {
    const auth = useAuthStore()
    auth.setTokens('access-token', 'refresh-token')
    const err = makeError(401, { headers: {}, _retry: true })

    await expect(responseHandlers[0]!.rejected!(err)).rejects.toBe(err)
    expect(auth.accessToken).toBeNull()
    expect(window.location.href).toBe('/login')
  })

  it('clears tokens and redirects when there is no refresh token available', async () => {
    const err = makeError(401, { headers: {} })

    await expect(responseHandlers[0]!.rejected!(err)).rejects.toBe(err)
    expect(window.location.href).toBe('/login')
  })

  it('refreshes the token and retries the original request on 401', async () => {
    const auth = useAuthStore()
    auth.setTokens('old-access', 'old-refresh')
    postMock.mockResolvedValueOnce({
      data: { accessToken: 'new-access', refreshToken: 'new-refresh' },
    })
    instanceFn.mockResolvedValueOnce({ data: 'retried-response' })

    const originalRequest: FakeConfig = { headers: {} }
    const err = makeError(401, originalRequest)

    const result = await responseHandlers[0]!.rejected!(err)

    expect(postMock).toHaveBeenCalledWith('/api/auth/refresh', { refreshToken: 'old-refresh' })
    expect(auth.accessToken).toBe('new-access')
    expect(auth.refreshToken).toBe('new-refresh')
    expect(originalRequest.headers.Authorization).toBe('Bearer new-access')
    expect(originalRequest._retry).toBe(true)
    expect(instanceFn).toHaveBeenCalledWith(originalRequest)
    expect(result).toEqual({ data: 'retried-response' })
  })

  it('clears tokens and redirects when the refresh call fails', async () => {
    const auth = useAuthStore()
    auth.setTokens('old-access', 'old-refresh')
    postMock.mockRejectedValueOnce(new Error('refresh failed'))

    const originalRequest: FakeConfig = { headers: {} }
    const err = makeError(401, originalRequest)

    await expect(responseHandlers[0]!.rejected!(err)).rejects.toBe(err)
    expect(auth.accessToken).toBeNull()
    expect(window.location.href).toBe('/login')
  })
})
