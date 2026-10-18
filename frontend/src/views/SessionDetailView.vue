<template>
  <div>
    <div v-if="loading" class="flex flex-col gap-4">
      <Skeleton height="4rem" width="40%"/>
      <Skeleton height="24rem"/>
    </div>

    <template v-else-if="session">
      <PageHeader :color="session.sessionType === 'COMPETITION' ? 'red' : 'green'"
                  :icon="session.sessionType === 'COMPETITION' ? 'pi pi-trophy' : 'pi pi-bullseye'"
                  :subtitle="dateLabel" :title="session.title">
        <template #actions>
          <div class="mr-2 text-right">
            <div class="text-3xl font-bold leading-none text-surface-800">{{ session.formattedShotSum }}</div>
            <div v-if="session.decimalScoring" class="mt-1 text-sm text-surface-500">
              {{ $t('session.detail.whole', {count: summary.wholeTotal}) }}
            </div>
          </div>
          <Button :label="$t('common.edit')" icon="pi pi-pencil" severity="secondary" @click="router.push(`${listPath}/${props.id}/edit`)"/>
          <Button :label="$t('common.delete')" icon="pi pi-trash" severity="danger" outlined
                  @click="showDeleteConfirm = true"/>
        </template>
      </PageHeader>

      <div class="flex flex-col gap-6">
        <!-- Target and results -->
        <div :class="hasPositions ? 'lg:grid-cols-[minmax(0,5fr)_minmax(0,6fr)]' : ''" class="grid items-start gap-6">
          <section v-if="hasPositions" class="card-surface flex flex-col gap-3 p-4">
            <SessionTarget :auto-zoom="autoZoom" :highlight="highlight" :max-width="560" :only="selected"
                           :session="session" :shot-numbers="shotNumbers" :show-group="showGroup"
                           :show-mean="showMean" class="mx-auto"/>
            <div v-if="selected != null" class="flex items-center justify-center gap-2 text-sm">
              <span>{{ $t('session.detail.onlyShot', {number: selected + 1}) }}</span>
              <Button :label="$t('session.detail.showAll')" size="small" text @click="selected = undefined"/>
            </div>
            <div class="flex flex-wrap justify-center gap-4 text-sm">
              <label class="flex items-center gap-2"><ToggleSwitch v-model="autoZoom"/>{{ $t('session.detail.autoZoom') }}</label>
              <label class="flex items-center gap-2"><ToggleSwitch v-model="showGroup"/>{{ $t('session.detail.group') }}</label>
              <label class="flex items-center gap-2"><ToggleSwitch v-model="showMean"/>{{ $t('session.detail.mean') }}</label>
              <label class="flex items-center gap-2"><ToggleSwitch v-model="shotNumbers"/>{{ $t('session.detail.numbers') }}</label>
            </div>
          </section>

          <section class="flex min-w-0 flex-col gap-5">
            <p v-if="!hasPositions" class="text-sm text-surface-500">{{ $t('session.detail.noPositions') }}</p>

            <div class="flex flex-col gap-3">
              <div v-for="s in series" :key="s.number" class="overflow-hidden rounded-xl border border-surface-200 bg-surface-50">
                <div class="flex items-center justify-between border-b border-surface-200 bg-surface-100 px-3 py-2">
                  <span class="text-sm font-semibold text-surface-700">
                    {{ $t('session.seriesLabel', {number: s.number}) }}
                    <span v-if="s.testShot" class="ml-1 text-xs font-normal text-surface-400">({{ $t('session.testShot') }})</span>
                  </span>
                  <span class="text-sm font-semibold text-surface-800">
                    {{ fmt(s.decimal, session.decimalScoring ? 1 : 0) }}
                    <span v-if="session.decimalScoring" class="ml-1 text-xs font-normal text-surface-400">({{ s.whole }})</span>
                  </span>
                </div>
                <div class="grid grid-cols-5 gap-2 p-3 sm:grid-cols-10">
                  <button v-for="(v, i) in s.shots" :key="i" :disabled="!selectable(s)"
                          :class="[
                            selected !== undefined && selected === globalIndex(s, i)
                              ? 'border-primary-500 ring-2 ring-primary-500/40' : 'border-surface-200',
                            highlight === globalIndex(s, i) ? 'bg-primary-50 dark:bg-primary-400/10' : '',
                            selectable(s) ? 'cursor-pointer hover:border-primary-400' : 'cursor-default'
                          ]"
                          :title="$t('session.detail.shotTitle', {number: i + 1})"
                          class="relative flex flex-col items-center rounded-lg border bg-card px-1 py-1.5 text-sm font-medium transition-colors"
                          type="button"
                          @click="toggleSelected(globalIndex(s, i))"
                          @mouseenter="highlight = globalIndex(s, i)" @mouseleave="highlight = undefined">
                    <span class="text-[0.65rem] leading-none text-surface-400">{{ i + 1 }}</span>
                    <span class="mt-1 tabular-nums">{{ fmt(v, session.decimalScoring ? 1 : 0) }}</span>
                    <span :style="{background: shotColor(v)}" class="mt-1 h-1 w-6 rounded-full"/>
                  </button>
                </div>
              </div>
            </div>

            <dl v-if="summary.count" class="grid grid-cols-2 gap-3 text-sm sm:grid-cols-4">
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

            <div v-if="summary.count">
              <h4 class="mb-2 text-sm font-semibold text-surface-700">{{ $t('session.detail.distribution') }}</h4>
              <div class="overflow-x-auto rounded-xl border border-surface-200">
                <table class="w-full text-center text-sm">
                  <thead>
                  <tr class="bg-surface-100 text-surface-500">
                    <th class="px-2 py-1.5 text-left font-medium">{{ $t('session.detail.ring') }}</th>
                    <th v-for="d in distribution" :key="d.ring" class="px-1 py-1.5 font-medium">{{ d.ring }}</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr class="border-t border-surface-200">
                    <td class="px-2 py-1.5 text-left text-surface-500">{{ $t('session.detail.count') }}</td>
                    <td v-for="d in distribution" :key="d.ring" :class="d.count ? 'font-semibold text-surface-800' : 'text-surface-300'"
                        class="px-1 py-1.5 tabular-nums">{{ d.count }}</td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        </div>

        <!-- Time -->
        <section v-if="hasTimes" class="card-surface flex flex-col gap-4 p-4">
          <h2 class="text-lg font-semibold text-surface-800">{{ $t('session.page.timeHeading') }}</h2>
          <dl class="grid grid-cols-2 gap-3 text-sm sm:grid-cols-5">
            <div><dt class="text-surface-500">{{ $t('session.detail.time.total') }}</dt><dd class="font-medium">{{ fmtDuration(timeStats.total) }}</dd></div>
            <div><dt class="text-surface-500">{{ $t('session.detail.time.average') }}</dt><dd class="font-medium">{{ fmt(timeStats.average, 1) }} s</dd></div>
            <div><dt class="text-surface-500">{{ $t('session.detail.time.median') }}</dt><dd class="font-medium">{{ fmt(timeStats.median, 1) }} s</dd></div>
            <div><dt class="text-surface-500">{{ $t('session.detail.time.fastest') }}</dt><dd class="font-medium">{{ timeStats.min }} s</dd></div>
            <div><dt class="text-surface-500">{{ $t('session.detail.time.slowest') }}</dt><dd class="font-medium">{{ timeStats.max }} s</dd></div>
          </dl>
          <ShotTimeChart :average="timeStats.average" :highlight="highlight" :median="timeStats.median"
                         :points="timePoints" :selected="selected" @hover="highlight = $event"
                         @select="toggleSelected"/>
          <div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-surface-500">
            <span class="flex items-center gap-1.5"><span class="inline-block h-0.5 w-5" style="background: var(--color-primary-500, #2a9d8f)"/>{{ $t('session.detail.time.averageLine') }}</span>
            <span class="flex items-center gap-1.5"><span class="inline-block w-5 border-t-2 border-dashed border-current"/>{{ $t('session.detail.time.medianLine') }}</span>
            <span>{{ $t('session.detail.time.hint') }}</span>
          </div>
          <div>
            <h4 class="mb-2 text-sm font-semibold text-surface-700">{{ $t('session.detail.time.perSeries') }}</h4>
            <div class="flex flex-wrap gap-2">
              <span v-for="ps in timeStats.perSeries" :key="ps.number"
                    class="rounded-lg border border-surface-200 bg-surface-50 px-3 py-1.5 text-sm">
                {{ $t('session.seriesLabel', {number: ps.number}) }}:
                <span class="font-medium">{{ fmtDuration(ps.seconds) }}</span>
              </span>
            </div>
          </div>
        </section>

        <!-- Playback -->
        <section v-if="hasPositions" class="card-surface flex flex-col gap-4 p-4">
          <h2 class="text-lg font-semibold text-surface-800">{{ $t('session.page.playbackHeading') }}</h2>
          <div class="grid items-center gap-6 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
            <SessionTarget :max-width="460" :session="session" :up-to="playback.shown.value" class="mx-auto" shot-numbers/>
            <div class="flex flex-col gap-5">
              <div class="flex items-center gap-3">
                <Button :aria-label="playback.playing.value ? $t('session.detail.playback.pause') : $t('session.detail.playback.play')"
                        :icon="playback.playing.value ? 'pi pi-pause' : 'pi pi-play'" rounded @click="playback.toggle()"/>
                <Slider :max="playback.count.value" :min="0" :model-value="playback.shown.value" :step="1" class="flex-1"
                        @update:model-value="playback.seek($event)"/>
                <span class="w-16 text-right text-sm tabular-nums">{{ playback.shown.value }} / {{ playback.count.value }}</span>
              </div>
              <div class="flex items-center gap-2 text-sm">
                <span class="text-surface-500">{{ $t('session.detail.playback.speed') }}</span>
                <SelectButton :allow-empty="false" :model-value="playback.speed.value" :options="PLAYBACK_SPEEDS"
                              size="small" @update:model-value="playback.setSpeed($event)">
                  <template #option="{option}">{{ option }}x</template>
                </SelectButton>
              </div>
            </div>
          </div>
        </section>
      </div>

      <ConfirmModal v-model="showDeleteConfirm" :confirm-text="$t('shooting.confirmDeleteText')"
                    :message="$t('shooting.deleteMessage')" :title="$t('shooting.deleteTitle')"
                    @confirm="confirmDelete"/>
    </template>
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import Button from 'primevue/button'
import Skeleton from 'primevue/skeleton'
import Slider from 'primevue/slider'
import SelectButton from 'primevue/selectbutton'
import ToggleSwitch from 'primevue/toggleswitch'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'
import {useBreadcrumbStore} from '@/stores/breadcrumb'
import PageHeader from '@/components/PageHeader.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import SessionTarget from '@/components/session/SessionTarget.vue'
import ShotTimeChart from '@/components/session/ShotTimeChart.vue'
import {useSessionTarget} from '@/composables/useSessionTarget'
import {useShotTimes} from '@/composables/useShotTimes'
import {PLAYBACK_SPEEDS, usePlayback} from '@/composables/usePlayback'
import {shotColor} from '@/lib/shotColor'

