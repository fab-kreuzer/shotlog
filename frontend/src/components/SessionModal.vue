<template>
  <!-- Modal backdrop -->
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="fixed inset-0 z-50 flex items-start justify-center pt-8 pb-8 px-4 overflow-y-auto">
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="close"></div>

        <!-- Modal content -->
        <div class="relative w-full max-w-3xl bg-card rounded-2xl shadow-2xl" style="animation: scaleIn 0.2s ease-out">
          <form @submit.prevent="handleSubmit">
            <!-- Header -->
            <div class="flex items-center justify-between px-6 py-4 border-b border-surface-200">
              <h3 class="text-lg font-semibold text-surface-800">
                {{ isEditing ? 'Schießabend bearbeiten' : 'Neuen Schießabend erstellen' }}
              </h3>
              <button
                  class="p-2 rounded-lg text-surface-400 hover:text-surface-600 hover:bg-surface-100 transition-colors"
                  type="button"
                  @click="close"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path d="M6 18L18 6M6 6l12 12" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
                </svg>
              </button>
            </div>

            <!-- Body -->
            <div class="px-6 py-5 max-h-[70vh] overflow-y-auto space-y-6">
              <!-- Session data -->
              <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Beschreibung</label>
                  <input
                      v-model="form.title"
                      class="w-full px-3 py-2 rounded-lg border border-surface-300 text-surface-800 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
                      required
                      type="text"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Datum</label>
                  <input
                      v-model="form.sessionDate"
                      class="w-full px-3 py-2 rounded-lg border border-surface-300 text-surface-800 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
                      required
                      type="date"
                  >
                </div>

                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Startzeit</label>
                  <input
                      v-model="form.sessionTime"
                      class="w-full px-3 py-2 rounded-lg border border-surface-300 text-surface-800 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
                      required
                      type="time"
                  >
                </div>

                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Gegner</label>
                  <select
                      v-model="form.enemyId"
                      class="w-full px-3 py-2 rounded-lg border border-surface-300 text-surface-800 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow bg-card"
                      required
                  >
                    <option v-for="loc in locations" :key="loc.id" :value="loc.id">{{ loc.club }}</option>
                  </select>
                </div>

                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Art des Schießens</label>
                  <select
                      v-model="form.sessionType"
                      class="w-full px-3 py-2 rounded-lg border border-surface-300 text-surface-800 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow bg-card"
                      required
                  >
                    <option value="TRAINING">Training</option>
                    <option value="COMPETITION">Wettkampf</option>
                  </select>
                </div>

                <div class="flex items-end gap-5 sm:col-span-2 lg:col-span-2">
                  <label class="flex items-center gap-2 cursor-pointer">
                    <input
                        v-model="form.decimalScoring"
                        class="w-4 h-4 rounded border-surface-300 text-primary-600 focus:ring-primary-500"
                        type="checkbox"
                    >
                    <span class="text-sm text-surface-700">Zehntelwertung</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <input
                        v-model="form.home"
                        class="w-4 h-4 rounded border-surface-300 text-primary-600 focus:ring-primary-500"
                        type="checkbox"
                    >
                    <span class="text-sm text-surface-700">Heimwettkampf</span>
                  </label>
                </div>
              </div>

              <!-- Divider -->
              <hr class="border-surface-200">

              <!-- Series -->
              <div ref="seriesContainerRef" class="space-y-4">
                <div v-for="(series, sIndex) in form.series" :key="sIndex"
                     class="bg-surface-50 rounded-xl border border-surface-200 overflow-hidden">
                  <!-- Series header -->
                  <div class="flex items-center justify-between px-4 py-3 bg-surface-100 border-b border-surface-200">
                    <span class="text-sm font-semibold text-surface-700">Serie {{ sIndex + 1 }}</span>
                    <div class="flex items-center gap-3">
                      <label class="flex items-center gap-2 cursor-pointer">
                        <input
                            v-model="series.testShot"
                            class="w-4 h-4 rounded border-surface-300 text-primary-600 focus:ring-primary-500"
                            type="checkbox"
                        >
                        <span class="text-xs text-surface-600">Probe</span>
                      </label>
                      <button
                          class="flex items-center gap-1 px-2.5 py-1 text-xs font-medium text-danger-600 hover:bg-danger-50 rounded-lg transition-colors"
                          type="button"
                          @click="removeSeries(sIndex)"
                      >
                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" stroke-linecap="round" stroke-linejoin="round"
                                stroke-width="2"/>
                        </svg>
                        Entfernen
                      </button>
                    </div>
                  </div>

                  <!-- Shots grid -->
                  <div class="p-4 grid grid-cols-2 sm:grid-cols-5 gap-3">
                    <div v-for="(shot, shotIndex) in series.shots" :key="shotIndex">
                      <label class="block text-xs font-medium text-surface-500 mb-1">Schuss {{ shotIndex + 1 }}</label>
                      <input
                          v-model.number="shot.value"
                          class="w-full px-2.5 py-1.5 rounded-lg border border-surface-300 text-sm text-center focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
                          min="0"
                          max="10.9"
                          step="0.1"
                          type="number"
                      >
                    </div>
                  </div>
                </div>
              </div>

              <!-- Add series button -->
              <button
                  class="flex items-center gap-2 px-4 py-2.5 rounded-lg border-2 border-dashed border-surface-300 text-surface-500 hover:border-primary-400 hover:text-primary-600 hover:bg-primary-50 transition-all duration-200 text-sm font-medium w-full justify-center"
                  type="button"
                  @click="addSeries"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path d="M12 4v16m8-8H4" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
                </svg>
                Serie hinzufügen
              </button>
            </div>

            <!-- Footer -->
            <div
                class="flex items-center justify-end gap-3 px-6 py-4 border-t border-surface-200 bg-surface-50 rounded-b-2xl">
              <button
                  class="px-5 py-2.5 rounded-lg text-sm font-medium text-surface-600 bg-card border border-surface-300 hover:bg-surface-50 transition-colors"
                  type="button"
                  @click="close"
              >
                Abbrechen
              </button>
              <button
                  class="px-5 py-2.5 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors"
                  type="submit"
              >
                Speichern
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import {nextTick, onMounted, reactive, ref} from 'vue'
import {api} from '@/api/http'

