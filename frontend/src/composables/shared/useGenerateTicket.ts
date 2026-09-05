import gql from 'graphql-tag'
import { getApolloClient } from '@/utils/apollo'

const GENERATE_TICKET = gql`
  mutation GenerateTicket($userId: ID!) {
    generateTicket(userId: $userId)
  }
`

export function useGenerateTicket() {
  async function generateTicket(userId: string): Promise<string> {
    const client = getApolloClient()
    const result = await client.mutate({
      mutation: GENERATE_TICKET,
      variables: { userId: String(userId) },
    })
    const ticket = result?.data?.generateTicket
    if (!ticket) {
      throw new Error('Failed to generate ticket')
    }
    return ticket
  }

  return { generateTicket }
}
