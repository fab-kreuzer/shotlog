<template>
  <div>
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
      <div>
        <h1 class="text-2xl font-bold text-surface-800">Übersicht deiner {{ typeLabel }}-Sessions</h1>
        <p class="mt-1 text-surface-500">{{ sessions.length }} Session{{ sessions.length !== 1 ? 's' : '' }}
          gefunden</p>
      </div>
      <button
          v-if="sessions.length > 0"
          class="inline-flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors"
          type="button"
          @click="sessionModal?.openCreate()"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path d="M12 4v16m8-8H4" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
        </svg>
        Neue Session anlegen
      </button>
    </div>

    <!-- Session cards grid -->
    <div v-if="sessions.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
      <div
          v-for="session in sessions"
          :key="session.id"
          class="group relative bg-card rounded-xl border border-surface-200 shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 overflow-hidden"
      >
        <!-- Actions overlay -->
        <div
            class="absolute top-2 right-2 flex items-center rounded-lg bg-card shadow-md gap-1 opacity-0 group-hover:opacity-100 transition-opacity sm:opacity-0 max-sm:opacity-100">
          <button
              class="p-1.5 rounded-lg text-warning-500 hover:bg-warning-50 transition-colors"
              title="Bearbeiten"
              @click="editSession(session.id)"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" stroke-linecap="round" stroke-linejoin="round"
                    stroke-width="2"/>
            </svg>
          </button>
          <button
              class="p-1.5 rounded-lg text-danger-500 hover:bg-danger-50 transition-colors"
              title="Löschen"
              @click="handleDelete(session.id)"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" stroke-linecap="round" stroke-linejoin="round"
                    stroke-width="2"/>
            </svg>
          </button>
        </div>

        <div class="p-2">
          <!-- Card header -->
          <div class="flex items-center gap-2 mb-4">
            <!-- Home / Away Icon -->
            <svg v-if="session.home"
                 class="w-4 h-4 text-surface-400 shrink-0"
                 fill="none"
                 stroke="currentColor"
                 viewBox="0 0 24 24">
              <!-- Home (house) -->
              <path stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M3 10.5L12 3l9 7.5M5 9.75V21h5.25v-6h3.5v6H19V9.75"/>
            </svg>

            <svg v-else
                 class="w-4 h-4 text-surface-400 shrink-0"
                 fill="none"
                 stroke="currentColor"
                 viewBox="0 0 24 24">
              <!-- Away (location pin) -->
              <path stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17.657 16.657L13.414 20.9a2 2 0 01-2.828 0l-4.243-4.243a8 8 0 1111.314 0z"/>
              <path stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
            </svg>
            <h3 class="text-base font-semibold text-surface-800 leading-tight">{{ session.title }}</h3>
          </div>

          <!-- Session details -->
          <div class="space-y-2.5">
            <div class="flex items-center gap-2.5 text-sm text-surface-600">
              <svg class="w-4 h-4 text-surface-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" stroke-linecap="round" stroke-linejoin="round"
                      stroke-width="2"/>
              </svg>
              <span>{{ formatDate(session.sessionDate) }}</span>
            </div>

            <div class="flex items-center gap-2.5 text-sm text-surface-600">
              <svg class="w-4 h-4 text-surface-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" stroke-linecap="round" stroke-linejoin="round"
                      stroke-width="2"/>
              </svg>
              <span>{{ formatTime(session.sessionTime) }} Uhr</span>
            </div>

            <div class="flex items-center gap-2.5 text-sm">
              <svg class="w-4 h-4 text-surface-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <circle cx="12" cy="12" r="10" stroke-width="2"/>
                <circle cx="12" cy="12" r="6" stroke-width="2"/>
                <circle cx="12" cy="12" r="2" stroke-width="2"/>
              </svg>
              <span class="font-semibold text-surface-800">Summe: {{ session.formattedShotSum }}
                <span class="font-normal text-surface-500">({{ session.formattedShotSumOfTestShots }})</span>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else class="text-center py-20">
      <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-surface-100 mb-4">
        <svg class="w-8 h-8 text-surface-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" stroke-linecap="round" stroke-linejoin="round"
                stroke-width="1.5"/>
        </svg>
      </div>
      <h3 class="text-lg font-medium text-surface-700 mb-1">Keine Sessions verfügbar</h3>
      <p class="text-surface-500 mb-6">Erstellen Sie Ihre erste Session, um loszulegen.</p>
      <button
          class="inline-flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors"
          type="button"
          @click="sessionModal?.openCreate()"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path d="M12 4v16m8-8H4" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
        </svg>
        Erste Session anlegen
      </button>
    </div>

    <!-- Session Modal -->
    <SessionModal ref="sessionModal" @saved="loadSessions"/>
    <ConfirmModal
        v-model="showDeleteSessionConfirm"
        confirmText="Ja, löschen"
        message="Willst du diesen Eintrag wirklich löschen?"
        title="Eintrag löschen"
        @confirm="confirmDeleteUser"
    />
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import {api} from '@/api/http'
import SessionModal from '@/components/SessionModal.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import {useNotificationStore} from '@/stores/notifications'

const notify = useNotificationStore()

const showDeleteSessionConfirm = ref(false)
const sessionToDelete = ref(null)

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
    notify.error('Fehler beim Laden der Sessions!')
  }
}

async function editSession(id) {
  try {
    const session = await api.getSession(id)
    sessionModal.value?.openEdit(session)
  } catch (err) {
    console.error('Error loading session:', err)
    notify.error('Fehler beim Laden der Session!')
  }
}

async function handleDelete(id) {
  sessionToDelete.value = id
  showDeleteSessionConfirm.value = true
}

async function confirmDeleteUser() {
  try {
    await api.deleteSession(id)
    await loadSessions()
  } catch (err) {
    console.error('Error deleting session:', err)
    notify.error('Fehler beim Löschen der Session!')
  }
}

watch(() => route.query.type, () => {
  loadSessions()
})

onMounted(() => {
  loadSessions()
})
</script>