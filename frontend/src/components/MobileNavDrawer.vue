<template>
  <Teleport to="body">
    <Transition name="drawer-overlay">
      <div v-if="modelValue" class="fixed inset-0 z-overlay bg-black/40" @click="close"/>
    </Transition>
    <Transition name="drawer-slide">
      <aside
          v-if="modelValue"
          aria-modal="true"
          class="fixed inset-y-0 left-0 z-modal flex w-72 max-w-[85vw] flex-col bg-card shadow-xl"
          role="dialog"
          @keydown.esc="close"
      >
        <div class="flex items-center justify-between border-b border-surface-200 px-4 py-3">
          <router-link class="flex items-center gap-2.5 text-lg font-bold tracking-tight" to="/dashboard"
                       @click="close">
            <img alt="ShotLog" class="h-8 w-8 rounded-full object-cover" src="/logo.png"/>
            ShotLog
          </router-link>
          <Button :aria-label="$t('common.close')" icon="pi pi-times" rounded severity="secondary" text type="button"
                  @click="close"/>
        </div>

        <nav class="flex flex-col gap-1 overflow-y-auto p-3">
          <router-link
              v-for="link in navLinks"
              :key="link.to"
              v-slot="{ href, navigate, isActive }"
              :to="link.to"
              custom
          >
            <a
                :class="isActive ? 'bg-surface-100 font-semibold text-primary-500' : 'text-surface-800 hover:bg-surface-100'"
                :href="href"
                class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition-colors"
                @click="navigate(); close()"
            >
              <i :class="link.icon"/>
              <span>{{ $t(link.labelKey) }}</span>
            </a>
          </router-link>
        </nav>

        <div class="mt-auto flex flex-col gap-1 border-t border-surface-200 p-3">
          <button
              v-for="(item, i) in actionItems"
              :key="i"
              class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm text-surface-800 transition-colors hover:bg-surface-100"
              type="button"
              @click="handleAction(item)"
          >
            <i :class="item.icon"/>
            <span>{{ item.label }}</span>
          </button>
        </div>
      </aside>
    </Transition>
  </Teleport>
</template>

<script setup>
import {computed, onUnmounted, watch} from 'vue'
import {useRoute} from 'vue-router'
import Button from 'primevue/button'

const props = defineProps({
  modelValue: {type: Boolean, required: true},
  navLinks: {type: Array, required: true},
  userMenuItems: {type: Array, default: () => []}
})

const emit = defineEmits(['update:modelValue'])

const route = useRoute()

const actionItems = computed(() => props.userMenuItems.filter(item => !item.separator))

function close() {
  emit('update:modelValue', false)
}

function handleAction(item) {
  close()
  item.command?.()
}

// Close the drawer automatically whenever navigation happens (link click, back/forward, etc.)
watch(() => route.fullPath, () => close())

function onKeydown(event) {
  if (event.key === 'Escape' && props.modelValue) close()
}

watch(() => props.modelValue, (open) => {
  if (open) {
    window.addEventListener('keydown', onKeydown)
  } else {
    window.removeEventListener('keydown', onKeydown)
  }
})

onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>
