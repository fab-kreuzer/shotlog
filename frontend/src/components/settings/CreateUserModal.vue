<template>
  <Dialog
      :draggable="false"
      :style="{ width: '32rem' }"
      :visible="modelValue"
      header="Neuen Benutzer erstellen"
      modal
      @update:visible="$emit('update:modelValue', $event)"
  >
    <form id="create-user-form" class="flex flex-col gap-4" @submit.prevent="submit">
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createUsername">Benutzername</label>
        <InputText id="createUsername" v-model="user.username" fluid placeholder="Benutzername"/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createDisplayName">Anzeigename</label>
        <InputText id="createDisplayName" v-model="user.displayName" fluid placeholder="Anzeigename"/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createPassword">Passwort</label>
        <Password v-model="user.password" :feedback="false" fluid inputId="createPassword" placeholder="Passwort"
                  toggleMask/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700">Rollen</label>
        <Multiselect
            v-model="user.roleIds"
            :options="roles"
            optionLabel="name"
            optionValue="id"
            placeholder="Rollen auswählen"
        />
      </div>
    </form>

    <template #footer>
      <Button label="Abbrechen" severity="secondary" text type="button" @click="$emit('update:modelValue', false)"/>
      <Button form="create-user-form" label="Erstellen" type="submit"/>
    </template>
  </Dialog>
</template>

<script setup>
import {ref, watch} from 'vue'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Multiselect from '@/components/Multiselect.vue'
import {useNotificationStore} from "@/stores/notifications.js";

const notify = useNotificationStore()

const props = defineProps({
  modelValue: Boolean,
  roles: Array
})

const emit = defineEmits(['update:modelValue', 'create'])

const user = ref({
  username: '',
  displayName: '',
  password: '',
  roleIds: []
})

watch(() => props.modelValue, (open) => {
  if (open) {
    user.value = {username: '', displayName: '', password: '', roleIds: []}
  }
})

function submit() {
  const errors = []

  const username = user.value.username?.trim()
  const displayName = user.value.displayName?.trim()
  const password = user.value.password

  if (!username) {
    errors.push('Benutzername darf nicht leer sein')
  } else if (username.length < 3) {
    errors.push('Benutzername muss mindestens 3 Zeichen haben')
  }

  if (!displayName) {
    errors.push('Anzeigename darf nicht leer sein')
  }

  if (!password) {
    errors.push('Passwort darf nicht leer sein')
  } else if (password.length < 6) {
    errors.push('Passwort muss mindestens 6 Zeichen haben')
  }

  if (errors.length) {
    errors.forEach(msg => {
      notify.warn(msg);
    })
    return
  }

  emit('create', user.value)
  emit('update:modelValue', false)
}
</script>
