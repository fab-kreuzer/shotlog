<template>
  <form class="flex flex-col gap-5" @submit.prevent="handleLogin">
    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="login-username">Benutzername</label>
      <InputText
          id="login-username"
          v-model="username"
          autofocus
          fluid
          placeholder="Benutzername eingeben"
          required
      />
    </div>

    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-surface-700" for="login-password">Passwort</label>
      <Password
          v-model="password"
          :feedback="false"
          fluid
          inputId="login-password"
          placeholder="Passwort eingeben"
          required
          toggleMask
      />
    </div>

    <Button
        :loading="submitting"
        class="w-full"
        label="Anmelden"
        type="submit"
    />
  </form>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'

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
      notify.error('Ungültiger Benutzername oder Passwort.')
    }
  } catch {
    notify.error('Ungültiger Benutzername oder Passwort.')
  } finally {
    submitting.value = false
  }
}
</script>
