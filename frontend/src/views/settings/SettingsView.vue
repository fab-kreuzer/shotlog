<template>
  <div>
    <PageHeader :subtitle="$t('settings.subtitle')" :title="$t('settings.title')" color="purple" icon="pi pi-cog"/>

    <Card>
      <template #content>
        <Tabs v-model:value="activeTab">
          <TabList>
            <Tab value="profile"><i class="pi pi-user mr-2"/>{{ $t('settings.tabProfile') }}</Tab>
            <Tab v-if="auth.hasPermission('view_user_tab')" value="user-management">
              <i class="pi pi-users mr-2"/>{{ $t('settings.tabUserManagement') }}
            </Tab>
            <Tab v-if="auth.hasPermission('view_role_tab')" value="role-management">
              <i class="pi pi-shield mr-2"/>{{ $t('settings.tabRoleManagement') }}
            </Tab>
            <Tab v-if="auth.hasPermission('view_club_tab')" value="club-management">
              <i class="pi pi-building mr-2"/>{{ $t('settings.tabClubManagement') }}
            </Tab>
            <Tab v-if="auth.hasPermission('view_team_tab')" value="team-management">
              <i class="pi pi-sitemap mr-2"/>{{ $t('settings.tabTeamManagement') }}
            </Tab>
          </TabList>
          <TabPanels>
            <TabPanel value="profile">
              <ProfileTab/>
            </TabPanel>
            <TabPanel v-if="auth.hasPermission('view_user_tab')" value="user-management">
              <UserTab/>
            </TabPanel>
            <TabPanel v-if="auth.hasPermission('view_role_tab')" value="role-management">
              <RoleTab/>
            </TabPanel>
            <TabPanel v-if="auth.hasPermission('view_club_tab')" value="club-management">
              <ClubManagementTab/>
            </TabPanel>
            <TabPanel v-if="auth.hasPermission('view_team_tab')" value="team-management">
              <TeamTab/>
            </TabPanel>
          </TabPanels>
        </Tabs>
      </template>
    </Card>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import Card from 'primevue/card'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import {useAuthStore} from '@/stores/auth'
import PageHeader from '@/components/PageHeader.vue'

import ProfileTab from './ProfileTab.vue'
import UserTab from "@/views/settings/UserTab.vue";
import RoleTab from "@/views/settings/RoleTab.vue";
import ClubManagementTab from "@/views/settings/ClubManagementTab.vue";
import TeamTab from "@/views/settings/TeamTab.vue";

const auth = useAuthStore()
const activeTab = ref('profile')
</script>