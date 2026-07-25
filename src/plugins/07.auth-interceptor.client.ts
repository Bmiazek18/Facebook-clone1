import { defineNuxtPlugin } from '#app'
import { ofetch } from 'ofetch'

export default defineNuxtPlugin(() => {
  if (typeof window === 'undefined') return

  // Override global $fetch with response interceptor for 401 errors
  globalThis.$fetch = ofetch.create({
    onResponseError({ response }) {
      if (response.status === 401) {
        console.warn('BFF Session expired (401). Redirecting to Keycloak login...')
        const keycloakLoginUrl = 'http://localhost:8089/realms/myrealm/protocol/openid-connect/auth?client_id=facebook-clone&redirect_uri=' + encodeURIComponent('http://localhost:3000/api/auth/callback') + '&response_type=code&scope=openid'
        window.location.href = keycloakLoginUrl
      }
    }
  })
})
