<template>
  <div>
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
      <div>
        <h1 class="text-2xl font-bold text-surface-800">Übersicht deiner Wettkampf-Sessions</h1>
        <p class="mt-1 text-surface-500">{{ sessions.length }} Session{{ sessions.length !== 1 ? 's' : '' }}
          gefunden</p>
      </div>
      <button
          v-if="sessions.length > 0"
          class="inline-flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors"
          type="button"
          @click="openCreate"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path d="M12 4v16m8-8H4" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
        </svg>
        Neuen Wettkampf anlegen
      </button>
    </div>

    <SessionGrid :sessions="sessions" @create="openCreate" @delete="handleDelete" @edit="editSession"/>

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
</script>