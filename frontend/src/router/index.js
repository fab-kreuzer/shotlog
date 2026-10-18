import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from '@/stores/auth'
import i18n from '@/i18n'

import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import TrainingPage from '@/views/TrainingPage.vue'
import CompetitionPage from '@/views/CompetitionPage.vue'
import CalenderView from '@/views/CalenderView.vue'
import LeaderboardView from '@/views/LeaderboardView.vue'
import SettingsView from "@/views/settings/SettingsView.vue";

// Land on the first settings tab the current user is allowed to see; if none,
// fall back to the access-denied page (regular users normally reach settings
// only via a permission-gated nav link, so this is an edge case).
function firstSettingsTab() {
    const auth = useAuthStore()
    const tabs = [
        {name: 'settings-user-management', permission: 'view_user_tab'},
        {name: 'settings-role-management', permission: 'view_role_tab'},
        {name: 'settings-club-management', permission: 'view_club_tab'},
        {name: 'settings-team-management', permission: 'view_team_tab'},
        {name: 'settings-season-management', permission: 'view_season_tab'}
    ]
    const first = tabs.find(t => auth.hasPermission(t.permission))
    return first ? {name: first.name} : {name: 'settings-access-denied'}
}

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
        meta: {titleKey: 'nav.training', parent: 'dashboard'}
    },
    {
        path: '/competition',
        name: 'competition',
        component: CompetitionPage,
        meta: {titleKey: 'nav.competition', parent: 'dashboard'}
    },
    {
        path: '/training/new',
        name: 'training-session-new',
        component: () => import('@/views/SessionFormView.vue'),
        props: {type: 'TRAINING'},
        meta: {titleKey: 'session.createTrainingTitle', parent: 'training'}
    },
    {
        path: '/training/:id',
        name: 'training-session',
        component: () => import('@/views/SessionDetailView.vue'),
        props: route => ({id: route.params.id, type: 'TRAINING'}),
        meta: {parent: 'training', dynamic: true}
    },
    {
        path: '/training/:id/edit',
        name: 'training-session-edit',
        component: () => import('@/views/SessionFormView.vue'),
        props: route => ({id: route.params.id, type: 'TRAINING'}),
        meta: {titleKey: 'common.edit', parent: 'training-session', parentParams: true}
    },
    {
        path: '/competition/new',
        name: 'competition-session-new',
        component: () => import('@/views/SessionFormView.vue'),
        props: {type: 'COMPETITION'},
        meta: {titleKey: 'session.createCompetitionTitle', parent: 'competition'}
    },
    {
        path: '/competition/:id',
        name: 'competition-session',
        component: () => import('@/views/SessionDetailView.vue'),
        props: route => ({id: route.params.id, type: 'COMPETITION'}),
        meta: {parent: 'competition', dynamic: true}
    },
    {
        path: '/competition/:id/edit',
        name: 'competition-session-edit',
        component: () => import('@/views/SessionFormView.vue'),
        props: route => ({id: route.params.id, type: 'COMPETITION'}),
        meta: {titleKey: 'common.edit', parent: 'competition-session', parentParams: true}
    },
    {
        path: '/overview',
        redirect: (to) => (to.query.type === 'competition' ? '/competition' : '/training')
    },
    {
        path: '/calender',
        name: 'calender',
        component: CalenderView,
        meta: {titleKey: 'nav.calender', parent: 'dashboard'}
    },
    {
        path: '/leaderboard',
        name: 'leaderboard',
        component: LeaderboardView,
        meta: {titleKey: 'nav.leaderboard', permission: 'view_leaderboard', parent: 'dashboard'}
    },
    {
        path: '/profile',
        name: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: {titleKey: 'nav.profile', parent: 'dashboard'}
    },
    {
        path: '/settings',
        name: 'settings',
        component: SettingsView,
        meta: {titleKey: 'nav.settings', requiresAdmin: false, parent: 'dashboard'},
        redirect: firstSettingsTab,
        children: [
            {
                path: 'users',
                name: 'settings-user-management',
                component: () => import('@/views/settings/UserTab.vue'),
                meta: {permission: 'view_user_tab', titleKey: 'settings.tabUserManagement', parent: 'settings'}
            },
            {
                path: 'roles',
                name: 'settings-role-management',
                component: () => import('@/views/settings/RoleTab.vue'),
                meta: {permission: 'view_role_tab', titleKey: 'settings.tabRoleManagement', parent: 'settings'}
            },
            {
                path: 'clubs',
                name: 'settings-club-management',
                component: () => import('@/views/settings/ClubManagementTab.vue'),
                meta: {permission: 'view_club_tab', titleKey: 'settings.tabClubManagement', parent: 'settings'}
            },
            {
                path: 'teams',
                name: 'settings-team-management',
                component: () => import('@/views/settings/TeamTab.vue'),
                meta: {permission: 'view_team_tab', titleKey: 'settings.tabTeamManagement', parent: 'settings'}
            },
            {
                path: 'seasons',
                name: 'settings-season-management',
                component: () => import('@/views/settings/SeasonTab.vue'),
                meta: {permission: 'view_season_tab', titleKey: 'settings.tabSeasonManagement', parent: 'settings'}
            },
            {
                path: 'access-denied',
                name: 'settings-access-denied',
                component: () => import('@/views/settings/AccessDeniedTab.vue')
            },
            {
                path: ':pathMatch(.*)*',
                redirect: '/settings'
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