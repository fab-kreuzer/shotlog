<template>
  <div>
    <PageHeader :subtitle="$t('profile.subtitle')" :title="$t('profile.title')" color="primary" icon="pi pi-user"/>

    <Card>
      <template #content>
        <div class="flex flex-col gap-4">
          <div class="flex items-center gap-3">
            <span class="text-sm text-surface-500 w-32">{{ $t('user.username') }}:</span>
            <span class="text-sm font-medium text-surface-800">
              {{ auth.user?.username }}
            </span>
          </div>

          <div class="flex items-center gap-3">
            <label class="text-sm text-surface-500 w-32" for="displayName">{{ $t('user.displayName') }}:</label>
            <InputText
                id="displayName"
                v-model="displayName"
                :disabled="savingName"
                class="w-64"
                @keyup.enter="saveDisplayName"
            />
            <Button
                :disabled="savingName || !isDisplayNameDirty"
                :label="$t('profile.save')"
                :loading="savingName"
                icon="pi pi-check"
                severity="secondary"
                size="small"
                @click="saveDisplayName"
            />
          </div>

          <div class="flex items-start gap-3">
            <span class="text-sm text-surface-500 w-32">{{ $t('user.roles') }}:</span>
            <div class="flex flex-wrap gap-1.5">
              <template v-if="auth.user?.roles?.length">
                <Tag v-for="(role, i) in auth.user.roles" :key="i" :value="role" severity="info"/>
              </template>
              <span v-else class="text-sm text-surface-500">{{ $t('profile.noRoles') }}</span>
            </div>
          </div>

          <div class="flex items-start gap-3">
            <span class="text-sm text-surface-500 w-32">{{ $t('user.teams') }}:</span>
            <div class="flex flex-wrap gap-1.5">
              <template v-if="auth.user?.teams?.length">
                <Tag v-for="(team, i) in auth.user.teams" :key="i" :value="teamLabel(team)" severity="info"/>
              </template>
              <span v-else class="text-sm text-surface-500">{{ $t('profile.noTeams') }}</span>
            </div>
          </div>

          <div class="flex items-center gap-3">
            <label class="text-sm text-surface-500 w-32" for="homeClub">{{ $t('profile.homeClub') }}:</label>
            <Select
                id="homeClub"
                v-model="homeClubId"
                :disabled="savingClub"
                :options="clubs"
                :placeholder="$t('profile.noHomeClub')"
                class="w-64"
                optionLabel="club"
                optionValue="id"
                showClear
                @change="saveHomeClub"
            />
          </div>

          <div class="flex items-center gap-3">
            <span class="text-sm text-surface-500 w-32">{{ $t('profile.activeSeason') }}:</span>
            <span class="text-sm font-medium text-surface-800">
              {{ activeSeasonName || '—' }}
            </span>
          </div>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import Card from 'primevue/card'
import Select from 'primevue/select'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications.js'
import {api} from '@/api/http'
import PageHeader from '@/components/PageHeader.vue'

const {t} = useI18n()
const auth = useAuthStore()
const notify = useNotificationStore()

const displayName = ref(auth.user?.displayName ?? '')
const savingName = ref(false)
const isDisplayNameDirty = computed(() =>
    displayName.value.trim() && displayName.value.trim() !== (auth.user?.displayName ?? '')
)

const clubs = ref([])
const homeClubId = ref(auth.user?.homeClubId ?? null)
const savingClub = ref(false)

const seasons = ref([])
const activeSeasonName = computed(() => seasons.value.find(s => s.active)?.description ?? '')

const teamRoles = ref([])
const roleLabels = computed(() =>
    Object.fromEntries(teamRoles.value.map(role => [role.name, role.type]))
)

function roleLabel(role) {
  return roleLabels.value[role] ?? role
}

function teamLabel(team) {
  const base = `${team.name} - ${roleLabel(team.role)}`
  return team.season?.description ? `${base} (${team.season.description})` : base
}

async function saveDisplayName() {
  const value = displayName.value.trim()
  if (!value) {
    notify.error(t('profile.displayNameRequired'))
    return
  }
  savingName.value = true
  try {
    // Success toast is emitted by the HTTP layer from the API response.
    await api.updateProfile({displayName: value})
    await auth.fetchUser()
    displayName.value = auth.user?.displayName ?? value
  } catch (err) {
    console.error('Error updating display name:', err)
  } finally {
    savingName.value = false
  }
}

async function saveHomeClub() {
  savingClub.value = true
  try {
    await api.updateProfile({homeClubId: homeClubId.value})
    await auth.fetchUser()
  } catch (err) {
    console.error('Error updating home club:', err)
  } finally {
    savingClub.value = false
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
  } catch (err) {
    console.error('Error fetching seasons:', err)
  }
  try {
    teamRoles.value = await api.getTeamRoles()
  } catch (err) {
    console.error('Error fetching team roles:', err)
  }
})
</script>
