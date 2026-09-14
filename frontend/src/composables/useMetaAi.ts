export const useMetaAi = () => {
  const refreshHistoryTrigger = useState<number>('meta_ai_refresh_history', () => 0)

  const triggerRefreshHistory = () => {
    refreshHistoryTrigger.value++
  }

  return {
    refreshHistoryTrigger,
    triggerRefreshHistory
  }
}
