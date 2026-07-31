<template>
  <div class="animate-slide-up flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
    <div class="flex items-center gap-4">
      <div :class="[COLORS[color].bg, COLORS[color].text]"
           class="flex items-center justify-center w-12 h-12 rounded-xl shrink-0">
        <i :class="icon" style="font-size: 1.25rem"/>
      </div>
      <div>
        <h1 class="text-2xl font-bold text-surface-800">{{ title }}</h1>
        <p v-if="subtitle" class="mt-1 text-surface-500">{{ subtitle }}</p>
      </div>
    </div>
    <div v-if="$slots.actions" class="flex flex-wrap items-center gap-2">
      <slot name="actions"/>
    </div>
  </div>
</template>

<script setup>
const COLORS = {
  primary: {bg: 'bg-primary-50 dark:bg-primary-400/10', text: 'text-primary-600 dark:text-primary-400'},
  green: {bg: 'bg-green-50 dark:bg-green-400/10', text: 'text-green-600 dark:text-green-400'},
  red: {bg: 'bg-red-50 dark:bg-red-400/10', text: 'text-red-600 dark:text-red-400'},
  blue: {bg: 'bg-blue-50 dark:bg-blue-400/10', text: 'text-blue-600 dark:text-blue-400'},
  purple: {bg: 'bg-purple-50 dark:bg-purple-400/10', text: 'text-purple-600 dark:text-purple-400'},
  amber: {bg: 'bg-amber-50 dark:bg-amber-400/10', text: 'text-amber-600 dark:text-amber-400'}
}

defineProps({
  icon: {type: String, required: true},
  title: {type: String, required: true},
  subtitle: {type: String, default: ''},
  color: {type: String, default: 'primary'}
})
</script>
