import { defineNuxtRouteMiddleware, navigateTo, useCookie } from '#imports'

export default defineNuxtRouteMiddleware((to) => {
  const token = useCookie('jwt_token')
  const isAuthenticated = !!token.value

  const authPaths = ['/login', '/register', '/identify', '/confirmemail']
  const isAuthPath = authPaths.includes(to.path) || to.path.startsWith('/recover')

  if (!isAuthenticated && !isAuthPath) {
    // Redirect to Keycloak login page
    const keycloakLoginUrl = 'http://localhost:8089/realms/myrealm/protocol/openid-connect/auth?client_id=facebook-clone&redirect_uri=' + encodeURIComponent('http://localhost:3000/api/auth/callback') + '&response_type=code&scope=openid'
    return navigateTo(keycloakLoginUrl, { external: true })
  }

  if (isAuthenticated && isAuthPath) {
    // Redirect to / (home)
    return navigateTo('/')
  }
})
