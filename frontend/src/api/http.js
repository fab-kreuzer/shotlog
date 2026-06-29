import {useNotificationStore} from '@/stores/notifications'

const BASE = ''

async function request(method, url, body = null, {notify = true} = {}) {
    const options = {
        method,
        headers: {},
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
        const error = {status: 401, message: 'Nicht authentifiziert'}
        if (notify) useNotificationStore().fromApi(error)
        throw error
    }

    if (!response.ok) {
        let errorData
        try {
            errorData = await response.json()
        } catch {
            errorData = {error: await response.text()}
        }
        const error = {status: response.status, ...errorData}
        if (notify) useNotificationStore().fromApi(error)
        throw error
    }

    if (response.status === 204) return null

    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
        const data = await response.json()
        if (notify && hasMessage(data)) useNotificationStore().fromApi(data)
        return data
    }
    return null
}

const MESSAGE_KEYS = ['error', 'warn', 'success', 'info', 'message']

// Only treat a successful response as a notification when its body
// explicitly carries a message — never for plain data (arrays, etc.).
function hasMessage(data) {
    return data && !Array.isArray(data) && typeof data === 'object'
        && MESSAGE_KEYS.some(k => data[k])
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
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: formData,
            credentials: 'same-origin'
        })
    },
    logout: () => fetch('/api/auth/logout', {method: 'POST', credentials: 'same-origin'}),
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

    // Settings - Users
    getUsers: () => request('GET', '/api/settings/users'),
    createUser: (data) => request('POST', '/api/settings/users', data),
    updateUser: (id, data) => request('PUT', `/api/settings/users/${id}`, data),
    deleteUser: (id) => request('DELETE', `/api/settings/users/${id}`),

    // Settings - Roles
    getRoles: () => request('GET', '/api/settings/roles'),
    createRole: (data) => request('POST', '/api/settings/roles', data),
    updateRole: (id, data) => request('PUT', `/api/settings/roles/${id}`, data),
    deleteRole: (id) => request('DELETE', `/api/settings/roles/${id}`)
}