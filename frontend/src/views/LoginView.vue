<template>
  <div class="login-container">
    <div class="login-header">
      <h1>Shotlog</h1>
      <h2>Anmeldung</h2>
    </div>

    <form class="login-form" @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="username">Benutzername</label>
        <input
            id="username"
            v-model="username"
            autofocus
            class="form-control"
            required
        />
      </div>

      <div class="form-group">
        <label for="password">Passwort</label>
        <input
            id="password"
            v-model="password"
            class="form-control"
            required
            type="password"
        />
      </div>

      <button :disabled="submitting" class="btn btn-primary" type="submit">Anmelden</button>
    </form>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const notify = useNotificationStore()

const username = ref('')
const password = ref('')
const submitting = ref(false)

onMounted(() => {
  if (route.query.logout !== undefined) {
    notify.success('Sie wurden erfolgreich abgemeldet.')
  }
})

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
