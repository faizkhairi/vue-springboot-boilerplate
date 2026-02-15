import axios, { type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '../stores/auth'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (err.response?.status !== 401) {
      return Promise.reject(err)
    }

    const isRefreshRequest = originalRequest.url?.includes('/api/auth/refresh')
    if (isRefreshRequest || originalRequest._retry) {
      const auth = useAuthStore()
      auth.clearTokens()
      window.location.href = '/login'
      return Promise.reject(err)
    }

    const auth = useAuthStore()
    if (!auth.refreshToken) {
      auth.clearTokens()
      window.location.href = '/login'
      return Promise.reject(err)
    }

    originalRequest._retry = true
    try {
      const res = await api.post<{ accessToken: string; refreshToken: string }>('/api/auth/refresh', {
        refreshToken: auth.refreshToken,
      })
      const { accessToken, refreshToken } = res.data
      auth.setTokens(accessToken, refreshToken)
      originalRequest.headers.Authorization = `Bearer ${accessToken}`
      return api(originalRequest)
    } catch {
      const authStore = useAuthStore()
      authStore.clearTokens()
      window.location.href = '/login'
      return Promise.reject(err)
    }
  }
)
