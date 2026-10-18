<template>
  <!-- eslint-disable-next-line vue/no-v-html -- SVG is generated from numbers by the bundled target library -->
  <div v-if="hasPositions" class="session-target" :style="{maxWidth: `${maxWidth}px`}" v-html="markup"/>
</template>

<script setup>
import {computed} from 'vue'
import {useSessionTarget} from '@/composables/useSessionTarget'

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
  only: {type: Number, default: undefined}
})

const {hasPositions, svg} = useSessionTarget(() => props.session)

const markup = computed(() => svg({
  size: props.size,
  showNumbers: props.showNumbers,
  showGroup: props.showGroup,
  showMean: props.showMean,
  shotNumbers: props.shotNumbers,
  shotColor: props.shotColor,
  highlight: props.highlight,
  only: props.only
}))
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
