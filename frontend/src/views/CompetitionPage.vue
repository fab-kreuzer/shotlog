<template>
  <div>
    <PageHeader
        :subtitle="$t('shooting.sessionsFound', filteredSessions.length)"
        :title="$t('competition.title')"
        color="red"
        icon="pi pi-trophy"
    >
      <template #actions>
        <IconField>
          <InputIcon class="pi pi-search"/>
          <InputText v-model="searchTerm" :placeholder="$t('shooting.searchPlaceholder')"/>
        </IconField>
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
      </template>
    </PageHeader>

    <SessionGrid :loading="loading" :sessions="filteredSessions" @create="openCreate" @delete="handleDelete"
                 @edit="editSession"/>

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
import PageHeader from '@/components/PageHeader.vue'
import SessionGrid from '@/components/SessionGrid.vue'
import CompetitionSessionModal from '@/components/session/CompetitionSessionModal.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import {useSessionList} from '@/composables/useSessionList'
import {useSeasonFilter} from '@/composables/useSeasonFilter'
import {useSessionSearch} from '@/composables/useSessionSearch'
import Multiselect from "@/components/Multiselect.vue";
import Button from "primevue/button";
import FileUpload from 'primevue/fileupload'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'

const {t} = useI18n()
const notify = useNotificationStore()

const {
  sessions,
  loading,
  sessionModal,
  showDeleteConfirm,
  loadSessions,
  editSession,
  openCreate,
  handleDelete,
  confirmDeleteSession
} = useSessionList('COMPETITION')

const {selected, options, filteredSessions: seasonFilteredSessions, loadSeasons} = useSeasonFilter(sessions)
const {searchTerm, filteredSessions} = useSessionSearch(seasonFilteredSessions)

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