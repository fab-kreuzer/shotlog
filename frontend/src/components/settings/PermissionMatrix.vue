<script setup>
import {computed, ref, watch} from 'vue'
import Checkbox from 'primevue/checkbox'
import {useI18n} from 'vue-i18n'

const {t, te} = useI18n()

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  permissions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const internalValue = ref([...props.modelValue])

watch(() => props.modelValue, (val) => {
  internalValue.value = [...(val || [])]
})

watch(internalValue, (val) => {
  emit('update:modelValue', val)
})

const ACTION_ORDER = ['view', 'create', 'edit', 'delete']

const resources = computed(() => {
  const seen = []
  for (const p of props.permissions) {
    if (!seen.includes(p.resource)) seen.push(p.resource)
  }
  return seen
})

const actions = computed(() => {
  const seen = new Set(props.permissions.map(p => p.action))
  const known = ACTION_ORDER.filter(a => seen.has(a))
  const rest = [...seen].filter(a => !ACTION_ORDER.includes(a)).sort()
  return [...known, ...rest]
})

function permissionFor(resource, action) {
  return props.permissions.find(p => p.resource === resource && p.action === action)
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
  <div class="border border-surface-200 rounded-lg overflow-hidden">
    <table class="w-full text-sm">
      <thead>
      <tr class="bg-surface-50">
        <th class="text-left font-medium text-surface-600 px-3 py-2"/>
        <th v-for="action in actions" :key="action" class="text-center font-medium text-surface-600 px-3 py-2">
          {{ actionLabel(action) }}
        </th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="resource in resources" :key="resource" class="border-t border-surface-200">
        <td class="px-3 py-2 font-medium text-surface-700">{{ resourceLabel(resource) }}</td>
        <td v-for="action in actions" :key="action" class="text-center px-3 py-2">
          <Checkbox
              v-if="permissionFor(resource, action)"
              v-model="internalValue"
              :value="permissionFor(resource, action).id"
          />
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>
