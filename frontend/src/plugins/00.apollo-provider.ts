import { provideApolloClient, provideApolloClients } from '@vue/apollo-composable'

export default defineNuxtPlugin((nuxtApp) => {
  const getClient = () => {
    return (
      (nuxtApp.$apollo as any)?.defaultClient ||
      (nuxtApp.$apollo as any)?.clients?.default ||
      nuxtApp.$apollo?.default ||
      (nuxtApp as any)._apolloClients?.default ||
      (nuxtApp.vueApp as any)?._context?.provides?.ApolloClients?.default ||
      (nuxtApp.vueApp as any)?.config?.globalProperties?.$apolloProvider?.defaultClient
    )
  }

  const apolloClient = getClient()
  if (apolloClient) {
    provideApolloClient(apolloClient)
    provideApolloClients({ default: apolloClient })
  }

  nuxtApp.hook('app:created', () => {
    const client = getClient()
    if (client) {
      provideApolloClient(client)
      provideApolloClients({ default: client })
    }
  })
})

