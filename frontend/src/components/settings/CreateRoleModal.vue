<template>
  <Dialog
      :draggable="false"
      :style="{ width: '28rem' }"
      :visible="modelValue"
      :header="$t('role.createTitle')"
      modal
      @update:visible="$emit('update:modelValue', $event)"
  >
    <form id="create-role-form" class="flex flex-col gap-4" @submit.prevent="submit">
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700" for="createRoleName">{{ $t('role.roleName') }}</label>
        <InputText id="createRoleName" v-model="name" :placeholder="$t('role.roleName')" fluid/>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium text-surface-700">{{ $t('role.permissions') }}</label>
        <Multiselect
            v-model="permissionIds"
            :options="permissions"
            :placeholder="$t('role.selectPermissions')"
            optionLabel="permissionName"
            optionValue="id"
        />
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
import Multiselect from '@/components/Multiselect.vue'
import {useNotificationStore} from "@/stores/notifications.js";
import {useI18n} from 'vue-i18n'

const {t} = useI18n()
const notify = useNotificationStore()
const props = defineProps({
  modelValue: Boolean,
  permissions: Array
})

const emit = defineEmits(['update:modelValue', 'create'])

const name = ref('')
const permissionIds = ref([])

watch(() => props.modelValue, (open) => {
  if (open) {
    name.value = ''
    permissionIds.value = []
  }
})

function submit() {
  if (!name.value?.trim()) {
    notify.warn(t('role.nameRequired'))
    return
  }

  emit('create', {name: name.value, permissionIds: permissionIds.value})
  emit('update:modelValue', false)
}
</script>
