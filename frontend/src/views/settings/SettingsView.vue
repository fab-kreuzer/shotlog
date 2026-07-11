<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">{{ $t('settings.title') }}</h1>
      <p class="mt-1 text-surface-500">{{ $t('settings.subtitle') }}</p>
    </div>

    <Card>
      <template #content>
        <Tabs v-model:value="activeTab">
          <TabList>
            <Tab value="profile">{{ $t('settings.tabProfile') }}</Tab>
            <Tab v-if="auth.isAdmin" value="user-management">{{ $t('settings.tabUserManagement') }}</Tab>
            <Tab v-if="auth.isAdmin" value="role-management">{{ $t('settings.tabRoleManagement') }}</Tab>
            <Tab v-if="auth.isAdmin" value="club-management">{{ $t('settings.tabClubManagement') }}</Tab>
            <Tab v-if="auth.isAdmin || auth.isSportLeader" value="team-management">{{
                $t('settings.tabTeamManagement')
              }}
            </Tab>
          </TabList>
          <TabPanels>
            <TabPanel value="profile">
              <ProfileTab/>
            </TabPanel>
            <TabPanel v-if="auth.isAdmin" value="user-management">
              <UserTab/>
            </TabPanel>
            <TabPanel v-if="auth.isAdmin" value="role-management">
              <RoleTab/>
            </TabPanel>
            <TabPanel v-if="auth.isAdmin" value="club-management">
              <ClubManagementTab/>
            </TabPanel>
            <TabPanel v-if="auth.isAdmin || auth.isSportLeader" value="team-management">
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

import ProfileTab from './ProfileTab.vue'
import UserTab from "@/views/settings/UserTab.vue";
import RoleTab from "@/views/settings/RoleTab.vue";
import ClubManagementTab from "@/views/settings/ClubManagementTab.vue";
import TeamTab from "@/views/settings/TeamTab.vue";

const auth = useAuthStore()
const activeTab = ref('profile')
</script>