import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from '@/stores/auth'

import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import TrainingPage from '@/views/TrainingPage.vue'
import CompetitionPage from '@/views/CompetitionPage.vue'
import CalenderView from '@/views/CalenderView.vue'
import SettingsView from "@/views/settings/SettingsView.vue";

const routes = [
    {
        path: '/login',
        name: 'login',
        component: LoginView,
        meta: {public: true, title: 'ShotLog - Anmelden'}
    },
    {
        path: '/',
        redirect: '/dashboard'
    },
    {
        path: '/dashboard',
        name: 'dashboard',
        component: DashboardView,
        meta: {title: 'ShotLog - Dashboard'}
    },
    {
        path: '/training',
        name: 'training',
        component: TrainingPage,
        meta: {title: 'ShotLog - Training'}
    },
    {
        path: '/competition',
        name: 'competition',
        component: CompetitionPage,
        meta: {title: 'ShotLog - Wettkampf'}
    },
    {
        path: '/overview',
        redirect: (to) => (to.query.type === 'competition' ? '/competition' : '/training')
    },
    {
        path: '/calender',
        name: 'calender',
        component: CalenderView,
        meta: {title: 'ShotLog - Kalender'}
    },
    {
        path: '/settings',
        name: 'settings',
        component: SettingsView,
        meta: {title: 'ShotLog - Einstellungen', requiresAdmin: false}
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