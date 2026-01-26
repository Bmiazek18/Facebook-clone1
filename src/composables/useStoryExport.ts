import { ref } from 'vue';
import { domToPng } from 'modern-screenshot';
import type { StoryElement } from '@/types/StoryElement';

export function useStoryExport() {
  const isRendering = ref(false);
  const renderProgress = ref(0);

  const downloadImage = (dataUrl: string, filename: string = 'story.png') => {
    const link = document.createElement('a');
    link.download = filename;
    link.href = dataUrl;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const renderStoryToImage = async (
    backgroundElement: HTMLElement,
    elements: StoryElement[],
    autoDownload: boolean = false
  ): Promise<string> => {
    try {
      isRendering.value = true;
      renderProgress.value = 10;

      // Czekamy chwilę, aby upewnić się, że DOM jest stabilny
      await new Promise(resolve => setTimeout(resolve, 100));

      renderProgress.value = 20;

      // modern-screenshot lepiej radzi sobie z nowoczesnym CSS (oklch) i Blobami
      const dataUrl = await domToPng(backgroundElement, {
        scale: 2, // Lepsza jakość
        backgroundColor: '#ffffff', // Ustaw kolor tła (usuwa przezroczystość)
        quality: 1.0,
        filter: (node) => {
          // Filtrowanie elementów UI
          if (node instanceof HTMLElement) {
             if (node.hasAttribute('data-story-control')) return false;
             if (node.hasAttribute('data-guide-line')) return false;
             if (node.hasAttribute('data-story-shadow')) return false;
             if (node.classList.contains('ui-control')) return false;
          }
          return true;
        },
        // Opcje fetch pomagają przy problemach z CORS i Blobami
        fetch: {
          bypassingCache: true,
          requestInit: {
             cache: 'no-store' // Wymusza świeże pobranie zasobów
          }
        },
        // Ważne dla Safari i niektórych problemów z renderowaniem
        features: {
            removeControlCharacter: true
        }
      });

      if (!dataUrl || dataUrl === 'data:,') {
        throw new Error('Wygenerowano pusty obraz');
      }

      renderProgress.value = 90;

      if (autoDownload) {
        downloadImage(dataUrl, `story-${Date.now()}.png`);
      }

      renderProgress.value = 100;

      setTimeout(() => {
        isRendering.value = false;
        renderProgress.value = 0;
      }, 500);

      return dataUrl;

    } catch (error) {
      console.error('Błąd renderowania modern-screenshot:', error);
      isRendering.value = false;
      renderProgress.value = 0;
      throw error;
    }
  };

  return {
    isRendering,
    renderProgress,
    renderStoryToImage,
    downloadImage
  };
}
