<template>
  <div class="fixed bottom-0 right-0 z-50 p-4 flex flex-col-reverse gap-3 max-w-md w-full pointer-events-none">
    <TransitionGroup name="notification">
      <div
          v-for="notif in notifications.notifications"
          :key="notif.id"
          :class="[
            'pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg border backdrop-blur-sm',
            notif.type === 'error'
              ? 'bg-danger-50 border-danger-200 text-danger-700'
              : 'bg-primary-50 border-primary-200 text-primary-800'
          ]"
      >
        <!-- Icon -->
        <div :class="[
          'flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center',
          notif.type === 'error' ? 'bg-danger-100' : 'bg-primary-100'
        ]">
          <svg v-if="notif.type === 'error'" class="w-4 h-4 text-danger-500" fill="none" stroke="currentColor"
               viewBox="0 0 24 24">
            <path d="M6 18L18 6M6 6l12 12" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
          </svg>
          <svg v-else class="w-4 h-4 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path d="M5 13l4 4L19 7" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
          </svg>
        </div>

        <!-- Message -->
        <span class="flex-1 text-sm font-medium">{{ notif.message }}</span>

        <!-- Close button -->
        <button
            class="flex-shrink-0 p-1 rounded-lg hover:bg-black/5 transition-colors"
            @click="notifications.remove(notif.id)"
        >
          <svg class="w-4 h-4 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path d="M6 18L18 6M6 6l12 12" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
          </svg>
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import {useNotificationStore} from '@/stores/notifications'

const notifications = useNotificationStore()
</script>

<style scoped>
.notification-enter-active {
  animation: slideIn 0.3s ease-out;
}

.notification-leave-active {
  animation: slideOut 0.3s ease-in;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(1rem);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(1rem);
  }
}
</style>
