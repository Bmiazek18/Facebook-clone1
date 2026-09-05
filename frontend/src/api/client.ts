import { getApolloClient, gqlQuery, gqlMutate } from '@/utils/apollo'
import type { DocumentNode, OperationVariables } from '@apollo/client'

export { getApolloClient }

export const apiClient = {
  query: <T = any, V extends OperationVariables = OperationVariables>(
    query: DocumentNode,
    variables?: V,
    options?: { fetchPolicy?: 'network-only' | 'cache-first' | 'no-cache'; errorPolicy?: 'none' | 'ignore' | 'all' }
  ) => gqlQuery<T, V>(query, variables, options),

  mutate: <T = any, V extends OperationVariables = OperationVariables>(
    mutation: DocumentNode,
    variables?: V,
    options?: { update?: any; errorPolicy?: 'none' | 'ignore' | 'all' }
  ) => gqlMutate<T, V>(mutation, variables, options),
}
