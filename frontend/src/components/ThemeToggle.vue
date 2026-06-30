<template>
  <Button
      :aria-label="`Design umschalten, aktuell: ${label}`"
      :icon="icon"
      :title="`Design: ${label} (klicken zum Wechseln)`"
      rounded
      severity="secondary"
      text
      type="button"
      @click="theme.cycle()"
  />
</template>

<script setup>
import {computed} from 'vue'
import Button from 'primevue/button'
import {useThemeStore} from '@/stores/theme'

const theme = useThemeStore()

const labels = {system: 'System', light: 'Hell', dark: 'Dunkel'}
const icons = {system: 'pi pi-desktop', light: 'pi pi-sun', dark: 'pi pi-moon'}

const label = computed(() => labels[theme.mode])
const icon = computed(() => icons[theme.mode])
</script>
