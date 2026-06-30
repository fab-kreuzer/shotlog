<template>
  <div class="min-h-screen flex flex-col">
    <!-- Navigation -->
    <Menubar v-if="auth.isLoggedIn" :model="items" class="rounded-none border-x-0 border-t-0 px-4 sm:px-6 lg:px-8">
      <template #start>
        <router-link
            class="flex items-center gap-2.5 text-xl font-bold tracking-tight mr-4"
            to="/dashboard"
        >
          <img alt="ShotLog" class="h-9 w-9 rounded-full object-cover" src="/logo.png"/>
          ShotLog
        </router-link>
      </template>

      <template #item="{ item, props }">
        <router-link v-slot="{ href, navigate, isActive }" :to="item.route" custom>
          <a
              :class="{ 'font-semibold text-primary-500': isActive }"
              :href="href"
              v-bind="props.action"
              @click="navigate"
          >
            <span>{{ item.label }}</span>
          </a>
        </router-link>
      </template>

      <template #end>
        <div class="flex items-center gap-1">
          <ThemeToggle/>
          <Button
              icon="pi pi-sign-out"
              label="Logout"
              severity="secondary"
              text
              @click="handleLogout"
          />
        </div>
      </template>
    </Menubar>

    <!-- Main content -->
    <main class="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <router-view/>
    </main>

    <!-- Notifications -->
    <Toast position="bottom-right"/>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRouter} from 'vue-router'
import {useToast} from 'primevue/usetoast'
import Toast from 'primevue/toast'
import Menubar from 'primevue/menubar'
import Button from 'primevue/button'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router = useRouter()
const auth = useAuthStore()
const notify = useNotificationStore()
const toast = useToast()

// Route all app notifications through PrimeVue's Toast service.
notify.register((options) => toast.add(options))

const navLinks = [
  {to: '/dashboard', label: 'Dashboard'},
  {to: '/training', label: 'Training'},
  {to: '/competition', label: 'Wettkampf'},
  {to: '/settings', label: 'Einstellungen'},
  {to: '/calender', label: 'Kalender'},
]

const items = computed(() => navLinks.map(l => ({label: l.label, route: l.to})))

async function handleLogout() {
  await auth.logout()
  notify.success('Sie wurden erfolgreich abgemeldet.')
  router.push('/login')
}
</script>
