import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from '@/stores/auth'
import i18n from '@/i18n'

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
        meta: {public: true, titleKey: 'login.signIn'}
    },
    {
        path: '/',
        redirect: '/dashboard'
    },
    {
        path: '/dashboard',
        name: 'dashboard',
        component: DashboardView,
        meta: {titleKey: 'nav.dashboard'}
    },
    {
        path: '/training',
        name: 'training',
        component: TrainingPage,
        meta: {titleKey: 'nav.training'}
    },
    {
        path: '/competition',
        name: 'competition',
        component: CompetitionPage,
        meta: {titleKey: 'nav.competition'}
    },
    {
        path: '/overview',
        redirect: (to) => (to.query.type === 'competition' ? '/competition' : '/training')
    },
    {
        path: '/calender',
        name: 'calender',
        component: CalenderView,
        meta: {titleKey: 'nav.calender'}
    },
    {
        path: '/settings',
        name: 'settings',
        component: SettingsView,
        meta: {titleKey: 'nav.settings', requiresAdmin: false}
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

    if (to.meta.titleKey) {
        document.title = `ShotLog - ${i18n.global.t(to.meta.titleKey)}`
    }
})

export default router