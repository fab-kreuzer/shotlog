<template>
  <form class="flex flex-col gap-4" novalidate @submit.prevent="handleSignup">
    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="signup-username">
        {{ $t('auth.username') }} <span class="text-danger-500">*</span>
      </label>
      <InputText
          id="signup-username"
          v-model="form.username"
          :invalid="!!errors.username"
          fluid
          :placeholder="$t('auth.chooseUsernamePlaceholder')"
          @input="clearError('username')"
      />
      <small v-if="errors.username" class="text-danger-500">{{ errors.username }}</small>
    </div>

    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="signup-displayname">
        {{ $t('auth.displayName') }} <span class="text-danger-500">*</span>
      </label>
      <InputText
          id="signup-displayname"
          v-model="form.displayName"
          :invalid="!!errors.displayName"
          fluid
          :placeholder="$t('auth.displayNamePlaceholder')"
          @input="clearError('displayName')"
      />
      <small v-if="errors.displayName" class="text-danger-500">{{ errors.displayName }}</small>
    </div>

    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="signup-password">
        {{ $t('auth.password') }} <span class="text-danger-500">*</span>
      </label>
      <Password
          v-model="form.password"
          :feedback="false"
          :invalid="!!errors.password"
          fluid
          inputId="signup-password"
          :placeholder="$t('auth.createPasswordPlaceholder')"
          toggleMask
          @input="clearError('password')"
      />
      <small v-if="errors.password" class="text-danger-500">{{ errors.password }}</small>
      <small v-else class="text-surface-400">{{ $t('auth.passwordHint') }}</small>
    </div>

    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="signup-confirm">
        {{ $t('auth.confirmPassword') }} <span class="text-danger-500">*</span>
      </label>
      <Password
          v-model="form.confirmPassword"
          :feedback="false"
          :invalid="!!errors.confirmPassword"
          fluid
          inputId="signup-confirm"
          :placeholder="$t('auth.repeatPasswordPlaceholder')"
          toggleMask
          @input="clearError('confirmPassword')"
      />
      <small v-if="errors.confirmPassword" class="text-danger-500">{{ errors.confirmPassword }}</small>
    </div>

    <Button
        :loading="submitting"
        class="w-full"
        :label="$t('auth.createAccount')"
        type="submit"
    />
  </form>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'

const emit = defineEmits(['success'])
const {t} = useI18n()
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
    errors.username = t('validation.usernameRequired')
    valid = false
  } else if (form.username.trim().length < 3) {
    errors.username = t('validation.usernameMinLength')
    valid = false
  }

  if (!form.displayName.trim()) {
    errors.displayName = t('validation.displayNameRequired')
    valid = false
  }

  if (!form.password) {
    errors.password = t('validation.passwordRequired')
    valid = false
  } else if (form.password.length < 6) {
    errors.password = t('validation.passwordMinLength')
    valid = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = t('validation.confirmPasswordRequired')
    valid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = t('validation.passwordsMismatch')
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
    const message = err.errors?.[0] || t('auth.registrationFailed')
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
