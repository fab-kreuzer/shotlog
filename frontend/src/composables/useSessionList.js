import {onMounted, ref} from 'vue'
import {api} from '@/api/http'
import {useNotificationStore} from '@/stores/notifications'

/**
 * Shared data + modal logic for a session overview of a given type
 * ('TRAINING' | 'COMPETITION'). The page owns its own header/layout and
 * the SessionGrid renders the cards; this just wires up loading and the
 * create/edit/delete flow.
 */
export function useSessionList(type) {
    const notify = useNotificationStore()

    const sessions = ref([])
    const sessionModal = ref(null)
    const showDeleteConfirm = ref(false)
    const sessionToDelete = ref(null)

    async function loadSessions() {
        try {
            sessions.value = await api.getSessionsByType(type.toLowerCase())
        } catch (err) {
            console.error('Error loading sessions:', err)
            if (!err._notified) notify.error('Fehler beim Laden der Sessions!')
        }
    }

    async function editSession(id) {
        try {
            const session = await api.getSession(id)
            sessionModal.value?.openEdit(session)
        } catch (err) {
            console.error('Error loading session:', err)
            if (!err._notified) notify.error('Fehler beim Laden der Session!')
        }
    }

    function openCreate() {
        sessionModal.value?.openCreate(type)
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
            if (!err._notified) notify.error('Fehler beim Löschen der Session!')
        }
    }

    onMounted(loadSessions)

    return {
        sessions,
        sessionModal,
        showDeleteConfirm,
        loadSessions,
        editSession,
        openCreate,
        handleDelete,
        confirmDeleteSession
    }
}
