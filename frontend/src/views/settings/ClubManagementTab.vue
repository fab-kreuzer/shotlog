<script setup>
import {onMounted, ref} from "vue";
import DataTable from "primevue/datatable";
import Button from "primevue/button";
import Column from "primevue/column";
import Tag from "primevue/tag";
import {api} from "@/api/http.js";
import Dialog from "primevue/dialog";
import ConfirmModal from "@/components/ConfirmModal.vue";
import InputText from "primevue/inputtext";
import CreateLocationModal from "@/components/settings/CreateLocationModal.vue";
import {useAuthStore} from "@/stores/auth";

const auth = useAuthStore();

const showCreateClub = ref(false);
const clubs = ref([]);
const clubToDelete = ref(null);
const showConfirmClub = ref(false);
const clubToEdit = ref(null);

async function loadClubs() {
  clubs.value = await api.getLocations();
}

function mapsUrl(club) {
  const query = [club.club, club.location].filter(Boolean).join(', ');
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(query)}`;
}

async function handleDeleteClub(id) {
  clubToDelete.value = id;
  showConfirmClub.value = true;
}

async function confirmDeleteClub() {
  try {
    await api.deleteLocation(clubToDelete.value);
    await loadClubs();
  } catch {
    // Deletion was blocked (e.g. club still in use); the message is shown via the API response.
  }
}

function openEditClub(club) {
  clubToEdit.value = {...club};
}

async function handleUpdateClub() {
  await api.updateLocation(clubToEdit.value.id, {
    club: clubToEdit.value.club,
    location: clubToEdit.value.location
  })

  clubToEdit.value = null
  await loadClubs()
}

async function handleCreateClub(club) {
  await api.createLocation(club)
  await loadClubs()
}

onMounted(loadClubs);
</script>

<template>
  <div class="p-2">
    <div class="flex justify-between items-center mb-6">
      <h3 class="flex items-center gap-2 text-lg font-semibold text-surface-700">
        <i class="pi pi-building text-primary-500"/>
        {{ $t('club.listTitle') }}
      </h3>
      <Button v-if="auth.hasPermission('create_club')" :label="$t('club.newButton')" icon="pi pi-plus" @click="showCreateClub = true"/>
    </div>

    <DataTable :value="clubs" class="border border-surface-200 rounded-lg overflow-hidden" dataKey="id" stripedRows>
      <Column :header="$t('club.id')" field="id" sortable/>
      <Column :header="$t('club.clubName')" field="club" sortable>
        <template #body="{ data }">
          <span class="inline-flex items-center gap-2">
            {{ data.club }}
            <Tag v-if="data.id === auth.user?.homeClubId" :value="$t('profile.homeClub')" severity="success"/>
          </span>
        </template>
      </Column>
      <Column :header="$t('common.location')" field="location" sortable>
        <template #body="{ data }">
          <a
              v-if="data.location"
              :href="mapsUrl(data)"
              class="inline-flex items-center gap-1 text-primary hover:underline"
              rel="noopener noreferrer"
              target="_blank"
          >
            <i class="pi pi-map-marker text-xs"/>
            {{ data.location }}
          </a>
          <span v-else>{{ data.location }}</span>
        </template>
      </Column>
      <Column v-if="auth.hasPermission('edit_club') || auth.hasPermission('delete_club')" :header="$t('common.actions')">
        <template #body="{ data }">
          <div class="flex justify-end gap-2">
            <Button v-if="auth.hasPermission('edit_club')" icon="pi pi-pencil" rounded severity="info" size="small" text @click="openEditClub(data)"/>
            <Button v-if="auth.hasPermission('delete_club')" icon="pi pi-trash" rounded severity="danger" size="small" text @click="handleDeleteClub(data.id)"/>
          </div>
        </template>
      </Column>
    </DataTable>
  </div>

  <Dialog
      :draggable="false"
      :style="{ width: '28rem' }"
      :visible="!!clubToEdit"
      :header="$t('club.editTitle')"
      modal
      @update:visible="(v) => { if (!v) clubToEdit = null }"
  >
    <form v-if="clubToEdit" id="edit-club-form" class="flex flex-col gap-4" @submit.prevent="handleUpdateClub">
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="editClubName">{{ $t('club.clubName') }}</label>
        <InputText id="editClubName" v-model="clubToEdit.club" fluid required/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="editClubLocation">{{ $t('common.location') }}</label>
        <InputText id="editClubLocation" v-model="clubToEdit.location" fluid required/>
      </div>
    </form>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text type="button" @click="clubToEdit = null"/>
      <Button :label="$t('common.save')" form="edit-club-form" type="submit"/>
    </template>

  </Dialog>

  <CreateLocationModal
      v-model="showCreateClub"
      @create="handleCreateClub"
  />

  <ConfirmModal
      v-model="showConfirmClub"
      :confirmText="$t('club.deleteConfirm')"
      :message="$t('club.deleteMessage')"
      :title="$t('club.deleteTitle')"
      @confirm="confirmDeleteClub"
  />
</template>