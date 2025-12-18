<script setup lang="ts">
import { ref, watch, computed, type Ref } from 'vue';

// Definicja typu dla danych koloru (obiekt RGB)
interface ColorData {
    r: number;
    g: number;
    b: number;
}

// Props
interface Props {
    imageUrl?: string
}

const props = withDefaults(defineProps<Props>(), {
    imageUrl: "https://picsum.photos/id/45/2000/800"
})

// URL obrazu wczytanego przez użytkownika
const IMAGE_URL = computed(() => props.imageUrl)

// Referencje do elementów DOM
// HTMLImageElement reprezentuje element <img>
const imgRef: Ref<HTMLImageElement | null> = ref(null);

// Stany przechowujące style CSS dla obu gradientów
const leftGradientStyle: Ref<string> = ref('');
const rightGradientStyle: Ref<string> = ref('');

// Stan do obsługi ładowania i błędów
const status: Ref<string> = ref('Ładowanie obrazu...');
const imageLoaded: Ref<boolean> = ref(false);

// Stałe zdefiniowane przez użytkownika
const SAMPLING_AREA_WIDTH: number = 50; // Nowa szerokość obszaru uśredniania (w pikselach)
const samplePoints: number = 7; // Nowa liczba punktów próbkowania
const gradientHeightPercentage: number = 0.8; // Wysokość gradientu (80% wysokości kontenera)

// Statyczny biały gradient, aby zwiększyć przezroczystość na dole.
const WHITE_OVERLAY_GRADIENT: string =
    `linear-gradient(to bottom, transparent 0%, transparent 50%, rgba(255, 255, 255, 0.4) 75%, rgba(255, 255, 255, 0.7) 90%, rgb(255, 255, 255) 100%)`;

// Stałe wymiary widocznego kontenera obrazu (odczytane z szablonu)
const DISPLAY_WIDTH: number = 1250;
const DISPLAY_HEIGHT: number = 450;


/**
 * Konwertuje składowe RGB na format rgba(r, g, b, a)
 */
const toRgba = (r: number, g: number, b: number, a: number): string => `rgba(${r}, ${g}, ${b}, ${a.toFixed(3)})`;


/**
 * Główna funkcja do pobierania kolorów i generowania stylów gradientu,
 * z uwzględnieniem przycięcia przez object-cover.
 */
