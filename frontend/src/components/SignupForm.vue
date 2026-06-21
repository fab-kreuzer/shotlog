<template>
  <form class="space-y-4" novalidate @submit.prevent="handleSignup">
    <div>
      <label class="block text-sm font-medium text-surface-700 mb-1.5" for="signup-username">Benutzername <span
          class="text-danger-500">*</span></label>
      <input
          id="signup-username"
          v-model="form.username"
          :class="errors.username ? 'border-danger-400' : 'border-surface-300'"
          class="w-full px-4 py-2.5 rounded-lg border text-surface-800 placeholder-surface-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
          placeholder="Benutzername wählen"
          @input="clearError('username')"
      />
      <p v-if="errors.username" class="mt-1 text-sm text-danger-500">{{ errors.username }}</p>
    </div>

    <div>
      <label class="block text-sm font-medium text-surface-700 mb-1.5" for="signup-displayname">Anzeigename</label>
      <input
          id="signup-displayname"
          v-model="form.displayName"
          class="w-full px-4 py-2.5 rounded-lg border border-surface-300 text-surface-800 placeholder-surface-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
          placeholder="Ihr Anzeigename (optional)"
      />
    </div>

    <div>
      <label class="block text-sm font-medium text-surface-700 mb-1.5" for="signup-password">Passwort <span
          class="text-danger-500">*</span></label>
      <input
          id="signup-password"
          v-model="form.password"
          :class="errors.password ? 'border-danger-400' : 'border-surface-300'"
          class="w-full px-4 py-2.5 rounded-lg border text-surface-800 placeholder-surface-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
          placeholder="Passwort erstellen"
          type="password"
          @input="clearError('password')"
      />
      <p v-if="errors.password" class="mt-1 text-sm text-danger-500">{{ errors.password }}</p>
      <p v-else class="mt-1 text-xs text-surface-400">Mindestens 6 Zeichen</p>
    </div>

    <div>
      <label class="block text-sm font-medium text-surface-700 mb-1.5" for="signup-confirm">Passwort bestätigen <span
          class="text-danger-500">*</span></label>
      <input
          id="signup-confirm"
          v-model="form.confirmPassword"
          :class="errors.confirmPassword ? 'border-danger-400' : 'border-surface-300'"
          class="w-full px-4 py-2.5 rounded-lg border text-surface-800 placeholder-surface-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
          placeholder="Passwort wiederholen"
          type="password"
          @input="clearError('confirmPassword')"
      />
      <p v-if="errors.confirmPassword" class="mt-1 text-sm text-danger-500">{{ errors.confirmPassword }}</p>
    </div>

    <button
        :disabled="submitting"
        class="w-full py-2.5 px-4 rounded-lg text-sm font-semibold text-white bg-primary-700 hover:bg-primary-800 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm transition-all duration-200 flex items-center justify-center gap-2"
        type="submit"
    >
      <svg v-if="submitting" class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
        <path class="opacity-75" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" fill="currentColor"/>
      </svg>
      {{ submitting ? 'Konto erstellen...' : 'Konto erstellen' }}
    </button>
  </form>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'

const emit = defineEmits(['success'])
const notify = useNotificationStore()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  displayName: ''
})

const errors = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const submitting = ref(false)

function clearError(field) {
  errors[field] = ''
}

function validate() {
  let valid = true

  if (!form.username.trim()) {
    errors.username = 'Benutzername ist erforderlich'
    valid = false
  } else if (form.username.trim().length < 3) {
    errors.username = 'Benutzername muss mindestens 3 Zeichen lang sein'
    valid = false
  }

  if (!form.password) {
    errors.password = 'Passwort ist erforderlich'
    valid = false
  } else if (form.password.length < 6) {
    errors.password = 'Passwort muss mindestens 6 Zeichen lang sein'
    valid = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = 'Passwort-Bestätigung ist erforderlich'
    valid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = 'Passwörter stimmen nicht überein'
    valid = false
  }

  return valid
}

async function handleSignup() {
  if (!validate()) return

  submitting.value = true
  try {
    await api.register({
      username: form.username.trim(),
      password: form.password,
      displayName: form.displayName.trim() || null
    })
    notify.success('Konto erfolgreich erstellt! Sie können sich jetzt anmelden.')
    emit('success')
  } catch (err) {
    const message = err.error || 'Registrierung fehlgeschlagen. Bitte versuchen Sie es erneut.'
    if (message.toLowerCase().includes('benutzername')) {
      errors.username = message
    } else {
      notify.error(message)
    }
  } finally {
    submitting.value = false
  }
}
</script>
