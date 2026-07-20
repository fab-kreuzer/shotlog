<template>
  <Card>
    <template #title>
      <div class="flex items-center justify-between text-base">
        <div class="flex items-center gap-2">
          <i class="pi pi-history text-surface-400"/>
          <span>{{ $t('dashboard.recentSessions') }}</span>
        </div>
        <router-link class="flex items-center gap-1 text-sm font-normal text-primary-500 hover:text-primary-600"
                     to="/calender">
          {{ $t('dashboard.viewAll') }}
          <i class="pi pi-arrow-right text-xs"/>
        </router-link>
      </div>
    </template>
    <template #content>
      <div v-if="sessions.length === 0" class="text-center py-10">
        <div class="inline-flex items-center justify-center w-14 h-14 rounded-full bg-surface-100 mb-3">
          <i class="pi pi-inbox text-surface-400" style="font-size: 1.5rem"/>
        </div>
        <p class="text-surface-500 text-sm">{{ $t('dashboard.noRecent') }}</p>
      </div>
      <div v-else class="flex flex-col">
        <div
            v-for="session in sessions"
            :key="session.id"
            class="group flex items-center gap-3 py-2.5 px-2 -mx-2 rounded-lg cursor-pointer hover:bg-surface-50 transition-colors"
            @click="goTo(session)"
        >
          <div class="flex items-center justify-center w-9 h-9 rounded-full bg-surface-100 shrink-0">
            <i :class="session.home ? 'pi pi-home' : 'pi pi-map-marker'" class="text-surface-500 text-sm"/>
          </div>
          <div class="flex-1 min-w-0">
            <div class="font-medium text-surface-800 truncate">{{ session.title }}</div>
            <div class="flex items-center gap-2 mt-0.5">
              <Tag :severity="typeSeverity(session)" :value="typeLabel(session)"/>
              <span class="text-xs text-surface-500">{{ formatDate(session.sessionDate) }}</span>
            </div>
          </div>
          <div class="font-semibold text-surface-800 whitespace-nowrap">{{ session.formattedShotSum }}</div>
          <i class="pi pi-chevron-right text-surface-300 text-xs opacity-0 group-hover:opacity-100 transition-opacity"/>
        </div>
      </div>
    </template>
  </Card>
</template>

<script setup>
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import Card from 'primevue/card'
import Tag from 'primevue/tag'

defineProps({
  sessions: {type: Array, required: true}
})

const {t} = useI18n()
const router = useRouter()

function typeLabel(session) {
  return session.sessionType === 'COMPETITION' ? t('session.typeCompetition') : t('session.typeTraining')
}

function typeSeverity(session) {
  return session.sessionType === 'COMPETITION' ? 'danger' : 'success'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const [y, m, d] = dateStr.split('-')
  return `${d}.${m}.${y}`
}

function goTo(session) {
  router.push(session.sessionType === 'COMPETITION' ? '/competition' : '/training')
}
</script>
