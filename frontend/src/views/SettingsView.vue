<template>
  <div>
    <h1>Einstellungen</h1>

    <div class="settings-container">
      <!-- Tabs navigation -->
      <div class="tabs">
        <button :class="{ active: activeTab === 'profile' }" class="tab-button" @click="activeTab = 'profile'">Profil
        </button>
        <button v-if="auth.isAdmin" :class="{ active: activeTab === 'admin' }" class="tab-button"
                @click="activeTab = 'admin'">Admin
        </button>
      </div>

      <!-- Profile Tab -->
      <div v-if="activeTab === 'profile'" class="tab-content active">
        <h2>Mein Profil</h2>
        <p>Benutzername: <span>{{ auth.user?.username }}</span></p>
        <p>Rollen:
          <span v-for="(role, i) in auth.user?.roles" :key="i">
            {{ role }}<span v-if="i < auth.user.roles.length - 1">, </span>
          </span>
        </p>
        <p>In zukünftigen Versionen können Sie hier Ihr Profil bearbeiten.</p>
      </div>

      <!-- Admin Tab -->
      <div v-if="activeTab === 'admin' && auth.isAdmin" class="tab-content active">
        <h2>Benutzerverwaltung</h2>

        <!-- User Management Section -->
        <div class="user-management">
          <h3>Benutzer</h3>

          <!-- Create User Form -->
          <div class="create-form">
            <h4>Neuen Benutzer erstellen</h4>
            <form @submit.prevent="handleCreateUser">
              <div class="form-group">
                <label for="newUsername">Benutzername:</label>
                <input id="newUsername" v-model="newUser.username" required type="text">
              </div>
              <div class="form-group">
                <label for="newPassword">Passwort:</label>
                <input id="newPassword" v-model="newUser.password" required type="password">
              </div>
              <div class="form-group">
                <label>Rollen:</label>
                <div class="checkbox-group">
                  <div v-for="role in roles" :key="role.id">
                    <input :id="'role-' + role.id" v-model="newUser.roleIds" :value="role.id" type="checkbox">
                    <label :for="'role-' + role.id">{{ role.name }}</label>
                  </div>
                </div>
              </div>
              <button class="btn btn-primary" type="submit">Benutzer erstellen</button>
            </form>
          </div>

          <!-- User List -->
          <div class="user-list">
            <h4>Benutzerliste</h4>
            <table>
              <thead>
              <tr>
                <th>ID</th>
                <th>Benutzername</th>
                <th>Rollen</th>
                <th>Aktionen</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="user in users" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.username }}</td>
                <td>{{ user.roles.map(r => r.name).join(', ') }}</td>
                <td>
                  <button class="btn btn-edit" @click="openEditUser(user)">Bearbeiten</button>
                  <button class="btn btn-delete" @click="handleDeleteUser(user.id)">Löschen</button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- Edit User Modal -->
          <div v-if="editingUser" class="settings-modal" style="display: block;">
            <div class="settings-modal-content">
              <span class="close" @click="editingUser = null">&times;</span>
              <h4>Benutzer bearbeiten</h4>
              <form @submit.prevent="handleUpdateUser">
                <div class="form-group">
                  <label for="editUsername">Benutzername:</label>
                  <input id="editUsername" v-model="editingUser.username" type="text">
                </div>
                <div class="form-group">
                  <label for="editPassword">Neues Passwort (leer lassen, um beizubehalten):</label>
                  <input id="editPassword" v-model="editingUser.password" type="password">
                </div>
                <div class="form-group">
                  <label>Rollen:</label>
                  <div class="checkbox-group">
                    <div v-for="role in roles" :key="role.id">
                      <input :id="'edit-role-' + role.id" v-model="editingUser.roleIds" :value="role.id"
                             type="checkbox">
                      <label :for="'edit-role-' + role.id">{{ role.name }}</label>
                    </div>
                  </div>
                </div>
                <button class="btn btn-primary" type="submit">Speichern</button>
              </form>
            </div>
          </div>
        </div>

        <!-- Role Management Section -->
        <div class="role-management">
          <h3>Rollen</h3>

          <!-- Create Role Form -->
          <div class="create-form">
            <h4>Neue Rolle erstellen</h4>
            <form @submit.prevent="handleCreateRole">
              <div class="form-group">
                <label for="newRoleName">Rollenname:</label>
                <input id="newRoleName" v-model="newRoleName" required type="text">
              </div>
              <button class="btn btn-primary" type="submit">Rolle erstellen</button>
            </form>
          </div>

          <!-- Role List -->
          <div class="role-list">
            <h4>Rollenliste</h4>
            <table>
              <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Aktionen</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="role in roles" :key="role.id">
                <td>{{ role.id }}</td>
                <td>{{ role.name }}</td>
                <td>
                  <button class="btn btn-edit" @click="openEditRole(role)">Bearbeiten</button>
                  <button class="btn btn-delete" @click="handleDeleteRole(role.id)">Löschen</button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- Edit Role Modal -->
          <div v-if="editingRole" class="settings-modal" style="display: block;">
            <div class="settings-modal-content">
              <span class="close" @click="editingRole = null">&times;</span>
              <h4>Rolle bearbeiten</h4>
              <form @submit.prevent="handleUpdateRole">
                <div class="form-group">
                  <label for="editRoleName">Rollenname:</label>
                  <input id="editRoleName" v-model="editingRole.name" required type="text">
                </div>
                <button class="btn btn-primary" type="submit">Speichern</button>
              </form>
            </div>
          </div>
        </div>
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
