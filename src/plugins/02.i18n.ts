import { createI18n } from 'vue-i18n'
import pl from '~/locales/pl.json'
import en from '~/locales/en.json'

export default defineNuxtPlugin((nuxtApp) => {
  const messages = { pl, en }

  // Safe check for localStorage on the server
  const initialLocale = import.meta.client ? localStorage.getItem('locale') || 'pl' : 'pl'

  const i18n = createI18n({
    legacy: false,
    locale: initialLocale,
    fallbackLocale: 'pl',
    messages,
  })

  nuxtApp.vueApp.use(i18n)
})
