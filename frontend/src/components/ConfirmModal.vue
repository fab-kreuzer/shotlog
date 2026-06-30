<template>
  <Dialog
      :draggable="false"
      :header="title"
      :style="{ width: '28rem' }"
      :visible="modelValue"
      modal
      @update:visible="$emit('update:modelValue', $event)"
  >
    <p class="text-sm text-surface-500">{{ message }}</p>

    <template #footer>
      <Button label="Abbrechen" severity="secondary" text @click="cancel"/>
      <Button :label="confirmText" severity="danger" @click="confirm"/>
    </template>
  </Dialog>
</template>

<script setup>
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'

defineProps({
  modelValue: Boolean,
  title: {
    type: String,
    default: 'Bestätigen'
  },
  message: {
    type: String,
    default: 'Bist du sicher?'
  },
  confirmText: {
    type: String,
    default: 'Löschen'
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
