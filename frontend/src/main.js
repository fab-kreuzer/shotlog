import {createApp} from 'vue'
import {createPinia} from 'pinia'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import Aura from '@primeuix/themes/aura'
import 'primeicons/primeicons.css'
import App from './App.vue'
import router from './router'
import './assets/css/main.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(PrimeVue, {
    theme: {
        preset: Aura,
        options: {
            darkModeSelector: '.dark',
            // Place PrimeVue's styles in a cascade layer ordered before Tailwind's
            // `utilities`, so Tailwind utility classes keep overriding component styles.
            cssLayer: {
                name: 'primevue',
                order: 'theme, base, primevue, components, utilities'
            }
        }
    }
})
app.use(ToastService)
app.use(ConfirmationService)
app.mount('#app')
