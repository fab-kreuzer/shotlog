import {computed, ref} from 'vue'
import {api} from '@/api/http'

/**
 * Season filter for a session overview. Loads the available seasons, defaults
 * the selection to the active season, and exposes the sessions filtered by it.
 *
 * @param sessions a ref to the full session list to filter.
 */
export function useSeasonFilter(sessions) {
    const selected = ref([])
    const options = ref([])

    // No season selected -> show all; otherwise only sessions in a selected season.
    const filteredSessions = computed(() => {
        if (!selected.value || selected.value.length === 0) return sessions.value
        return sessions.value.filter(s => selected.value.includes(s.season?.id))
    })

    async function loadSeasons() {
        options.value = await api.getSeasons()
        // Default the filter to the active season.
        const active = options.value.find(s => s.active)
        if (active) selected.value = [active.id]
    }

    return {selected, options, filteredSessions, loadSeasons}
}
