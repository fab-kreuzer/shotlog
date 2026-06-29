<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div
            class="fixed inset-0 bg-black/50 backdrop-blur-sm"
            @click="cancel"
        />

        <!-- Modal -->
        <div class="relative w-full max-w-md bg-card rounded-2xl shadow-2xl p-6">
          <div class="mb-4">
            <h3 class="text-lg font-semibold text-surface-800">
              {{ title }}
            </h3>
            <p class="text-sm text-surface-500 mt-1">
              {{ message }}
            </p>
          </div>

          <div class="flex justify-end gap-2 pt-2">
            <button
                class="px-3 py-1.5 rounded-lg bg-surface-200 text-surface-700 hover:bg-surface-300 transition-colors"
                @click="cancel"
            >
              Abbrechen
            </button>

            <button
                class="px-3 py-1.5 rounded-lg bg-danger-500 text-white hover:bg-danger-600 transition-colors"
                @click="confirm"
            >
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
const props = defineProps({
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