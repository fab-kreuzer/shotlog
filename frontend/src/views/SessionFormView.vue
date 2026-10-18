<template>
  <div>
    <div v-if="loading" class="flex flex-col gap-4">
      <Skeleton height="4rem" width="40%"/>
      <Skeleton height="20rem"/>
    </div>

    <template v-else>
      <PageHeader :color="isCompetition ? 'red' : 'green'" :icon="isCompetition ? 'pi pi-trophy' : 'pi pi-bullseye'"
                  :subtitle="session?.title"
                  :title="title"/>

      <form class="card-surface flex flex-col gap-6 p-5" @submit.prevent="handleSubmit">
        <!-- Session data -->
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
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

          <div v-if="isCompetition" class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-surface-700">{{ $t('session.opponent') }}</label>
            <Select v-model="form.enemyId" :options="locations" fluid optionLabel="club" optionValue="id"/>
          </div>

          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-surface-700">{{ $t('session.season') }}</label>
            <span class="py-2 text-sm font-medium text-surface-800">{{ seasonName || '—' }}</span>
          </div>

          <div v-if="isCompetition" class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-surface-700">{{ $t('session.team') }}</label>
            <Select v-model="form.teamId" :options="visibleTeams" fluid optionLabel="name" optionValue="id"/>
          </div>

          <div class="flex items-end gap-5 sm:col-span-2 lg:col-span-3">
            <label class="flex cursor-pointer items-center gap-2">
              <Checkbox v-model="form.decimalScoring" binary/>
              <span class="text-sm text-surface-700">{{ $t('session.decimalScoring') }}</span>
            </label>
            <label v-if="isCompetition" class="flex cursor-pointer items-center gap-2">
              <Checkbox v-model="form.home" binary/>
              <span class="text-sm text-surface-700">{{ $t('session.homeMatch') }}</span>
            </label>
          </div>
        </div>

        <hr class="border-surface-200">

        <SessionSeriesEditor :series="form.series" @add="addSeries" @remove="removeSeries"/>

        <div class="flex justify-end gap-2">
          <Button :label="$t('common.cancel')" severity="secondary" text type="button" @click="goBack"/>
          <Button :label="$t('common.save')" type="submit"/>
        </div>
      </form>
    </template>
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import DatePicker from 'primevue/datepicker'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Skeleton from 'primevue/skeleton'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'
import {useBreadcrumbStore} from '@/stores/breadcrumb'
import PageHeader from '@/components/PageHeader.vue'
import SessionSeriesEditor from '@/components/session/SessionSeriesEditor.vue'
import {useSessionForm} from '@/composables/useSessionForm'

const props = defineProps({
  id: {type: String, default: undefined}, // set when editing, absent when creating
  type: {type: String, required: true} // 'TRAINING' | 'COMPETITION' from the route
})

const {t} = useI18n()
const router = useRouter()
const notify = useNotificationStore()
const breadcrumb = useBreadcrumbStore()

const isCompetition = computed(() => props.type === 'COMPETITION')
const listPath = computed(() => (isCompetition.value ? '/competition' : '/training'))
const isEdit = computed(() => props.id != null)
const title = computed(() => {
  if (isEdit.value) return isCompetition.value ? t('session.editCompetitionTitle') : t('session.editTrainingTitle')
  return isCompetition.value ? t('session.createCompetitionTitle') : t('session.createTrainingTitle')
})

const session = ref(null)
const loading = ref(true)

const {
  ready,
  locations,
  visibleTeams,
  seasonName,
  form,
  sessionDateModel,
  sessionTimeModel,
  addSeries,
  removeSeries,
  startCreate,
  startEdit,
  handleSubmit
} = useSessionForm(props.type, {
  // Back to the saved session's page
  onSaved: (saved) => router.push(`${listPath.value}/${saved?.id ?? props.id}`)
})

function goBack() {
  router.push(isEdit.value ? `${listPath.value}/${props.id}` : listPath.value)
}

async function load() {
  loading.value = true
  session.value = null
  try {
    if (isEdit.value) {
      const loaded = await api.getSession(props.id)
      if (loaded.sessionType !== props.type) {
        return router.replace(`${loaded.sessionType === 'COMPETITION' ? '/competition' : '/training'}/${loaded.id}/edit`)
      }
      session.value = loaded
      await whenReady()
      startEdit(loaded)
      breadcrumb.setLabel(loaded.title)
      document.title = `ShotLog - ${loaded.title}`
    } else {
      await whenReady()
      startCreate()
    }
  } catch (err) {
    if (!err?._notified) notify.error(t('session.page.notFound'))
    router.replace(listPath.value)
  } finally {
    loading.value = false
  }
}

// The option lists (places, seasons, teams) load in the composable; the form needs them to pick defaults.
function whenReady() {
  return new Promise(resolve => {
    if (ready.value) return resolve()
    const stop = watch(ready, (v) => {
      if (v) {
        stop()
        resolve()
      }
    })
  })
}

watch(() => props.id, load, {immediate: true})
onBeforeUnmount(() => breadcrumb.setLabel(''))
</script>
