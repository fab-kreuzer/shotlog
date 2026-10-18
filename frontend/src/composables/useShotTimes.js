import {computed, toValue} from 'vue'

/**
 * Time statistics of a session, following the DISAG ShotsApp: `secondsSincePrevious` is the gap to the
 * previous shot (0 for the first). Test-shot series are left out. The app's "average" is the sum divided by
 * ALL shots (including the first one with 0); the median is a real median over the shots with a predecessor.
 */
export function useShotTimes(sessionRef) {
    const ringValue = (v) => (toValue(sessionRef)?.decimalScoring ? Number(v) : Math.floor(Number(v)))

    const counted = computed(() => (toValue(sessionRef)?.series ?? []).filter(s => !s.testShot))
    const shots = computed(() => counted.value.flatMap(s => s.shots ?? []).filter(sh => sh.value != null))

    const points = computed(() => shots.value.map((sh, index) => ({
        index,
        offset: sh.secondsSincePrevious ?? 0,
        value: ringValue(sh.value)
    })))

    const hasTimes = computed(() => points.value.some(p => p.offset > 0))

    const stats = computed(() => {
        const offsets = points.value.map(p => p.offset)
        const n = offsets.length
        const total = offsets.reduce((a, b) => a + (b > 0 ? b : 0), 0)
        const rest = offsets.slice(1).sort((a, b) => a - b)
        const m = rest.length
        const median = m ? (m % 2 ? rest[(m - 1) / 2] : (rest[m / 2 - 1] + rest[m / 2]) / 2) : 0
        return {
            n,
            total,
            average: n ? total / n : 0,
            median,
            max: n ? Math.max(...offsets) : 0,
            min: m ? rest[0] : 0,
            perSeries: counted.value.map(s => ({
                number: s.seriesNumber,
                seconds: (s.shots ?? []).reduce((a, sh) => a + (sh.secondsSincePrevious ?? 0), 0)
            }))
        }
    })

    /** Seconds as m:ss (or h:mm:ss from an hour on). */
    function fmtDuration(seconds) {
        const s = Math.round(seconds)
        const h = Math.floor(s / 3600)
        const mm = Math.floor((s % 3600) / 60)
        const ss = String(s % 60).padStart(2, '0')
        return h ? `${h}:${String(mm).padStart(2, '0')}:${ss}` : `${mm}:${ss}`
    }

    return {points, hasTimes, stats, fmtDuration}
}
