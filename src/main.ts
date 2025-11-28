import './assets/main.css'

// Initialize theme BEFORE rendering Vue app to avoid white flash
const savedTheme = localStorage.getItem('theme')
const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches

if (savedTheme === 'dark' || (savedTheme === null && prefersDark)) {
  document.documentElement.classList.add('dark')
} else {
  document.documentElement.classList.remove('dark')
}

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createI18n } from 'vue-i18n'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import pl from './locales/pl.json'
import en from './locales/en.json'
import App from './App.vue'
import router from './router'
import FloatingVue from 'floating-vue'



const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const messages = {
  pl,
  en
}

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'pl',
  fallbackLocale: 'pl',
  messages
})

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(i18n)
app.use(FloatingVue)
app.mount('#app')
