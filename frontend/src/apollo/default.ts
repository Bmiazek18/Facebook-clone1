import {
  ApolloLink,
  HttpLink,
} from '@apollo/client'

import { createApolloTracingLink } from '~/utils/observability/apolloTracingLink'

export default () => {
  const config = useRuntimeConfig()

  const httpLink = new HttpLink({
    uri: `${config.public.apiUrl}/graphql`,
  })

  const tracingLink = createApolloTracingLink()

  return {
    link: ApolloLink.from([
      tracingLink,
      httpLink,
    ]),
  }
}
