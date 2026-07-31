<template>
  <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
    <div v-for="n in 4" :key="n" class="card-surface p-4 flex flex-col gap-3">
      <div class="flex items-center gap-2.5">
        <Skeleton borderRadius="9999px" height="2rem" width="2rem"/>
        <Skeleton height="1.25rem" width="60%"/>
      </div>
      <Skeleton height="0.9rem" width="45%"/>
      <Skeleton height="0.9rem" width="55%"/>
      <Skeleton height="0.9rem" width="40%"/>
    </div>
  </div>
  <DataView v-else v-model:first="first" v-model:rows="rows"
            :paginator="sessions.length > DEFAULT_PAGE_SIZE" :rowsPerPageOptions="[10, 20, 30]"
            :value="sessions" layout="grid">
    <template #grid="{ items }">
      <TransitionGroup appear class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5"
                       name="list-item" tag="div">
        <div
            v-for="(session, index) in items"
            :key="session.id"
            :style="{'--stagger-index': index}"
            class="card-surface-interactive group relative overflow-hidden"
            role="button"
            tabindex="0"
            @click="emit('edit', session.id)"
            @keydown.enter="emit('edit', session.id)"
        >
          <!-- Actions overlay -->
          <div
              class="absolute top-2 right-2 flex items-center rounded-lg bg-card shadow-md gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200 max-sm:opacity-100">
            <Button
                icon="pi pi-trash"
                severity="danger"
                size="small"
                text
                class="!rounded-lg"
                :title="$t('common.delete')"
                @click.stop="emit('delete', session.id)"
            />
          </div>

          <div class="p-2">
            <!-- Card header -->
            <div class="flex items-center gap-2.5 mb-4">
              <div class="flex items-center justify-center w-8 h-8 rounded-full bg-surface-100 shrink-0">
                <i :class="session.home ? 'pi pi-home' : 'pi pi-map-marker'" class="text-surface-500 text-sm"/>
              </div>
              <h3 class="text-base font-semibold text-surface-800 leading-tight">{{ session.title }}</h3>
            </div>

            <!-- Session details -->
            <div class="flex flex-col gap-2.5">
              <div class="flex items-center gap-2.5 text-sm text-surface-600">
                <i class="pi pi-calendar text-surface-400"/>
                <span>{{ formatDate(session.sessionDate) }}</span>
              </div>

              <div class="flex items-center gap-2.5 text-sm text-surface-600">
                <i class="pi pi-clock text-surface-400"/>
                <span>{{ $t('session.timeClock', {time: formatTime(session.sessionTime)}) }}</span>
              </div>

              <div class="flex items-center gap-2.5 text-sm">
                <i class="pi pi-chevron-right text-surface-400"/>
                <span class="font-semibold text-surface-800">{{ $t('session.total') }} {{ session.formattedShotSum }}
                  <span class="font-normal text-surface-500">({{ session.formattedShotSumOfTestShots }})</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </TransitionGroup>
    </template>

    <!-- Empty state -->
    <template #empty>
      <div class="text-center py-20">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-surface-100 mb-4">
          <i class="pi pi-inbox text-surface-400" style="font-size: 2rem"/>
        </div>
        <h3 class="text-lg font-medium text-surface-700 mb-1">{{ $t('session.empty') }}</h3>
        <p class="text-surface-500 mb-6">{{ $t('session.emptyHint') }}</p>
        <Button :label="$t('session.createFirst')" icon="pi pi-plus" @click="emit('create')"/>
      </div>
    </template>
  </DataView>
</template>

<script setup>
import DataView from 'primevue/dataview'
import Button from 'primevue/button'
import Skeleton from 'primevue/skeleton'
import {ref, watch} from 'vue'

const props = defineProps({
  sessions: {type: Array, required: true},
  loading: {type: Boolean, default: false}
})

const emit = defineEmits(['edit', 'delete', 'create'])

const DEFAULT_PAGE_SIZE = 20
const first = ref(0)
const rows = ref(DEFAULT_PAGE_SIZE)

// Jump back to page 1 whenever the (filtered) session list changes, so a
// search/season filter change can't leave the user stranded on an
// out-of-range page.
watch(() => props.sessions, () => {
  first.value = 0
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const [y, m, d] = dateStr.split('-')
  return `${d}.${m}.${y}`
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return timeStr.substring(0, 5)
}
</script>
