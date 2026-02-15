import { defineStore } from 'pinia'
import { ref } from 'vue'
import { logger } from '@/utils/logger'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)

  function setTokens(access: string, refresh: string) {
    accessToken.value = access
    refreshToken.value = refresh
    logger.audit('AUTH_TOKENS_SET', { hasAccess: !!access, hasRefresh: !!refresh })
  }

  function clearTokens() {
    accessToken.value = null
    refreshToken.value = null
    logger.audit('AUTH_TOKENS_CLEARED', {})
  }

  return { accessToken, refreshToken, setTokens, clearTokens }
})
