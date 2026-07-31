<template>
  <Button
      :aria-label="label"
      :title="label"
      class="p-button-icon-only"
      rounded
      severity="secondary"
      text
      type="button"
      @click="theme.cycle()"
  >
    <Transition mode="out-in" name="icon-swap">
      <i :key="theme.mode" :class="icon"/>
    </Transition>
  </Button>
</template>

<script setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import Button from 'primevue/button'
import {useThemeStore} from '@/stores/theme'

const {t} = useI18n()
const theme = useThemeStore()

const icons = {system: 'pi pi-desktop', light: 'pi pi-sun', dark: 'pi pi-moon'}

const label = computed(() => t('theme.switchLabel', {mode: t(`theme.${theme.mode}`)}))
const icon = computed(() => icons[theme.mode])
</script>
