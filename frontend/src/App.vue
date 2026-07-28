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
            <i :class="item.icon" class="mr-1.5"/>
            <span>{{ item.label }}</span>
          </a>
        </router-link>
      </template>

      <template #end>
        <div class="flex items-center gap-1">
          <LanguageToggle/>
          <ThemeToggle/>
          <button
              aria-haspopup="true"
              class="ml-1 flex items-center"
              type="button"
              @click="toggleUserMenu"
          >
            <Avatar :label="initials" class="cursor-pointer bg-primary-500 font-semibold text-white" shape="circle"/>
          </button>
          <Menu ref="userMenu" :model="userMenuItems" :popup="true">
            <template #start>
              <div class="border-b border-surface-200 px-3 py-2">
                <div class="text-sm font-semibold text-surface-800">{{ auth.user?.displayName }}</div>
                <div class="text-xs text-surface-500">@{{ auth.user?.username }}</div>
              </div>
            </template>
          </Menu>
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
import {computed, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import {useToast} from 'primevue/usetoast'
import Toast from 'primevue/toast'
import Menubar from 'primevue/menubar'
import Avatar from 'primevue/avatar'
import Menu from 'primevue/menu'
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

const SETTINGS_PERMISSIONS = ['view_user_tab', 'view_role_tab', 'view_club_tab', 'view_team_tab', 'view_season_tab']

const navLinks = [
  {to: '/dashboard', labelKey: 'nav.dashboard', icon: 'pi pi-home'},
  {to: '/training', labelKey: 'nav.training', icon: 'pi pi-bullseye'},
  {to: '/competition', labelKey: 'nav.competition', icon: 'pi pi-trophy'},
  {to: '/calender', labelKey: 'nav.calender', icon: 'pi pi-calendar'},
]

const items = computed(() => navLinks.map(l => ({label: t(l.labelKey), route: l.to, icon: l.icon})))

// Account actions live in the top-right avatar menu rather than the main nav.
const initials = computed(() => {
  const name = auth.user?.displayName || auth.user?.username || '?'
  return name.trim().split(/\s+/).map(w => w[0]).slice(0, 2).join('').toUpperCase()
})

const canViewSettings = computed(() => SETTINGS_PERMISSIONS.some(p => auth.hasPermission(p)))

const userMenuItems = computed(() => {
  const menu = [
    {label: t('nav.profile'), icon: 'pi pi-user', command: () => router.push('/profile')}
  ]
  if (canViewSettings.value) {
    menu.push({label: t('nav.settings'), icon: 'pi pi-cog', command: () => router.push('/settings')})
  }
  menu.push({separator: true})
  menu.push({label: t('nav.logout'), icon: 'pi pi-sign-out', command: handleLogout})
  return menu
})

const userMenu = ref(null)

function toggleUserMenu(event) {
  userMenu.value.toggle(event)
}

async function handleLogout() {
  await auth.logout()
  notify.success(t('login.loggedOut'))
  router.push('/login')
}
</script>