const generateGradient = (): void => {
    // Sprawdzamy, czy referencja jest elementem HTMLImageElement
    const img = imgRef.value;

    if (!img || !(img instanceof HTMLImageElement) || !img.naturalWidth) {
        status.value = 'Błąd: Obraz nie jest załadowany lub jest uszkodzony.';
        return;
    }

    try {
        // Dynamiczne tworzenie Canvas do odczytu pikseli
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        if (!ctx) {
             status.value = 'Błąd: Nie można uzyskać kontekstu 2D Canvas.';
            return;
        }

        const NW: number = img.naturalWidth; // Natural Width (oryginalna szerokość)
        const NH: number = img.naturalHeight; // Natural Height (oryginalna wysokość)

        canvas.width = NW;
        canvas.height = NH;

        // Rysowanie obrazu na canvas w oryginalnych wymiarach
        ctx.drawImage(img, 0, 0, NW, NH);

        // --- 1. Obliczenia dla object-cover, aby znaleźć widoczny obszar na Canvas (w pixelach oryginalnych) ---
        const SW: number = DISPLAY_WIDTH / NW; // Skala potrzebna do wypełnienia szerokości
        const SH: number = DISPLAY_HEIGHT / NH; // Skala potrzebna do wypełnienia wysokości
        const Scale: number = Math.max(SW, SH); // Rzeczywista skala (większa z dwóch)

        // Obliczenie offsetów (o ile przycięto na krawędziach w pikselach ekranu)
        const ScaledWidth: number = NW * Scale;
        const ScaledHeight: number = NH * Scale;
        const OX: number = (DISPLAY_WIDTH - ScaledWidth) / 2; // Offset X w pikselach ekranu
        const OY: number = (DISPLAY_HEIGHT - ScaledHeight) / 2; // Offset Y w pikselach ekranu

        // Mapowanie punktów zerowych ekranu na Canvas (w pikselach oryginalnych)
        const C_VisibleX_Start: number = -OX / Scale; // Początkowy X widocznego obszaru na Canvas
        const C_VisibleY_Start: number = -OY / Scale; // Początkowy Y widocznego obszaru na Canvas

        // --- 2. Obliczenia dla próbkowania ---

        // Wysokość obszaru próbkowania (80% wysokości wyświetlania, zmapowane na Canvas)
        const C_SamplingRangeY: number = (DISPLAY_HEIGHT * gradientHeightPercentage) / Scale;

        // Rzeczywisty Y startowy na Canvas (minimalna wartość to 0)
        // Zapewnia, że jeśli obraz jest przycięty od góry, zaczynamy od góry widocznej części
        const C_Y_Offset: number = Math.max(0, C_VisibleY_Start);

        // Krok próbkowania w oryginalnych pikselach obrazu
        const stepY: number = C_SamplingRangeY / (samplePoints - 1);

        // --- 3. Funkcja próbkowania z uśrednianiem ---

        /**
         * Funkcja pobiera i uśrednia dane kolorów z obszaru o szerokości SAMPLING_AREA_WIDTH.
         * @param startX - Początkowa współrzędna X obszaru uśredniania (na Canvas).
         */
        const getColorsDataFromArea = (startX: number): ColorData[] => {
            const colorData: ColorData[] = [];

            for (let i = 0; i < samplePoints; i++) {
                // sampleY jest rzeczywistą współrzędną Y na Canvas (piksel oryginalnego obrazu)
                const relativeSampleY: number = i * stepY;
                const sampleY: number = Math.min(Math.round(C_Y_Offset + relativeSampleY), NH - 1);

                // Bezpieczna pozycja startowa X
                const safeStartX: number = Math.max(0, Math.min(NW - SAMPLING_AREA_WIDTH, Math.round(startX)));

                // Pobieramy dane 1xSAMPLING_AREA_WIDTH pikseli
                // TypeScript gwarantuje, że ctx nie jest null dzięki sprawdzeniu powyżej
                const imageData: ImageData = ctx!.getImageData(safeStartX, sampleY, SAMPLING_AREA_WIDTH, 1);
                const data: Uint8ClampedArray = imageData.data;

                let rTotal: number = 0, gTotal: number = 0, bTotal: number = 0;
                const pixelCount: number = SAMPLING_AREA_WIDTH;

                // Uśrednianie kolorów
                for (let j = 0; j < data.length; j += 4) {
                    rTotal += data[j] ?? 0;
                    gTotal += data[j + 1] ?? 0;
                    bTotal += data[j + 2] ?? 0;
                }

                // Obliczanie średnich składowych RGB
                const r: number = Math.round(rTotal / pixelCount);
                const g: number = Math.round(gTotal / pixelCount);
                const b: number = Math.round(bTotal / pixelCount);

                colorData.push({ r, g, b });
            }
            return colorData;
        };

        // --- 4. Obliczanie pozycji próbkowania na Canvas ---

        // Próbkowanie na LEWEJ KRAWĘDZI (wewnątrz 5px marginesu widocznego obszaru)
        const C_SampleX_Left_Start: number = C_VisibleX_Start + 5;

        // Próbkowanie na PRAWEJ KRAWĘDZI (5px przed końcem widocznego obszaru)
        const C_VisibleX_End: number = C_VisibleX_Start + DISPLAY_WIDTH / Scale;
        const C_SampleX_Right_Start: number = C_VisibleX_End - SAMPLING_AREA_WIDTH - 5;

        const leftColorData: ColorData[] = getColorsDataFromArea(C_SampleX_Left_Start);
        const rightColorData: ColorData[] = getColorsDataFromArea(C_SampleX_Right_Start);


        /**
         * Funkcja pomocnicza do generowania stylu gradientu.
         * @param colorData - Tablica obiektów ColorData.
         */
        const generateStyle = (colorData: ColorData[]): string => {
            const stops: string[] = colorData.map((data, index) => {
                // Pozycja w kontenerze (gdzie ma się znaleźć ten kolor)
                const positionPercent: number = (index / (colorData.length - 1)) * (gradientHeightPercentage * 100);

                // ODWRÓCONY WSPÓŁCZYNNIK ALPHA (Krycie):
                const linearAlpha: number = (colorData.length - 1 - index) / (colorData.length - 1);
                const alpha: number = Math.pow(linearAlpha, 2); // Użycie potęgi spowalnia zanikanie.

                const rgbaColor: string = toRgba(data.r, data.g, data.b, alpha);

                return `${rgbaColor} ${positionPercent.toFixed(1)}%`;
            });

            const finalGradientStops: string = stops.join(', ');

            // Dynamiczny gradient zanikający do dołu (GÓRNA WARSTWA)
            const dynamicGradient: string = `linear-gradient(to bottom, ${finalGradientStops}, transparent ${(gradientHeightPercentage * 100).toFixed(1)}%, transparent 100%)`;

            // Łączymy dynamiczny gradient z gradientem nakładki białej
            return `${dynamicGradient}, ${WHITE_OVERLAY_GRADIENT}`;
        }

        // Ustawianie stylów
        leftGradientStyle.value = generateStyle(leftColorData);
        rightGradientStyle.value = generateStyle(rightColorData);

        status.value = 'Gotowe';

    } catch (error) {
        console.error("Błąd generowania gradientu:", error);
        // Używamy instanceof do zawężenia typu, aby bezpiecznie pobrać wiadomość
        if (error instanceof Error) {
            status.value = `Wystąpił błąd: ${error.message}`;
        } else {
             status.value = 'Wystąpił nieznany błąd.';
        }
    }
};

// Monitorowanie stanu załadowania obrazu
watch(imageLoaded, (isLoaded: boolean) => {
    if (isLoaded) {
        generateGradient();
    }
});

// Obsługa błędu ładowania obrazu
const handleImageError = (event: Event): void => {
    console.error("Błąd ładowania obrazu:", event);
    status.value = 'Błąd: Nie udało się załadować obrazu. Sprawdź, czy URL jest poprawny i czy obsługuje CORS.';
};
</script>

<template>
    <div
        class="flex w-full mt-14  overflow-hidden"
        style="height: 450px;"
    >

        <div class="grow flex items-center justify-center relative ">
            <div v-if="leftGradientStyle"
                class="w-full h-full"
                :style="{ background: leftGradientStyle }"
            />
            <p v-else class="text-gray-500 font-semibold">{{ status }}</p>
        </div>

        <div class="w-[1250px] relative  flex items-center justify-center">

            <img
                ref="imgRef"
                :src="IMAGE_URL"
                alt="Źródłowy obraz"
                class="object-cover w-full h-full rounded-b-xl"
                @load="imageLoaded = true"
                @error="handleImageError"
                crossOrigin="anonymous"

            />
        </div>

        <div class="grow flex items-center justify-center relative">
            <div v-if="rightGradientStyle"
                class="w-full h-full"
                :style="{ background: rightGradientStyle }"
            />
            <p v-else class="text-gray-500 font-semibold">{{ status }}</p>
        </div>
    </div>
</template>
