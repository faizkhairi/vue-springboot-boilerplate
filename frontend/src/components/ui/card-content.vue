<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

interface CardContentProps {
  class?: HTMLAttributes['class']
}

const props = defineProps<CardContentProps>()

const computedClass = computed(() =>
  cn('p-6 pt-0', props.class)
)
</script>

<template>
  <div :class="computedClass">
    <slot />
  </div>
</template>
