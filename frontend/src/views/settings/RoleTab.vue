<script setup>
import {onMounted, ref} from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import PermissionMatrix from '@/components/settings/PermissionMatrix.vue'
import CreateRoleModal from "@/components/settings/CreateRoleModal.vue";
import ConfirmModal from "@/components/ConfirmModal.vue";
import {useAuthStore} from '@/stores/auth'
import {api} from '@/api/http'

const auth = useAuthStore()

const showConfirmRole = ref(false)
const roleToDelete = ref(null)
const roles = ref([])
const permissions = ref([])
const showCreateRoleModal = ref(false)
const editingRole = ref(null)

async function loadData() {
  if (!auth.hasPermission('view_role_tab')) return

  roles.value = await api.getRoles()
  permissions.value = await api.getPermissions()
}

async function handleCreateRole(role) {
  await api.createRole(role)
  await loadData()
}

function openEditRole(role) {
  editingRole.value = {
    ...role,
    permissionIds: role.permissions.map(p => p.id)
  }
}

async function handleUpdateRole() {
  await api.updateRole(editingRole.value.id, {
    name: editingRole.value.name,
    permissionIds: editingRole.value.permissionIds
  })

  // If the edited role belongs to the current user, their cached permissions
  // are now stale — refresh them so the UI reflects the change without a reload.
  const affectsCurrentUser = auth.user?.roles?.includes(editingRole.value.name)

  editingRole.value = null
  await loadData()

  if (affectsCurrentUser) {
    await auth.fetchUser()
  }
}

async function handleDeleteRole(id) {
  roleToDelete.value = id
  showConfirmRole.value = true
}

async function confirmDeleteRole() {
  await api.deleteRole(roleToDelete.value)
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="p-2">
    <div class="flex justify-between items-center mb-6">
      <h3 class="flex items-center gap-2 text-lg font-semibold text-surface-700">
        <i class="pi pi-shield text-primary-500"/>
        {{ $t('role.listTitle') }}
      </h3>
      <Button v-if="auth.hasPermission('create_role')" :label="$t('role.newButton')" icon="pi pi-plus" @click="showCreateRoleModal = true"/>
    </div>

    <DataTable :value="roles" class="border border-surface-200 rounded-lg overflow-hidden" dataKey="id" stripedRows>
      <Column :header="$t('role.id')" field="id" sortable/>
      <Column :header="$t('common.name')" field="name" sortable/>
      <Column :header="$t('role.permissions')">
        <template #body="{ data }">
          <div class="flex flex-wrap gap-1">
            <Tag v-for="p in data.permissions" :key="p.id" :value="p.permissionName" severity="secondary"/>
          </div>
        </template>
      </Column>
      <Column v-if="auth.hasPermission('edit_role') || auth.hasPermission('delete_role')" :header="$t('common.actions')">
        <template #body="{ data }">
          <div class="flex justify-end gap-2">
            <Button v-if="auth.hasPermission('edit_role')" icon="pi pi-pencil" rounded severity="info" size="small" text @click="openEditRole(data)"/>
            <Button v-if="auth.hasPermission('delete_role')" icon="pi pi-trash" rounded severity="danger" size="small" text @click="handleDeleteRole(data.id)"/>
          </div>
        </template>
      </Column>
    </DataTable>
  </div>

  <!-- Edit role dialog -->
  <Dialog
      :draggable="false"
      :style="{ width: '32rem' }"
      :visible="!!editingRole"
      :header="$t('role.editTitle')"
      modal
      @update:visible="(v) => { if (!v) editingRole = null }"
  >
    <form v-if="editingRole" id="edit-role-form" class="flex flex-col gap-4" @submit.prevent="handleUpdateRole">
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="editRoleName">{{ $t('role.roleName') }}</label>
        <InputText id="editRoleName" v-model="editingRole.name" fluid required/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700">{{ $t('role.permissions') }}</label>
        <PermissionMatrix v-model="editingRole.permissionIds" :permissions="permissions"/>
      </div>
    </form>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text type="button" @click="editingRole = null"/>
      <Button :label="$t('common.save')" form="edit-role-form" type="submit"/>
    </template>
  </Dialog>

  <CreateRoleModal
      v-model="showCreateRoleModal"
      :permissions="permissions"
      @create="handleCreateRole"
  />

  <ConfirmModal
      v-model="showConfirmRole"
      :confirmText="$t('role.deleteConfirm')"
      :message="$t('role.deleteMessage')"
      :title="$t('role.deleteTitle')"
      @confirm="confirmDeleteRole"
  />
</template>
