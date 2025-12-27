// src/composables/useFlipAnimation.ts
import { type Ref } from 'vue';

export function useFlipAnimation(containerRef: Ref<HTMLElement | null>) {

  // 1. Mapa jest teraz LOKALNA dla każdego wywołania tej funkcji
  const avatarPositions = new Map<string, DOMRect>();

  const capturePositions = () => {
    // Zabezpieczenie
    if (!containerRef.value) return;

    avatarPositions.clear();

    // 2. Szukamy TYLKO wewnątrz konkretnego kontenera (boxa)
    // Zamiast document.querySelectorAll używamy containerRef.value.querySelectorAll
    const elements = containerRef.value.querySelectorAll('[data-avatar-userid]');

    elements.forEach((el) => {
      const htmlEl = el as HTMLElement;
      const userId = htmlEl.dataset.avatarUserid;
      const rect = htmlEl.getBoundingClientRect();

      if (userId && rect.width > 0) {
        avatarPositions.set(userId, rect);
      }
    });
  };

  const onAvatarEnter = (el: Element, done: () => void) => {
    const htmlEl = el as HTMLElement;
    const userId = htmlEl.dataset.avatarUserid;

    // Sprawdzamy lokalną mapę tego konkretnego boxa
    if (!userId || !avatarPositions.has(userId)) {
      htmlEl.style.opacity = '0';
      requestAnimationFrame(() => {
        htmlEl.animate([{ opacity: 0 }, { opacity: 1 }], { duration: 300 }).onfinish = done;
      });
      return;
    }

    const oldPos = avatarPositions.get(userId)!;
    const newRect = htmlEl.getBoundingClientRect();
    const deltaX = oldPos.left - newRect.left;
    const deltaY = oldPos.top - newRect.top;

    if (Math.abs(deltaX) < 1 && Math.abs(deltaY) < 1) {
      done();
      return;
    }

    htmlEl.style.transition = 'none';
    htmlEl.style.transform = `translate(${deltaX}px, ${deltaY}px) scale(1.1)`;
    htmlEl.style.opacity = '0.9';

    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        const animation = htmlEl.animate(
          [
            { transform: `translate(${deltaX}px, ${deltaY}px) scale(1.1)`, opacity: 0.9 },
            { transform: `translate(0, 0) scale(1)`, opacity: 1 }
          ],
          { duration: 600, easing: 'cubic-bezier(0.2, 0.8, 0.2, 1)', fill: 'both' }
        );
        animation.onfinish = () => {
          htmlEl.style.transform = '';
          htmlEl.style.opacity = '';
          htmlEl.style.transition = '';
          done();
        };
      });
    });
  };

  const onAvatarLeave = (el: Element, done: () => void) => {
    const htmlEl = el as HTMLElement;
    htmlEl.style.pointerEvents = 'none';
    htmlEl.animate([
      { opacity: 1, transform: 'scale(1)' },
      { opacity: 0, transform: 'scale(0.01)' }
    ], { duration: 300, easing: 'ease-in', fill: 'both' }).onfinish = done;
  };

  // Zwracamy funkcje, które będą używane w Provide/Inject
  return { capturePositions, onAvatarEnter, onAvatarLeave };
}
