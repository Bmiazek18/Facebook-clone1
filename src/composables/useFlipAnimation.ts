
const avatarPositions = new Map<string, DOMRect>();

export function useFlipAnimation() {

  const capturePositions = () => {
    const elements = document.querySelectorAll('[data-avatar-userid]');
    elements.forEach((el) => {
      const htmlEl = el as HTMLElement;
      const userId = htmlEl.dataset.avatarUserid;
      if (userId) {
        avatarPositions.set(userId, htmlEl.getBoundingClientRect());
      }
    });
  };

  // Animacja wejścia (FLIP - lot)
  const onAvatarEnter = (el: Element, done: () => void) => {
    const htmlEl = el as HTMLElement;
    const userId = htmlEl.dataset.avatarUserid;

    if (!userId || !avatarPositions.has(userId)) {
      // Fade In dla nowych
      htmlEl.animate([{ opacity: 0 }, { opacity: 1 }], { duration: 300 }).onfinish = done;
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

    // Lot do nowej pozycji
    htmlEl.animate(
      [
        { transform: `translate(${deltaX}px, ${deltaY}px) scale(1.1)`, opacity: 0.9 },
        { transform: `translate(0, 0) scale(1)`, opacity: 1 }
      ],
      {
        duration: 500,
        easing: 'cubic-bezier(0.34, 1.56, 0.64, 1)',
        fill: 'both'
      }
    ).onfinish = done;
  };


  const onAvatarLeave = (el: Element, done: () => void) => {
    const htmlEl = el as HTMLElement;



    htmlEl.animate(
      [
        { opacity: 0, width: '14px' }, // Start: widoczny
        { opacity: 0, width: '0' } // Koniec: znika
      ],
      {
        duration: 150,
        easing: 'ease-in'
      }
    ).onfinish = done;
  };

  return {
    capturePositions,
    onAvatarEnter,
    onAvatarLeave // <--- Eksportujemy nową funkcję
  };
}
