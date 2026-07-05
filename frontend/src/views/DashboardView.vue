<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">{{ $t('dashboard.title') }}</h1>
      <p class="mt-1 text-surface-500">{{ $t('dashboard.welcome') }}</p>
    </div>

    <!-- Content card -->
    <Card>
      <template #content>
        <div class="flex items-center justify-between">
          <p class="text-surface-600">{{ $t('dashboard.loggedIn') }}</p>
          <FileUpload
              ref="uploader"
              :auto="true"
              :chooseLabel="importing ? t('dashboard.importing') : t('dashboard.importDates')"
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
import {useI18n} from 'vue-i18n'
import Card from 'primevue/card'
import FileUpload from 'primevue/fileupload'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'

const {t} = useI18n()
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
    notify.success(t('dashboard.imported', {count}, count))
  } catch (err) {
    if (!err._notified) notify.error(t('dashboard.importFailed'))
  } finally {
    importing.value = false
    uploader.value?.clear()
  }
}
</script>
