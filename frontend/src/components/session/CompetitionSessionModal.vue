<template>
  <Dialog
      :draggable="false"
      :header="isEditing ? $t('session.editCompetitionTitle') : $t('session.createCompetitionTitle')"
      :style="{ width: '48rem' }"
      :visible="visible"
      class="max-w-[95vw]"
      modal
      @update:visible="onVisibleChange"
  >
    <form id="competition-session-form" class="flex flex-col gap-6" @submit.prevent="handleSubmit">
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
          <label class="text-sm font-medium text-surface-700">{{ $t('session.opponent') }}</label>
          <Select
              v-model="form.enemyId"
              :options="locations"
              fluid
              optionLabel="club"
              optionValue="id"
          />
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('session.season') }}</label>
          <Select
              v-model="form.seasonId"
              :options="seasons"
              fluid
              optionLabel="description"
              optionValue="id"
          />
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium text-surface-700">{{ $t('session.team') }}</label>
          <Select
              v-model="form.teamId"
              :options="assignedTeams"
              fluid
              optionLabel="name"
              optionValue="id"
          />
        </div>

        <div class="flex items-end gap-5 sm:col-span-2 lg:col-span-3">
          <label class="flex items-center gap-2 cursor-pointer">
            <Checkbox v-model="form.decimalScoring" binary/>
            <span class="text-sm text-surface-700">{{ $t('session.decimalScoring') }}</span>
          </label>
          <label class="flex items-center gap-2 cursor-pointer">
            <Checkbox v-model="form.home" binary/>
            <span class="text-sm text-surface-700">{{ $t('session.homeMatch') }}</span>
          </label>
        </div>
      </div>

      <hr class="border-surface-200">

      <SessionSeriesEditor :series="form.series" @add="addSeries" @remove="removeSeries"/>
    </form>

    <template #footer>
      <Button :label="$t('common.cancel')" severity="secondary" text type="button" @click="close"/>
      <Button :label="$t('common.save')" form="competition-session-form" type="submit"/>
    </template>
  </Dialog>
</template>

<script setup>
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
import Select from 'primevue/select'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import SessionSeriesEditor from '@/components/session/SessionSeriesEditor.vue'
import {useSessionForm} from '@/composables/useSessionForm'

const emit = defineEmits(['saved'])

const {
  visible,
  locations,
  seasons,
  assignedTeams,
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
} = useSessionForm('COMPETITION', {onSaved: () => emit('saved')})

defineExpose({openCreate, openEdit})
</script>