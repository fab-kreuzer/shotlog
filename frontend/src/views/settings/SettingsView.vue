<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">Einstellungen</h1>
      <p class="mt-1 text-surface-500">Verwalten Sie Ihr Profil und Systemeinstellungen</p>
    </div>

    <Card>
      <template #content>
        <Tabs v-model:value="activeTab">
          <TabList>
            <Tab value="profile">Profil</Tab>
            <Tab v-if="auth.isAdmin" value="user-management">Benutzerverwaltung</Tab>
            <Tab v-if="auth.isAdmin" value="role-management">Rollenverwaltung</Tab>
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

const auth = useAuthStore()
const activeTab = ref('profile')
</script>
