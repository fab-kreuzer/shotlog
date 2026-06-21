<template>
  <div id="createSessionModal" ref="modalRef" class="modal fade" tabindex="-1">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
      <div class="modal-content">
        <form @submit.prevent="handleSubmit">
          <!-- HEADER -->
          <div class="modal-header">
            <h5 class="modal-title">{{ isEditing ? 'Schießabend bearbeiten' : 'Neuen Schießabend erstellen' }}</h5>
            <button class="btn-close" data-bs-dismiss="modal" type="button"></button>
          </div>

          <!-- BODY -->
          <div class="modal-body" style="max-height: 70vh; overflow-y: auto;">
            <!-- SESSION DATA -->
            <div class="row g-3 mb-4">
              <div class="col-md-4">
                <label class="form-label">Datum</label>
                <input v-model="form.sessionDate" class="form-control" required type="date">
              </div>

              <div class="col-md-4">
                <label class="form-label">Startzeit</label>
                <input v-model="form.sessionTime" class="form-control" required type="time">
              </div>

              <div class="col-md-4">
                <label class="form-label">Gegner</label>
                <select v-model="form.enemyId" class="form-select" required>
                  <option v-for="loc in locations" :key="loc.id" :value="loc.id">{{ loc.club }}</option>
                </select>
              </div>

              <div class="col-md-4">
                <label class="form-label">Art des Schießens</label>
                <select v-model="form.sessionType" class="form-select" required>
                  <option value="TRAINING">Training</option>
                  <option value="COMPETITION">Wettkampf</option>
                </select>
              </div>

              <div class="col-md-4 d-flex align-items-end">
                <div class="form-check">
                  <input id="decimalScoring" v-model="form.decimalScoring" class="form-check-input" type="checkbox">
                  <label class="form-check-label" for="decimalScoring">Zehntelwertung</label>
                </div>
                <div class="form-check">
                  <input id="isHomeCheckbox" v-model="form.home" class="form-check-input" type="checkbox">
                  <label class="form-check-label" for="isHomeCheckbox">Heimwettkampf</label>
                </div>
              </div>
            </div>

            <hr>

            <!-- SERIES -->
            <div id="seriesContainer">
              <div v-for="(series, sIndex) in form.series" :key="sIndex" class="card mb-4 series-block">
                <div class="card-header">
                  <strong>Serie <span class="series-number">{{ sIndex + 1 }}</span></strong>
                  <div class="float-end">
                    <button class="btn btn-sm btn-outline-danger me-2" title="Serie entfernen" type="button"
                            @click="removeSeries(sIndex)">
                      <i class="bi bi-trash"></i> Entfernen
                    </button>
                    <input v-model="series.testShot" class="form-check-input" type="checkbox">
                    <label class="form-check-label">Probe</label>
                  </div>
                </div>

                <div class="card-body">
                  <div class="row g-2">
                    <div v-for="(shot, shotIndex) in series.shots" :key="shotIndex" class="col-6 col-md-3">
                      <label class="form-label">Schuss {{ shotIndex + 1 }}</label>
                      <input
                          v-model.number="shot.value"
                          class="form-control"
                          max="10.9"
                          min="0"
                          step="0.1"
                          type="number"
                      >
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- ADD SERIES BUTTON -->
            <button class="btn btn-outline-secondary" type="button" @click="addSeries">
              + Serie hinzufügen
            </button>
          </div>

          <!-- FOOTER -->
          <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal" type="button">Abbrechen</button>
            <button class="btn btn-success" type="submit">Speichern</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import {nextTick, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {api} from '@/api/http'

const emit = defineEmits(['saved'])

const modalRef = ref(null)
const locations = ref([])
const isEditing = ref(false)
const editingId = ref(null)
let bsModal = null

const form = reactive({
  sessionDate: '',
  sessionTime: '',
  enemyId: null,
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
    const container = document.getElementById('seriesContainer')
    if (container && container.lastElementChild) {
      container.lastElementChild.scrollIntoView({behavior: 'smooth', block: 'end'})
    }
  })
}

function removeSeries(index) {
  form.series.splice(index, 1)
  // Update series numbers
  form.series.forEach((s, i) => {
    s.seriesNumber = i + 1
  })
}

function resetForm() {
  const now = new Date()
  form.sessionDate = now.toISOString().split('T')[0]
  form.sessionTime = String(now.getHours()).padStart(2, '0') + ':' + String(now.getMinutes()).padStart(2, '0')
  form.enemyId = locations.value.length > 0 ? locations.value[0].id : null
  form.sessionType = 'TRAINING'
  form.decimalScoring = false
  form.home = false
  form.series = [createEmptySeries()]
  isEditing.value = false
  editingId.value = null
}

function openCreate() {
  resetForm()
  showModal()
}

function openEdit(session) {
  isEditing.value = true
  editingId.value = session.id
  form.sessionDate = session.sessionDate
  form.sessionTime = session.sessionTime
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

  // Ensure each series has 10 shots
  form.series.forEach(s => {
    while (s.shots.length < 10) {
      s.shots.push({shotNumber: s.shots.length + 1, value: 0})
    }
  })

  showModal()
}

function showModal() {
  if (bsModal) {
    bsModal.show()
  }
}

function hideModal() {
  if (bsModal) {
    bsModal.hide()
  }
}

async function handleSubmit() {
  const data = {
    sessionDate: form.sessionDate,
    sessionTime: form.sessionTime,
    enemyId: form.enemyId,
    sessionType: form.sessionType,
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
    hideModal()
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

  // Initialize Bootstrap modal
  if (modalRef.value && window.bootstrap) {
    bsModal = new window.bootstrap.Modal(modalRef.value)
  }
})

onBeforeUnmount(() => {
  if (bsModal) {
    bsModal.dispose()
    bsModal = null
  }
})

defineExpose({openCreate, openEdit})
</script>
