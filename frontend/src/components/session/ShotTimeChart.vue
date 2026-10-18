<template>
  <div class="overflow-x-auto rounded-xl border border-surface-200 bg-card">
    <svg :height="H" :viewBox="`0 0 ${W} ${H}`" :width="W" class="block text-surface-500" role="img"
         :aria-label="$t('session.detail.time.chart')">
      <!-- Y axis marks -->
      <g v-for="m in marks" :key="m.value">
        <line :x1="LEFT" :x2="W - 8" :y1="m.y" :y2="m.y" class="stroke-surface-200" stroke-width="1"/>
        <text :x="LEFT - 6" :y="m.y" dominant-baseline="central" fill="currentColor" font-size="11" text-anchor="end">
          {{ m.value }}s
        </text>
      </g>

      <!-- Median (dashed) and average (solid) -->
      <g v-if="median > 0">
        <line :x1="LEFT" :x2="W - 8" :y1="y(median)" :y2="y(median)" stroke="currentColor" stroke-dasharray="5 4"
              stroke-width="1.5"/>
      </g>
      <line :x1="LEFT" :x2="W - 8" :y1="y(average)" :y2="y(average)" stroke="var(--color-primary-500, #2a9d8f)"
            stroke-width="2"/>

      <!-- Line through the shots -->
      <polyline :points="polyline" fill="none" stroke="currentColor" opacity="0.45" stroke-width="1.5"/>

      <!-- Shots -->
      <g v-for="p in points" :key="p.index" class="cursor-pointer" @click="emit('select', p.index)"
         @mouseenter="emit('hover', p.index)" @mouseleave="emit('hover', undefined)">
        <circle :cx="x(p.index)" :cy="y(p.offset)" :fill="shotColor(p.value)" :r="selected === p.index ? 9 : 6.5"
                :stroke="selected === p.index || highlight === p.index ? 'var(--color-primary-800, #1b4f48)' : 'none'"
                stroke-width="3"/>
        <!-- larger invisible hit area for touch -->
        <circle :cx="x(p.index)" :cy="y(p.offset)" fill="transparent" r="14"/>
        <text :x="x(p.index)" :y="H - 10" fill="currentColor" font-size="11" text-anchor="middle">S{{ p.index + 1 }}</text>
      </g>
    </svg>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {shotColor} from '@/lib/shotColor'

const props = defineProps({
  points: {type: Array, required: true}, // [{index, offset, value}]
  average: {type: Number, default: 0},
  median: {type: Number, default: 0},
  selected: {type: Number, default: undefined},
  highlight: {type: Number, default: undefined}
})
const emit = defineEmits(['select', 'hover'])

const H = 260
const LEFT = 44
const TOP = 16
const BOTTOM = 32
const STEP = 50 // px per shot, like the app

const W = computed(() => LEFT + Math.max(props.points.length, 1) * STEP)
const max = computed(() => Math.max(1, ...props.points.map(p => p.offset)))

function x(i) {
  return LEFT + STEP / 2 + i * STEP
}

function y(seconds) {
  return TOP + (H - TOP - BOTTOM) * (1 - seconds / max.value)
}

const marks = computed(() => [0, 1, 2, 3, 4].map(i => {
  const value = Math.round((max.value * i) / 4)
  return {value, y: y(value)}
}))

const polyline = computed(() => props.points.map(p => `${x(p.index)},${y(p.offset)}`).join(' '))
</script>
