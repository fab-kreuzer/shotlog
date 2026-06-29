<template>
  <div class="min-h-screen flex flex-col">
    <!-- Navigation -->
    <header v-if="auth.isLoggedIn" class="bg-primary-800 shadow-lg">
      <nav class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <!-- Brand -->
          <router-link
              to="/dashboard"
              class="flex items-center gap-2.5 text-white text-xl font-bold tracking-tight hover:text-primary-200 transition-colors"
          >
            <img src="/logo.png" alt="ShotLog" class="h-9 w-9 rounded-full object-cover"/>
            ShotLog
          </router-link>

          <!-- Desktop nav -->
          <div class="hidden md:flex items-center gap-1">
            <router-link
                v-for="link in navLinks"
                :key="link.to"
                :class="[
                  'px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200',
                  isActiveLink(link)
                    ? 'bg-primary-900 text-white shadow-inner'
                    : 'text-primary-100 hover:bg-primary-700 hover:text-white'
                ]"
                :to="link.to"
            >
              {{ link.label }}
            </router-link>

            <div class="ml-4 pl-4 border-l border-primary-600 flex items-center gap-1">
              <ThemeToggle variant="nav"/>
              <button
                  class="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-primary-100 hover:bg-primary-700 hover:text-white transition-all duration-200"
                  @click="handleLogout"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" stroke-linecap="round" stroke-linejoin="round"
                        stroke-width="2"/>
                </svg>
                Logout
              </button>
            </div>
          </div>

          <!-- Mobile menu button -->
          <button
              class="md:hidden p-2 rounded-lg text-primary-100 hover:bg-primary-700 hover:text-white transition-colors"
              @click="mobileMenuOpen = !mobileMenuOpen"
          >
            <svg v-if="!mobileMenuOpen" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path d="M4 6h16M4 12h16M4 18h16" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
            </svg>
            <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path d="M6 18L18 6M6 6l12 12" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
            </svg>
          </button>
        </div>

        <!-- Mobile menu -->
        <div v-if="mobileMenuOpen" class="md:hidden pb-4 space-y-1">
          <router-link
              v-for="link in navLinks"
              :key="link.to"
              :class="[
                'block px-4 py-2.5 rounded-lg text-sm font-medium transition-all duration-200',
                isActiveLink(link)
                  ? 'bg-primary-900 text-white'
                  : 'text-primary-100 hover:bg-primary-700 hover:text-white'
              ]"
              :to="link.to"
              @click="mobileMenuOpen = false"
          >
            {{ link.label }}
          </router-link>
          <div class="mt-2 pt-2 border-t border-primary-600 flex items-center gap-1">
            <ThemeToggle variant="nav"/>
            <span class="text-sm font-medium text-primary-100">Design</span>
          </div>
          <button
              class="w-full text-left flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-medium text-primary-100 hover:bg-primary-700 hover:text-white transition-all duration-200"
              @click="handleLogout"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" stroke-linecap="round" stroke-linejoin="round"
                    stroke-width="2"/>
            </svg>
            Logout
          </button>
        </div>
      </nav>
    </header>

    <!-- Main content -->
    <main class="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <router-view/>
    </main>

    <!-- Notifications -->
    <NotificationContainer/>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'
import NotificationContainer from '@/components/NotificationContainer.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const notify = useNotificationStore()
const mobileMenuOpen = ref(false)

const navLinks = [
  {to: '/dashboard', name: 'dashboard', label: 'Dashboard'},
  {to: '/training', name: 'training', label: 'Training'},
  {to: '/competition', name: 'competition', label: 'Wettkampf'},
  {to: '/settings', name: 'settings', label: 'Einstellungen'},
  {to: '/calender', name: 'calender', label: 'Kalender'},
]

function isActiveLink(link) {
  return route.name === link.name
}

async function handleLogout() {
  await auth.logout()
  notify.success('Sie wurden erfolgreich abgemeldet.')
  router.push('/login')
}
</script>