<template>
  <DataView :value="sessions" layout="grid">
    <template #grid="{ items }">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
        <div
            v-for="session in items"
            :key="session.id"
            class="group relative bg-card rounded-xl border border-surface-200 shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 overflow-hidden"
        >
          <!-- Actions overlay -->
          <div
              class="absolute top-2 right-2 flex items-center rounded-lg bg-card shadow-md gap-1 opacity-0 group-hover:opacity-100 transition-opacity max-sm:opacity-100">
            <Button
                icon="pi pi-pencil"
                rounded
                severity="warn"
                size="small"
                text
                :title="$t('common.edit')"
                @click="emit('edit', session.id)"
            />
            <Button
                icon="pi pi-trash"
                rounded
                severity="danger"
                size="small"
                text
                :title="$t('common.delete')"
                @click="emit('delete', session.id)"
            />
          </div>

          <div class="p-2">
            <!-- Card header -->
            <div class="flex items-center gap-2 mb-4">
              <i :class="session.home ? 'pi pi-home' : 'pi pi-map-marker'" class="text-surface-400 text-sm"/>
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
                <i class="pi pi-circle text-surface-400"/>
                <span class="font-semibold text-surface-800">{{ $t('session.total') }} {{ session.formattedShotSum }}
                  <span class="font-normal text-surface-500">({{ session.formattedShotSumOfTestShots }})</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
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

defineProps({
  sessions: {type: Array, required: true}
})

const emit = defineEmits(['edit', 'delete', 'create'])

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
