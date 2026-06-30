<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">Dashboard</h1>
      <p class="mt-1 text-surface-500">Willkommen bei ShotLog</p>
    </div>

    <!-- Content card -->
    <Card>
      <template #content>
        <div class="flex items-center justify-between">
          <p class="text-surface-600">You are logged in.</p>
          <FileUpload
              ref="uploader"
              :auto="true"
              :chooseLabel="importing ? 'Importiere…' : 'Termine importieren'"
              :disabled="importing"
              accept=".ics"
              customUpload
              mode="basic"
              @uploader="onUpload"
          />
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import Card from 'primevue/card'
import FileUpload from 'primevue/fileupload'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'

const notify = useNotificationStore()

const uploader = ref(null)
const importing = ref(false)

async function onUpload(event) {
  const file = event.files?.[0]
  if (!file) return

  importing.value = true
  try {
    const result = await api.importSessions(file)
    const count = result?.imported ?? 0
    notify.success(`${count} Termin${count === 1 ? '' : 'e'} importiert`)
  } catch (err) {
    if (!err._notified) notify.error('Import fehlgeschlagen')
  } finally {
    importing.value = false
    uploader.value?.clear()
  }
}
</script>
