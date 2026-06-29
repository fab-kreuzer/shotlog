<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div
            class="fixed inset-0 bg-black/50 backdrop-blur-sm"
            @click="$emit('update:modelValue', false)"
        />

        <!-- Modal -->
        <div class="relative w-full max-w-md bg-white rounded-2xl shadow-2xl p-6">
          <div class="flex justify-between items-center mb-5">
            <h4 class="text-lg font-semibold">Neuen Benutzer erstellen</h4>
            <button @click="$emit('update:modelValue', false)">✕</button>
          </div>

          <form class="space-y-4" @submit.prevent="submit">
            <input v-model="user.username" class="flex-1 px-3 py-2 rounded-lg border border-surface-300 text-sm w-full"
                   placeholder="Benutzername"/>
            <input v-model="user.displayName" class="flex-1 px-3 py-2 rounded-lg border border-surface-300 text-sm w-full"
                   placeholder="Anzeigename"/>
            <input v-model="user.password" class="flex-1 px-3 py-2 rounded-lg border border-surface-300 text-sm w-full"
                   placeholder="Passwort"
                   type="password"/>

            <Multiselect
                v-model="selectedRoles"
                :multiple="true"
                :options="roles"
                label="name"
                placeholder="Rollen auswählen"
                track-by="id"
                class="rounded-lg"
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
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'
import {useNotificationStore} from "@/stores/notifications.js";

const notify = useNotificationStore()

const props = defineProps({
  modelValue: Boolean,
  roles: Array
})

const selectedRoles = ref([])

watch(selectedRoles, (val) => {
  user.value.roleIds = val.map(r => r.id)
})

const emit = defineEmits(['update:modelValue', 'create'])

const user = ref({
  username: '',
  displayName: '',
  password: '',
  roleIds: []
})

watch(() => props.modelValue, (open) => {
  if (open) {
    user.value = {username: '', displayName: '', password: '', roleIds: []}
  }
})

function submit() {
  const errors = []

  const username = user.value.username?.trim()
  const displayName = user.value.displayName?.trim()
  const password = user.value.password

  if (!username) {
    errors.push('Benutzername darf nicht leer sein')
  } else if (username.length < 3) {
    errors.push('Benutzername muss mindestens 3 Zeichen haben')
  }

  if (!displayName) {
    errors.push('Anzeigename darf nicht leer sein')
  }

  if (!password) {
    errors.push('Passwort darf nicht leer sein')
  } else if (password.length < 6) {
    errors.push('Passwort muss mindestens 6 Zeichen haben')
  }

  if (errors.length) {
    errors.forEach(msg => {
      notify.warn(msg);
    })
    return
  }

  emit('create', user.value)
  emit('update:modelValue', false)
}
</script>