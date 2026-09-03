import { provideApolloClient } from '@vue/apollo-composable'

export default defineNuxtPlugin((nuxtApp) => {
  const apolloClient =
    nuxtApp.$apollo?.default ||
    (nuxtApp as any)._apolloClients?.default

  if (apolloClient) {
    provideApolloClient(apolloClient)
  }
})
