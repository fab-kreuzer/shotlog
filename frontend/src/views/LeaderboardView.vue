<template>
  <div>
    <PageHeader :subtitle="$t('leaderboard.subtitle')" :title="$t('leaderboard.title')" color="purple"
                icon="pi pi-chart-bar">
      <template v-if="teams.length > 0" #actions>
        {{ $t('leaderboard.teamLabel') }}:
        <Select v-model="selectedTeamId" :options="teams" class="w-56" optionLabel="name" optionValue="id"
                @change="loadLeaderboard"/>
      </template>
    </PageHeader>

    <!-- Not assigned to any team -->
    <div v-if="!teamsLoading && teams.length === 0" class="text-center py-20">
      <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-surface-100 mb-4">
        <i class="pi pi-users text-surface-400" style="font-size: 2rem"/>
      </div>
      <h3 class="text-lg font-medium text-surface-700 mb-1">{{ $t('leaderboard.noTeams') }}</h3>
    </div>

    <template v-else>
      <!-- Loading skeleton -->
      <div v-if="loading" class="space-y-2">
        <div v-for="n in 6" :key="n" class="card-surface p-4 flex items-center gap-4">
          <Skeleton borderRadius="9999px" height="2.5rem" width="2.5rem"/>
          <div class="flex-1 flex flex-col gap-2">
            <Skeleton height="1rem" width="30%"/>
            <Skeleton height="0.8rem" width="20%"/>
          </div>
          <Skeleton height="1rem" width="10%"/>
          <Skeleton height="1rem" width="10%"/>
        </div>
      </div>

      <!-- Empty -->
      <div v-else-if="entries.length === 0" class="text-center py-20">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-surface-100 mb-4">
          <i class="pi pi-inbox text-surface-400" style="font-size: 2rem"/>
        </div>
        <h3 class="text-lg font-medium text-surface-700 mb-1">{{ $t('leaderboard.empty') }}</h3>
      </div>

      <!-- Leaderboard -->
      <div v-else class="space-y-2">
        <div
            v-for="entry in entries"
            :key="entry.userId"
            :class="entry.currentUser ? 'bg-primary-50 border-primary-200' : 'bg-surface-50 border-surface-200'"
            class="flex flex-wrap items-center gap-4 p-3 rounded-lg border"
        >
          <div :class="rankBadgeClasses(entry.rank)"
               class="flex items-center justify-center w-9 h-9 rounded-full font-semibold shrink-0">
            {{ entry.rank }}
          </div>

          <div class="min-w-40 flex-1">
            <p :class="entry.currentUser ? 'font-bold text-surface-900' : 'font-medium text-surface-900'">
              {{ entry.displayName || entry.username }}
              <span v-if="entry.currentUser"
                    class="ml-1.5 px-2 py-0.5 rounded-full bg-primary-100 text-primary-700 text-xs font-medium">
                {{ $t('leaderboard.you') }}
              </span>
            </p>
            <p class="text-sm text-surface-500">@{{ entry.username }}</p>
          </div>

          <div class="flex items-center gap-6 text-sm">
            <div class="text-center">
              <p class="text-xs text-surface-400">{{ $t('leaderboard.sessions') }}</p>
              <p class="font-medium text-surface-800">{{ entry.sessionCount }}</p>
            </div>
            <div class="text-center">
              <p class="text-xs text-surface-400">{{ $t('leaderboard.total') }}</p>
              <p class="font-medium text-surface-800">{{ fmt(entry.totalShotSum) }}</p>
            </div>
            <div class="text-center">
              <p class="text-xs text-surface-400">{{ $t('leaderboard.average') }}</p>
              <p class="font-medium text-surface-800">{{ fmt(entry.averagePerSession) }}</p>
            </div>
            <div class="text-center">
              <p class="text-xs text-surface-400">{{ $t('leaderboard.best') }}</p>
              <p class="font-medium text-surface-800">{{ fmt(entry.bestSessionShotSum) }}</p>
            </div>
            <div :title="trendTitle(entry.trend)" class="text-center">
              <p class="text-xs text-surface-400">&nbsp;</p>
              <i :class="trendIconClasses(entry.trend)"/>
            </div>
            <div class="text-center min-w-20">
              <p class="text-xs text-surface-400">&nbsp;</p>
              <span v-if="entry.rank === 1"
                    class="px-2 py-0.5 rounded-full bg-amber-100 text-amber-700 text-xs font-medium">
                {{ $t('leaderboard.leader') }}
              </span>
              <span v-else class="font-medium text-surface-600">
                &minus;{{ fmt(entry.gapToLeader) }} {{ $t('leaderboard.gapToLeader') }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import Select from 'primevue/select'
import Skeleton from 'primevue/skeleton'
import {api} from '@/api/http'
import {useAuthStore} from '@/stores/auth'
import PageHeader from '@/components/PageHeader.vue'

const {t} = useI18n()
const auth = useAuthStore()

const teams = ref([])
const teamsLoading = ref(true)
const selectedTeamId = ref(null)
const entries = ref([])
const loading = ref(false)

async function loadTeams() {
  teamsLoading.value = true
  try {
    teams.value = auth.user?.id ? await api.getAssignedTeams(auth.user.id) : []
    if (teams.value.length > 0) {
      selectedTeamId.value = teams.value[0].id
      await loadLeaderboard()
    }
  } catch (err) {
    console.error('Error loading teams:', err)
  } finally {
    teamsLoading.value = false
  }
}

async function loadLeaderboard() {
  if (selectedTeamId.value == null) {
    entries.value = []
    return
  }
  loading.value = true
  try {
    entries.value = await api.getLeaderboard(selectedTeamId.value)
  } catch (err) {
    console.error('Error loading leaderboard:', err)
    entries.value = []
  } finally {
    loading.value = false
  }
}

function rankBadgeClasses(rank) {
  if (rank === 1) return 'bg-amber-400 text-white'
  if (rank === 2) return 'bg-surface-300 text-surface-700'
  if (rank === 3) return 'bg-orange-400 text-white'
  return 'bg-surface-100 text-surface-600'
}

function trendIconClasses(trend) {
  switch (trend) {
    case 'UP':
      return 'pi pi-arrow-up text-green-500'
    case 'DOWN':
      return 'pi pi-arrow-down text-red-500'
    default:
      return 'pi pi-minus text-surface-400'
  }
}

function trendTitle(trend) {
  switch (trend) {
    case 'UP':
      return t('leaderboard.trendUp')
    case 'DOWN':
      return t('leaderboard.trendDown')
    default:
      return t('leaderboard.trendFlat')
  }
}

function fmt(n) {
  if (n == null) return '-'
  const num = Number(n)
  return Number.isInteger(num) ? String(num) : num.toFixed(1)
}

onMounted(loadTeams)
</script>
