<template>
  <div>
    <h1>Dashboard</h1>
    <p>You are logged in.</p>

    <button @click="notify.success('Dies ist eine Testbenachrichtigung vom Dashboard!')">
      Testbenachrichtigung anzeigen
    </button>
  </div>
</template>

<script setup>
import {useNotificationStore} from '@/stores/notifications'

const notify = useNotificationStore()
</script>
