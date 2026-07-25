<template>
  <div>
    <PageHeader :subtitle="$t('settings.subtitle')" :title="$t('settings.title')" color="purple" icon="pi pi-cog"/>

    <Card>
      <template #content>
        <Tabs v-model:value="activeTab">
          <TabList>
            <Tab value="settings-profile"><i class="pi pi-user mr-2"/>{{ $t('settings.tabProfile') }}</Tab>
            <Tab v-if="auth.hasPermission('view_user_tab')" value="settings-user-management">
              <i class="pi pi-users mr-2"/>{{ $t('settings.tabUserManagement') }}
            </Tab>
            <Tab v-if="auth.hasPermission('view_role_tab')" value="settings-role-management">
              <i class="pi pi-shield mr-2"/>{{ $t('settings.tabRoleManagement') }}
            </Tab>
            <Tab v-if="auth.hasPermission('view_club_tab')" value="settings-club-management">
              <i class="pi pi-building mr-2"/>{{ $t('settings.tabClubManagement') }}
            </Tab>
            <Tab v-if="auth.hasPermission('view_team_tab')" value="settings-team-management">
              <i class="pi pi-sitemap mr-2"/>{{ $t('settings.tabTeamManagement') }}
            </Tab>
          </TabList>
        </Tabs>
        <div class="pt-4">
          <router-view/>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import Card from 'primevue/card'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import {useAuthStore} from '@/stores/auth'
import PageHeader from '@/components/PageHeader.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const activeTab = computed({
  get: () => route.name,
  set: (name) => router.push({name})
})
</script>
