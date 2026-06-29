import {defineStore} from 'pinia'
import {computed, ref, watch, watchEffect} from 'vue'

const STORAGE_KEY = 'theme'
const MODES = ['system', 'light', 'dark']

export const useThemeStore = defineStore('theme', () => {
    // 'system' | 'light' | 'dark' — defaults to following the OS on first visit
    const stored = localStorage.getItem(STORAGE_KEY)
    const mode = ref(MODES.includes(stored) ? stored : 'system')

    const mql = window.matchMedia('(prefers-color-scheme: dark)')
    const systemDark = ref(mql.matches)
    mql.addEventListener('change', (e) => {
        systemDark.value = e.matches
    })

    const resolved = computed(() =>
        mode.value === 'system' ? (systemDark.value ? 'dark' : 'light') : mode.value
    )

    // Keep the <html> class and storage in sync with the resolved theme.
    watchEffect(() => {
        document.documentElement.classList.toggle('dark', resolved.value === 'dark')
    })
    watch(mode, (m) => localStorage.setItem(STORAGE_KEY, m))

    function setMode(m) {
        if (MODES.includes(m)) mode.value = m
    }

    // System → Light → Dark → System
    function cycle() {
        mode.value = MODES[(MODES.indexOf(mode.value) + 1) % MODES.length]
    }

    return {mode, resolved, setMode, cycle}
})
