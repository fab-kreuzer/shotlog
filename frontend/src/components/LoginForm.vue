<template>
  <form class="space-y-5" @submit.prevent="handleLogin">
    <div>
      <label class="block text-sm font-medium text-surface-700 mb-1.5" for="login-username">Benutzername</label>
      <input
          id="login-username"
          v-model="username"
          autofocus
          class="w-full px-4 py-2.5 rounded-lg border border-surface-300 text-surface-800 placeholder-surface-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
          placeholder="Benutzername eingeben"
          required
      />
    </div>

    <div>
      <label class="block text-sm font-medium text-surface-700 mb-1.5" for="login-password">Passwort</label>
      <input
          id="login-password"
          v-model="password"
          class="w-full px-4 py-2.5 rounded-lg border border-surface-300 text-surface-800 placeholder-surface-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
          placeholder="Passwort eingeben"
          required
          type="password"
      />
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
      {{ submitting ? 'Anmelden...' : 'Anmelden' }}
    </button>
  </form>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
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
