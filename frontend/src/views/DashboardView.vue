<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

function signOut() {
  auth.clearTokens()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <header class="border-b border-gray-200 bg-white">
      <div class="mx-auto flex h-14 max-w-7xl items-center justify-between px-4">
        <h1 class="text-lg font-semibold text-gray-900">Dashboard</h1>
        <button
          type="button"
          @click="signOut"
          class="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          Sign out
        </button>
      </div>
    </header>
    <main class="mx-auto max-w-7xl px-4 py-8">
      <div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h2 class="text-xl font-semibold text-gray-900">Welcome</h2>
        <p class="mt-2 text-gray-600">You are signed in. Use the header to sign out.</p>
      </div>
    </main>
  </div>
</template>
