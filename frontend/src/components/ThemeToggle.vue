<template>
  <button
      type="button"
      :class="buttonClass"
      :title="`Design: ${label} (klicken zum Wechseln)`"
      :aria-label="`Design umschalten, aktuell: ${label}`"
      @click="theme.cycle()"
  >
    <!-- System -->
    <svg v-if="theme.mode === 'system'" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
            stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
    </svg>
    <!-- Light -->
    <svg v-else-if="theme.mode === 'light'" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
            stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
    </svg>
    <!-- Dark -->
    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
            stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
    </svg>
  </button>
</template>

<script setup>
import {computed} from 'vue'
import {useThemeStore} from '@/stores/theme'

const props = defineProps({
  // 'nav' for the teal header, 'surface' for cards / login
  variant: {type: String, default: 'surface'}
})

const theme = useThemeStore()

const labels = {system: 'System', light: 'Hell', dark: 'Dunkel'}
const label = computed(() => labels[theme.mode])

const buttonClass = computed(() => [
  'p-2 rounded-lg transition-colors duration-200',
  props.variant === 'nav'
      ? 'text-primary-100 hover:bg-primary-700 hover:text-white'
      : 'text-surface-500 hover:bg-surface-100 hover:text-surface-800'
])
</script>
