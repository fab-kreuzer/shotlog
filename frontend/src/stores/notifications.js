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

    // Display every message from an ApiResponse envelope
    // ({errors, warnings, successes, infos}) with its matching type.
    const API_LIST_TYPES = {errors: 'error', warnings: 'warn', successes: 'success', infos: 'info'}

    function fromApiResponse(data) {
        for (const [key, type] of Object.entries(API_LIST_TYPES)) {
            const list = data?.[key]
            if (Array.isArray(list)) list.forEach(message => show(message, type))
        }
    }

    return {notifications, show, remove, success, error, warn, fromApi, fromApiResponse}
})