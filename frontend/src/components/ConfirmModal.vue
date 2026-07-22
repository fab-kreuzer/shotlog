<template>
  <Dialog
      :draggable="false"
      :style="{ width: '28rem' }"
      :visible="modelValue"
      modal
      @update:visible="$emit('update:modelValue', $event)"
  >
    <template #header>
      <div class="flex items-center gap-3">
        <div
            class="flex items-center justify-center w-9 h-9 rounded-full bg-red-50 text-red-600 dark:bg-red-400/10 dark:text-red-400">
          <i class="pi pi-exclamation-triangle"/>
        </div>
        <span class="font-semibold text-surface-800">{{ title }}</span>
      </div>
    </template>

    <p class="text-sm text-surface-500">{{ message }}</p>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text @click="cancel"/>
      <Button :label="confirmText" severity="danger" @click="confirm"/>
    </template>
  </Dialog>
</template>

<script setup>
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import i18n from '@/i18n'

defineProps({
  modelValue: Boolean,
  title: {
    type: String,
    default: () => i18n.global.t('common.confirm')
  },
  message: {
    type: String,
    default: () => i18n.global.t('confirm.message')
  },
  confirmText: {
    type: String,
    default: () => i18n.global.t('common.delete')
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

function cancel() {
  emit('update:modelValue', false)
}

function confirm() {
  emit('confirm')
  emit('update:modelValue', false)
}
</script>
