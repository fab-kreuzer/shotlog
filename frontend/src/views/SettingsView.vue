<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">Einstellungen</h1>
      <p class="mt-1 text-surface-500">Verwalten Sie Ihr Profil und Systemeinstellungen</p>
    </div>

    <!-- Tabs -->
    <div class="bg-white rounded-xl border border-surface-200 shadow-sm overflow-hidden">
      <div class="border-b border-surface-200">
        <nav aria-label="Tabs" class="flex gap-0">
          <button
              :class="[
                'px-6 py-3.5 text-sm font-medium border-b-2 transition-colors',
                activeTab === 'profile'
                  ? 'border-primary-700 text-primary-700'
                  : 'border-transparent text-surface-500 hover:text-surface-700 hover:border-surface-300'
              ]"
              @click="activeTab = 'profile'"
          >
            Profil
          </button>
          <button
              v-if="auth.isAdmin"
              :class="[
                'px-6 py-3.5 text-sm font-medium border-b-2 transition-colors',
                activeTab === 'admin'
                  ? 'border-primary-700 text-primary-700'
                  : 'border-transparent text-surface-500 hover:text-surface-700 hover:border-surface-300'
              ]"
              @click="activeTab = 'admin'"
          >
            Admin
          </button>
        </nav>
      </div>

      <!-- Profile Tab -->
      <div v-if="activeTab === 'profile'" class="p-6">
        <h2 class="text-lg font-semibold text-surface-800 mb-4">Mein Profil</h2>
        <div class="space-y-3">
          <div class="flex items-center gap-3">
            <span class="text-sm text-surface-500 w-32">Benutzername:</span>
            <span class="text-sm font-medium text-surface-800">{{ auth.user?.username }}</span>
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
        </div>
        <p class="mt-6 text-sm text-surface-400">In zukünftigen Versionen können Sie hier Ihr Profil bearbeiten.</p>
      </div>

      <!-- Admin Tab -->
      <div v-if="activeTab === 'admin' && auth.isAdmin" class="p-6 space-y-10">
        <h2 class="text-lg font-semibold text-surface-800">Benutzerverwaltung</h2>

        <!-- ── User Management ── -->
        <section class="space-y-6">
          <h3 class="text-base font-semibold text-surface-700">Benutzer</h3>

          <!-- Create User Form -->
          <div class="bg-surface-50 rounded-xl border border-surface-200 p-5">
            <h4 class="text-sm font-semibold text-surface-700 mb-4">Neuen Benutzer erstellen</h4>
            <form class="space-y-4" @submit.prevent="handleCreateUser">
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5"
                         for="newUsername">Benutzername</label>
                  <input
                      id="newUsername"
                      v-model="newUser.username"
                      class="w-full px-3 py-2 rounded-lg border border-surface-300 text-sm text-surface-800 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
                      required
                      type="text"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5" for="newPassword">Passwort</label>
                  <input
                      id="newPassword"
                      v-model="newUser.password"
                      class="w-full px-3 py-2 rounded-lg border border-surface-300 text-sm text-surface-800 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
                      required
                      type="password"
                  >
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-surface-700 mb-2">Rollen</label>
                <div class="flex flex-wrap gap-3">
                  <label v-for="role in roles" :key="role.id" class="flex items-center gap-2 cursor-pointer">
                    <input
                        :id="'role-' + role.id"
                        v-model="newUser.roleIds"
                        :value="role.id"
                        class="w-4 h-4 rounded border-surface-300 text-primary-600 focus:ring-primary-500"
                        type="checkbox"
                    >
                    <span class="text-sm text-surface-700">{{ role.name }}</span>
                  </label>
                </div>
              </div>
              <button
                  class="px-4 py-2 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors"
                  type="submit"
              >
                Benutzer erstellen
              </button>
            </form>
          </div>

          <!-- User List -->
          <div>
            <h4 class="text-sm font-semibold text-surface-700 mb-3">Benutzerliste</h4>
            <div class="overflow-x-auto rounded-xl border border-surface-200">
              <table class="w-full text-sm">
                <thead>
                <tr class="bg-surface-50 border-b border-surface-200">
                  <th class="text-left px-4 py-3 font-semibold text-surface-600">ID</th>
                  <th class="text-left px-4 py-3 font-semibold text-surface-600">Benutzername</th>
                  <th class="text-left px-4 py-3 font-semibold text-surface-600">Rollen</th>
                  <th class="text-right px-4 py-3 font-semibold text-surface-600">Aktionen</th>
                </tr>
                </thead>
                <tbody class="divide-y divide-surface-100">
                <tr v-for="user in users" :key="user.id" class="hover:bg-surface-50 transition-colors">
                  <td class="px-4 py-3 text-surface-500">{{ user.id }}</td>
                  <td class="px-4 py-3 font-medium text-surface-800">{{ user.username }}</td>
                  <td class="px-4 py-3">
                    <div class="flex flex-wrap gap-1">
                      <span
                          v-for="r in user.roles"
                          :key="r.id"
                          class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-surface-100 text-surface-600"
                      >
                        {{ r.name }}
                      </span>
                    </div>
                  </td>
                  <td class="px-4 py-3 text-right">
                    <div class="flex items-center justify-end gap-2">
                      <button
                          class="px-3 py-1.5 rounded-lg text-xs font-medium text-info-600 bg-info-50 hover:bg-info-100 transition-colors"
                          @click="openEditUser(user)"
                      >
                        Bearbeiten
                      </button>
                      <button
                          class="px-3 py-1.5 rounded-lg text-xs font-medium text-danger-600 bg-danger-50 hover:bg-danger-100 transition-colors"
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
          </div>

          <!-- Edit User Modal -->
          <Teleport to="body">
            <Transition name="modal">
              <div v-if="editingUser" class="fixed inset-0 z-50 flex items-center justify-center p-4">
                <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="editingUser = null"></div>
                <div class="relative w-full max-w-md bg-white rounded-2xl shadow-2xl p-6"
                     style="animation: scaleIn 0.2s ease-out">
                  <div class="flex items-center justify-between mb-5">
                    <h4 class="text-lg font-semibold text-surface-800">Benutzer bearbeiten</h4>
                    <button class="p-1.5 rounded-lg text-surface-400 hover:text-surface-600 hover:bg-surface-100 transition-colors"
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
                      <button class="px-4 py-2 rounded-lg text-sm font-medium text-surface-600 bg-white border border-surface-300 hover:bg-surface-50 transition-colors" type="button"
                              @click="editingUser = null">
                        Abbrechen
                      </button>
                      <button class="px-4 py-2 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors"
                              type="submit">
                        Speichern
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </Transition>
          </Teleport>
        </section>

        <!-- ── Role Management ── -->
        <section class="space-y-6">
          <h3 class="text-base font-semibold text-surface-700">Rollen</h3>

          <!-- Create Role Form -->
          <div class="bg-surface-50 rounded-xl border border-surface-200 p-5">
            <h4 class="text-sm font-semibold text-surface-700 mb-4">Neue Rolle erstellen</h4>
            <form class="flex flex-col sm:flex-row gap-3" @submit.prevent="handleCreateRole">
              <div class="flex-1">
                <label class="sr-only" for="newRoleName">Rollenname</label>
                <input
                    id="newRoleName"
                    v-model="newRoleName"
                    class="w-full px-3 py-2 rounded-lg border border-surface-300 text-sm text-surface-800 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-shadow"
                    placeholder="Rollenname eingeben"
                    required
                    type="text"
                >
              </div>
              <button
                  class="px-4 py-2 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors whitespace-nowrap"
                  type="submit"
              >
                Rolle erstellen
              </button>
            </form>
          </div>

          <!-- Role List -->
          <div>
            <h4 class="text-sm font-semibold text-surface-700 mb-3">Rollenliste</h4>
            <div class="overflow-x-auto rounded-xl border border-surface-200">
              <table class="w-full text-sm">
                <thead>
                <tr class="bg-surface-50 border-b border-surface-200">
                  <th class="text-left px-4 py-3 font-semibold text-surface-600">ID</th>
                  <th class="text-left px-4 py-3 font-semibold text-surface-600">Name</th>
                  <th class="text-right px-4 py-3 font-semibold text-surface-600">Aktionen</th>
                </tr>
                </thead>
                <tbody class="divide-y divide-surface-100">
                <tr v-for="role in roles" :key="role.id" class="hover:bg-surface-50 transition-colors">
                  <td class="px-4 py-3 text-surface-500">{{ role.id }}</td>
                  <td class="px-4 py-3 font-medium text-surface-800">{{ role.name }}</td>
                  <td class="px-4 py-3 text-right">
                    <div class="flex items-center justify-end gap-2">
                      <button
                          class="px-3 py-1.5 rounded-lg text-xs font-medium text-info-600 bg-info-50 hover:bg-info-100 transition-colors"
                          @click="openEditRole(role)"
                      >
                        Bearbeiten
                      </button>
                      <button
                          class="px-3 py-1.5 rounded-lg text-xs font-medium text-danger-600 bg-danger-50 hover:bg-danger-100 transition-colors"
                          @click="handleDeleteRole(role.id)"
                      >
                        Löschen
                      </button>
                    </div>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Edit Role Modal -->
          <Teleport to="body">
            <Transition name="modal">
              <div v-if="editingRole" class="fixed inset-0 z-50 flex items-center justify-center p-4">
                <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="editingRole = null"></div>
                <div class="relative w-full max-w-md bg-white rounded-2xl shadow-2xl p-6"
                     style="animation: scaleIn 0.2s ease-out">
                  <div class="flex items-center justify-between mb-5">
                    <h4 class="text-lg font-semibold text-surface-800">Rolle bearbeiten</h4>
                    <button class="p-1.5 rounded-lg text-surface-400 hover:text-surface-600 hover:bg-surface-100 transition-colors"
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
                      <button class="px-4 py-2 rounded-lg text-sm font-medium text-surface-600 bg-white border border-surface-300 hover:bg-surface-50 transition-colors" type="button"
                              @click="editingRole = null">
                        Abbrechen
                      </button>
                      <button class="px-4 py-2 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors"
                              type="submit">
                        Speichern
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </Transition>
          </Teleport>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'
