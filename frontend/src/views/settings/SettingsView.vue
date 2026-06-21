<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">Einstellungen</h1>
      <p class="mt-1 text-surface-500">Verwalten Sie Ihr Profil und Systemeinstellungen</p>
    </div>

    <!-- Tabs -->
    <div class="bg-white rounded-xl border border-surface-200 shadow-sm overflow-hidden">
      <div class="border-b border-surface-200">
        <nav class="flex gap-0">
          <button
              :class="tabClass('profile')"
              @click="activeTab = 'profile'"
          >
            Profil
          </button>

          <button
              v-if="auth.isAdmin"
              :class="tabClass('admin')"
              @click="activeTab = 'admin'"
          >
            Admin
          </button>
        </nav>
      </div>

      <ProfileTab v-if="activeTab === 'profile'"/>

      <AdminTab
          v-if="activeTab === 'admin' && auth.isAdmin"
      />
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useAuthStore} from '@/stores/auth'

import ProfileTab from './ProfileTab.vue'
import AdminTab from './AdminTab.vue'

const auth = useAuthStore()
const activeTab = ref('profile')

function tabClass(tab) {
  return [
    'px-6 py-3.5 text-sm font-medium border-b-2 transition-colors cursor-pointer',
    activeTab.value === tab
        ? 'border-primary-700 text-primary-700'
        : 'border-transparent text-surface-500 hover:text-surface-700 hover:border-surface-300'
  ]
}
</script>