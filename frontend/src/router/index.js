import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from '@/stores/auth'

import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import OverviewView from '@/views/OverviewView.vue'
import CalenderView from '@/views/CalenderView.vue'
import SettingsView from "@/views/settings/SettingsView.vue";

const routes = [
    {
        path: '/login',
        name: 'login',
        component: LoginView,
        meta: {public: true, title: 'Anmelden - ShotLog'}
    },
    {
        path: '/',
        redirect: '/dashboard'
    },
    {
        path: '/dashboard',
        name: 'dashboard',
        component: DashboardView,
        meta: {title: 'Dashboard'}
    },
    {
        path: '/overview',
        name: 'overview',
        component: OverviewView,
        meta: {title: 'Übersicht'}
    },
    {
        path: '/calender',
        name: 'calender',
        component: CalenderView,
        meta: {title: 'Kalender'}
    },
    {
        path: '/settings',
        name: 'settings',
        component: SettingsView,
        meta: {title: 'Einstellungen', requiresAdmin: false}
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach(async (to) => {
    const auth = useAuthStore()

    if (auth.loading) {
        await auth.fetchUser()
    }

    if (!to.meta.public && !auth.isLoggedIn) {
        return {name: 'login'}
    }

    if (to.name === 'login' && auth.isLoggedIn) {
        return {name: 'dashboard'}
    }

    if (to.meta.title) {
        document.title = to.meta.title
    }
})

export default router
