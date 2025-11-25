// useCarousel.ts
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';

export function useCarousel(n:number) {
  const carouselRef = ref<HTMLElement | null>(null);
  const isStart = ref(true);
  const isEnd = ref(false);

  // Stałe kroki przewijania
  const SCROLL_STEP_NORMAL = n;
  const SCROLL_STEP_BOUNDARY = n+ 0.75;
  const BOUNDARY_THRESHOLD_MULTIPLIER = n + 1.25;

  // Szerokość karty z marginesem
  const cardWidthWithGap = ref(0);

  // Tolerancja granicy scrolla. Jest również używana do ustawienia isStart/isEnd.
  const SCROLL_TOLERANCE = 5;

  const calculateCardWidth = () => {
    if (carouselRef.value && carouselRef.value.firstElementChild) {
      const firstChild = carouselRef.value.firstElementChild as HTMLElement;

      const rect = firstChild.getBoundingClientRect();
      const styles = window.getComputedStyle(firstChild);

      // Pobieranie margin-right
      const marginRight = parseFloat(styles.marginRight || '0');
      const calculatedWidth = rect.width + marginRight;

      if (calculatedWidth > 0) {
        cardWidthWithGap.value = calculatedWidth;
      }
    }
  };

  const checkScrollStart = () => {
    if (carouselRef.value) {
      // isStart jest true, jeśli scrollLeft jest mniejsze lub równe tolerancji
      isStart.value = carouselRef.value.scrollLeft <= SCROLL_TOLERANCE;
    }
  };

  const checkScrollEnd = () => {
    if (carouselRef.value) {
      const { scrollLeft, scrollWidth, offsetWidth } = carouselRef.value;
      // isEnd jest true, jeśli pozostała do przewinięcia odległość jest mniejsza lub równa tolerancji
      isEnd.value = scrollWidth - scrollLeft - offsetWidth <= SCROLL_TOLERANCE;
    }
  };

  const checkScrollState = () => {
    checkScrollStart();
    checkScrollEnd();
  };

  const scrollRight = () => {
    if (carouselRef.value && cardWidthWithGap.value > 0) {
      const { scrollLeft, scrollWidth, offsetWidth } = carouselRef.value;
      const remainingScroll = scrollWidth - scrollLeft - offsetWidth;

      // Próg, po przekroczeniu którego użyjemy kroku granicznego (np. 3.25 szerokości karty od końca)
      const boundaryThreshold = cardWidthWithGap.value * BOUNDARY_THRESHOLD_MULTIPLIER;

      let multiplier = SCROLL_STEP_NORMAL;

      // Używamy kroku granicznego, jeśli jesteśmy na początku lub blisko końca
      if (isStart.value || remainingScroll < boundaryThreshold) {
        multiplier = SCROLL_STEP_BOUNDARY;
      }

      let scrollAmount = cardWidthWithGap.value * multiplier;

      // Bezpieczne dosunięcie do końca: Nie przewiń więcej, niż zostało
      if (remainingScroll > SCROLL_TOLERANCE && scrollAmount > remainingScroll) {
        scrollAmount = remainingScroll;
      } else if (remainingScroll <= SCROLL_TOLERANCE) {
        scrollAmount = 0; // Jesteśmy już na końcu
      }

      carouselRef.value.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    }
  };

  const scrollLeft = () => {
    if (carouselRef.value && cardWidthWithGap.value > 0) {
      const { scrollLeft } = carouselRef.value;

      // Próg, po przekroczeniu którego użyjemy kroku granicznego (np. 3.25 szerokości karty od początku)
      const boundaryThreshold = cardWidthWithGap.value * BOUNDARY_THRESHOLD_MULTIPLIER;

      let multiplier = SCROLL_STEP_NORMAL;

      // Używamy kroku granicznego, jeśli jesteśmy na końcu lub blisko początku
      if (isEnd.value || scrollLeft < boundaryThreshold) {
        multiplier = SCROLL_STEP_BOUNDARY;
      }

      let scrollAmount = cardWidthWithGap.value * multiplier;

      // Bezpieczne dosunięcie do początku: Nie przewiń poniżej 0
      if (scrollAmount > scrollLeft) {
        scrollAmount = scrollLeft;
      } else if (scrollLeft <= SCROLL_TOLERANCE) {
        scrollAmount = 0; // Jesteśmy już na początku
      }

      carouselRef.value.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
    }
  };


  const handleScroll = () => {
    checkScrollState();
  };

  onMounted(() => {
    nextTick(() => {
      calculateCardWidth();
      if (carouselRef.value) {
        checkScrollState();
        carouselRef.value.addEventListener('scroll', handleScroll);
      }
    });

    // Drugie sprawdzenie na wypadek problemów z renderowaniem/hydracją
    setTimeout(() => {
      calculateCardWidth();
      checkScrollState();
    }, 100);
  });

  onBeforeUnmount(() => {
    if (carouselRef.value) {
      carouselRef.value.removeEventListener('scroll', handleScroll);
    }
  });

  return {
    carouselRef,
    isStart,
    isEnd,
    scrollLeft,
    scrollRight,
    checkScrollState
  };
}
