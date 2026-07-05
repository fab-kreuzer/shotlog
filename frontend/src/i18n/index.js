import {createI18n} from 'vue-i18n'
import de from './de.json'
import en from './en.json'

const SUPPORTED = ['de', 'en']

export function detectLocale() {
    const saved = localStorage.getItem('locale')
    if (SUPPORTED.includes(saved)) return saved
    const nav = (navigator.language || 'de').slice(0, 2).toLowerCase()
    return SUPPORTED.includes(nav) ? nav : 'de'
}

const i18n = createI18n({
    legacy: false,            // Composition API (this codebase uses <script setup>)
    globalInjection: true,    // enables $t in templates
    locale: detectLocale(),
    fallbackLocale: 'de',
    messages: {de, en},
})

// Reflect the initial locale on the <html> element for accessibility/SEO.
document.documentElement.lang = i18n.global.locale.value

export default i18n