<template>
  <form class="flex flex-col gap-5" @submit.prevent="handleLogin">
    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="login-username">{{ $t('auth.username') }}</label>
      <InputText
          id="login-username"
          v-model="username"
          autofocus
          fluid
          :placeholder="$t('auth.usernamePlaceholder')"
          required
      />
    </div>

    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="login-password">{{ $t('auth.password') }}</label>
      <Password
          v-model="password"
          :feedback="false"
          fluid
          inputId="login-password"
          :placeholder="$t('auth.passwordPlaceholder')"
          required
          toggleMask
      />
    </div>

    <Button
        :loading="submitting"
        class="w-full"
        :label="$t('login.signIn')"
        type="submit"
    />
  </form>
</template>

<script setup>
import {ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'

const {t} = useI18n()
const router = useRouter()
const auth = useAuthStore()
const notify = useNotificationStore()

const username = ref('')
const password = ref('')
const submitting = ref(false)

async function handleLogin() {
  submitting.value = true
  try {
    const success = await auth.login(username.value, password.value)
    if (success) {
      router.push('/dashboard')
    } else {
      notify.error(t('auth.invalidCredentials'))
    }
  } catch {
    notify.error(t('auth.invalidCredentials'))
  } finally {
    submitting.value = false
  }
}
</script>
