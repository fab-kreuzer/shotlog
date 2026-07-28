<script setup>
import {onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useAuthStore} from '@/stores/auth'
import {api} from '@/api/http'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Tag from 'primevue/tag'
import ConfirmDialog from 'primevue/confirmdialog'
import {useConfirm} from 'primevue/useconfirm'

const auth = useAuthStore()
const confirm = useConfirm()
const {t} = useI18n()

const seasons = ref([])
const showDialog = ref(false)
const editingId = ref(null)
const descriptionInput = ref('')

async function loadSeasons() {
  try {
    seasons.value = await api.getSeasons()
  } catch (error) {
    console.error('Error loading seasons:', error)
  }
}

function openCreate() {
  editingId.value = null
  descriptionInput.value = ''
  showDialog.value = true
}

function openEdit(season) {
  editingId.value = season.id
  descriptionInput.value = season.description
  showDialog.value = true
}

async function saveSeason() {
  if (!descriptionInput.value.trim()) return
  try {
    if (editingId.value != null) {
      await api.updateSeason(editingId.value, {description: descriptionInput.value})
    } else {
      await api.createSeason({description: descriptionInput.value})
    }
    showDialog.value = false
    await loadSeasons()
  } catch (error) {
    // The backend already surfaced a localized message (duplicate name, etc.).
    console.error('Error saving season:', error)
  }
}

async function setActive(season) {
  try {
    await api.setActiveSeason(season.id)
    await loadSeasons()
  } catch (error) {
    console.error('Error setting active season:', error)
  }
}

function deleteSeason(season) {
  confirm.require({
    message: t('season.deleteMessage', {name: season.description}),
    header: t('season.deleteTitle'),
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: t('season.deleteConfirm'),
    rejectLabel: t('common.cancel'),
    accept: async () => {
      try {
        await api.deleteSeason(season.id)
        await loadSeasons()
      } catch (error) {
        // The backend blocks deletion (active / in use) with a localized message.
        console.error('Error deleting season:', error)
      }
    }
  })
}

onMounted(loadSeasons)
</script>

<template>
  <div class="p-2">
    <div class="flex justify-between items-center mb-6">
      <h3 class="flex items-center gap-2 text-lg font-semibold text-surface-700">
        <i class="pi pi-calendar text-primary-500"/>
        {{ $t('season.listTitle') }}
      </h3>
      <Button v-if="auth.hasPermission('create_season')" :label="$t('season.newButton')" icon="pi pi-plus"
              @click="openCreate()"/>
    </div>

    <div class="border border-surface-200 rounded-lg overflow-hidden">
      <div v-if="seasons.length === 0" class="p-4 text-center text-surface-500">{{ $t('common.noData') }}</div>
      <div v-for="season in seasons" :key="season.id"
           class="flex justify-between items-center p-3 border-b border-surface-200 last:border-b-0">
        <div class="flex items-center gap-3">
          <span class="font-medium text-surface-800">{{ season.description }}</span>
          <Tag v-if="season.active" :value="$t('season.activeLabel')" severity="success"/>
        </div>
        <div class="flex items-center gap-2">
          <Button v-if="!season.active" :label="$t('season.setActive')" icon="pi pi-check" size="small" text
                  @click="setActive(season)"/>
          <Button v-if="auth.hasPermission('edit_season')" :title="$t('common.edit')" icon="pi pi-pencil" rounded
                  severity="secondary" size="small" text @click="openEdit(season)"/>
          <Button v-if="auth.hasPermission('delete_season')" :title="$t('season.deleteTitle')" icon="pi pi-trash" rounded
                  severity="danger" size="small" text @click="deleteSeason(season)"/>
        </div>
      </div>
    </div>

    <Dialog v-model:visible="showDialog" :header="editingId ? $t('season.editTitle') : $t('season.createTitle')" modal>
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-2">{{ $t('season.descriptionLabel') }}</label>
          <InputText v-model="descriptionInput" :placeholder="$t('season.descriptionPlaceholder')" class="w-full"
                     @keyup.enter="saveSeason()"/>
        </div>
      </div>
      <template #footer>
        <Button :label="$t('common.cancel')" severity="secondary" @click="showDialog = false"/>
        <Button :label="editingId ? $t('common.save') : $t('common.create')" @click="saveSeason()"/>
      </template>
    </Dialog>

    <ConfirmDialog/>
  </div>
</template>
