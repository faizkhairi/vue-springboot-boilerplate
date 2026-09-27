import { describe, it, expect, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import router from './index'
import { useAuthStore } from '../stores/auth'

describe('router guards', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    await router.push('/')
    await router.isReady()
  })

  it('redirects to login when a protected route is visited without a token', async () => {
    await router.push('/dashboard')
    expect(router.currentRoute.value.name).toBe('Login')
  })

  it('allows an authenticated user to reach a protected route', async () => {
    const auth = useAuthStore()
    auth.setTokens('access-token', 'refresh-token')
    await router.push('/dashboard')
    expect(router.currentRoute.value.name).toBe('Dashboard')
  })

  it('redirects an authenticated user away from guest-only routes', async () => {
    const auth = useAuthStore()
    auth.setTokens('access-token', 'refresh-token')
    await router.push('/login')
    expect(router.currentRoute.value.name).toBe('Dashboard')
  })

  it('allows an unauthenticated user to reach guest-only routes', async () => {
    await router.push('/register')
    expect(router.currentRoute.value.name).toBe('Register')
  })
})
