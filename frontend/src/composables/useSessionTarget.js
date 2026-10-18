import {computed, toValue} from 'vue'
import {useI18n} from 'vue-i18n'
import {render, summarize} from '@/lib/shotsapp-target'

/**
 * Adapts a shotlog session to the webhook-shaped input of the ShotsApp target library.
 * Shot values are stored as rings (decimal), the library expects ring x 10.
 * Test-shot series are left out of the totals, like Session.getShotSum on the backend.
 */
export function useSessionTarget(sessionRef) {
    const {locale} = useI18n()

    // Without decimal scoring (Zehntelwertung) a shot counts as its whole ring: 9.6 -> 9.
    const ringValue = (v) => (toValue(sessionRef)?.decimalScoring ? Number(v) : Math.floor(Number(v)))

    const countedSeries = computed(() => (toValue(sessionRef)?.series ?? []).filter(s => !s.testShot))
    const shots = computed(() => countedSeries.value.flatMap(s => s.shots ?? []).filter(sh => sh.value != null))

    function toResult(list) {
        return {
            data: {
                meta: {diskType: toValue(sessionRef)?.diskType || 'lg', seriesShots: 10},
                results: list.map(sh => ({valid: 2, flag: 2, x: sh.x ?? 0, y: sh.y ?? 0, value: Math.round(ringValue(sh.value) * 10)}))
            }
        }
    }

    const positioned = computed(() => shots.value.filter(sh => sh.x != null && sh.y != null))
    const hasPositions = computed(() => positioned.value.length > 0)

    // Per-series sums are computed from the stored series (not re-sliced) so they match the session as entered.
    const series = computed(() => (toValue(sessionRef)?.series ?? []).map(s => {
        const values = (s.shots ?? []).map(sh => ringValue(sh.value ?? 0))
        return {
            number: s.seriesNumber,
            testShot: s.testShot,
            shots: values,
            decimal: values.reduce((a, b) => a + b, 0),
            whole: values.reduce((a, b) => a + Math.floor(b), 0)
        }
    }))

    // Statistics (best/worst/mean point) are only meaningful with positions; best/worst work without.
    const summary = computed(() => summarize(toResult(shots.value.map(sh => ({...sh, x: sh.x ?? 0, y: sh.y ?? 0})))))
    const positionSummary = computed(() => (hasPositions.value ? summarize(toResult(positioned.value)) : null))

    // `only` = index (into the positioned shots) of the single shot to draw; undefined draws all of them.
    function svg({only, ...opts} = {}) {
        if (!hasPositions.value) return ''
        const list = only != null && positioned.value[only] ? [positioned.value[only]] : positioned.value
        return render(toResult(list), only != null ? {...opts, highlight: undefined} : opts)
    }

    function fmt(n, digits = 1) {
        return new Intl.NumberFormat(locale.value, {minimumFractionDigits: digits, maximumFractionDigits: digits}).format(n)
    }

    return {hasPositions, positioned, series, summary, positionSummary, svg, fmt}
}
