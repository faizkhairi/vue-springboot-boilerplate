<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

interface LabelProps {
  for?: string
  class?: HTMLAttributes['class']
}

const props = defineProps<LabelProps>()

const computedClass = computed(() =>
  cn(
    'text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70',
    props.class
  )
)
</script>

<template>
  <label :for="for" :class="computedClass">
    <slot />
  </label>
</template>
