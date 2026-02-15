<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

interface CardHeaderProps {
  class?: HTMLAttributes['class']
}

const props = defineProps<CardHeaderProps>()

const computedClass = computed(() =>
  cn('flex flex-col space-y-1.5 p-6', props.class)
)
</script>

<template>
  <div :class="computedClass">
    <slot />
  </div>
</template>
