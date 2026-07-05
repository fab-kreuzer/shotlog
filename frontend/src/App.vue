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
          <LanguageToggle/>
          <ThemeToggle/>
          <Button
              icon="pi pi-sign-out"
              :label="$t('nav.logout')"
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
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import {useToast} from 'primevue/usetoast'
import Toast from 'primevue/toast'
import Menubar from 'primevue/menubar'
import Button from 'primevue/button'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'
import ThemeToggle from '@/components/ThemeToggle.vue'
import LanguageToggle from '@/components/LanguageToggle.vue'

const {t} = useI18n()
const router = useRouter()
const auth = useAuthStore()
const notify = useNotificationStore()
const toast = useToast()

// Route all app notifications through PrimeVue's Toast service.
notify.register((options) => toast.add(options))

const navLinks = [
  {to: '/dashboard', labelKey: 'nav.dashboard'},
  {to: '/training', labelKey: 'nav.training'},
  {to: '/competition', labelKey: 'nav.competition'},
  {to: '/settings', labelKey: 'nav.settings'},
  {to: '/calender', labelKey: 'nav.calender'},
]

const items = computed(() => navLinks.map(l => ({label: t(l.labelKey), route: l.to})))

async function handleLogout() {
  await auth.logout()
  notify.success(t('login.loggedOut'))
  router.push('/login')
}
</script>
