<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">{{ greeting }}</h1>
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
import {computed, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import Card from 'primevue/card'
import FileUpload from 'primevue/fileupload'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'
import {useAuthStore} from '@/stores/auth'

const {t, locale} = useI18n()
const notify = useNotificationStore()
const auth = useAuthStore()

// Time-of-day bucket and a random variant, both fixed once per page load.
function currentBucket() {
  const h = new Date().getHours()
  if (h >= 5 && h < 10) return 'morning'
  if (h >= 10 && h < 16) return 'day'
  if (h >= 16 && h < 20) return 'evening'
  return 'night'
}

const bucket = currentBucket()
const variant = Math.floor(Math.random() * 5)
const name = computed(() => auth.user?.displayName || auth.user?.username || '')
const greeting = computed(() => {
  void locale.value // re-resolve when the UI language is toggled
  return t(`dashboard.greetings.${bucket}.${variant}`, {name: name.value})
})

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
