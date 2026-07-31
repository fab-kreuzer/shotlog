<template>
  <div class="flex flex-col gap-6">
    <TransitionGroup ref="seriesContainerRef" appear class="flex flex-col gap-4" name="list-item" tag="div">
      <div v-for="(series, sIndex) in series" :key="sIndex"
           :style="{'--stagger-index': sIndex}"
           class="bg-surface-50 rounded-xl border border-surface-200 overflow-hidden">
        <!-- Series header -->
        <div class="flex items-center justify-between px-4 py-3 bg-surface-100 border-b border-surface-200">
          <span class="text-sm font-semibold text-surface-700">{{
              $t('session.seriesLabel', {number: sIndex + 1})
            }}</span>
          <div class="flex items-center gap-3">
            <label class="flex items-center gap-2 cursor-pointer">
              <Checkbox v-model="series.testShot" binary/>
              <span class="text-xs text-surface-600">{{ $t('session.testShot') }}</span>
            </label>
            <Button
                icon="pi pi-trash"
                :label="$t('session.remove')"
                severity="danger"
                size="small"
                text
                type="button"
                @click="$emit('remove', sIndex)"
            />
          </div>
        </div>

        <!-- Shots grid -->
        <div class="p-4 grid grid-cols-2 sm:grid-cols-5 gap-3">
          <div v-for="(shot, shotIndex) in series.shots" :key="shotIndex" class="flex flex-col gap-1">
            <label class="text-xs font-medium text-surface-500">{{
                $t('session.shotLabel', {number: shotIndex + 1})
              }}</label>
            <InputNumber
                v-model="shot.value"
                :min="0.0"
                :minFractionDigits="1"
                :step="0.1"
                inputId="minmax-buttons"
                mode="decimal"
                :max="10.9"
                showButtons
                :maxFractionDigits="1"
                fluid
            />
          </div>
        </div>
      </div>
    </TransitionGroup>

    <!-- Add series button -->
    <Button
        class="w-full"
        icon="pi pi-plus"
        :label="$t('session.addSeries')"
        outlined
        severity="secondary"
        type="button"
        @click="$emit('add')"
    />
  </div>
</template>

<script setup>
import {nextTick, ref, watch} from 'vue'
import InputNumber from 'primevue/inputnumber'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'

const props = defineProps({
  series: {type: Array, required: true}
})

defineEmits(['add', 'remove'])

const seriesContainerRef = ref(null)

// Scroll the newly added series into view.
watch(() => props.series.length, (next, prev) => {
  if (next > prev) {
    nextTick(() => {
      seriesContainerRef.value?.$el?.lastElementChild?.scrollIntoView({behavior: 'smooth', block: 'end'})
    })
  }
})
</script>
