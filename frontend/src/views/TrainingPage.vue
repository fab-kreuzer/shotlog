<template>
  <div>
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
      <div>
        <h1 class="text-2xl font-bold text-surface-800">Übersicht deiner Training-Sessions</h1>
        <p class="mt-1 text-surface-500">{{ filteredSessions.length }}
          Session{{ filteredSessions.length !== 1 ? 's' : '' }}
          gefunden</p>
      </div>
      <div class="flex items-center gap-2">
        Saison:
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
            label="Neues Training anlegen"
            @click="openCreate"
        />
      </div>
    </div>

    <SessionGrid :sessions="filteredSessions" @create="openCreate" @delete="handleDelete" @edit="editSession"/>

    <SessionModal ref="sessionModal" @saved="loadSessions"/>
    <ConfirmModal
        v-model="showDeleteConfirm"
        confirmText="Ja, löschen"
        message="Willst du diesen Eintrag wirklich löschen?"
        title="Eintrag löschen"
        @confirm="confirmDeleteSession"
    />
  </div>
</template>

<script setup>
import SessionGrid from '@/components/SessionGrid.vue'
import SessionModal from '@/components/SessionModal.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import {useSessionList} from '@/composables/useSessionList'
import {useSeasonFilter} from '@/composables/useSeasonFilter'
import Multiselect from '@/components/Multiselect.vue'
import Button from 'primevue/button'
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

const {selected, options, filteredSessions, loadSeasons} = useSeasonFilter(sessions)

onMounted(loadSeasons)
</script>