<template>
  <div class="p-6 space-y-10">
    <h2 class="text-lg font-semibold text-surface-800">Benutzerverwaltung</h2>

    <!-- --- USER MANAGEMENT --- -->
    <section class="space-y-6">
      <!-- Create User Form -->
      <div class="flex justify-between items-center">
        <h4 class="text-sm font-semibold text-surface-700">
          Benutzerliste
        </h4>

        <button
            class="px-4 py-2 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 cursor-pointer"
            @click="showCreateUserModal = true"
        >
          + Benutzer
        </button>
      </div>
      <!-- User Table -->
        <div class="overflow-x-auto rounded-xl border border-surface-200">
          <table class="w-full text-sm">
            <thead>
            <tr class="bg-surface-50 border-b border-surface-200">
              <th class="text-left px-4 py-3">ID</th>
              <th class="text-left px-4 py-3">Benutzername</th>
              <th class="text-left px-4 py-3">Rollen</th>
              <th class="text-right px-4 py-3">Aktionen</th>
            </tr>
            </thead>

            <tbody class="divide-y divide-surface-100">
            <tr
                v-for="user in users"
                :key="user.id"
                class="hover:bg-surface-50"
            >
              <td class="px-4 py-3 text-surface-500">
                {{ user.id }}
              </td>

              <td class="px-4 py-3 font-medium text-surface-800">
                {{ user.username }}
              </td>

              <td class="px-4 py-3">
                <div class="flex flex-wrap gap-1">
                    <span
                        v-for="r in user.roles"
                        :key="r.id"
                        class="px-2 py-0.5 rounded-full text-xs bg-surface-100 text-surface-600"
                    >
                      {{ r.name }}
                    </span>
                </div>
              </td>

              <td class="px-4 py-3 text-right">
                <div class="flex justify-end gap-2">
                  <button
                      class="px-3 py-1.5 text-xs rounded-lg text-info-600 bg-info-50"
                      @click="openEditUser(user)"
                  >
                    Bearbeiten
                  </button>

                  <button
                      class="px-3 py-1.5 text-xs rounded-lg text-danger-600 bg-danger-50"
                      @click="handleDeleteUser(user.id)"
                  >
                    Löschen
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
      </div>
    </section>

    <!-- ---- ROLE MODAL ----- -->
    <section class="space-y-6">
      <h3 class="text-base font-semibold text-surface-700">Rollen</h3>

      <form class="flex gap-3" @submit.prevent="handleCreateRole">
        <input v-model="newRoleName" class="flex-1 px-3 py-2 rounded-lg border border-surface-300 text-sm"
               placeholder="Rollenname"/>
        <button class="px-4 py-2 rounded-lg text-sm font-medium text-white bg-primary-700" type="submit">Erstellen
        </button>
      </form>

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
                <button class="px-3 py-1.5 text-xs rounded-lg text-info-600 bg-info-50" @click="openEditRole(role)">
                  Bearbeiten
                </button>
                <button class="px-3 py-1.5 text-xs rounded-lg text-danger-600 bg-danger-50"
                        @click="handleDeleteRole(role.id)">Löschen
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- ----USER MODAL ----- -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="editingUser" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="editingUser = null"></div>
          <div class="relative w-full max-w-md bg-white rounded-2xl shadow-2xl p-6"
               style="animation: scaleIn 0.2s ease-out">
            <div class="flex items-center justify-between mb-5">
              <h4 class="text-lg font-semibold text-surface-800">Benutzer bearbeiten</h4>
              <button
                  class="p-1.5 rounded-lg text-surface-400 hover:text-surface-600 hover:bg-surface-100 transition-colors"
                  @click="editingUser = null">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path d="M6 18L18 6M6 6l12 12" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
                </svg>
              </button>
            </div>
            <form class="space-y-4" @submit.prevent="handleUpdateUser">
              <div>
                <label class="block text-sm font-medium text-surface-700 mb-1.5"
                       for="editUsername">Benutzername</label>
                <input
                    id="editUsername"
                    v-model="editingUser.username"
                    class="w-full px-3 py-2 rounded-lg border border-surface-300 text-sm text-surface-800 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
                    type="text"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-surface-700 mb-1.5" for="editPassword">Neues Passwort
                  <span class="text-surface-400 font-normal">(leer lassen, um beizubehalten)</span></label>
                <input
                    id="editPassword"
                    v-model="editingUser.password"
                    class="w-full px-3 py-2 rounded-lg border border-surface-300 text-sm text-surface-800 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
                    type="password"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-surface-700 mb-2">Rollen</label>
                <div class="flex flex-wrap gap-3">
                  <label v-for="role in roles" :key="role.id" class="flex items-center gap-2 cursor-pointer">
                    <input
                        :id="'edit-role-' + role.id"
                        v-model="editingUser.roleIds"
                        :value="role.id"
                        class="w-4 h-4 rounded border-surface-300 text-primary-600 focus:ring-primary-500"
                        type="checkbox"
                    >
                    <span class="text-sm text-surface-700">{{ role.name }}</span>
                  </label>
                </div>
              </div>
              <div class="flex justify-end gap-3 pt-2">
                <button
                    class="px-4 py-2 rounded-lg text-sm font-medium text-surface-600 bg-white border border-surface-300 hover:bg-surface-50 transition-colors"
                    type="button"
                    @click="editingUser = null">
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

    <!-- ---- ROLE MODAL ----- -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="editingRole" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="editingRole = null"></div>
          <div class="relative w-full max-w-md bg-white rounded-2xl shadow-2xl p-6"
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
                    class="px-4 py-2 rounded-lg text-sm font-medium text-surface-600 bg-white border border-surface-300 hover:bg-surface-50 transition-colors"
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

    <CreateUserModal
        v-model="showCreateUserModal"
        :roles="roles"
        @create="handleCreateUser"
    />

  </div>

</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'
import {api} from '@/api/http'
import CreateUserModal from "@/components/settings/CreateUserModal.vue";

const auth = useAuthStore()
const notify = useNotificationStore()

const users = ref([])
const roles = ref([])

const showCreateUserModal = ref(false)

const newUser = ref({
  username: '',
  password: '',
  roleIds: []
})

const newRoleName = ref('')

const editingUser = ref(null)
const editingRole = ref(null)

async function loadData() {
  if (!auth.isAdmin) return

  users.value = await api.getUsers()
  console.log(users.value)
  roles.value = await api.getRoles()
}

/* ───────── USERS ───────── */

async function handleCreateUser(user) {
  try {
    await api.createUser(user)
    notify.success('Benutzer erstellt')
    await loadData()
  } catch (e) {
    notify.error(e.error || 'Fehler')
  }
}

function openEditUser(user) {
  editingUser.value = {
    id: user.id,
    username: user.username,
    password: '',
    roleIds: user.roles.map(r => r.id)
  }
}

async function handleUpdateUser() {
  const data = {
    username: editingUser.value.username,
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
  if (!confirm('Löschen?')) return
  await api.deleteUser(id)
  await loadData()
}

/* ───────── ROLES ───────── */

async function handleCreateRole() {
  await api.createRole({name: newRoleName.value})
  newRoleName.value = ''
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
  if (!confirm('Löschen?')) return
  await api.deleteRole(id)
  await loadData()
}

onMounted(loadData)
</script>