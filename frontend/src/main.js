import {createApp} from 'vue'
import {createPinia} from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/css/app.css'
import './assets/css/overview.css'
import './assets/css/calender.css'
import './assets/css/settings.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
