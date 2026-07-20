<template>
  <div>
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
      <div>
        <h1 class="text-2xl font-bold text-surface-800">{{ $t('competition.title') }}</h1>
        <p class="mt-1 text-surface-500">{{ $t('shooting.sessionsFound', filteredSessions.length) }}</p>
      </div>
      <div class="flex items-center gap-2">
        {{ $t('shooting.season') }}:
        <Multiselect
            v-model="selected"
            :fluid="false"
            :options="options"
            class="w-56"
            optionLabel="description"
            optionValue="id"
        />
        <FileUpload
            ref="uploader"
            :auto="true"
            :chooseButtonProps="{size: 'small'}"
            :chooseLabel="importing ? t('competition.importing') : t('competition.importDates')"
            :disabled="importing"
            :title="$t('competition.importHint')"
            accept=".ics"
            customUpload
            mode="basic"
            @uploader="onUpload"
        />
        <Button
            v-if="sessions.length > 0"
            icon="pi pi-plus"
            :label="$t('competition.create')"
            size="small"
            @click="openCreate"
        />
      </div>
    </div>

    <SessionGrid :sessions="filteredSessions" @create="openCreate" @delete="handleDelete" @edit="editSession"/>

    <CompetitionSessionModal ref="sessionModal" @saved="loadSessions"/>
    <ConfirmModal
        v-model="showDeleteConfirm"
        :confirmText="$t('shooting.confirmDeleteText')"
        :message="$t('shooting.deleteMessage')"
        :title="$t('shooting.deleteTitle')"
        @confirm="confirmDeleteSession"
    />
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {useI18n} from 'vue-i18n'
import SessionGrid from '@/components/SessionGrid.vue'
import CompetitionSessionModal from '@/components/session/CompetitionSessionModal.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import {useSessionList} from '@/composables/useSessionList'
import {useSeasonFilter} from '@/composables/useSeasonFilter'
import Multiselect from "@/components/Multiselect.vue";
import Button from "primevue/button";
import FileUpload from 'primevue/fileupload'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'

const {t} = useI18n()
const notify = useNotificationStore()

const {
  sessions,
  sessionModal,
  showDeleteConfirm,
  loadSessions,
  editSession,
  openCreate,
  handleDelete,
  confirmDeleteSession
} = useSessionList('COMPETITION')

const {selected, options, filteredSessions, loadSeasons} = useSeasonFilter(sessions)

onMounted(loadSeasons);

const uploader = ref(null)
const importing = ref(false)

async function onUpload(event) {
  const file = event.files?.[0]
  if (!file) return

  importing.value = true
  try {
    const result = await api.importSessions(file)
    const count = result?.imported ?? 0
    notify.success(t('competition.imported', {count}, count))
    if (count > 0) await loadSessions()
  } catch (err) {
    if (!err._notified) notify.error(t('competition.importFailed'))
  } finally {
    importing.value = false
    uploader.value?.clear()
  }
}
</script>