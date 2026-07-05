import {defineStore} from 'pinia'
import i18n from '@/i18n'

// Maps our internal message types to PrimeVue Toast severities.
const SEVERITY = {error: 'error', warn: 'warn', success: 'success', info: 'info'}

// Maps our internal message types to their translation keys.
const SUMMARY_KEYS = {
    error: 'notify.summary.error',
    warn: 'notify.summary.warn',
    success: 'notify.summary.success',
    info: 'notify.summary.info'
}

export const useNotificationStore = defineStore('notifications', () => {
    // A reference to PrimeVue's `toast.add`, registered once from App.vue.
    let add = null

    function register(toastAdd) {
        add = toastAdd
    }

    function show(message, type = 'success', duration = 5000) {
        if (!add) {
            // Toast service not registered yet — surface it rather than swallow.
            console.warn('[notifications] toast not registered:', type, message)
            return
        }
        const severity = SEVERITY[type] || 'info'
        add({
            severity,
            summary: i18n.global.t(SUMMARY_KEYS[type] || SUMMARY_KEYS.info),
            detail: message,
            life: duration > 0 ? duration : undefined
        })
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

    function fromApi(err, fallback = i18n.global.t('error.generic')) {
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

    return {register, show, success, error, warn, fromApi, fromApiResponse}
})
