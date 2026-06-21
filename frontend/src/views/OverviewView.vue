<template>
  <div class="container-fluid p-0">
    <!-- Header with title and action button -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="h2 mb-0">Übersicht deiner {{ typeLabel }}-Sessions</h1>
      <button v-if="sessions.length > 0" class="btn btn-success" type="button" @click="sessionModal?.openCreate()">
        <i class="bi bi-plus-circle me-2"></i>Neue Session anlegen
      </button>
    </div>

    <!-- Sessions Display -->
    <div v-if="sessions.length > 0" class="mt-4">
      <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 row-cols-xl-4 g-4">
        <div v-for="session in sessions" :key="session.id" class="col">
          <div class="card h-100 shadow-sm session-card">
            <div class="card-body position-relative">
              <div class="session-actions position-absolute top-0 end-0 mt-2 me-2">
                <button class="btn btn-sm btn-warning me-1" title="Bearbeiten" @click="editSession(session.id)">
                  <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-danger" title="Löschen" @click="handleDelete(session.id)">
                  <i class="bi bi-trash"></i>
                </button>
              </div>

              <h5 class="card-title mb-3">{{ session.translatedLocation }}</h5>

              <div class="d-flex align-items-center mb-2">
                <i class="bi bi-calendar-event text-muted me-2"></i>
                <span>{{ formatDate(session.sessionDate) }}</span>
              </div>

              <div class="d-flex align-items-center mb-2">
                <i class="bi bi-clock text-muted me-2"></i>
                <span>{{ formatTime(session.sessionTime) }} Uhr</span>
              </div>

              <div class="d-flex align-items-center">
                <i class="bi bi-bullseye text-muted me-2"></i>
                <span class="fw-bold">Summe: {{ session.formattedShotSum }} ({{
                    session.formattedShotSumOfTestShots
                  }})</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state message -->
    <div v-else class="text-center mt-5 pt-5">
      <i class="bi bi-inbox fs-1 text-muted"></i>
      <p class="mt-3 text-muted">Keine Sessions verfügbar</p>
      <button class="btn btn-outline-success mt-3" type="button" @click="sessionModal?.openCreate()">
        Erste Session anlegen
      </button>
    </div>

    <!-- Session Modal -->
    <SessionModal ref="sessionModal" @saved="loadSessions"/>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import {api} from '@/api/http'
import SessionModal from '@/components/SessionModal.vue'

const route = useRoute()
const sessions = ref([])
const sessionModal = ref(null)

const sessionType = computed(() => (route.query.type || 'training').toUpperCase())
const typeLabel = computed(() => sessionType.value === 'COMPETITION' ? 'Wettkampf' : 'Training')

function formatDate(dateStr) {
  if (!dateStr) return ''
  const [y, m, d] = dateStr.split('-')
  return `${d}.${m}.${y}`
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return timeStr.substring(0, 5)
}

async function loadSessions() {
  try {
    sessions.value = await api.getSessionsByType(route.query.type || 'training')
  } catch (err) {
    console.error('Error loading sessions:', err)
  }
}

async function editSession(id) {
  try {
    const session = await api.getSession(id)
    sessionModal.value?.openEdit(session)
  } catch (err) {
    console.error('Error loading session:', err)
    alert('Fehler beim Laden der Session!')
  }
}

async function handleDelete(id) {
  if (confirm('Sind Sie sicher, dass Sie diese Session mit allen zugehörigen Daten löschen möchten?')) {
    try {
      await api.deleteSession(id)
      await loadSessions()
    } catch (err) {
      console.error('Error deleting session:', err)
      alert('Fehler beim Löschen der Session!')
    }
  }
}

watch(() => route.query.type, () => {
  loadSessions()
})

onMounted(() => {
  loadSessions()
})
</script>
