<template>
  <div class="p-6">
    <h2 class="text-lg font-semibold text-surface-800 mb-4">Mein Profil</h2>

    <div class="space-y-3">
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
          <span
              v-for="(role, i) in auth.user?.roles"
              :key="i"
              class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary-100 text-primary-700"
          >
            {{ role }}
          </span>
        </div>
      </div>

      <div class="flex items-center gap-3">
        <label class="text-sm text-surface-500 w-32" for="homeClub">Stammverein:</label>
        <select
            id="homeClub"
            v-model="homeClubId"
            :disabled="saving"
            class="px-3 py-2 rounded-lg border border-surface-300 text-surface-800 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
            @change="saveHomeClub"
        >
          <option :value="null">Kein Stammverein</option>
          <option v-for="club in clubs" :key="club.id" :value="club.id">{{ club.club }}</option>
        </select>
      </div>
    </div>

    <p class="mt-6 text-sm text-surface-400">
      In zukünftigen Versionen können Sie hier Ihr Profil bearbeiten.
    </p>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from "@/stores/notifications.js";
import {api} from '@/api/http'

const auth = useAuthStore()
const notify = useNotificationStore()

const clubs = ref([])
const homeClubId = ref(auth.user?.homeClubId ?? null)
const saving = ref(false)
const saved = ref(false)

async function saveHomeClub() {
  saving.value = true
  saved.value = false
  try {
    await api.updateProfile({homeClubId: homeClubId.value})
    await auth.fetchUser()
    notify.success('Stammverein aktualisiert!')
    setTimeout(() => (saved.value = false), 2000)
  } catch (err) {
    console.error('Error updating home club:', err)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    clubs.value = await api.getLocations()
    homeClubId.value = auth.user?.homeClubId ?? null
  } catch (err) {
    console.error('Error fetching clubs:', err)
  }
})
</script>