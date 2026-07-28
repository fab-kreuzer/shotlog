<template>
  <Dialog
      :draggable="false"
      :style="{ width: '48rem' }"
      :visible="visible"
      class="max-w-[95vw]"
      modal
      @update:visible="onVisibleChange"
  >
    <template #header>
      <div class="flex items-center gap-3">
        <div
            class="flex items-center justify-center w-9 h-9 rounded-full bg-green-50 text-green-600 dark:bg-green-400/10 dark:text-green-400">
          <i class="pi pi-bullseye"/>
        </div>
        <span class="font-semibold text-surface-800">
          {{ isEditing ? $t('session.editTrainingTitle') : $t('session.createTrainingTitle') }}
        </span>
      </div>
    </template>

    <form id="training-session-form" class="flex flex-col gap-6" @submit.prevent="handleSubmit">
      <!-- Session data -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('common.description') }}</label>
          <InputText v-model="form.title" fluid required/>
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('common.date') }}</label>
          <DatePicker v-model="sessionDateModel" dateFormat="dd.mm.yy" fluid showIcon/>
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('session.startTime') }}</label>
          <DatePicker v-model="sessionTimeModel" fluid showIcon timeOnly/>
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('session.season') }}</label>
          <span class="py-2 text-sm font-medium text-surface-800">{{ seasonName || '—' }}</span>
        </div>

        <div class="flex items-end gap-5 sm:col-span-2">
          <label class="flex items-center gap-2 cursor-pointer">
            <Checkbox v-model="form.decimalScoring" binary/>
            <span class="text-sm text-surface-700">{{ $t('session.decimalScoring') }}</span>
          </label>
        </div>
      </div>

      <hr class="border-surface-200">

      <SessionSeriesEditor :series="form.series" @add="addSeries" @remove="removeSeries"/>
    </form>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text type="button" @click="close"/>
      <Button :label="$t('common.save')" form="training-session-form" type="submit"/>
    </template>
  </Dialog>
</template>

<script setup>
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import SessionSeriesEditor from '@/components/session/SessionSeriesEditor.vue'
import {useSessionForm} from '@/composables/useSessionForm'

const emit = defineEmits(['saved'])

const {
  visible,
  seasonName,
  isEditing,
  form,
  sessionDateModel,
  sessionTimeModel,
  addSeries,
  removeSeries,
  openCreate,
  openEdit,
  close,
  onVisibleChange,
  handleSubmit
} = useSessionForm('TRAINING', {onSaved: () => emit('saved')})

defineExpose({openCreate, openEdit})
</script>