const props = defineProps({
  id: {type: String, required: true},
  type: {type: String, required: true} // 'TRAINING' | 'COMPETITION' from the route
})

const {t} = useI18n()
const router = useRouter()
const notify = useNotificationStore()
const breadcrumb = useBreadcrumbStore()

const session = ref(null)
const loading = ref(true)
const showDeleteConfirm = ref(false)
const highlight = ref(undefined)
const selected = ref(undefined)
const autoZoom = ref(true)
const showGroup = ref(false)
const showMean = ref(false)
const shotNumbers = ref(false)

const {points: timePoints, hasTimes, stats: timeStats, fmtDuration} = useShotTimes(session)
const {hasPositions, positioned, distribution, series, summary, positionSummary, fmt} = useSessionTarget(session)
const playback = usePlayback(() => positioned.value.length)

const listPath = computed(() => (props.type === 'COMPETITION' ? '/competition' : '/training'))

const dateLabel = computed(() => {
  const s = session.value
  if (!s) return ''
  const [y, m, d] = s.sessionDate.split('-')
  return `${d}.${m}.${y} ${s.sessionTime?.substring(0, 5) ?? ''}`
})

// Index of a shot among the drawn (counted, non-test) shots, matching the target's highlight index.
function globalIndex(s, i) {
  if (s.testShot) return undefined
  let offset = 0
  for (const c of series.value.filter(x => !x.testShot)) {
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

function selectable(s) {
  return hasPositions.value && !s.testShot
}

// Click a shot to show only that one on the target; click it again to show all.
function toggleSelected(index) {
  if (!hasPositions.value || index === undefined) return
  selected.value = selected.value === index ? undefined : index
}

async function load() {
  loading.value = session.value === null
  try {
    const loaded = await api.getSession(props.id)
    // An id of the other type lives under the other path.
    if (loaded.sessionType !== props.type) {
      return router.replace(`${loaded.sessionType === 'COMPETITION' ? '/competition' : '/training'}/${loaded.id}`)
    }
    session.value = loaded
    breadcrumb.setLabel(loaded.title)
    document.title = `ShotLog - ${loaded.title}`
  } catch (err) {
    if (!err?._notified) notify.error(t('session.page.notFound'))
    router.replace(listPath.value)
  } finally {
    loading.value = false
  }
}

async function confirmDelete() {
  try {
    await api.deleteSession(props.id)
    router.push(listPath.value)
  } catch (err) {
    if (!err?._notified) notify.error(t('session.deleteError'))
  }
}

watch(() => props.id, () => {
  session.value = null
  selected.value = undefined
  playback.reset()
  load()
}, {immediate: true})

onBeforeUnmount(() => breadcrumb.setLabel(''))
</script>
