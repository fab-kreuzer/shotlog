<template>
  <div class="min-h-[80vh] flex items-center justify-center">
    <div class="w-full max-w-md">
      <!-- Card -->
      <div class="bg-white rounded-2xl shadow-xl border border-surface-200 p-8">
        <!-- Header -->
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-primary-800 mb-2">ShotLog</h1>
          <p class="text-surface-500">
            {{ isLogin ? 'Melden Sie sich an, um fortzufahren' : 'Erstellen Sie ein neues Konto' }}
          </p>
        </div>

        <!-- Tab Toggle -->
        <div class="flex mb-6 bg-surface-100 rounded-lg p-1">
          <button
              :class="isLogin
                ? 'bg-white text-primary-800 shadow-sm'
                : 'text-surface-500 hover:text-surface-700'"
              class="flex-1 py-2 text-sm font-medium rounded-md transition-all duration-200"
              @click="switchTo('login')"
          >
            Anmelden
          </button>
          <button
              :class="!isLogin
                ? 'bg-white text-primary-800 shadow-sm'
                : 'text-surface-500 hover:text-surface-700'"
              class="flex-1 py-2 text-sm font-medium rounded-md transition-all duration-200"
              @click="switchTo('signup')"
          >
            Registrieren
          </button>
        </div>

        <!-- Forms -->
        <Transition mode="out-in" name="auth-fade">
          <LoginForm v-if="isLogin" key="login"/>
          <SignupForm v-else key="signup" @success="onSignupSuccess"/>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute} from 'vue-router'
import {useNotificationStore} from '@/stores/notifications'
import LoginForm from '@/components/LoginForm.vue'
import SignupForm from '@/components/SignupForm.vue'

const route = useRoute()
const notify = useNotificationStore()

const isLogin = ref(true)

onMounted(() => {
  if (route.query.logout !== undefined) {
    notify.success('Sie wurden erfolgreich abgemeldet.')
  }
})

function switchTo(mode) {
  isLogin.value = mode === 'login'
}

function onSignupSuccess() {
  isLogin.value = true
}
</script>

<style scoped>
.auth-fade-enter-active,
.auth-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.auth-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.auth-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
