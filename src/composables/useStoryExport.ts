import { ref } from 'vue';
import { toBlob } from 'html-to-image';
import type { StoryElement } from '@/types/StoryElement';

const CANVAS_WIDTH = 558;
const CANVAS_HEIGHT = 1000;

export function useStoryExport() {
  const isRendering = ref(false);
  const renderProgress = ref(0);

  const downloadImage = (blobUrl: string, filename: string = 'story.png') => {
    const link = document.createElement('a');
    link.download = filename;
    link.href = blobUrl;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };


  const preloadImage = (img: HTMLImageElement): Promise<void> => {
    return new Promise((resolve) => {
      if (img.complete && img.naturalHeight !== 0) {
        resolve();
        return;
      }
      img.onload = () => resolve();
      img.onerror = () => {
        console.warn('Pominięto uszkodzony obrazek:', img.src);
        resolve();
      };
    });
  };


  const blobToDataURL = async (blobUrl: string): Promise<string> => {
    try {
      const response = await fetch(blobUrl);
      const blob = await response.blob();
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result as string);
        reader.onerror = reject;
        reader.readAsDataURL(blob);
      });
    } catch (error) {
      console.error('Failed to convert blob to data URL:', error);
      return blobUrl; // Return original if conversion fails
    }
  };

  const renderStoryToImage = async (
    backgroundElement: HTMLElement,
    elements: StoryElement[],
    autoDownload: boolean = false
  ): Promise<string> => {
    try {
      isRendering.value = true;
      renderProgress.value = 5;


      const images = Array.from(backgroundElement.querySelectorAll('img'));


      let loadedCount = 0;
      const totalImages = images.length;

      if (totalImages > 0) {
        await Promise.all(
          images.map(async (img) => {

            if (img.src.startsWith('blob:')) {
              const dataUrl = await blobToDataURL(img.src);
              img.src = dataUrl;

              await preloadImage(img);
            }

            else if (img.src.startsWith('http') && !img.src.includes(window.location.origin)) {
              if (!img.crossOrigin) img.crossOrigin = 'anonymous';
              await preloadImage(img);
            } else {
              await preloadImage(img);
            }
            loadedCount++;
            renderProgress.value = 5 + Math.floor((loadedCount / totalImages) * 20);
          })
        );
      } else {
         renderProgress.value = 25;
      }


      const blob = await toBlob(backgroundElement, {
        width: CANVAS_WIDTH,
        height: CANVAS_HEIGHT,
        pixelRatio: 2,
        cacheBust: true,
        skipAutoScale: true,
        backgroundColor: '#ffffff',
        includeQueryParams: false,
        fetchRequestInit: {
            cache: 'no-cache',
        },
      });

      if (!blob) throw new Error('Blob jest pusty');

      renderProgress.value = 90;
      const blobUrl = URL.createObjectURL(blob);

      if (autoDownload) {
        downloadImage(blobUrl, `story-${Date.now()}.png`);
      }

      console.log('Wygenerowano URL:', blobUrl);
      renderProgress.value = 100;

      setTimeout(() => isRendering.value = false, 500);
      return blobUrl;

    } catch (error) {
      console.error('Błąd renderowania:', error);
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
