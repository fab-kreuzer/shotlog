<template>
  <div class="p-6">
    <h2 class="text-lg font-semibold text-surface-800 mb-4">Mein Profil</h2>

    <div class="space-y-3">
      <div class="flex items-center gap-3">
        <span class="text-sm text-surface-500 w-32">Benutzername:</span>
        <span class="text-sm font-medium text-surface-800">
          {{ auth.user?.username }}
        </span>
      </div>

      <div class="flex items-center gap-3">
        <span class="text-sm text-surface-500 w-32">Anzeigename:</span>
        <span class="text-sm font-medium text-surface-800">
          {{ auth.user?.displayName }}
        </span>
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

    <p class="mt-6 text-sm text-surface-400">
      In zukünftigen Versionen können Sie hier Ihr Profil bearbeiten.
    </p>
  </div>
</template>

<script setup>
import {useAuthStore} from '@/stores/auth'

const auth = useAuthStore()
</script>