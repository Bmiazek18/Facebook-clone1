import { ref } from 'vue'

export interface LinkPreviewData {
  url: string
  title: string
  description: string
  image?: string
  domain: string
}

export function useLinkPreview() {
  const linkPreview = ref<LinkPreviewData | null>(null)
  const isLoadingPreview = ref(false)
  const isPreviewDismissed = ref(false) // Zapobiega ponownemu ładowaniu po usunięciu
  const GRAPHQL_URL = '/api/linkguard/graphql'

  const fetchLinkMetadata = async (url: string) => {
    // Jeśli już ładujemy, użytkownik zamknął podgląd lub mamy już dane dla tego URL -> stop
    if (
      isLoadingPreview.value ||
      isPreviewDismissed.value ||
      (linkPreview.value && linkPreview.value.url === url)
    ) {
      return
    }

    isLoadingPreview.value = true
    try {
      const response = await fetch(GRAPHQL_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `query ScrapeOg($url: String!) { scrapeOg(url: $url) { title description image siteName } }`,
          variables: { url },
        }),
      })
      const result = await response.json()
      if (!response.ok || result.errors?.length)
        throw new Error(result.errors?.[0]?.message || 'LinkGuard unavailable')
      const data = result.data.scrapeOg

      linkPreview.value = {
        url: url,
        domain: data.domain || new URL(url).hostname,
        title: data.title || 'Link Preview',
        description: data.description || '',
        image: data.image || undefined,
      }
    } catch (error) {
      console.error('Link preview error:', error)
      // Fallback - tworzymy prosty obiekt bez dopytywania API
      linkPreview.value = {
        url: url,
        domain: new URL(url).hostname,
        title: url,
        description: '',
      }
    } finally {
      isLoadingPreview.value = false
    }
  }

  const removeLinkPreview = () => {
    linkPreview.value = null
    isPreviewDismissed.value = true
  }

  const resetLinkPreview = () => {
    linkPreview.value = null
    isPreviewDismissed.value = false
    isLoadingPreview.value = false
  }

  return {
    linkPreview,
    isLoadingPreview,
    fetchLinkMetadata,
    removeLinkPreview,
    resetLinkPreview,
  }
}
