<script setup>
import {onMounted, ref} from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import CreateRoleModal from "@/components/settings/CreateRoleModal.vue";
import ConfirmModal from "@/components/ConfirmModal.vue";
import {useAuthStore} from '@/stores/auth'
import {api} from '@/api/http'

const auth = useAuthStore()

const showConfirmRole = ref(false)
const roleToDelete = ref(null)
const roles = ref([])
const showCreateRoleModal = ref(false)
const editingRole = ref(null)

async function loadData() {
  if (!auth.isAdmin) return

  roles.value = await api.getRoles()
}

async function handleCreateRole(role) {
  await api.createRole(role)
  await loadData()
}

function openEditRole(role) {
  editingRole.value = {...role}
}

async function handleUpdateRole() {
  await api.updateRole(editingRole.value.id, {
    name: editingRole.value.name
  })

  editingRole.value = null
  await loadData()
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
      <h3 class="text-lg font-semibold text-surface-700">{{ $t('role.listTitle') }}</h3>
      <Button :label="$t('role.newButton')" icon="pi pi-plus" @click="showCreateRoleModal = true"/>
    </div>

    <DataTable :value="roles" class="border border-surface-200 rounded-lg overflow-hidden" dataKey="id" stripedRows>
      <Column :header="$t('role.id')" field="id" sortable/>
      <Column :header="$t('common.name')" field="name" sortable/>
      <Column :header="$t('common.actions')">
        <template #body="{ data }">
          <div class="flex justify-end gap-2">
            <Button icon="pi pi-pencil" rounded severity="info" size="small" text @click="openEditRole(data)"/>
            <Button icon="pi pi-trash" rounded severity="danger" size="small" text @click="handleDeleteRole(data.id)"/>
          </div>
        </template>
      </Column>
    </DataTable>
  </div>

  <!-- Edit role dialog -->
  <Dialog
      :draggable="false"
      :style="{ width: '28rem' }"
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
    </form>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text type="button" @click="editingRole = null"/>
      <Button :label="$t('common.save')" form="edit-role-form" type="submit"/>
    </template>
  </Dialog>

  <CreateRoleModal
      v-model="showCreateRoleModal"
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
