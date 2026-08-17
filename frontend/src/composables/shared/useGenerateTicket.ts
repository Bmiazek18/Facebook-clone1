import gql from 'graphql-tag'
import { useMutation } from '@vue/apollo-composable'

const GENERATE_TICKET = gql`
  mutation GenerateTicket($userId: ID!) {
    generateTicket(userId: $userId)
  }
`

export function useGenerateTicket() {
  const { mutate } = useMutation(GENERATE_TICKET)

  async function generateTicket(userId: string): Promise<string> {
    const result = await mutate({ userId: String(userId) })
    const ticket = result?.data?.generateTicket
    if (!ticket) {
      throw new Error('Failed to generate ticket')
    }
    return ticket
  }

  return { generateTicket }
}
