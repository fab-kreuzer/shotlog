<script setup>
import {computed, ref} from 'vue'
import Tag from 'primevue/tag'
import Popover from 'primevue/popover'
import {useI18n} from 'vue-i18n'

const {t, te} = useI18n()

const props = defineProps({
  permissions: {
    type: Array,
    default: () => []
  }
})

// Keep action ordering consistent with PermissionMatrix.vue
const ACTION_ORDER = ['view', 'create', 'edit', 'delete']

function actionRank(action) {
  const i = ACTION_ORDER.indexOf(action)
  return i === -1 ? ACTION_ORDER.length : i
}

// Group the flat permission list into { resource, actions[] }, preserving
// first-seen resource order and ACTION_ORDER within each group.
const groups = computed(() => {
  const byResource = new Map()
  for (const p of props.permissions) {
    if (!byResource.has(p.resource)) byResource.set(p.resource, [])
    byResource.get(p.resource).push(p.action)
  }
  return [...byResource.entries()].map(([resource, actions]) => ({
    resource,
    actions: [...actions].sort((a, b) => actionRank(a) - actionRank(b))
  }))
})

const op = ref()
const active = ref(null)

function openPopover(event, group) {
  active.value = group
  op.value.show(event)
}

function resourceLabel(resource) {
  const key = `permission.resource.${resource}`
  return te(key) ? t(key) : resource
}

function actionLabel(action) {
  const key = `permission.action.${action}`
  return te(key) ? t(key) : action
}
</script>

<template>
  <div v-if="groups.length" class="flex flex-wrap gap-1.5">
    <Tag
        v-for="group in groups"
        :key="group.resource"
        class="cursor-pointer select-none hover:brightness-95"
        severity="secondary"
        @click="openPopover($event, group)"
    >
      <span class="inline-flex items-center gap-1.5">
        {{ resourceLabel(group.resource) }}
        <span class="inline-flex items-center justify-center min-w-[1.1rem] h-[1.1rem] px-1 rounded-full bg-primary-500 text-white text-[0.65rem] font-semibold leading-none">
          {{ group.actions.length }}
        </span>
      </span>
    </Tag>
  </div>
  <span v-else class="text-surface-400 text-sm">—</span>

  <Popover ref="op">
    <div v-if="active" class="flex flex-col gap-2 min-w-[9rem]">
      <span class="text-xs font-semibold uppercase tracking-wide text-surface-500">
        {{ resourceLabel(active.resource) }}
      </span>
      <div class="flex flex-wrap gap-1">
        <Tag v-for="action in active.actions" :key="action" :value="actionLabel(action)" severity="info"/>
      </div>
    </div>
  </Popover>
</template>
