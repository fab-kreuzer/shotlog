<template>
  <Dialog v-model:visible="visible" :header="$t('dashboard.scan.title')" :style="{width: '28rem'}" modal
          @hide="stopScanner">
    <div v-if="step === 'scan'" class="flex flex-col gap-4">
      <p class="text-surface-500">{{ $t('dashboard.scan.hint') }}</p>
      <video ref="videoEl" class="w-full rounded-lg bg-surface-900 aspect-square object-cover"/>
      <p v-if="cameraError" class="text-red-500">{{ $t('dashboard.scan.cameraError') }}</p>
      <div class="flex gap-2">
        <InputText v-model="manualUrl" :placeholder="$t('dashboard.scan.manualPlaceholder')" class="flex-1"/>
        <Button :disabled="!manualUrl" :label="$t('common.confirm')" @click="handleLink(manualUrl)"/>
      </div>
      <p v-if="error" class="text-red-500">{{ error }}</p>
    </div>

    <div v-else-if="step === 'confirm' && preview" class="flex flex-col gap-4">
      <div class="rounded-lg bg-surface-100 dark:bg-surface-800 p-3">
        <div class="font-semibold">{{ preview.date }} {{ preview.time }}</div>
        <div>{{ $t('dashboard.scan.summary', {count: preview.shotCount, total: preview.total.toFixed(1)}) }}</div>
      </div>

      <p v-if="preview.alreadyImported" class="text-amber-600">{{ $t('dashboard.scan.alreadyImported') }}</p>

      <div class="flex flex-col gap-1">
        <label class="font-medium">{{ $t('dashboard.scan.type') }}</label>
        <SelectButton v-model="form.sessionType" :options="typeOptions" optionLabel="label" optionValue="value"
                      :allowEmpty="false"/>
      </div>

      <div class="flex flex-col gap-1">
        <label class="font-medium">{{ $t('dashboard.scan.place') }}</label>
        <Select v-model="form.enemyId" :options="locations" optionLabel="club" optionValue="id"/>
      </div>

      <div v-if="form.sessionType === 'COMPETITION' && visibleTeams.length" class="flex flex-col gap-1">
        <label class="font-medium">{{ $t('dashboard.scan.team') }}</label>
        <Select v-model="form.teamId" :options="visibleTeams" optionLabel="name" optionValue="id" showClear/>
      </div>

      <div v-if="preview.existingSessions.length" class="flex flex-col gap-2">
        <p class="font-medium">{{ $t('dashboard.scan.existing') }}</p>
        <div class="flex items-center gap-2">
          <RadioButton v-model="overwriteId" :value="NEW" inputId="scan-new"/>
          <label for="scan-new">{{ $t('dashboard.scan.createNew') }}</label>
        </div>
        <div v-for="s in preview.existingSessions" :key="s.id" class="flex items-center gap-2">
          <RadioButton v-model="overwriteId" :value="s.id" :inputId="`scan-ow-${s.id}`"/>
          <label :for="`scan-ow-${s.id}`">
            {{ $t('dashboard.scan.overwrite', {title: s.title || s.sessionType, total: s.formattedShotSum}) }}
          </label>
        </div>
      </div>

      <p v-if="error" class="text-red-500">{{ error }}</p>
    </div>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text @click="visible = false"/>
      <Button v-if="step === 'confirm'" :disabled="!form.enemyId || saving || (preview?.alreadyImported && overwriteId === NEW)"
              :label="$t('common.save')" :loading="saving" @click="submit"/>
    </template>
  </Dialog>
</template>

<script setup>
import {computed, nextTick, reactive, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import QrScanner from 'qr-scanner'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import SelectButton from 'primevue/selectbutton'
import RadioButton from 'primevue/radiobutton'
import {api} from '@/api/http'
import {useAuthStore} from '@/stores/auth'

const emit = defineEmits(['saved'])
const {t} = useI18n()
const auth = useAuthStore()

const visible = defineModel('visible', {default: false})
const step = ref('scan')
const videoEl = ref(null)
const cameraError = ref(false)
const error = ref('')
const manualUrl = ref('')
const saving = ref(false)
const preview = ref(null)
const NEW = 'new' // sentinel for "create a new session" (PrimeVue radios don't handle a null value reliably)
const overwriteId = ref(NEW)
const locations = ref([])
const seasons = ref([])
const assignedTeams = ref([])
let scanner = null
let scannedUrl = ''

const LAST_PLACE_KEY = 'shotlog.scan.lastPlace'
const form = reactive({sessionType: 'TRAINING', enemyId: null, teamId: null})

const typeOptions = computed(() => [
  {label: t('nav.training'), value: 'TRAINING'},
  {label: t('nav.competition'), value: 'COMPETITION'}
])
const activeSeason = computed(() => seasons.value.find(s => s.active))
const visibleTeams = computed(() => assignedTeams.value.filter(tm => tm.season?.id === activeSeason.value?.id))

async function loadOptions() {
  try {
    locations.value = await api.getLocations()
    seasons.value = await api.getSeasons()
    if (auth.user?.id != null) assignedTeams.value = await api.getAssignedTeams(auth.user.id)
    const last = Number(localStorage.getItem(LAST_PLACE_KEY))
    if (last && locations.value.some(l => l.id === last)) form.enemyId = last
  } catch (err) {
    console.error('Error loading scan options:', err)
  }
}

function stopScanner() {
  scanner?.stop()
  scanner?.destroy()
  scanner = null
}

async function startScanner() {
  await nextTick()
  if (!videoEl.value) return
  cameraError.value = false
  scanner = new QrScanner(videoEl.value, r => handleLink(r.data), {
    preferredCamera: 'environment',
    highlightScanRegion: true,
    returnDetailedScanResult: true
  })
  try {
    await scanner.start()
  } catch {
    cameraError.value = true
  }
}

async function handleLink(url) {
  if (!url || preview.value) return
  stopScanner()
  error.value = ''
  try {
    preview.value = await api.previewScan(url)
    scannedUrl = url
    overwriteId.value = NEW
    step.value = 'confirm'
  } catch (err) {
    error.value = err?.errors?.[0] || t('dashboard.scan.error')
    preview.value = null
    startScanner()
  }
}

async function submit() {
  saving.value = true
  error.value = ''
  try {
    await api.submitScan({
      url: scannedUrl,
      sessionType: form.sessionType,
      seasonId: activeSeason.value?.id,
      enemyId: form.enemyId,
      teamId: form.sessionType === 'COMPETITION' ? form.teamId : null,
      overwriteSessionId: overwriteId.value === NEW ? null : overwriteId.value
    })
    try { localStorage.setItem(LAST_PLACE_KEY, String(form.enemyId)) } catch { /* ignore */ }
    visible.value = false
    emit('saved')
  } catch (err) {
    error.value = err?.errors?.[0] || t('dashboard.scan.error')
  } finally {
    saving.value = false
  }
}

watch(visible, async (v) => {
  if (v) {
    step.value = 'scan'
    preview.value = null
    scannedUrl = ''
    manualUrl.value = ''
    error.value = ''
    await loadOptions()
    startScanner()
  } else {
    stopScanner()
  }
})
</script>
