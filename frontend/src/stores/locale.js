import {defineStore} from 'pinia'
import {computed} from 'vue'
import i18n from '@/i18n'

const STORAGE_KEY = 'locale'
const LOCALES = ['de', 'en']

export const useLocaleStore = defineStore('locale', () => {
    // Current locale, driven by the vue-i18n global instance.
    const locale = computed(() => i18n.global.locale.value)

    function setLocale(l) {
        if (!LOCALES.includes(l)) return
        i18n.global.locale.value = l
        localStorage.setItem(STORAGE_KEY, l)
        document.documentElement.lang = l
    }

    // DE ↔ EN
    function toggle() {
        setLocale(locale.value === 'de' ? 'en' : 'de')
    }

    return {locale, locales: LOCALES, setLocale, toggle}
})
