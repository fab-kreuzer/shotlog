<template>
  <div class="fixed bottom-0 right-0 z-9999 p-4 flex flex-col-reverse gap-3 max-w-md w-full pointer-events-none">
    <TransitionGroup name="notification">
      <div
          v-for="notif in notifications.notifications"
          :key="notif.id"
          :class="[
          'pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg border backdrop-blur-sm',
          typeClass[notif.type] || typeClass.success
        ]"
      >
        <!-- Icon -->
        <div
            :class="[
            'flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center',
            iconBgClass(notif.type)
          ]"
        >
          <!-- Error -->
          <svg
              v-if="notif.type === 'error'"
              class="w-4 h-4 text-danger-500 dark:text-danger-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
          >
            <path
                d="M6 18L18 6M6 6l12 12"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
            />
          </svg>

          <!-- Warning -->
          <svg
              v-else-if="notif.type === 'warn'"
              class="w-4 h-4 text-warning-600 dark:text-warning-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
          >
            <path
                d="M12 9v4m0 4h.01M10.29 3.86l-8.2 14.2A1.5 1.5 0 0 0 3.35 20h16.3a1.5 1.5 0 0 0 1.26-2.24l-8.2-14.2a1.5 1.5 0 0 0-2.42 0z"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
            />
          </svg>

          <!-- Success / default -->
          <svg
              v-else
              class="w-4 h-4 text-success-600 dark:text-success-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
          >
            <path
                d="M5 13l4 4L19 7"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
            />
          </svg>
        </div>

        <!-- Message -->
        <span class="flex-1 text-sm font-medium">
          {{ notif.message }}
        </span>

        <!-- Close button -->
        <button
            class="flex-shrink-0 p-1 rounded-lg hover:bg-black/5 dark:hover:bg-white/10 transition-colors"
            @click="notifications.remove(notif.id)"
        >
          <svg
              class="w-4 h-4 opacity-50"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
          >
            <path
                d="M6 18L18 6M6 6l12 12"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
            />
          </svg>
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import {useNotificationStore} from '@/stores/notifications'

const notifications = useNotificationStore()

const typeClass = {
  error: 'bg-danger-50 border-danger-200 text-danger-700 dark:bg-danger-500/15 dark:border-danger-500/30 dark:text-danger-200',
  warn: 'bg-warning-50 border-warning-200 text-warning-800 dark:bg-warning-500/15 dark:border-warning-500/30 dark:text-warning-200',
  success: 'bg-success-50 border-success-200 text-success-800 dark:bg-success-500/15 dark:border-success-500/30 dark:text-success-200'
}

function iconBgClass(type) {
  switch (type) {
    case 'error':
      return 'bg-danger-100 dark:bg-danger-500/25'
    case 'warn':
      return 'bg-warning-100 dark:bg-warning-500/25'
    default:
      return 'bg-success-100 dark:bg-success-500/25'
  }
}
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