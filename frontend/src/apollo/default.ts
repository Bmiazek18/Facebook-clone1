import {
  ApolloLink,
  HttpLink,
} from '@apollo/client'

import { createApolloTracingLink } from '~/utils/observability/apolloTracingLink'
import { createApolloETagLink } from './links/apolloETagLink'

export default () => {
  const config = useRuntimeConfig()

  const httpLink = new HttpLink({
    uri: `${config.public.apiUrl}/graphql`,
  })

  const tracingLink = createApolloTracingLink()
  const eTagLink = createApolloETagLink()

  return {
    link: ApolloLink.from([
      tracingLink,
      eTagLink,
      httpLink,
    ]),
  }
}
