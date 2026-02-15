import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth'

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('starts with null tokens', () => {
    const store = useAuthStore()
    expect(store.accessToken).toBeNull()
    expect(store.refreshToken).toBeNull()
  })

  it('setTokens stores access and refresh', () => {
    const store = useAuthStore()
    store.setTokens('access-1', 'refresh-1')
    expect(store.accessToken).toBe('access-1')
    expect(store.refreshToken).toBe('refresh-1')
  })

  it('clearTokens resets to null', () => {
    const store = useAuthStore()
    store.setTokens('a', 'r')
    store.clearTokens()
    expect(store.accessToken).toBeNull()
    expect(store.refreshToken).toBeNull()
  })
})
