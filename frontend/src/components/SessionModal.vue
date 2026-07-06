<template>
  <Dialog
      :draggable="false"
      :header="isEditing ? $t('session.editTitle') : $t('session.createTitle')"
      :style="{ width: '48rem' }"
      :visible="visible"
      class="max-w-[95vw]"
      modal
      @update:visible="onVisibleChange"
  >
    <form id="session-form" class="flex flex-col gap-6" @submit.prevent="handleSubmit">
      <!-- Session data -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('common.description') }}</label>
          <InputText v-model="form.title" fluid required/>
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('common.date') }}</label>
          <DatePicker v-model="sessionDateModel" dateFormat="dd.mm.yy" fluid showIcon/>
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('session.startTime') }}</label>
          <DatePicker v-model="sessionTimeModel" fluid showIcon timeOnly/>
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('session.opponent') }}</label>
          <Select
              v-model="form.enemyId"
              :options="locations"
              fluid
              optionLabel="club"
              optionValue="id"
          />
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('session.shootingType') }}</label>
          <Select
              v-model="form.sessionType"
              :options="sessionTypeOptions"
              fluid
              optionLabel="label"
              optionValue="value"
          />
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('session.season') }}</label>
          <Select
              v-model="form.seasonId"
              :options="seasons"
              fluid
              optionLabel="description"
              optionValue="id"
          />
        </div>

        <div class="flex items-end gap-5 sm:col-span-2 lg:col-span-2">
          <label class="flex items-center gap-2 cursor-pointer">
            <Checkbox v-model="form.decimalScoring" binary/>
            <span class="text-sm text-surface-700">{{ $t('session.decimalScoring') }}</span>
          </label>
          <label class="flex items-center gap-2 cursor-pointer">
            <Checkbox v-model="form.home" binary/>
            <span class="text-sm text-surface-700">{{ $t('session.homeMatch') }}</span>
          </label>
        </div>
      </div>

      <hr class="border-surface-200">

      <!-- Series -->
      <div ref="seriesContainerRef" class="flex flex-col gap-4">
        <div v-for="(series, sIndex) in form.series" :key="sIndex"
             class="bg-surface-50 rounded-xl border border-surface-200 overflow-hidden">
          <!-- Series header -->
          <div class="flex items-center justify-between px-4 py-3 bg-surface-100 border-b border-surface-200">
            <span class="text-sm font-semibold text-surface-700">{{
                $t('session.seriesLabel', {number: sIndex + 1})
              }}</span>
            <div class="flex items-center gap-3">
              <label class="flex items-center gap-2 cursor-pointer">
                <Checkbox v-model="series.testShot" binary/>
                <span class="text-xs text-surface-600">{{ $t('session.testShot') }}</span>
              </label>
              <Button
                  icon="pi pi-trash"
                  :label="$t('session.remove')"
                  severity="danger"
                  size="small"
                  text
                  type="button"
                  @click="removeSeries(sIndex)"
              />
            </div>
          </div>

          <!-- Shots grid -->
          <div class="p-4 grid grid-cols-2 sm:grid-cols-5 gap-3">
            <div v-for="(shot, shotIndex) in series.shots" :key="shotIndex" class="flex flex-col gap-1">
              <label class="text-xs font-medium text-surface-500">{{
                  $t('session.shotLabel', {number: shotIndex + 1})
                }}</label>
              <InputNumber
                  v-model="shot.value"
                  :min="0.0"
                  :minFractionDigits="1"
                  :step="0.1"
                  inputId="minmax-buttons"
                  mode="decimal"
                  :max="10.9"
                  showButtons
                  :maxFractionDigits="1"
                  fluid
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Add series button -->
      <Button
          class="w-full"
          icon="pi pi-plus"
          :label="$t('session.addSeries')"
          outlined
          severity="secondary"
          type="button"
          @click="addSeries"
      />
    </form>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text type="button" @click="close"/>
      <Button :label="$t('common.save')" form="session-form" type="submit"/>
    </template>
  </Dialog>
</template>

