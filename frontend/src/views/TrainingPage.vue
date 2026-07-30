<template>
  <div>
    <PageHeader
        :subtitle="$t('shooting.sessionsFound', filteredSessions.length)"
        :title="$t('training.title')"
        color="green"
        icon="pi pi-bullseye"
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
        <Button
            v-if="sessions.length > 0"
            icon="pi pi-plus"
            :label="$t('training.create')"
            @click="openCreate"
        />
      </template>
    </PageHeader>

    <SessionGrid :sessions="filteredSessions" @create="openCreate" @delete="handleDelete" @edit="editSession"/>

    <TrainingSessionModal ref="sessionModal" @saved="loadSessions"/>
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
import PageHeader from '@/components/PageHeader.vue'
import SessionGrid from '@/components/SessionGrid.vue'
import TrainingSessionModal from '@/components/session/TrainingSessionModal.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import {useSessionList} from '@/composables/useSessionList'
import {useSeasonFilter} from '@/composables/useSeasonFilter'
import {useSessionSearch} from '@/composables/useSessionSearch'
import Multiselect from '@/components/Multiselect.vue'
import Button from 'primevue/button'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import {onMounted} from 'vue'

const {
  sessions,
  sessionModal,
  showDeleteConfirm,
  loadSessions,
  editSession,
  openCreate,
  handleDelete,
  confirmDeleteSession
} = useSessionList('TRAINING')

const {selected, options, filteredSessions: seasonFilteredSessions, loadSeasons} = useSeasonFilter(sessions)
const {searchTerm, filteredSessions} = useSessionSearch(seasonFilteredSessions)

onMounted(loadSeasons)
</script>