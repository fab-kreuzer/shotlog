<template>
  <Dialog
      :draggable="false"
      :style="{ width: '28rem' }"
      :visible="modelValue"
      :header="$t('club.createTitle')"
      modal
      @update:visible="$emit('update:modelValue', $event)"
  >
    <form id="create-role-form" class="flex flex-col gap-4" @submit.prevent="submit">
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createClubName">{{ $t('club.clubName') }}</label>
        <InputText id="createClubName" v-model="clubName" :placeholder="$t('club.clubName')" fluid/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createClubLocation">{{ $t('common.location') }}</label>
        <InputText id="createClubLocation" v-model="location" :placeholder="$t('common.location')" fluid/>
      </div>
    </form>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text type="button"
              @click="$emit('update:modelValue', false)"/>
      <Button :label="$t('common.create')" form="create-role-form" type="submit"/>
    </template>
  </Dialog>
</template>

<script setup>
import {ref, watch} from 'vue'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import {useNotificationStore} from "@/stores/notifications.js";
import {useI18n} from 'vue-i18n'

const {t} = useI18n()
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
    notify.warn(t('club.nameRequired'))
    return
  }

  if (!location.value?.trim()) {
    notify.warn(t('club.locationRequired'))
    return
  }

  emit('create', {club: clubName.value, location: location.value})
  emit('update:modelValue', false)
}
</script>
