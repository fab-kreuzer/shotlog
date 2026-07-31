<template>
  <div>
    <PageHeader :subtitle="$t('profile.subtitle')" :title="$t('profile.title')" color="primary" icon="pi pi-user"/>

    <Card>
      <template #content>
        <div class="flex flex-col gap-4">
          <div class="flex items-center gap-4">
            <Avatar v-if="auth.avatarUrl" :image="auth.avatarUrl" shape="circle" size="xlarge"/>
            <Avatar v-else :label="initials" class="bg-primary-500 font-semibold text-white" shape="circle"
                    size="xlarge"/>
            <div class="flex flex-col gap-2">
              <input ref="fileInput" accept="image/*" class="hidden" type="file" @change="onAvatarSelected"/>
              <div class="flex gap-2">
                <Button
                    :label="$t('profile.uploadAvatar')"
                    :loading="uploadingAvatar"
                    icon="pi pi-upload"
                    severity="secondary"
                    size="small"
                    @click="fileInput.click()"
                />
                <Button
                    v-if="auth.user?.hasAvatar"
                    :disabled="uploadingAvatar"
                    :label="$t('profile.removeAvatar')"
                    icon="pi pi-trash"
                    severity="secondary"
                    size="small"
                    text
                    @click="removeAvatar"
                />
              </div>
              <span class="text-xs text-surface-500">{{ $t('profile.avatarHint') }}</span>
            </div>
          </div>

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

          <div class="pt-2">
            <Button
                :label="$t('profile.changePassword')"
                icon="pi pi-lock"
                severity="secondary"
                size="small"
                @click="openChangePasswordDialog"
            />
          </div>
        </div>
      </template>
    </Card>

    <Dialog v-model:visible="showChangePasswordDialog" :header="$t('profile.changePassword')" class="w-full max-w-sm"
            modal>
      <div class="flex flex-col gap-4">
        <div class="flex flex-col gap-1.5">
          <label class="text-sm text-surface-500" for="currentPassword">{{ $t('profile.currentPassword') }}</label>
          <Password v-model="currentPassword" :disabled="changingPassword" :feedback="false" fluid
                    inputId="currentPassword" toggleMask/>
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-sm text-surface-500" for="newPassword">{{ $t('profile.newPassword') }}</label>
          <Password v-model="newPassword" :disabled="changingPassword" :feedback="false" fluid
                    inputId="newPassword" toggleMask/>
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-sm text-surface-500" for="confirmNewPassword">{{
              $t('profile.confirmNewPassword')
            }}</label>
          <Password v-model="confirmNewPassword" :disabled="changingPassword" :feedback="false" fluid
                    inputId="confirmNewPassword" toggleMask @keyup.enter="submitChangePassword"/>
        </div>
      </div>
      <template #footer>
        <Button :disabled="changingPassword" :label="$t('common.cancel')" severity="secondary"
                @click="showChangePasswordDialog = false"/>
        <Button :label="$t('profile.changePassword')" :loading="changingPassword" icon="pi pi-lock"
                @click="submitChangePassword"/>
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import Card from 'primevue/card'
import Select from 'primevue/select'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Avatar from 'primevue/avatar'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications.js'
import {api} from '@/api/http'
import PageHeader from '@/components/PageHeader.vue'

const {t} = useI18n()
const auth = useAuthStore()
const notify = useNotificationStore()

const MAX_AVATAR_BYTES = 2 * 1024 * 1024

const initials = computed(() => {
  const name = auth.user?.displayName || auth.user?.username || '?'
  return name.trim().split(/\s+/).map(w => w[0]).slice(0, 2).join('').toUpperCase()
})

const fileInput = ref(null)
const uploadingAvatar = ref(false)

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

const AVATAR_SIZE = 256

// Downscale + center-crop to a small square before upload. A profile picture is
// rendered tiny, so storing/serving/decoding a full-resolution phone photo just
// wastes bandwidth and main-thread decode time on every render.
async function downscaleImage(file) {
  const dataUrl = await new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })
  const img = await new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => resolve(image)
    image.onerror = () => reject(new Error('decode failed'))
    image.src = dataUrl
  })
  const side = Math.min(img.width, img.height)
  const sx = (img.width - side) / 2
  const sy = (img.height - side) / 2
  const canvas = document.createElement('canvas')
  canvas.width = AVATAR_SIZE
  canvas.height = AVATAR_SIZE
  canvas.getContext('2d').drawImage(img, sx, sy, side, side, 0, 0, AVATAR_SIZE, AVATAR_SIZE)
  const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.9))
  return new File([blob], 'avatar.jpg', {type: 'image/jpeg'})
}

async function onAvatarSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    notify.error(t('profile.avatarInvalidType'))
    resetFileInput()
    return
  }
  if (file.size > MAX_AVATAR_BYTES) {
    notify.error(t('profile.avatarTooLarge'))
    resetFileInput()
    return
  }
  uploadingAvatar.value = true
  try {
    const resized = await downscaleImage(file)
    // Success toast is emitted by the HTTP layer from the API response.
    await api.uploadAvatar(resized)
    await auth.fetchUser()
    auth.refreshAvatar()
  } catch (err) {
    console.error('Error uploading avatar:', err)
    notify.error(t('profile.avatarInvalidType'))
  } finally {
    uploadingAvatar.value = false
    resetFileInput()
  }
}

async function removeAvatar() {
  uploadingAvatar.value = true
  try {
    await api.removeAvatar()
    await auth.fetchUser()
    auth.refreshAvatar()
  } catch (err) {
    console.error('Error removing avatar:', err)
  } finally {
    uploadingAvatar.value = false
  }
}

function resetFileInput() {
  if (fileInput.value) fileInput.value.value = ''
}

const showChangePasswordDialog = ref(false)
const currentPassword = ref('')
const newPassword = ref('')
const confirmNewPassword = ref('')
const changingPassword = ref(false)

function openChangePasswordDialog() {
  currentPassword.value = ''
  newPassword.value = ''
  confirmNewPassword.value = ''
  showChangePasswordDialog.value = true
}

async function submitChangePassword() {
  if (!currentPassword.value) {
    notify.error(t('profile.currentPasswordRequired'))
    return
  }
  if (newPassword.value.length < 6) {
    notify.error(t('validation.passwordMinLength'))
    return
  }
  if (newPassword.value !== confirmNewPassword.value) {
    notify.error(t('validation.passwordsMismatch'))
    return
  }

  changingPassword.value = true
  try {
    // Success toast is emitted by the HTTP layer from the API response.
    await api.changePassword({currentPassword: currentPassword.value, newPassword: newPassword.value})
    currentPassword.value = ''
    newPassword.value = ''
    confirmNewPassword.value = ''
    showChangePasswordDialog.value = false
  } catch (err) {
    console.error('Error changing password:', err)
  } finally {
    changingPassword.value = false
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
