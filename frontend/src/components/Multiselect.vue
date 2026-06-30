<script setup>
import {ref, watch} from 'vue'
import MultiSelect from 'primevue/multiselect'

// Props
const props = defineProps({
  modelValue: {
    type: [Array, Object, String, Number, null],
    default: null
  },
  options: {
    type: Array,
    default: () => []
  },
  optionLabel: {
    type: String,
    default: 'label'
  },
  optionValue: {
    type: String,
    default: null // optional
  },
  placeholder: {
    type: String,
    default: 'Select...'
  },
  filter: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  fluid: {
    type: Boolean,
    default: true
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'change', 'filter'])

// Internal state
const internalValue = ref(props.modelValue)

// Sync v-model
watch(
    () => props.modelValue,
    (val) => {
      internalValue.value = val
    }
)

watch(internalValue, (val) => {
  emit('update:modelValue', val)
  emit('change', val)
})

// Filter event (for async search)
function onFilter(event) {
  emit('filter', event.value)
}
</script>

<template>
  <MultiSelect
      v-model="internalValue"
      :class="{ 'w-full': fluid }"
      :disabled="disabled"
      :filter="filter"
      :loading="loading"
      :optionLabel="optionLabel"
      :optionValue="optionValue"
      :options="options"
      :placeholder="placeholder"
      display="chip"
      @filter="onFilter"
  />
</template>