<template>
  <div class="p-2">
    <h2 class="text-lg font-semibold text-surface-800 mb-4">Mein Profil</h2>

    <div class="flex flex-col gap-3">
      <div class="flex items-center gap-3">
        <span class="text-sm text-surface-500 w-32">Benutzername:</span>
        <span class="text-sm font-medium text-surface-800">
          {{ auth.user?.username }}
        </span>
      </div>

      <div class="flex items-center gap-3">
        <span class="text-sm text-surface-500 w-32">Anzeigename:</span>
        <span class="text-sm font-medium text-surface-800">
          {{ auth.user?.displayName }}
        </span>
      </div>

      <div class="flex items-start gap-3">
        <span class="text-sm text-surface-500 w-32">Rollen:</span>
        <div class="flex flex-wrap gap-1.5">
          <Tag v-for="(role, i) in auth.user?.roles" :key="i" :value="role" severity="info"/>
        </div>
      </div>

      <div class="flex items-center gap-3">
        <label class="text-sm text-surface-500 w-32" for="homeClub">Stammverein:</label>
        <Select
            id="homeClub"
            v-model="homeClubId"
            :disabled="saving"
            :options="clubs"
            class="w-64"
            optionLabel="club"
            optionValue="id"
            placeholder="Kein Stammverein"
            showClear
            @change="saveHomeClub"
        />
      </div>

      <div class="flex items-center gap-3">
        <label class="text-sm text-surface-500 w-32" for="activeSeason">Aktive Saison:</label>
        <Select
            id="activeSeason"
            v-model="activeSeasonId"
            :disabled="savingSeason"
            :options="seasons"
            class="w-64"
            optionLabel="description"
            optionValue="id"
            @change="saveActiveSeason"
        />
      </div>
    </div>

    <p class="mt-6 text-sm text-surface-400">
      In zukünftigen Versionen können Sie hier Ihr Profil bearbeiten.
    </p>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from "@/stores/notifications.js";
import {api} from '@/api/http'

const auth = useAuthStore()
const notify = useNotificationStore()

const clubs = ref([])
const homeClubId = ref(auth.user?.homeClubId ?? null)
const saving = ref(false)

const seasons = ref([])
const activeSeasonId = ref(null)
const savingSeason = ref(false)

async function saveHomeClub() {
  saving.value = true
  try {
    await api.updateProfile({homeClubId: homeClubId.value})
    await auth.fetchUser()
  } catch (err) {
    console.error('Error updating home club:', err)
  } finally {
    saving.value = false
  }
}

async function saveActiveSeason() {
  savingSeason.value = true
  try {
    await api.setActiveSeason(activeSeasonId.value)
  } catch (err) {
    console.error('Error updating active season:', err)
  } finally {
    savingSeason.value = false
  }
}

onMounted(async () => {
  try {
    clubs.value = await api.getLocations()
    homeClubId.value = auth.user?.homeClubId ?? null
  } catch (err) {
    console.error('Error fetching clubs:', err)
  }
  try {
    seasons.value = await api.getSeasons()
    activeSeasonId.value = seasons.value.find(s => s.active)?.id ?? null
  } catch (err) {
    console.error('Error fetching seasons:', err)
  }
})
</script>
