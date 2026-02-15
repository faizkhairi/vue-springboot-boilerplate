<template>
  <div class="min-h-screen flex items-center justify-center bg-background px-4">
    <Card class="max-w-md w-full">
      <CardHeader class="text-center">
        <CardTitle class="text-6xl font-bold">500</CardTitle>
        <CardDescription class="text-xl mt-2">Server Error</CardDescription>
      </CardHeader>
      <CardContent class="space-y-4">
        <p class="text-center text-muted-foreground">
          Something went wrong on our end. Our team has been notified and is working on a fix.
        </p>

        <div class="flex flex-col sm:flex-row gap-3">
          <Button class="flex-1" @click="refreshPage">
            Refresh Page
          </Button>
          <Button variant="outline" class="flex-1" @click="router.push('/')">
            Go Home
          </Button>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

const router = useRouter()

const refreshPage = () => {
  window.location.reload()
}
</script>
