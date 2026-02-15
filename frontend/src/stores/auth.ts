import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)

  function setTokens(access: string, refresh: string) {
    accessToken.value = access
    refreshToken.value = refresh
  }

  function clearTokens() {
    accessToken.value = null
    refreshToken.value = null
  }

  return { accessToken, refreshToken, setTokens, clearTokens }
})