import {api} from '@/api/http'

const auth = useAuthStore()
const notify = useNotificationStore()

const activeTab = ref('profile')
const users = ref([])
const roles = ref([])

// New user form
const newUser = ref({username: '', password: '', roleIds: []})
const newRoleName = ref('')

// Edit state
const editingUser = ref(null)
const editingRole = ref(null)

async function loadData() {
  if (!auth.isAdmin) return
  try {
    users.value = await api.getUsers()
    roles.value = await api.getRoles()
  } catch (err) {
    console.error('Error loading settings data:', err)
  }
}

// User management
async function handleCreateUser() {
  try {
    await api.createUser({
      username: newUser.value.username,
      password: newUser.value.password,
      roleIds: newUser.value.roleIds
    })
    notify.success('Benutzer erfolgreich erstellt')
    newUser.value = {username: '', password: '', roleIds: []}
    await loadData()
  } catch (err) {
    notify.error(err.error || 'Fehler beim Erstellen des Benutzers')
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
  try {
    const data = {
      username: editingUser.value.username,
      roleIds: editingUser.value.roleIds
    }
    if (editingUser.value.password) {
      data.password = editingUser.value.password
    }
    await api.updateUser(editingUser.value.id, data)
    notify.success('Benutzer erfolgreich aktualisiert')
    editingUser.value = null
    await loadData()
  } catch (err) {
    notify.error(err.error || 'Fehler beim Aktualisieren des Benutzers')
  }
}

async function handleDeleteUser(userId) {
  if (confirm('Sind Sie sicher, dass Sie diesen Benutzer löschen möchten?')) {
    try {
      await api.deleteUser(userId)
      notify.success('Benutzer erfolgreich gelöscht')
      await loadData()
    } catch (err) {
      notify.error(err.error || 'Fehler beim Löschen des Benutzers')
    }
  }
}

// Role management
async function handleCreateRole() {
  try {
    await api.createRole({name: newRoleName.value})
    notify.success('Rolle erfolgreich erstellt')
    newRoleName.value = ''
    await loadData()
  } catch (err) {
    notify.error(err.error || 'Fehler beim Erstellen der Rolle')
  }
}

function openEditRole(role) {
  editingRole.value = {id: role.id, name: role.name}
}

async function handleUpdateRole() {
  try {
    await api.updateRole(editingRole.value.id, {name: editingRole.value.name})
    notify.success('Rolle erfolgreich aktualisiert')
    editingRole.value = null
    await loadData()
  } catch (err) {
    notify.error(err.error || 'Fehler beim Aktualisieren der Rolle')
  }
}

async function handleDeleteRole(roleId) {
  if (confirm('Sind Sie sicher, dass Sie diese Rolle löschen möchten?')) {
    try {
      await api.deleteRole(roleId)
      notify.success('Rolle erfolgreich gelöscht')
      await loadData()
    } catch (err) {
      notify.error(err.error || 'Fehler beim Löschen der Rolle')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.modal-enter-active {
  animation: fadeIn 0.2s ease-out;
}

.modal-leave-active {
  animation: fadeIn 0.15s ease-in reverse;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
