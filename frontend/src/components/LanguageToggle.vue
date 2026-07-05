<template>
  <Button
      :aria-label="$t('language.switch')"
      :label="localeStore.locale.toUpperCase()"
      :title="$t('language.switch')"
      icon="pi pi-globe"
      rounded
      severity="secondary"
      text
      type="button"
      @click="localeStore.toggle()"
  />
</template>

<script setup>
import Button from 'primevue/button'
import {useLocaleStore} from '@/stores/locale'

const localeStore = useLocaleStore()
</script>
