<script setup>
import {onMounted, ref} from 'vue'
import CreateRoleModal from "@/components/settings/CreateRoleModal.vue";
import ConfirmModal from "@/components/ConfirmModal.vue";
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'
import {api} from '@/api/http'

const auth = useAuthStore()
const notify = useNotificationStore()

const showConfirmRole = ref(false)
const roleToDelete = ref(null)
const roles = ref([])
const showCreateRoleModal = ref(false)
const newRoleName = ref('')
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
  <div class="p-6 space-y-10">
    <!-- ---- ROLE MODAL ----- -->
    <section class="space-y-6">
      <div class="flex justify-between items-center">
        <h3 class="text-base font-semibold text-surface-700">Rollenliste</h3>

        <button
            class="px-4 py-2 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800"
            @click="showCreateRoleModal = true"
        >
          + Rolle
        </button>
      </div>

      <div class="overflow-x-auto rounded-xl border border-surface-200">
        <table class="w-full text-sm">
          <thead>
          <tr class="bg-surface-50 border-b border-surface-200">
            <th class="px-4 py-3 text-left">ID</th>
            <th class="px-4 py-3 text-left">Name</th>
            <th class="px-4 py-3 text-right">Aktionen</th>
          </tr>
          </thead>

          <tbody class="divide-y divide-surface-100">
          <tr v-for="role in roles" :key="role.id">
            <td class="px-4 py-3 text-surface-500">
              {{ role.id }}
            </td>

            <td class="px-4 py-3">
              {{ role.name }}
            </td>

            <td class="px-4 py-3 text-right">
              <div class="flex justify-end gap-2">
                <button class="px-3 py-1.5 text-xs rounded-lg text-info-600 bg-info-50 dark:bg-info-500/15 dark:text-info-400" @click="openEditRole(role)">
                  Bearbeiten
                </button>
                <button class="px-3 py-1.5 text-xs rounded-lg text-danger-600 bg-danger-50 dark:bg-danger-500/15 dark:text-danger-400"
                        @click="handleDeleteRole(role.id)">Löschen
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>
    <!-- ---- ROLE MODAL ----- -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="editingRole" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="editingRole = null"></div>
          <div class="relative w-full max-w-md bg-card rounded-2xl shadow-2xl p-6"
               style="animation: scaleIn 0.2s ease-out">
            <div class="flex items-center justify-between mb-5">
              <h4 class="text-lg font-semibold text-surface-800">Rolle bearbeiten</h4>
              <button
                  class="p-1.5 rounded-lg text-surface-400 hover:text-surface-600 hover:bg-surface-100 transition-colors"
                  @click="editingRole = null">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path d="M6 18L18 6M6 6l12 12" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
                </svg>
              </button>
            </div>
            <form class="space-y-4" @submit.prevent="handleUpdateRole">
              <div>
                <label class="block text-sm font-medium text-surface-700 mb-1.5"
                       for="editRoleName">Rollenname</label>
                <input
                    id="editRoleName"
                    v-model="editingRole.name"
                    class="w-full px-3 py-2 rounded-lg border border-surface-300 text-sm text-surface-800 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
                    required
                    type="text"
                >
              </div>
              <div class="flex justify-end gap-3 pt-2">
                <button
                    class="px-4 py-2 rounded-lg text-sm font-medium text-surface-600 bg-card border border-surface-300 hover:bg-surface-50 transition-colors"
                    type="button"
                    @click="editingRole = null">
                  Abbrechen
                </button>
                <button
                    class="px-4 py-2 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors"
                    type="submit">
                  Speichern
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>

  <CreateRoleModal
      v-model="showCreateRoleModal"
      @create="handleCreateRole"
  />

  <ConfirmModal
      v-model="showConfirmRole"
      confirmText="Ja, löschen"
      message="Willst du diese Rolle wirklich löschen?"
      title="Rolle löschen"
      @confirm="confirmDeleteRole"
  />

</template>

<style scoped>

</style>