const emit = defineEmits(['saved'])

const visible = ref(false)
const locations = ref([])
const isEditing = ref(false)
const editingId = ref(null)
const seriesContainerRef = ref(null)

const form = reactive({
  sessionDate: '',
  sessionTime: '',
  enemyId: null,
  title: '',
  sessionType: 'TRAINING',
  decimalScoring: false,
  home: false,
  series: []
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
  form.title = ''
  form.sessionType = 'TRAINING'
  form.decimalScoring = false
  form.home = false
  form.series = [createEmptySeries()]
  isEditing.value = false
  editingId.value = null
}

function openCreate() {
  resetForm()
  visible.value = true
}

function openEdit(session) {
  isEditing.value = true
  editingId.value = session.id
  form.sessionDate = session.sessionDate
  form.sessionTime = session.sessionTime
  form.title = session.title
  form.enemyId = session.enemy ? session.enemy.id : null
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

async function handleSubmit() {
  const data = {
    sessionDate: form.sessionDate,
    sessionTime: form.sessionTime,
    enemyId: form.enemyId,
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
    alert('Fehler beim Speichern der Session!')
  }
}

onMounted(async () => {
  try {
    locations.value = await api.getLocations()
  } catch (err) {
    console.error('Error fetching locations:', err)
  }
})

defineExpose({openCreate, openEdit})
</script>

<style scoped>
.modal-enter-active {
  animation: fadeIn 0.2s ease-out;
}

.modal-leave-active {
  animation: fadeIn 0.15s ease-in reverse;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>