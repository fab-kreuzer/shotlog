import {onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'

/**
 * Shared data + modal logic for a session overview of a given type
 * ('TRAINING' | 'COMPETITION'). The page owns its own header/layout and
 * the SessionGrid renders the cards; this just wires up loading and the
 * create/edit/delete flow.
 */
export function useSessionList(type) {
    const {t} = useI18n()
    const notify = useNotificationStore()
    const router = useRouter()

    const sessions = ref([])
    const loading = ref(true)
    const showDeleteConfirm = ref(false)
    const sessionToDelete = ref(null)

    async function loadSessions() {
        loading.value = true
        try {
            sessions.value = await api.getSessionsByType(type.toLowerCase())
        } catch (err) {
            console.error('Error loading sessions:', err)
            if (!err._notified) notify.error(t('session.loadError'))
        } finally {
            loading.value = false
        }
    }

    function openCreate() {
        router.push(`/${type.toLowerCase()}/new`)
    }

    function handleDelete(id) {
        sessionToDelete.value = id
        showDeleteConfirm.value = true
    }

    async function confirmDeleteSession() {
        try {
            await api.deleteSession(sessionToDelete.value)
            await loadSessions()
        } catch (err) {
            console.error('Error deleting session:', err)
            if (!err._notified) notify.error(t('session.deleteError'))
        }
    }

    onMounted(loadSessions)

    return {
        sessions,
        loading,
        showDeleteConfirm,
        loadSessions,
        openCreate,
        handleDelete,
        confirmDeleteSession
    }
}
