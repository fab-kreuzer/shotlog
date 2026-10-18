<template>
  <!-- eslint-disable-next-line vue/no-v-html -- SVG is generated from numbers by the bundled target library -->
  <div v-if="hasPositions" class="session-target" :style="{maxWidth: `${maxWidth}px`}" v-html="markup"/>
</template>

<script setup>
import {computed} from 'vue'
import {useSessionTarget} from '@/composables/useSessionTarget'
import {DISKS} from '@/lib/shotsapp-target'

const props = defineProps({
  session: {type: Object, required: true},
  size: {type: Number, default: 600},
  maxWidth: {type: Number, default: 600},
  showNumbers: {type: Boolean, default: true},
  showGroup: {type: Boolean, default: false},
  showMean: {type: Boolean, default: false},
  shotNumbers: {type: Boolean, default: false},
  // 'app' = DISAG ShotsApp colours: >= 10.0 red, >= 9.0 gold, otherwise green
  shotColor: {type: String, default: 'app'},
  highlight: {type: Number, default: undefined},
  // draw only this shot (index among the drawn shots)
  only: {type: Number, default: undefined},
  // zoom the view onto the drawn shots (or the selected shot)
  autoZoom: {type: Boolean, default: false}
})

const {hasPositions, positioned, svg} = useSessionTarget(() => props.session)

// Auto zoom: narrow the SVG viewBox (units 1/100 mm, origin top-left, y pointing down) to the drawn shots.
const PAD = 600 // margin around the shots, so the surrounding rings stay visible
const MIN_SPAN = 1000 // never zoom closer than 10 mm, so a single shot still shows its ring
function zoomed(markup) {
  if (!props.autoZoom) return markup
  const R = (DISKS[props.session.diskType] ?? DISKS.lg).R
  const shots = props.only != null && positioned.value[props.only] ? [positioned.value[props.only]] : positioned.value
  if (!shots.length) return markup
  const xs = shots.map(sh => R + sh.x)
  const ys = shots.map(sh => R - sh.y)
  const minX = Math.min(...xs), maxX = Math.max(...xs), minY = Math.min(...ys), maxY = Math.max(...ys)
  const span = Math.min(2 * R, Math.max(MIN_SPAN, maxX - minX + 2 * PAD, maxY - minY + 2 * PAD))
  const x0 = (minX + maxX) / 2 - span / 2
  const y0 = (minY + maxY) / 2 - span / 2
  return markup.replace(/viewBox="[^"]*"/, `viewBox="${x0.toFixed(1)} ${y0.toFixed(1)} ${span.toFixed(1)} ${span.toFixed(1)}"`)
}

const markup = computed(() => zoomed(svg({
  size: props.size,
  showNumbers: props.showNumbers,
  showGroup: props.showGroup,
  showMean: props.showMean,
  shotNumbers: props.shotNumbers,
  shotColor: props.shotColor,
  highlight: props.highlight,
  only: props.only
})))
</script>

<style scoped>
.session-target {
  width: 100%;
}

.session-target :deep(svg) {
  width: 100%;
  height: auto;
  display: block;
}
</style>
