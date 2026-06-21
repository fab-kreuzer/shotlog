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

      <button
          class="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-medium text-white bg-primary-700 hover:bg-primary-800 shadow-sm transition-colors"
          @click="notify.success('Dies ist eine Testbenachrichtigung vom Dashboard!')"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" stroke-linecap="round" stroke-linejoin="round"
                stroke-width="2"/>
        </svg>
        Testbenachrichtigung anzeigen
      </button>
    </div>
  </div>
</template>

<script setup>
import {useNotificationStore} from '@/stores/notifications'

const notify = useNotificationStore()
</script>
