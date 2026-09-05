import { usersApi } from '@/api/users'

export function useGenerateTicket() {
  async function generateTicket(userId: string): Promise<string> {
    return await usersApi.generateTicket(userId)
  }

  return { generateTicket }
}
