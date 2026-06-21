<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center p-4">

        <!-- Backdrop -->
        <div
            class="fixed inset-0 bg-black/50 backdrop-blur-sm"
            @click="$emit('update:modelValue', false)"
        ></div>

        <!-- Modal -->
        <div class="relative w-full max-w-md bg-white rounded-2xl shadow-2xl p-6">

          <div class="flex justify-between items-center mb-5">
            <h4 class="text-lg font-semibold">Neue Rolle erstellen</h4>
            <button @click="$emit('update:modelValue', false)">✕</button>
          </div>

          <form class="space-y-4" @submit.prevent="submit">
            <input
                v-model="name"
                class="flex-1 px-3 py-2 rounded-lg border border-surface-300 text-sm w-full"
                placeholder="Rollenname"
            />

            <div class="flex justify-end gap-2 pt-2">
              <button class="px-3 py-1.5 text-md rounded-lg text-danger-600 bg-danger-50" type="button"
                      @click="$emit('update:modelValue', false)">
                Abbrechen
              </button>

              <button class="px-3 py-1.5 text-md rounded-lg text-primary-500 bg-primary-100" type="submit">
                Erstellen
              </button>
            </div>
          </form>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import {ref, watch} from 'vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'create'])

const name = ref('')

watch(() => props.modelValue, (open) => {
  if (open) {
    name.value = ''
  }
})

function submit() {
  emit('create', {name: name.value})
  emit('update:modelValue', false)
}
</script>