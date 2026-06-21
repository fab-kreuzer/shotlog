<template>
  <div>
    <header v-if="auth.isLoggedIn">
      <nav class="navbar navbar-expand-lg">
        <div class="container-fluid">
          <router-link
              :class="{ active: route.name === 'dashboard' }"
              class="navbar-brand text-decoration-none"
              to="/dashboard"
          >ShotLog
          </router-link>

          <button
              class="navbar-toggler"
              data-bs-target="#navbarNav"
              data-bs-toggle="collapse"
              type="button"
          >
            <span class="navbar-toggler-icon"></span>
          </button>

          <div id="navbarNav" class="collapse navbar-collapse">
            <div class="navbar-nav me-auto">
              <router-link
                  :class="{ active: route.name === 'overview' && route.query.type === 'training' }"
                  class="nav-link"
                  to="/overview?type=training"
              >Training
              </router-link>

              <router-link
                  :class="{ active: route.name === 'overview' && route.query.type === 'competition' }"
                  class="nav-link"
                  to="/overview?type=competition"
              >Wettkampf
              </router-link>

              <router-link
                  :class="{ active: route.name === 'settings' }"
                  class="nav-link"
                  to="/settings"
              >Einstellungen
              </router-link>

              <router-link
                  :class="{ active: route.name === 'calender' }"
                  class="nav-link"
                  to="/calender"
              >Kalender
              </router-link>
            </div>

            <div class="navbar-nav">
              <button class="btn btn-outline-light" type="button" @click="handleLogout">
                <i class="fas fa-sign-out-alt me-1"></i>Logout
              </button>
            </div>
          </div>
        </div>
      </nav>
    </header>

    <main>
      <router-view/>
    </main>

    <NotificationContainer/>
  </div>
</template>

<script setup>
import {useRoute, useRouter} from 'vue-router'
import {useAuthStore} from '@/stores/auth'
import {useNotificationStore} from '@/stores/notifications'
import NotificationContainer from '@/components/NotificationContainer.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const notify = useNotificationStore()

async function handleLogout() {
  await auth.logout()
  notify.success('Sie wurden erfolgreich abgemeldet.')
  router.push('/login')
}
</script>
