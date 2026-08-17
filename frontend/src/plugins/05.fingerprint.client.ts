import { FingerprintPlugin } from '@fingerprint/vue'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.use(FingerprintPlugin, {
    apiKey: 'tTMZjTZHbPVov7K7zdvL',
    region: 'eu',
  })
})
