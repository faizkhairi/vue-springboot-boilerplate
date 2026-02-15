<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { api } from '../services/api'

const router = useRouter()
const auth = useAuthStore()
const name = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  if (password.value.length < 8) {
    error.value = 'Password must be at least 8 characters.'
    return
  }
  loading.value = true
  try {
    const { data } = await api.post<{ accessToken: string; refreshToken: string }>('/api/auth/register', {
      name: name.value,
      email: email.value,
      password: password.value,
    })
    auth.setTokens(data.accessToken, data.refreshToken)
    router.push('/dashboard')
  } catch (e: unknown) {
    const err = e as { response?: { status: number } }
    error.value = err.response?.status === 400 ? 'Email may already be in use.' : 'Registration failed.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 p-4">
    <div class="w-full max-w-md rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      <h1 class="text-2xl font-bold text-gray-900">Create an account</h1>
      <p class="text-gray-600 mt-1">Enter your details to register.</p>
      <form @submit.prevent="submit" class="mt-4 space-y-4">
        <p v-if="error" class="text-sm text-red-600" role="alert">{{ error }}</p>
        <div>
          <label for="name" class="block text-sm font-medium text-gray-700">Name</label>
          <input
            id="name"
            v-model="name"
            type="text"
            required
            class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-gray-500 focus:outline-none focus:ring-1 focus:ring-gray-500"
            placeholder="Your name"
          />
        </div>
        <div>
          <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
          <input
            id="email"
            v-model="email"
            type="email"
            required
            class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-gray-500 focus:outline-none focus:ring-1 focus:ring-gray-500"
            placeholder="you@example.com"
          />
        </div>
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700">Password</label>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-gray-500 focus:outline-none focus:ring-1 focus:ring-gray-500"
            placeholder="At least 8 characters"
          />
        </div>
        <button
          type="submit"
          :disabled="loading"
          class="w-full rounded-md bg-gray-900 py-2 text-sm font-medium text-white hover:bg-gray-800 disabled:opacity-50"
        >
          {{ loading ? 'Creating account…' : 'Register' }}
        </button>
        <p class="text-center text-sm text-gray-600">
          Already have an account? <router-link to="/login" class="font-medium text-gray-900 underline">Sign in</router-link>
        </p>
      </form>
    </div>
  </div>
</template>
