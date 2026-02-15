<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

interface CardDescriptionProps {
  class?: HTMLAttributes['class']
}

const props = defineProps<CardDescriptionProps>()

const computedClass = computed(() =>
  cn('text-sm text-muted-foreground', props.class)
)
</script>

<template>
  <p :class="computedClass">
    <slot />
  </p>
</template>
