import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createI18n } from 'vue-i18n'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
import App from './App.vue'
import router from './router'
import FloatingVue from 'floating-vue'
const app = createApp(App)
const i18n = createI18n({
  // something vue-i18n options here ...
})
app.use(pinia)
app.use(router)
app.use(i18n)
app.use(FloatingVue)
app.mount('#app')
