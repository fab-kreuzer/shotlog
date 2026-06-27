<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">Dashboard</h1>
      <p class="mt-1 text-surface-500">Willkommen bei ShotLog</p>
    </div>

    <!-- Content card -->
    <div class="bg-white rounded-xl border border-surface-200 shadow-sm p-6 flex items-center justify-between">
      <p class="text-surface-600">You are logged in.</p>
      <input ref="fileInput" accept=".ics" class="hidden" type="file" @change="onFileSelected">
      <button
          :disabled="importing"
          class="inline-flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
          type="button"
          @click="fileInput?.click()">
        {{ importing ? 'Importiere…' : 'Termine importieren' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'

const notify = useNotificationStore()

const fileInput = ref(null)
const importing = ref(false)

async function onFileSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return

  importing.value = true
  try {
    const result = await api.importSessions(file)
    const count = result?.imported ?? 0
    notify.success(`${count} Termin${count === 1 ? '' : 'e'} importiert`)
  } catch (err) {
    notify.error(err.message || 'Import fehlgeschlagen')
  } finally {
    importing.value = false
    event.target.value = ''
  }
}
</script>
