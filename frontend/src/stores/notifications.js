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

    return {notifications, show, remove, success, error}
})
