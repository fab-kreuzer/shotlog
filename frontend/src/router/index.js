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
        component: SettingsView,
        meta: {titleKey: 'nav.settings', requiresAdmin: false},
        redirect: {name: 'settings-profile'},
        children: [
            {
                path: 'profile',
                name: 'settings-profile',
                component: () => import('@/views/settings/ProfileTab.vue')
            },
            {
                path: 'users',
                name: 'settings-user-management',
                component: () => import('@/views/settings/UserTab.vue'),
                meta: {permission: 'view_user_tab'}
            },
            {
                path: 'roles',
                name: 'settings-role-management',
                component: () => import('@/views/settings/RoleTab.vue'),
                meta: {permission: 'view_role_tab'}
            },
            {
                path: 'clubs',
                name: 'settings-club-management',
                component: () => import('@/views/settings/ClubManagementTab.vue'),
                meta: {permission: 'view_club_tab'}
            },
            {
                path: 'teams',
                name: 'settings-team-management',
                component: () => import('@/views/settings/TeamTab.vue'),
                meta: {permission: 'view_team_tab'}
            },
            {
                path: 'access-denied',
                name: 'settings-access-denied',
                component: () => import('@/views/settings/AccessDeniedTab.vue')
            },
            {
                path: ':pathMatch(.*)*',
                redirect: {name: 'settings-profile'}
            }
        ]
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

    if (to.meta.permission && !auth.hasPermission(to.meta.permission)) {
        return {name: 'settings-access-denied'}
    }

    if (to.meta.titleKey) {
        document.title = `ShotLog - ${i18n.global.t(to.meta.titleKey)}`
    }
})

export default router