import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'

export interface PageInsightsData {
  pageId: string
  period: string
  totalViews: number
  viewsGrowthPercent: number
  netFollowers: number
  followersGrowthPercent: number
  totalReactions: number
  totalComments: number
  totalShares: number
  totalProfileVisits: number
  followerViewsPercent: number
  nonFollowerViewsPercent: number
  viewsByContentType: {
    text: number
    photo: number
    video: number
  }
  timeline: Array<{
    date: string
    views: number
    reactions: number
    comments: number
    visits: number
    followers: number
  }>
  audience: {
    womenPercent: number
    menPercent: number
    otherPercent: number
    ageGroups: Record<string, number>
    topCities: Array<{ name: string; percent: number }>
    topCountries: Array<{ name: string; percent: number }>
  }
}

export function usePageAnalytics(targetPageId?: string) {
  const authStore = useAuthStore()
  const apiUrl = ''

  const effectivePageId = computed(() => {
    return targetPageId || authStore.currentUserId || '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce'
  })

  const insights = ref<PageInsightsData | null>(null)
  const isLoading = ref(false)
  const selectedPeriod = ref('28d')

  const fetchInsights = async (period: string = selectedPeriod.value) => {
    if (!import.meta.client || !effectivePageId.value) return
    isLoading.value = true

    try {
      const res = await fetch(
        `${apiUrl}/api/analytics/pages/${effectivePageId.value}/insights?period=${period}`,
        {
          headers: {
            'X-User-Id': String(authStore.originalUserId || authStore.currentUserId)
          }
        }
      )

      if (res.ok) {
        insights.value = await res.json()
      }
    } catch (err) {
      console.warn('Could not fetch page analytics insights:', err)
    } finally {
      isLoading.value = false
    }
  }

  watch([effectivePageId, selectedPeriod], () => {
    fetchInsights(selectedPeriod.value)
  }, { immediate: true })

  return {
    insights,
    isLoading,
    selectedPeriod,
    fetchInsights
  }
}
