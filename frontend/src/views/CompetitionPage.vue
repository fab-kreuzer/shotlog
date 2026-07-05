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
        <Button
            v-if="sessions.length > 0"
            icon="pi pi-plus"
            :label="$t('competition.create')"
            @click="openCreate"
        />
      </div>
    </div>

    <SessionGrid :sessions="filteredSessions" @create="openCreate" @delete="handleDelete" @edit="editSession"/>

    <SessionModal ref="sessionModal" @saved="loadSessions"/>
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
import SessionGrid from '@/components/SessionGrid.vue'
import SessionModal from '@/components/SessionModal.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import {useSessionList} from '@/composables/useSessionList'
import {useSeasonFilter} from '@/composables/useSeasonFilter'
import Multiselect from "@/components/Multiselect.vue";
import Button from "primevue/button";
import {onMounted} from "vue";

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
</script>