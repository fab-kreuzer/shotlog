import {defineStore} from 'pinia'
import {ref} from 'vue'

let nextId = 0

export const useNotificationStore = defineStore('notifications', () => {
    const notifications = ref([])

    function show(message, type = 'success', duration = 5000) {
        const id = nextId++
        notifications.value.push({id, message, type})

        if (duration > 0) {
            setTimeout(() => {
                remove(id)
            }, duration)
        }
    }

    function remove(id) {
        const index = notifications.value.findIndex(n => n.id === id)
        if (index !== -1) {
            notifications.value.splice(index, 1)
        }
    }

    function success(message) {
        show(message, 'success')
    }

    function error(message) {
        show(message, 'error')
    }

    function warn(message) {
        show(message, 'warn')
    }

    const TYPES = ['error', 'warn', 'success', 'info']

    function fromApi(err, fallback = 'Ein Fehler ist aufgetreten') {
        if (typeof err === 'string') {
            show(err, 'error')
            return
        }
        const type = TYPES.find(t => err?.[t]) || 'error'
        show(err?.[type] || err?.message || fallback, type)
    }

    return {notifications, show, remove, success, error, warn, fromApi}
})