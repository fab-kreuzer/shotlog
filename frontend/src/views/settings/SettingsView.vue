<template>
  <div>
    <PageHeader :subtitle="$t('settings.subtitle')" :title="$t('settings.title')" color="purple" icon="pi pi-cog"/>

    <Card>
      <template #content>
        <div class="flex flex-col gap-6 md:flex-row">
          <nav class="shrink-0 md:w-56">
            <ul class="flex gap-1 overflow-x-auto md:flex-col md:overflow-visible">
              <li v-for="item in visibleItems" :key="item.name">
                <router-link
                    :class="route.name === item.name
                      ? 'bg-surface-100 font-semibold text-primary-500'
                      : 'text-surface-800 hover:bg-surface-100'"
                    :to="{name: item.name}"
                    class="flex items-center gap-2 whitespace-nowrap rounded-lg px-3 py-2 text-sm transition-colors"
                >
                  <i :class="item.icon"/>
                  <span>{{ $t(item.label) }}</span>
                </router-link>
              </li>
            </ul>
          </nav>
          <div class="min-w-0 flex-1">
            <router-view/>
          </div>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import Card from 'primevue/card'
import {useAuthStore} from '@/stores/auth'
import PageHeader from '@/components/PageHeader.vue'

const auth = useAuthStore()
const route = useRoute()

const navItems = [
  {name: 'settings-profile', icon: 'pi pi-user', label: 'settings.tabProfile'},
  {name: 'settings-user-management', icon: 'pi pi-users', label: 'settings.tabUserManagement', permission: 'view_user_tab'},
  {name: 'settings-role-management', icon: 'pi pi-shield', label: 'settings.tabRoleManagement', permission: 'view_role_tab'},
  {name: 'settings-club-management', icon: 'pi pi-building', label: 'settings.tabClubManagement', permission: 'view_club_tab'},
  {name: 'settings-team-management', icon: 'pi pi-sitemap', label: 'settings.tabTeamManagement', permission: 'view_team_tab'},
  {name: 'settings-season-management', icon: 'pi pi-calendar', label: 'settings.tabSeasonManagement', permission: 'view_season_tab'}
]

const visibleItems = computed(() =>
    navItems.filter(item => !item.permission || auth.hasPermission(item.permission))
)
</script>
