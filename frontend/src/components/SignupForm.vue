<template>
  <form class="flex flex-col gap-4" novalidate @submit.prevent="handleSignup">
    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="signup-username">
        Benutzername <span class="text-danger-500">*</span>
      </label>
      <InputText
          id="signup-username"
          v-model="form.username"
          :invalid="!!errors.username"
          fluid
          placeholder="Benutzername wählen"
          @input="clearError('username')"
      />
      <small v-if="errors.username" class="text-danger-500">{{ errors.username }}</small>
    </div>

    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="signup-displayname">
        Anzeigename <span class="text-danger-500">*</span>
      </label>
      <InputText
          id="signup-displayname"
          v-model="form.displayName"
          :invalid="!!errors.displayName"
          fluid
          placeholder="Ihr Anzeigename"
          @input="clearError('displayName')"
      />
      <small v-if="errors.displayName" class="text-danger-500">{{ errors.displayName }}</small>
    </div>

    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="signup-password">
        Passwort <span class="text-danger-500">*</span>
      </label>
      <Password
          v-model="form.password"
          :feedback="false"
          :invalid="!!errors.password"
          fluid
          inputId="signup-password"
          placeholder="Passwort erstellen"
          toggleMask
          @input="clearError('password')"
      />
      <small v-if="errors.password" class="text-danger-500">{{ errors.password }}</small>
      <small v-else class="text-surface-400">Mindestens 6 Zeichen</small>
    </div>

    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="signup-confirm">
        Passwort bestätigen <span class="text-danger-500">*</span>
      </label>
      <Password
          v-model="form.confirmPassword"
          :feedback="false"
          :invalid="!!errors.confirmPassword"
          fluid
          inputId="signup-confirm"
          placeholder="Passwort wiederholen"
          toggleMask
          @input="clearError('confirmPassword')"
      />
      <small v-if="errors.confirmPassword" class="text-danger-500">{{ errors.confirmPassword }}</small>
    </div>

    <Button
        :loading="submitting"
        class="w-full"
        label="Konto erstellen"
        type="submit"
    />
  </form>
</template>

<script setup>
import {reactive, ref} from 'vue'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
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
  displayName: '',
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

  if (!form.displayName.trim()) {
    errors.displayName = 'Anzeigename ist erforderlich'
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
      displayName: form.displayName.trim()
    })
    emit('success')
  } catch (err) {
    const message = err.errors?.[0] || 'Registrierung fehlgeschlagen. Bitte versuchen Sie es erneut.'
    if (message.toLowerCase().includes('benutzername')) {
      // Surface username problems inline on the field
      errors.username = message
    } else if (!err._notified) {
      notify.error(message)
    }
  } finally {
    submitting.value = false
  }
}
</script>
