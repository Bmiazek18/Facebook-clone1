import { ref } from 'vue';
import axios from 'axios';

export interface LinkPreviewData {
  url: string;
  title: string;
  description: string;
  image?: string;
  domain: string;
}

export function useLinkPreview() {
  const linkPreview = ref<LinkPreviewData | null>(null);
  const isLoadingPreview = ref(false);
  const isPreviewDismissed = ref(false); // Zapobiega ponownemu ładowaniu po usunięciu
  const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000';

  const fetchLinkMetadata = async (url: string) => {
    // Jeśli już ładujemy, użytkownik zamknął podgląd lub mamy już dane dla tego URL -> stop
    if (isLoadingPreview.value || isPreviewDismissed.value || (linkPreview.value && linkPreview.value.url === url)) {
      return;
    }

    isLoadingPreview.value = true;
    try {
      const { data } = await axios.post(`${API_URL}/scrape-og`, { url });

      linkPreview.value = {
        url: url,
        domain: data.domain || new URL(url).hostname,
        title: data.title || 'Link Preview',
        description: data.description || '',
        image: data.image || undefined
      };
    } catch (error) {
      console.error('Link preview error:', error);
      // Fallback - tworzymy prosty obiekt bez dopytywania API
      linkPreview.value = {
        url: url,
        domain: new URL(url).hostname,
        title: url,
        description: '',
      };
    } finally {
      isLoadingPreview.value = false;
    }
  };

  const removeLinkPreview = () => {
    linkPreview.value = null;
    isPreviewDismissed.value = true;
  };

  const resetLinkPreview = () => {
    linkPreview.value = null;
    isPreviewDismissed.value = false;
    isLoadingPreview.value = false;
  };

  return {
    linkPreview,
    isLoadingPreview,
    fetchLinkMetadata,
    removeLinkPreview,
    resetLinkPreview
  };
}
