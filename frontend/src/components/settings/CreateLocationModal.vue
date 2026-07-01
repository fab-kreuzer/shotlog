<template>
  <Dialog
      :draggable="false"
      :style="{ width: '28rem' }"
      :visible="modelValue"
      header="Neue Rolle erstellen"
      modal
      @update:visible="$emit('update:modelValue', $event)"
  >
    <form id="create-role-form" class="flex flex-col gap-4" @submit.prevent="submit">
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createClubName">Clubname</label>
        <InputText id="createClubName" v-model="clubName" fluid placeholder="Clubname"/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createClubLocation">Ort</label>
        <InputText id="createClubLocation" v-model="location" fluid placeholder="Ort"/>
      </div>
    </form>

    <template #footer>
      <Button label="Abbrechen" severity="secondary" text type="button" @click="$emit('update:modelValue', false)"/>
      <Button form="create-role-form" label="Erstellen" type="submit"/>
    </template>
  </Dialog>
</template>

<script setup>
import {ref, watch} from 'vue'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import {useNotificationStore} from "@/stores/notifications.js";

const notify = useNotificationStore()
const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'create'])

const clubName = ref('')
const location = ref('')

watch(() => props.modelValue, (open) => {
  if (open) {
    clubName.value = ''
    location.value = ''
  }
})

function submit() {
  if (!clubName.value?.trim()) {
    notify.warn('Bitte geben Sie einen Clubnamen ein.')
    return
  }

  if (!location.value?.trim()) {
    notify.warn('Bitte geben Sie einen Ort ein.')
    return
  }

  emit('create', {club: clubName.value, location: location.value})
  emit('update:modelValue', false)
}
</script>
