import { provideApolloClient, provideApolloClients } from '@vue/apollo-composable'
import { setGlobalApolloClient } from '@/utils/apollo'

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

  const initClient = () => {
    const apolloClient = getClient()
    if (apolloClient) {
      setGlobalApolloClient(apolloClient)
      provideApolloClient(apolloClient)
      provideApolloClients({ default: apolloClient })
    }
  }

  initClient()

  nuxtApp.hook('app:created', initClient)
  nuxtApp.hook('app:beforeMount', initClient)
  nuxtApp.hook('app:mounted', initClient)
})
