<script setup>
import {computed, onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'
import {api} from '@/api/http'
import Button from "primevue/button";
import Accordion from "primevue/accordion";
import AccordionPanel from "primevue/accordionpanel";
import AccordionHeader from "primevue/accordionheader";
import AccordionContent from "primevue/accordioncontent";
import Dialog from "primevue/dialog";
import ConfirmDialog from "primevue/confirmdialog";
import {useConfirm} from "primevue/useconfirm";
import InputGroup from "primevue/inputgroup";
import InputText from "primevue/inputtext";
import Select from "primevue/select";

const auth = useAuthStore()
const confirm = useConfirm()
const {t} = useI18n()
const notification = useNotificationStore()

const teams = ref([])
const allUsers = ref([])
const teamRoles = ref([])
const showAddMemberDialog = ref(false)
const selectedTeamForAdd = ref(null)
const selectedRole = ref('MEMBER')
const searchQuery = ref('')
const showCreateTeamDialog = ref(false)
const newTeamName = ref('')

const roleLabels = computed(() =>
    Object.fromEntries(teamRoles.value.map(role => [role.name, role.type]))
)

function roleLabel(role) {
  return roleLabels.value[role] ?? role
}

const filteredUsers = computed(() => {
  if (!selectedTeamForAdd.value) return []

  const teamMemberIds = new Set(
      selectedTeamForAdd.value.userTeams?.map(ut => ut.user.id) || []
  )

  return allUsers.value.filter(user =>
      !teamMemberIds.has(user.id) &&
      (user.displayName?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
          user.username?.toLowerCase().includes(searchQuery.value.toLowerCase()))
  )
})

async function loadData() {
  if (!auth.hasPermission('view_team_tab')) {
    return
  }

  console.log('loading teams data')
  try {
    teams.value = await api.getTeams()
    allUsers.value = await api.getUsers()
    teamRoles.value = await api.getTeamRoles()
    console.log('teams data loaded')
    console.log(teams.value)
  } catch (error) {
    console.error('Error loading teams:', error)
  }
}

function openAddMemberDialog(team) {
  selectedTeamForAdd.value = team
  searchQuery.value = ''
  selectedRole.value = 'MEMBER'
  showAddMemberDialog.value = true
}

async function addMemberToTeam(user) {
  if (!selectedTeamForAdd.value) return

  try {
    await api.post(`/api/teams/${selectedTeamForAdd.value.id}/members`, {
      userId: user.id,
      role: selectedRole.value
    })

    const userTeamItem = {
      user,
      role: selectedRole.value
    }
    selectedTeamForAdd.value.userTeams.push(userTeamItem)
    if (user.id === auth.user?.id) {
      await auth.fetchUser()
    }
    showAddMemberDialog.value = false
    notification.success(t('team.memberAdded'))
  } catch (error) {
    console.error('Error adding member:', error)
  }
}

function handleDeleteRole(userTeam, teamId) {
  const userName = userTeam.displayName || userTeam.username
  confirm.require({
    message: t('team.removeMemberMessage', {userName}),
    header: t('team.removeMemberTitle'),
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: t('team.removeMemberConfirm'),
    rejectLabel: t('common.cancel'),
    accept: async () => {
      try {
        await api.delete(`/api/teams/${teamId}/members/${userTeam.id}`)
        const team = teams.value.find(tm => tm.id === teamId)
        if (team) {
          team.userTeams = team.userTeams.filter(ut => ut.user.id !== userTeam.id)
        }
        if (userTeam.id === auth.user?.id) {
          await auth.fetchUser()
        }
        notification.success(t('team.memberRemoved'))
      } catch (error) {
        console.error('Error deleting member:', error)
      }
    }
  })
}

function openCreateTeamDialog() {
  newTeamName.value = ''
  showCreateTeamDialog.value = true
}

async function createTeam() {
  if (!newTeamName.value.trim()) return

  try {
    const newTeam = await api.createTeam({name: newTeamName.value})
    newTeam.userTeams = []
    teams.value.push(newTeam)
    showCreateTeamDialog.value = false
    newTeamName.value = ''
    notification.success(t('team.teamCreated'))
  } catch (error) {
    console.error('Error creating team:', error)
  }
}

function deleteTeam(team) {
  confirm.require({
    message: t('team.deleteMessage'),
    header: t('team.deleteTitle'),
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: t('team.deleteConfirm'),
    rejectLabel: t('common.cancel'),
    accept: () => performTeamDelete(team, false)
  })
}

async function performTeamDelete(team, deleteSessions) {
  try {
    await api.delete(`/api/teams/${team.id}?deleteSessions=${deleteSessions}`, {notify: false})
    teams.value = teams.value.filter(tm => tm.id !== team.id)
    notification.success(t('team.teamDeleted'))
  } catch (error) {
    // The team still has sessions attached — ask whether to delete them too.
    if (error?.status === 409 && !deleteSessions) {
      confirm.require({
        message: t('team.deleteWithSessionsMessage', {count: error.sessionCount ?? 0}),
        header: t('team.deleteWithSessionsTitle'),
        icon: 'pi pi-exclamation-triangle',
        acceptLabel: t('team.deleteWithSessionsConfirm'),
        rejectLabel: t('common.cancel'),
        accept: () => performTeamDelete(team, true)
      })
      return
    }
    console.error('Error deleting team:', error)
    notification.error(t('team.deleteFailed'))
  }
}

onMounted(loadData)
</script>

<template>
  <div class="p-2">
    <div class="flex justify-between items-center mb-6">
      <h3 class="flex items-center gap-2 text-lg font-semibold text-surface-700">
        <i class="pi pi-sitemap text-primary-500"/>
        {{ $t('team.listTitle') }}
      </h3>
      <Button v-if="auth.hasPermission('create_team')" :label="$t('team.newButton')" icon="pi pi-plus" @click="openCreateTeamDialog()"/>
    </div>
    <Accordion class="border border-surface-200 rounded-lg overflow-hidden">
      <AccordionPanel v-for="team in teams" :key="team.id" :value="String(team.id)">
        <AccordionHeader>
          <div class="flex justify-between items-center w-full pr-4">
            <div class="flex-1">
              <span class="font-semibold">{{ team.name }}</span>
              <span class="text-sm text-surface-500 ml-2">({{
                  team.userTeams?.length || 0
                }} {{
                  (team.userTeams?.length || 0) === 1 ? $t('team.memberSingular') : $t('team.memberPlural')
                }})</span>
            </div>
            <div class="flex gap-2">
              <Button v-if="auth.hasPermission('edit_team')" :title="$t('team.addMemberTitle', {teamName: team.name})" icon="pi pi-plus" rounded severity="success" size="small"
                      text
                      @click.stop="openAddMemberDialog(team)"/>
              <Button v-if="auth.hasPermission('delete_team')" :title="$t('team.deleteTitle')" icon="pi pi-trash" rounded severity="danger" size="small" text
                      @click.stop="deleteTeam(team)"/>
            </div>
          </div>
        </AccordionHeader>
        <AccordionContent>
        <div class="space-y-4">
          <div v-if="team.userTeams && team.userTeams.length > 0" class="space-y-2">
            <div v-for="userTeam in team.userTeams" :key="`${team.id}-${userTeam.user.id}`"
                 class="flex justify-between items-center p-3 bg-surface-50 rounded-lg border border-surface-200">
              <div>
                <p class="font-medium text-surface-900">{{ userTeam.user.displayName || userTeam.user.username }}</p>
                <p class="text-sm text-surface-500">@{{ userTeam.user.username }}</p>
              </div>
              <div class="flex items-center gap-3">
                <span class="px-3 py-1 bg-primary-100 text-primary-800 rounded text-sm font-medium">{{
                    roleLabel(userTeam.role)
                  }}</span>
                <Button v-if="auth.hasPermission('edit_team')" icon="pi pi-trash" rounded severity="danger" size="small" text
                        @click="handleDeleteRole(userTeam.user, team.id)"/>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-6 text-surface-500">
            {{ $t('common.noData') }}
          </div>
        </div>
        </AccordionContent>
      </AccordionPanel>
    </Accordion>

    <Dialog v-model:visible="showAddMemberDialog"
            :header="$t('team.addMemberTitle', {teamName: selectedTeamForAdd?.name})" modal>
      <div class="space-y-4">
        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('team.roleLabel') }}</label>
          <Select v-model="selectedRole" :options="teamRoles" optionLabel="type" optionValue="name"
                  :placeholder="$t('team.roleLabel')" fluid/>
        </div>
        <InputGroup>
          <InputText v-model="searchQuery" :placeholder="$t('team.searchPlaceholder')"/>
          <Button icon="pi pi-search"/>
        </InputGroup>

        <div class="border border-surface-200 rounded-lg max-h-96 overflow-y-auto">
          <div v-if="filteredUsers.length === 0" class="p-4 text-center text-surface-500">
            {{ searchQuery ? $t('team.noUsersFound') : $t('team.allUsersMembersMessage') }}
          </div>
          <div v-for="user in filteredUsers" :key="user.id"
               class="p-3 border-b border-surface-200 hover:bg-surface-50 cursor-pointer"
               @click="addMemberToTeam(user)">
            <p class="font-medium text-surface-900">{{ user.displayName || user.username }}</p>
            <p class="text-sm text-surface-500">@{{ user.username }}</p>
          </div>
        </div>
      </div>
    </Dialog>

    <Dialog v-model:visible="showCreateTeamDialog" :header="$t('team.createTitle')" modal>
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-2">{{ $t('team.nameLabel') }}</label>
          <InputText v-model="newTeamName" :placeholder="$t('team.namePlaceholder')" class="w-full"
                     @keyup.enter="createTeam()"/>
        </div>
      </div>
      <template #footer>
        <Button :label="$t('common.cancel')" severity="secondary" @click="showCreateTeamDialog = false"/>
        <Button :label="$t('common.create')" @click="createTeam()"/>
      </template>
    </Dialog>

    <ConfirmDialog/>
  </div>
</template>