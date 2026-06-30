<template>
  <div class="min-h-[80vh] flex items-center justify-center">
    <div class="w-full max-w-md">
      <!-- Theme toggle -->
      <div class="flex justify-end mb-3">
        <ThemeToggle/>
      </div>

      <Card>
        <template #content>
          <!-- Header -->
          <div class="text-center mb-6">
            <img alt="ShotLog" class="h-20 w-20 rounded-full object-cover mx-auto mb-4 shadow-md" src="/logo.png"/>
            <h1 class="text-3xl font-bold text-primary-800 dark:text-primary-300 mb-2">ShotLog</h1>
            <p class="text-surface-500">
              {{ activeTab === 'login' ? 'Melden Sie sich an, um fortzufahren' : 'Erstellen Sie ein neues Konto' }}
            </p>
          </div>

          <Tabs v-model:value="activeTab">
            <TabList>
              <Tab class="flex-1 justify-center" value="login">Anmelden</Tab>
              <Tab class="flex-1 justify-center" value="signup">Registrieren</Tab>
            </TabList>
            <TabPanels>
              <TabPanel value="login">
                <LoginForm/>
              </TabPanel>
              <TabPanel value="signup">
                <SignupForm @success="onSignupSuccess"/>
              </TabPanel>
            </TabPanels>
          </Tabs>
        </template>
      </Card>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute} from 'vue-router'
import Card from 'primevue/card'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import {useNotificationStore} from '@/stores/notifications'
import LoginForm from '@/components/LoginForm.vue'
import SignupForm from '@/components/SignupForm.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'

const route = useRoute()
const notify = useNotificationStore()

const activeTab = ref('login')

onMounted(() => {
  if (route.query.logout !== undefined) {
    notify.success('Sie wurden erfolgreich abgemeldet.')
  }
})

function onSignupSuccess() {
  activeTab.value = 'login'
}
</script>
