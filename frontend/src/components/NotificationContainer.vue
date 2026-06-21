<template>
  <div id="notification-container">
    <div
        v-for="(notif, index) in notifications.notifications"
        :key="notif.id"
        :class="'notification-' + notif.type"
        :style="{ bottom: (20 + index * 70) + 'px' }"
        class="notification"
    >
      <span class="notification-content">{{ notif.message }}</span>
      <button class="notification-close" @click="notifications.remove(notif.id)">&times;</button>
    </div>
  </div>
</template>

<script setup>
import {useNotificationStore} from '@/stores/notifications'

const notifications = useNotificationStore()
</script>
