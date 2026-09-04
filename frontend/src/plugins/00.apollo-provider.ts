import { provideApolloClient } from '@vue/apollo-composable'

export default defineNuxtPlugin((nuxtApp) => {
  const getClient = () => {
    return (
      nuxtApp.$apollo?.default ||
      (nuxtApp as any)._apolloClients?.default ||
      (nuxtApp.vueApp as any)?._context?.provides?.ApolloClients?.default ||
      (nuxtApp.vueApp as any)?.config?.globalProperties?.$apolloProvider?.defaultClient
    )
  }

  const apolloClient = getClient()
  if (apolloClient) {
    provideApolloClient(apolloClient)
  }

  nuxtApp.hook('app:created', () => {
    const client = getClient()
    if (client) {
      provideApolloClient(client)
    }
  })
})
