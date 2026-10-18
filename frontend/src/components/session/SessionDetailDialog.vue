<template>
  <Dialog v-model:visible="visible" :header="session?.title || $t('session.detail.title')" modal
          :style="{width: '44rem', maxWidth: '95vw'}">
    <div v-if="session" class="flex flex-col gap-5">
      <div class="flex flex-wrap items-end justify-between gap-2">
        <div class="text-surface-500">{{ dateLabel }}</div>
        <div class="text-right">
          <div class="text-3xl font-bold text-surface-800">{{ session.formattedShotSum }}</div>
          <div v-if="session.decimalScoring" class="text-sm text-surface-500">
            {{ $t('session.detail.whole', {count: summary.wholeTotal}) }}
          </div>
        </div>
      </div>

      <div v-if="hasPositions" class="flex flex-col gap-3">
        <SessionTarget :highlight="highlight" :session="session" :show-group="showGroup" :show-mean="showMean"
                       :shot-numbers="shotNumbers" :max-width="460" class="mx-auto"/>
        <div class="flex flex-wrap gap-4 justify-center text-sm">
          <label class="flex items-center gap-2"><ToggleSwitch v-model="showGroup"/>{{ $t('session.detail.group') }}</label>
          <label class="flex items-center gap-2"><ToggleSwitch v-model="showMean"/>{{ $t('session.detail.mean') }}</label>
          <label class="flex items-center gap-2"><ToggleSwitch v-model="shotNumbers"/>{{ $t('session.detail.numbers') }}</label>
        </div>
      </div>
      <p v-else class="text-sm text-surface-500">{{ $t('session.detail.noPositions') }}</p>

      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
          <tr class="text-left text-surface-500">
            <th class="py-1 pr-3">{{ $t('session.detail.series') }}</th>
            <th class="py-1 pr-3">{{ $t('session.detail.shots') }}</th>
            <th class="py-1 text-right">{{ $t('session.total') }}</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="s in series" :key="s.number" class="border-t border-surface-200">
            <td class="py-1.5 pr-3 whitespace-nowrap">
              {{ $t('session.seriesLabel', {number: s.number}) }}
              <span v-if="s.testShot" class="text-xs text-surface-400">({{ $t('session.testShot') }})</span>
            </td>
            <td class="py-1.5 pr-3">
              <span v-for="(v, i) in s.shots" :key="i" class="inline-block mr-2 cursor-default rounded px-0.5"
                    :class="{'bg-primary-100 dark:bg-primary-400/20': highlight === globalIndex(s, i)}"
                    @mouseenter="highlight = globalIndex(s, i)" @mouseleave="highlight = undefined"
                    @click="highlight = globalIndex(s, i)">{{ fmt(v, session.decimalScoring ? 1 : 0) }}</span>
            </td>
            <td class="py-1.5 text-right font-medium whitespace-nowrap">
              {{ fmt(s.decimal, session.decimalScoring ? 1 : 0) }}
              <span v-if="session.decimalScoring" class="text-xs text-surface-400">({{ s.whole }})</span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <dl v-if="summary.count" class="grid grid-cols-2 sm:grid-cols-4 gap-3 text-sm">
        <div><dt class="text-surface-500">{{ $t('session.detail.best') }}</dt><dd class="font-medium">{{ fmt(summary.best) }}</dd></div>
        <div><dt class="text-surface-500">{{ $t('session.detail.worst') }}</dt><dd class="font-medium">{{ fmt(summary.worst) }}</dd></div>
        <div><dt class="text-surface-500">{{ $t('session.detail.average') }}</dt><dd class="font-medium">{{ fmt(summary.decimalTotal / summary.count, 2) }}</dd></div>
        <template v-if="positionSummary">
          <div><dt class="text-surface-500">{{ $t('session.detail.meanRadius') }}</dt><dd class="font-medium">{{ fmt(positionSummary.meanRadius_mm, 2) }} mm</dd></div>
          <div class="col-span-2 sm:col-span-4">
            <dt class="text-surface-500">{{ $t('session.detail.meanPoint') }}</dt>
            <dd class="font-medium">{{ meanPointText }}</dd>
          </div>
        </template>
      </dl>
    </div>
  </Dialog>
</template>

<script setup>
import {computed, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import Dialog from 'primevue/dialog'
import ToggleSwitch from 'primevue/toggleswitch'
import SessionTarget from '@/components/session/SessionTarget.vue'
import {useSessionTarget} from '@/composables/useSessionTarget'

const {t} = useI18n()
const visible = ref(false)
const session = ref(null)
const highlight = ref(undefined)
const showGroup = ref(false)
const showMean = ref(false)
const shotNumbers = ref(false)

const {hasPositions, series, summary, positionSummary, fmt} = useSessionTarget(session)

const dateLabel = computed(() => {
  const s = session.value
  if (!s) return ''
  const [y, m, d] = s.sessionDate.split('-')
  return `${d}.${m}.${y} ${s.sessionTime?.substring(0, 5) ?? ''}`
})

// Index of a shot among the drawn (positioned, non-test) shots, matching the target's highlight index.
function globalIndex(s, i) {
  if (s.testShot) return undefined
  const counted = series.value.filter(x => !x.testShot)
  let offset = 0
  for (const c of counted) {
    if (c.number === s.number) break
    offset += c.shots.length
  }
  return offset + i
}

const meanPointText = computed(() => {
  const p = positionSummary.value?.meanPoint
  if (!p) return ''
  const horiz = p.x_mm < 0 ? t('session.detail.left') : t('session.detail.right')
  const vert = p.y_mm < 0 ? t('session.detail.low') : t('session.detail.high')
  return `${fmt(Math.abs(p.x_mm), 2)} mm ${horiz}, ${fmt(Math.abs(p.y_mm), 2)} mm ${vert}`
})

function open(s) {
  session.value = s
  highlight.value = undefined
  visible.value = true
}

defineExpose({open})
</script>
