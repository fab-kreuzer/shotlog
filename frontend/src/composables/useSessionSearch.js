import {computed, ref} from 'vue'

// Mirrors the formatting shown on the session cards (SessionGrid.vue) so a
// search term matching what's displayed (e.g. "30.07.2026") also matches.
function formatDate(dateStr) {
    if (!dateStr) return ''
    const [y, m, d] = dateStr.split('-')
    return `${d}.${m}.${y}`
}

function formatTime(timeStr) {
    if (!timeStr) return ''
    return timeStr.substring(0, 5)
}

/**
 * Client-side search for a session overview. Filters the given sessions ref
 * by a case-insensitive substring match on the title, date, or time.
 *
 * @param sessions a ref (or computed) to the session list to filter.
 */
export function useSessionSearch(sessions) {
    const searchTerm = ref('')

    const filteredSessions = computed(() => {
        const term = searchTerm.value.trim().toLowerCase()
        if (!term) return sessions.value
        return sessions.value.filter(s => {
            const haystack = [s.title, s.sessionDate, formatDate(s.sessionDate), s.sessionTime, formatTime(s.sessionTime)]
                .filter(Boolean)
                .join(' ')
                .toLowerCase()
            return haystack.includes(term)
        })
    })

    return {searchTerm, filteredSessions}
}
