import {computed, onBeforeUnmount, ref, toValue} from 'vue'

export const PLAYBACK_SPEEDS = [0.5, 1, 2, 3]
const BASE_INTERVAL_MS = 1500 // one shot per step, like the DISAG ShotsApp

/**
 * Shot-by-shot playback. `index` is the last shown shot (-1 = empty target), so the number of shots to
 * draw is `index + 1`. Mirrors the app: fixed pace of 1500 ms / speed, stops at the end, play at the end restarts.
 */
export function usePlayback(countRef) {
    const index = ref(-1)
    const playing = ref(false)
    const speed = ref(1)
    let timer = null

    const count = computed(() => toValue(countRef))
    const shown = computed(() => index.value + 1)

    function clear() {
        if (timer) clearInterval(timer)
        timer = null
    }

    function pause() {
        clear()
        playing.value = false
    }

    function step() {
        if (index.value + 1 >= count.value) return pause()
        index.value++
        if (index.value >= count.value - 1) pause()
    }

    function play() {
        clear()
        if (index.value >= count.value - 1) index.value = -1
        playing.value = true
        timer = setInterval(step, BASE_INTERVAL_MS / speed.value)
    }

    function toggle() {
        playing.value ? pause() : play()
    }

    function setSpeed(value) {
        speed.value = value
        if (playing.value) play()
    }

    /** Slider value 0..count: pauses and shows that many shots. */
    function seek(value) {
        pause()
        index.value = Math.max(-1, Math.min(count.value - 1, Math.round(value) - 1))
    }

    function reset() {
        pause()
        index.value = -1
    }

    onBeforeUnmount(clear)

    return {index, playing, speed, shown, count, toggle, setSpeed, seek, reset, pause}
}
