<template>
  <Card>
    <template #title>
      <div class="flex items-center gap-2 text-base">
        <i class="pi pi-chart-bar text-surface-400"/>
        <span>{{ $t('dashboard.seasonStats', {season: seasonName}) }}</span>
      </div>
    </template>
    <template #content>
      <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
        <div
            v-for="stat in stats"
            :key="stat.label"
            class="flex items-center gap-3 rounded-xl border border-surface-200 p-3.5"
        >
          <div
              :class="[stat.iconBg, stat.iconColor]"
              class="flex items-center justify-center w-10 h-10 rounded-full shrink-0"
          >
            <i :class="stat.icon" class="text-base"/>
          </div>
          <div class="min-w-0">
            <div class="text-xl font-semibold text-surface-800 leading-tight truncate">{{ stat.value }}</div>
            <div class="text-xs text-surface-500 truncate">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </template>
  </Card>
</template>

<script setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import Card from 'primevue/card'

const props = defineProps({
  sessions: {type: Array, required: true},
  seasonName: {type: String, default: ''}
})

const {t} = useI18n()

const stats = computed(() => {
  const trainings = props.sessions.filter(s => s.sessionType === 'TRAINING')
  const competitions = props.sessions.filter(s => s.sessionType === 'COMPETITION')
  const competitionScores = competitions.map(s => parseFloat(s.formattedShotSum)).filter(v => !Number.isNaN(v))

  const avgScore = competitionScores.length
      ? (competitionScores.reduce((a, b) => a + b, 0) / competitionScores.length).toFixed(1)
      : '–'
  const bestScore = competitionScores.length ? Math.max(...competitionScores).toFixed(1) : '–'
  const homeCount = competitions.filter(s => s.home).length
  const awayCount = competitions.length - homeCount

  return [
    {
      label: t('dashboard.stats.trainings'),
      value: trainings.length,
      icon: 'pi pi-bullseye',
      iconBg: 'bg-green-50 dark:bg-green-400/10',
      iconColor: 'text-green-600 dark:text-green-400'
    },
    {
      label: t('dashboard.stats.competitions'), value: competitions.length,
      icon: 'pi pi-trophy', iconBg: 'bg-red-50 dark:bg-red-400/10', iconColor: 'text-red-600 dark:text-red-400'
    },
    {
      label: t('dashboard.stats.avgScore'), value: avgScore,
      icon: 'pi pi-chart-line', iconBg: 'bg-blue-50 dark:bg-blue-400/10', iconColor: 'text-blue-600 dark:text-blue-400'
    },
    {
      label: t('dashboard.stats.bestScore'),
      value: bestScore,
      icon: 'pi pi-star-fill',
      iconBg: 'bg-amber-50 dark:bg-amber-400/10',
      iconColor: 'text-amber-600 dark:text-amber-400'
    },
    {
      label: t('dashboard.stats.homeAway'),
      value: `${homeCount} / ${awayCount}`,
      icon: 'pi pi-map-marker',
      iconBg: 'bg-purple-50 dark:bg-purple-400/10',
      iconColor: 'text-purple-600 dark:text-purple-400'
    }
  ]
})
</script>
