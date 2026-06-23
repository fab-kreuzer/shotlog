<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">Dashboard</h1>
      <p class="mt-1 text-surface-500">Willkommen bei ShotLog</p>
    </div>

    <!-- Content card -->
    <div class="bg-white rounded-xl border border-surface-200 shadow-sm p-6">
      <p class="text-surface-600 mb-4">You are logged in.</p>
    </div>
  </div>
</template>

<script setup>
import {useNotificationStore} from '@/stores/notifications'

const notify = useNotificationStore()
</script>
