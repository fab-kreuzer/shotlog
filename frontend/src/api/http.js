import {useNotificationStore} from '@/stores/notifications'
import i18n from '@/i18n'

const BASE = ''

async function request(method, url, body = null, {notify = true} = {}) {
    const options = {
        method,
        headers: {'Accept-Language': i18n.global.locale.value},
        credentials: 'same-origin'
    }

    if (body !== null) {
        if (body instanceof FormData) {
            // Let the browser set Content-Type with the multipart boundary
            options.body = body
        } else {
            options.headers['Content-Type'] = 'application/json'
            options.body = JSON.stringify(body)
        }
    }

    const response = await fetch(BASE + url, options)

    if (response.status === 401) {
        const notAuthenticated = i18n.global.t('error.notAuthenticated')
        const error = {status: 401, message: notAuthenticated, _notified: false}
        if (notify) {
            useNotificationStore().error(notAuthenticated)
            error._notified = true
        }
        throw error
    }

    if (!response.ok) {
        let errorData = null
        try {
            errorData = await response.json()
        } catch {
            // non-JSON error body — leave errorData null
        }
        const error = {
            status: response.status,
            ...(errorData && typeof errorData === 'object' ? errorData : {})
        }
        // _notified lets callers know the messages were already shown,
        // so their catch blocks can avoid showing a duplicate toast.
        error._notified = notify ? displayMessages(errorData) : false
        throw error
    }

    if (response.status === 204) return null

    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
        const data = await response.json()
        if (notify) displayMessages(data)
        return data
    }
    return null
}

const API_RESPONSE_KEYS = ['errors', 'warnings', 'successes', 'infos']

// An ApiResponse envelope carries one or more message lists.
function isApiResponse(data) {
    return data && typeof data === 'object' && !Array.isArray(data)
        && API_RESPONSE_KEYS.some(k => Array.isArray(data[k]))
}

// Auto-display all messages from an ApiResponse body. Returns true if it did.
function displayMessages(data) {
    if (!isApiResponse(data)) return false
    useNotificationStore().fromApiResponse(data)
    return true
}

export const api = {
    get: (url, opts) => request('GET', url, null, opts),
    post: (url, body, opts) => request('POST', url, body, opts),
    put: (url, body, opts) => request('PUT', url, body, opts),
    delete: (url, opts) => request('DELETE', url, null, opts),

    // Auth
    register: (data) => request('POST', '/api/auth/register', data),
    login: (username, password) => {
        const formData = new URLSearchParams()
        formData.append('username', username)
        formData.append('password', password)
        return fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept-Language': i18n.global.locale.value
            },
            body: formData,
            credentials: 'same-origin'
        })
    },
    logout: () => fetch('/api/auth/logout', {
        method: 'POST',
        headers: {'Accept-Language': i18n.global.locale.value},
        credentials: 'same-origin'
    }),
    me: () => request('GET', '/api/auth/me', null, {notify: false}),
    updateProfile: (data) => request('PUT', '/api/auth/me', data),

    // Sessions
    getSessions: () => request('GET', '/api/sessions'),
    getSessionsByType: (type) => request('GET', `/api/sessions/by-type?type=${type}`),
    getSession: (id) => request('GET', `/api/sessions/${id}`),
    createSession: (data) => request('POST', '/api/sessions', data),
    updateSession: (id, data) => request('PUT', `/api/sessions/${id}`, data),
    deleteSession: (id) => request('DELETE', `/api/sessions/${id}`),
    importSessions: (file) => {
        const formData = new FormData()
        formData.append('file', file)
        return request('POST', '/api/sessions/import', formData)
    },

    // Locations
    getLocations: () => request('GET', '/api/locations'),
    createLocation: (data) => request('POST', '/api/locations', data),
    updateLocation: (id, data) => request('PUT', `/api/locations/${id}`, data),
    deleteLocation: (id) => request('DELETE', `/api/locations/${id}`),

    // Users
    getUsers: () => request('GET', '/api/users'),
    createUser: (data) => request('POST', '/api/users', data),
    updateUser: (id, data) => request('PUT', `/api/users/${id}`, data),
    deleteUser: (id) => request('DELETE', `/api/users/${id}`),

    // Settings - Roles
    getRoles: () => request('GET', '/api/settings/roles'),
    createRole: (data) => request('POST', '/api/settings/roles', data),
    updateRole: (id, data) => request('PUT', `/api/settings/roles/${id}`, data),
    deleteRole: (id) => request('DELETE', `/api/settings/roles/${id}`),
    getPermissions: () => request('GET', '/api/settings/permissions'),


    //Seaons
    getSeasons: () => request('GET', '/api/seasons'),
    setActiveSeason: (id) => request('PUT', `/api/seasons/${id}/active`),

    //Teams
    getTeams: () => request('GET', '/api/teams'),
    getTeamRoles: () => request('GET', '/api/teams/roles'),
    getAssignedTeams: (id) => request('GET', `/api/teams/${id}`),
    createTeam: (data) => request('POST', '/api/teams', data),
}