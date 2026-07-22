<script setup>
import {onMounted, ref} from "vue";
import {api} from '@/api/http'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Multiselect from '@/components/Multiselect.vue'
import CreateUserModal from "@/components/settings/CreateUserModal.vue";
import ConfirmModal from '@/components/ConfirmModal.vue'
import {useAuthStore} from "@/stores/auth.js";
import {useNotificationStore} from "@/stores/notifications.js";
import {useI18n} from 'vue-i18n'

const {t} = useI18n()
const auth = useAuthStore()
const notify = useNotificationStore()

const showConfirmUser = ref(false)
const showCreateUserModal = ref(false)
const userToDelete = ref(null)
const users = ref([])
const roles = ref([])

const editingUser = ref(null)

async function loadData() {
  if (!auth.isAdmin)
    return

  users.value = await api.getUsers()
  roles.value = await api.getRoles()
}

async function handleCreateUser(user) {
  try {
    await api.createUser(user)
    await loadData()
  } catch (e) {
    if (!e._notified) notify.error(t('user.createError'))
  }
}

function openEditUser(user) {
  editingUser.value = {
    id: user.id,
    username: user.username,
    displayName: user.displayName,
    password: '',
    roleIds: user.roles.map(r => r.id)
  }
}

async function handleUpdateUser() {
  if (!editingUser.value.username?.trim()) {
    notify.warn(t('user.usernameRequired'))
    return
  }

  if (!editingUser.value.displayName?.trim()) {
    notify.warn(t('user.displayNameRequired'))
    return
  }

  const data = {
    username: editingUser.value.username.trim(),
    displayName: editingUser.value.displayName.trim(),
    roleIds: editingUser.value.roleIds
  }

  if (editingUser.value.password) {
    data.password = editingUser.value.password
  }

  await api.updateUser(editingUser.value.id, data)
  editingUser.value = null
  await loadData()
}

async function handleDeleteUser(id) {
  userToDelete.value = id
  showConfirmUser.value = true
}

async function confirmDeleteUser() {
  await api.deleteUser(userToDelete.value)
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="p-2">
    <div class="flex justify-between items-center mb-6">
      <h1 class="flex items-center gap-2 text-lg font-semibold text-surface-700">
        <i class="pi pi-users text-primary-500"/>
        {{ $t('user.listTitle') }}
      </h1>
      <Button :label="$t('user.newButton')" icon="pi pi-plus" @click="showCreateUserModal = true"/>
    </div>

    <DataTable :value="users" class="border border-surface-200 rounded-lg overflow-hidden" dataKey="id" stripedRows>
      <Column :header="$t('user.id')" field="id" sortable/>
      <Column :header="$t('user.username')" field="username" sortable/>
      <Column :header="$t('user.displayName')" field="displayName" sortable/>
      <Column :header="$t('user.roles')">
        <template #body="{ data }">
          <div class="flex flex-wrap gap-1">
            <Tag v-for="r in data.roles" :key="r.id" :value="r.name" severity="secondary"/>
          </div>
        </template>
      </Column>
      <Column :header="$t('common.actions')">
        <template #body="{ data }">
          <div class="flex justify-end gap-2">
            <Button icon="pi pi-pencil" rounded severity="info" size="small" text @click="openEditUser(data)"/>
            <Button icon="pi pi-trash" rounded severity="danger" size="small" text @click="handleDeleteUser(data.id)"/>
          </div>
        </template>
      </Column>
    </DataTable>
  </div>

  <!-- Edit user dialog -->
  <Dialog
      :draggable="false"
      :style="{ width: '32rem' }"
      :visible="!!editingUser"
      :header="$t('user.editTitle')"
      modal
      @update:visible="(v) => { if (!v) editingUser = null }"
  >
    <form v-if="editingUser" id="edit-user-form" class="flex flex-col gap-4" @submit.prevent="handleUpdateUser">
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="editUsername">
          {{ $t('user.username') }} <span class="text-danger-500">*</span>
        </label>
        <InputText id="editUsername" v-model="editingUser.username" fluid/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="editDisplayName">
          {{ $t('user.displayName') }} <span class="text-danger-500">*</span>
        </label>
        <InputText id="editDisplayName" v-model="editingUser.displayName" fluid/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="editPassword">
          {{ $t('user.newPassword') }} <span class="text-surface-400 font-normal">{{
            $t('user.passwordKeepHint')
          }}</span>
        </label>
        <Password v-model="editingUser.password" :feedback="false" fluid inputId="editPassword" toggleMask/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700">{{ $t('user.roles') }}</label>
        <Multiselect
            v-model="editingUser.roleIds"
            :options="roles"
            optionLabel="name"
            optionValue="id"
            :placeholder="$t('user.selectRoles')"
        />
      </div>
    </form>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text type="button" @click="editingUser = null"/>
      <Button :label="$t('common.save')" form="edit-user-form" type="submit"/>
    </template>
  </Dialog>

  <ConfirmModal
      v-model="showConfirmUser"
      :confirmText="$t('user.deleteConfirm')"
      :message="$t('user.deleteMessage')"
      :title="$t('user.deleteTitle')"
      @confirm="confirmDeleteUser"
  />

  <CreateUserModal
      v-model="showCreateUserModal"
      :roles="roles"
      @create="handleCreateUser"
  />
</template>
