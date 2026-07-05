<template>
  <Dialog
      :draggable="false"
      :style="{ width: '32rem' }"
      :visible="modelValue"
      :header="$t('user.createTitle')"
      modal
      @update:visible="$emit('update:modelValue', $event)"
  >
    <form id="create-user-form" class="flex flex-col gap-4" @submit.prevent="submit">
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createUsername">{{ $t('user.username') }}</label>
        <InputText id="createUsername" v-model="user.username" :placeholder="$t('user.username')" fluid/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createDisplayName">{{ $t('user.displayName') }}</label>
        <InputText id="createDisplayName" v-model="user.displayName" :placeholder="$t('user.displayName')" fluid/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createPassword">{{ $t('user.password') }}</label>
        <Password v-model="user.password" :feedback="false" :placeholder="$t('user.password')" fluid
                  inputId="createPassword"
                  toggleMask/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700">{{ $t('user.roles') }}</label>
        <Multiselect
            v-model="user.roleIds"
            :options="roles"
            optionLabel="name"
            optionValue="id"
            :placeholder="$t('user.selectRoles')"
        />
      </div>
    </form>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text type="button"
              @click="$emit('update:modelValue', false)"/>
      <Button :label="$t('common.create')" form="create-user-form" type="submit"/>
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
import {useI18n} from 'vue-i18n'

const {t} = useI18n()
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
    errors.push(t('user.usernameRequired'))
  } else if (username.length < 3) {
    errors.push(t('user.usernameMinLength'))
  }

  if (!displayName) {
    errors.push(t('user.displayNameRequired'))
  }

  if (!password) {
    errors.push(t('user.passwordRequired'))
  } else if (password.length < 6) {
    errors.push(t('user.passwordMinLength'))
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
