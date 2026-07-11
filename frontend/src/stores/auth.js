import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import {api} from '@/api/http'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const loading = ref(true)

    const isLoggedIn = computed(() => user.value !== null)

    const isAdmin = computed(() => {
        if (!user.value) return false
        return user.value.authorities?.includes('ROLE_ADMIN')
    })

    const isSportLeader = computed(() => {
        if (!user.value) return false
        return user.value.authorities?.includes('ROLE_SPORT_LEADER')
    })

    async function fetchUser() {
        loading.value = true
        try {
            user.value = await api.me()
            console.log(user.value)
        } catch {
            user.value = null
        } finally {
            loading.value = false
        }
    }

    async function login(username, password) {
        const response = await api.login(username, password)
        if (response.ok) {
            await fetchUser()
            return true
        }
        return false
    }

    async function logout() {
        await api.logout()
        user.value = null
    }

    return {user, loading, isLoggedIn, isAdmin, isSportLeader, fetchUser, login, logout}
})