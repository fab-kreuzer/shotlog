<template>
  <Dialog v-model:visible="visible" :header="session?.title || $t('session.detail.title')" modal
          :style="{width: '62rem', maxWidth: '95vw'}">
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

      <div :class="hasPositions ? 'grid gap-5 md:grid-cols-[minmax(0,5fr)_minmax(0,6fr)] items-start' : 'flex flex-col gap-5'">
      <!-- Left: target with toggles -->
      <div v-if="hasPositions" class="flex flex-col gap-3">
        <SessionTarget :highlight="highlight" :only="selected" :session="session" :show-group="showGroup" :show-mean="showMean"
                       :shot-numbers="shotNumbers" :max-width="460" class="mx-auto"/>
        <div v-if="selected != null" class="flex items-center justify-center gap-2 text-sm">
          <span>{{ $t('session.detail.onlyShot', {number: selected + 1}) }}</span>
          <Button :label="$t('session.detail.showAll')" size="small" text @click="selected = undefined"/>
        </div>
        <div class="flex flex-wrap gap-4 justify-center text-sm">
          <label class="flex items-center gap-2"><ToggleSwitch v-model="showGroup"/>{{ $t('session.detail.group') }}</label>
          <label class="flex items-center gap-2"><ToggleSwitch v-model="showMean"/>{{ $t('session.detail.mean') }}</label>
          <label class="flex items-center gap-2"><ToggleSwitch v-model="shotNumbers"/>{{ $t('session.detail.numbers') }}</label>
        </div>
      </div>
      <p v-else class="text-sm text-surface-500">{{ $t('session.detail.noPositions') }}</p>

      <!-- Right: series table and statistics -->
      <div :class="hasPositions ? 'md:max-h-[70vh] md:overflow-y-auto md:pr-2' : ''" class="flex flex-col gap-5 min-w-0">
      <div class="flex flex-col gap-3">
        <div v-for="s in series" :key="s.number"
             class="rounded-xl border border-surface-200 bg-surface-50 overflow-hidden">
          <div class="flex items-center justify-between px-3 py-2 bg-surface-100 border-b border-surface-200">
            <span class="text-sm font-semibold text-surface-700">
              {{ $t('session.seriesLabel', {number: s.number}) }}
              <span v-if="s.testShot" class="ml-1 text-xs font-normal text-surface-400">({{ $t('session.testShot') }})</span>
            </span>
            <span class="text-sm font-semibold text-surface-800">
              {{ fmt(s.decimal, session.decimalScoring ? 1 : 0) }}
              <span v-if="session.decimalScoring" class="ml-1 text-xs font-normal text-surface-400">({{ s.whole }})</span>
            </span>
          </div>
          <div :class="hasPositions ? 'grid-cols-5' : 'grid-cols-5 sm:grid-cols-10'" class="p-3 grid gap-2">
            <button v-for="(v, i) in s.shots" :key="i" type="button"
                    :disabled="!selectable(s)"
                    :title="$t('session.detail.shotTitle', {number: i + 1})"
                    class="relative flex flex-col items-center rounded-lg border px-1 py-1.5 text-sm font-medium transition-colors bg-card"
                    :class="[
                      selected !== undefined && selected === globalIndex(s, i)
                        ? 'border-primary-500 ring-2 ring-primary-500/40'
                        : 'border-surface-200',
                      highlight === globalIndex(s, i) ? 'bg-primary-50 dark:bg-primary-400/10' : '',
                      selectable(s) ? 'cursor-pointer hover:border-primary-400' : 'cursor-default'
                    ]"
                    @mouseenter="highlight = globalIndex(s, i)" @mouseleave="highlight = undefined"
                    @click="toggleSelected(globalIndex(s, i))">
              <span class="text-[0.65rem] leading-none text-surface-400">{{ i + 1 }}</span>
              <span class="mt-1 tabular-nums">{{ fmt(v, session.decimalScoring ? 1 : 0) }}</span>
              <span class="mt-1 h-1 w-6 rounded-full" :style="{background: dotColor(v)}"/>
            </button>
          </div>
        </div>
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
      </div>
    </div>
  </Dialog>
</template>

<script setup>
import {computed, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import ToggleSwitch from 'primevue/toggleswitch'
import {THEME} from '@/lib/shotsapp-target'
import SessionTarget from '@/components/session/SessionTarget.vue'
import {useSessionTarget} from '@/composables/useSessionTarget'

const {t} = useI18n()
const visible = ref(false)
const session = ref(null)
const highlight = ref(undefined)
const selected = ref(undefined)
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

// Same colour scale as the target (DISAG app): >= 10.0 red, >= 9.0 gold, otherwise green.
function dotColor(v) {
  return v >= 10 ? THEME.danger : v >= 9 ? THEME.warning : THEME.success
}

function selectable(s) {
  return hasPositions.value && !s.testShot
}

// Click a shot to show only that one on the target; click it again to show all.
function toggleSelected(index) {
  if (!hasPositions.value || index === undefined) return
  selected.value = selected.value === index ? undefined : index
}

function open(s) {
  session.value = s
  highlight.value = undefined
  selected.value = undefined
  visible.value = true
}

defineExpose({open})
</script>