<script setup>
import {computed, nextTick, onMounted, reactive, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import DatePicker from 'primevue/datepicker'
import Select from 'primevue/select'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import {api} from '@/api/http'

const emit = defineEmits(['saved'])

const {t} = useI18n()

const visible = ref(false)
const locations = ref([])
const seasons = ref([])
const isEditing = ref(false)
const editingId = ref(null)
const seriesContainerRef = ref(null)

const sessionTypeOptions = [
  {label: t('session.typeTraining'), value: 'TRAINING'},
  {label: t('session.typeCompetition'), value: 'COMPETITION'}
]

const form = reactive({
  sessionDate: '',
  sessionTime: '',
  enemyId: null,
  seasonId: null,
  title: '',
  sessionType: 'TRAINING',
  decimalScoring: false,
  home: false,
  series: []
})

// DatePicker works with Date objects; the form/API use 'yyyy-MM-dd' and 'HH:mm' strings.
function pad(n) {
  return String(n).padStart(2, '0')
}

const sessionDateModel = computed({
  get: () => (form.sessionDate ? new Date(`${form.sessionDate}T00:00:00`) : null),
  set: (d) => {
    form.sessionDate = d ? `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` : ''
  }
})

const sessionTimeModel = computed({
  get: () => {
    if (!form.sessionTime) return null
    const [h, m] = form.sessionTime.split(':')
    const d = new Date()
    d.setHours(Number(h), Number(m), 0, 0)
    return d
  },
  set: (d) => {
    form.sessionTime = d ? `${pad(d.getHours())}:${pad(d.getMinutes())}` : ''
  }
})

function createEmptySeries() {
  return {
    seriesNumber: form.series.length + 1,
    testShot: false,
    shots: Array.from({length: 10}, (_, i) => ({
      shotNumber: i + 1,
      value: 0
    }))
  }
}

function addSeries() {
  form.series.push(createEmptySeries())
  nextTick(() => {
    if (seriesContainerRef.value && seriesContainerRef.value.lastElementChild) {
      seriesContainerRef.value.lastElementChild.scrollIntoView({behavior: 'smooth', block: 'end'})
    }
  })
}

function removeSeries(index) {
  form.series.splice(index, 1)
  form.series.forEach((s, i) => {
    s.seriesNumber = i + 1
  })
}

function resetForm() {
  const now = new Date()
  form.sessionDate = now.toISOString().split('T')[0]
  form.sessionTime = String(now.getHours()).padStart(2, '0') + ':' + String(now.getMinutes()).padStart(2, '0')
  form.enemyId = locations.value.length > 0 ? locations.value[0].id : null
  form.seasonId = (seasons.value.find(s => s.active) ?? seasons.value[0])?.id ?? null
  form.title = ''
  form.sessionType = 'TRAINING'
  form.decimalScoring = false
  form.home = false
  form.series = [createEmptySeries()]
  isEditing.value = false
  editingId.value = null
}

function openCreate(type) {
  resetForm()
  if (type) form.sessionType = type
  visible.value = true
}

function openEdit(session) {
  isEditing.value = true
  editingId.value = session.id
  form.sessionDate = session.sessionDate
  form.sessionTime = session.sessionTime
  form.title = session.title
  form.enemyId = session.enemy ? session.enemy.id : null
  form.seasonId = session.season ? session.season.id : null
  form.sessionType = session.sessionType
  form.decimalScoring = session.decimalScoring
  form.home = session.home

  form.series = (session.series || []).map((s, i) => ({
    seriesNumber: i + 1,
    testShot: s.testShot,
    shots: (s.shots || []).map((shot, si) => ({
      shotNumber: si + 1,
      value: shot.value || 0
    }))
  }))

  form.series.forEach(s => {
    while (s.shots.length < 10) {
      s.shots.push({shotNumber: s.shots.length + 1, value: 0})
    }
  })

  visible.value = true
}

function close() {
  visible.value = false
}

function onVisibleChange(value) {
  visible.value = value
}

async function handleSubmit() {
  const data = {
    sessionDate: form.sessionDate,
    sessionTime: form.sessionTime,
    enemyId: form.enemyId,
    seasonId: form.seasonId,
    sessionType: form.sessionType,
    title: form.title,
    decimalScoring: form.decimalScoring,
    home: form.home,
    series: form.series.map((s, i) => ({
      seriesNumber: i + 1,
      testShot: s.testShot,
      shots: s.shots.map((shot, si) => ({
        shotNumber: si + 1,
        value: shot.value
      }))
    }))
  }

  try {
    if (isEditing.value) {
      await api.updateSession(editingId.value, data)
    } else {
      await api.createSession(data)
    }
    close()
    emit('saved')
  } catch (err) {
    console.error('Error saving session:', err)
    alert(t('session.saveError'))
  }
}

onMounted(async () => {
  try {
    locations.value = await api.getLocations()
  } catch (err) {
    console.error('Error fetching locations:', err)
  }
  try {
    seasons.value = await api.getSeasons()
  } catch (err) {
    console.error('Error fetching seasons:', err)
  }
})

defineExpose({openCreate, openEdit})
</script>
