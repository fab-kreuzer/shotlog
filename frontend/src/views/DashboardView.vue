<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">{{ greeting }}</h1>
      <p class="mt-1 text-surface-500">{{ $t('dashboard.welcome') }}</p>
    </div>

    <div class="flex flex-col gap-6">
      <SeasonStatsCard :season-name="activeSeasonName" :sessions="seasonSessions"/>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <RecentSessionsCard :sessions="recentSessions"/>
        <UpcomingSessionsCard :sessions="upcomingSessions"/>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api/http'
import {useAuthStore} from '@/stores/auth'
import {useSeasonFilter} from '@/composables/useSeasonFilter'
import SeasonStatsCard from '@/components/dashboard/SeasonStatsCard.vue'
import RecentSessionsCard from '@/components/dashboard/RecentSessionsCard.vue'
import UpcomingSessionsCard from '@/components/dashboard/UpcomingSessionsCard.vue'

const {t, locale} = useI18n()
const auth = useAuthStore()

const sessions = ref([])
const {options: seasons, filteredSessions: seasonSessions, loadSeasons} = useSeasonFilter(sessions)

const activeSeasonName = computed(() => seasons.value.find(s => s.active)?.description ?? '')

function sessionDateTime(session) {
  return new Date(`${session.sessionDate}T${session.sessionTime}`)
}

const recentSessions = computed(() => {
  const now = new Date()
  return sessions.value
      .filter(s => sessionDateTime(s) <= now)
      .sort((a, b) => sessionDateTime(b) - sessionDateTime(a))
      .slice(0, 5)
})

const upcomingSessions = computed(() => {
  const now = new Date()
  return sessions.value
      .filter(s => sessionDateTime(s) > now)
      .sort((a, b) => sessionDateTime(a) - sessionDateTime(b))
      .slice(0, 3)
})

async function loadDashboardData() {
  try {
    sessions.value = await api.getSessions()
  } catch (err) {
    console.error('Error loading sessions:', err)
  }
  await loadSeasons()
}

onMounted(loadDashboardData)

// Time-of-day bucket and a random variant, both fixed once per page load.
function currentBucket() {
  const h = new Date().getHours()
  if (h >= 5 && h < 10) return 'morning'
  if (h >= 10 && h < 16) return 'day'
  if (h >= 16 && h < 20) return 'evening'
  return 'night'
}

const bucket = currentBucket()
const variant = Math.floor(Math.random() * 5)
const name = computed(() => auth.user?.displayName || auth.user?.username || '')
const greeting = computed(() => {
  void locale.value // re-resolve when the UI language is toggled
  return t(`dashboard.greetings.${bucket}.${variant}`, {name: name.value})
})
</script>
