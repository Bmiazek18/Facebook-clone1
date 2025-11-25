<script setup>
import { ref, watch } from 'vue';

// URL obrazu wczytanego przez użytkownika
const IMAGE_URL = "https://scontent-waw2-2.xx.fbcdn.net/v/t39.30808-6/491917724_2675570919301331_2858468314561027549_n.jpg?_nc_cat=100&ccb=1-7&_nc_sid=cc71e4&_nc_ohc=BtRIxal83rMQ7kNvwFbPJiZ&_nc_oc=AdmxraKV3mg-DHCfXLivkwqM4srVRc8ERGqBnEFF-c1SwtvEsiIynN_6s_SY95Ofsi_9S4jaVkXc5A0kryr23ifF&_nc_zt=23&_nc_ht=scontent-waw2-2.xx&_nc_gid=UbM_da1u6IfB9fxxm5cVlw&oh=00_AfiAg3WdKzJlMkOTdpaLkzF--POchopQUkt45P2rELHW1A&oe=6929DBFD";

        // Referencje do elementów DOM
        const imgRef = ref(null);

        // Stany przechowujące style CSS dla obu gradientów
        const leftGradientStyle = ref('');
        const rightGradientStyle = ref('');

        // Stan do obsługi ładowania i błędów
        const status = ref('Ładowanie obrazu...');
        const imageLoaded = ref(false);

        // Stałe zdefiniowane przez użytkownika
        const SAMPLING_AREA_WIDTH = 50; // Nowa szerokość obszaru uśredniania (w pikselach)
        const samplePoints = 7; // Nowa liczba punktów próbkowania
        const gradientHeightPercentage = 0.8; // Wysokość gradientu (65% wysokości kontenera)

        // Statyczny biały gradient, aby zwiększyć przezroczystość na dole.
        const WHITE_OVERLAY_GRADIENT =
            `linear-gradient(to bottom, transparent 0%, transparent 50%, rgba(255, 255, 255, 0.4) 75%, rgba(255, 255, 255, 0.7) 90%, rgb(255, 255, 255) 100%)`;

        // Stałe wymiary widocznego kontenera obrazu (odczytane z szablonu)
        const DISPLAY_WIDTH = 1250;
        const DISPLAY_HEIGHT = 450;


        /**
         * Konwertuje składowe RGB na format rgba(r, g, b, a)
         */
        const toRgba = (r, g, b, a) => `rgba(${r}, ${g}, ${b}, ${a.toFixed(3)})`;


        /**
         * Główna funkcja do pobierania kolorów i generowania stylów gradientu,
         * z uwzględnieniem przycięcia przez object-cover.
         */
        const generateGradient = () => {
            const img = imgRef.value;

            if (!img || !img.naturalWidth) {
                status.value = 'Błąd: Obraz nie jest załadowany lub jest uszkodzony.';
                return;
            }

            try {
                // Dynamiczne tworzenie Canvas do odczytu pikseli
                const canvas = document.createElement('canvas');
                const ctx = canvas.getContext('2d');
                const NW = img.naturalWidth; // Natural Width (oryginalna szerokość)
                const NH = img.naturalHeight; // Natural Height (oryginalna wysokość)

                canvas.width = NW;
                canvas.height = NH;

                // Rysowanie obrazu na canvas w oryginalnych wymiarach
                ctx.drawImage(img, 0, 0, NW, NH);

                // --- 1. Obliczenia dla object-cover, aby znaleźć widoczny obszar na Canvas (w pixelach oryginalnych) ---
                const SW = DISPLAY_WIDTH / NW; // Skala potrzebna do wypełnienia szerokości
                const SH = DISPLAY_HEIGHT / NH; // Skala potrzebna do wypełnienia wysokości
                const Scale = Math.max(SW, SH); // Rzeczywista skala (większa z dwóch)

                // Obliczenie offsetów (o ile przycięto na krawędziach w pikselach ekranu)
                const ScaledWidth = NW * Scale;
                const ScaledHeight = NH * Scale;
                const OX = (DISPLAY_WIDTH - ScaledWidth) / 2; // Offset X w pikselach ekranu
                const OY = (DISPLAY_HEIGHT - ScaledHeight) / 2; // Offset Y w pikselach ekranu

                // Mapowanie punktów zerowych ekranu na Canvas (w pikselach oryginalnych)
                const C_VisibleX_Start = -OX / Scale; // Początkowy X widocznego obszaru na Canvas
                const C_VisibleY_Start = -OY / Scale; // Początkowy Y widocznego obszaru na Canvas

                // --- 2. Obliczenia dla próbkowania ---

                // Wysokość obszaru próbkowania (65% wysokości wyświetlania, zmapowane na Canvas)
                const C_SamplingRangeY = (DISPLAY_HEIGHT * gradientHeightPercentage) / Scale;

                // Rzeczywisty Y startowy na Canvas (minimalna wartość to 0)
                // Zapewnia, że jeśli obraz jest przycięty od góry, zaczynamy od góry widocznej części
                const C_Y_Offset = Math.max(0, C_VisibleY_Start);

                // Krok próbkowania w oryginalnych pikselach obrazu
                const stepY = C_SamplingRangeY / (samplePoints - 1);

                // --- 3. Funkcja próbkowania z uśrednianiem ---

                /**
                 * Funkcja pobiera i uśrednia dane kolorów z obszaru o szerokości SAMPLING_AREA_WIDTH,
                 * uwzględniając offset pionowy.
                 * @param {number} startX - Początkowa współrzędna X obszaru uśredniania (na Canvas).
                 */
                const getColorsDataFromArea = (startX) => {
                    const colorData = [];

                    for (let i = 0; i < samplePoints; i++) {
                        // sampleY jest rzeczywistą współrzędną Y na Canvas (piksel oryginalnego obrazu)
                        const relativeSampleY = i * stepY;
                        const sampleY = Math.min(Math.round(C_Y_Offset + relativeSampleY), NH - 1);

                        // Bezpieczna pozycja startowa X
                        const safeStartX = Math.max(0, Math.min(NW - SAMPLING_AREA_WIDTH, Math.round(startX)));

                        // Pobieramy dane 1xSAMPLING_AREA_WIDTH pikseli
                        const imageData = ctx.getImageData(safeStartX, sampleY, SAMPLING_AREA_WIDTH, 1);
                        const data = imageData.data;

                        let rTotal = 0, gTotal = 0, bTotal = 0;
                        const pixelCount = SAMPLING_AREA_WIDTH;

                        // Uśrednianie kolorów
                        for (let j = 0; j < data.length; j += 4) {
                            rTotal += data[j];
                            gTotal += data[j + 1];
                            bTotal += data[j + 2];
                        }

                        // Obliczanie średnich składowych RGB
                        const r = Math.round(rTotal / pixelCount);
                        const g = Math.round(gTotal / pixelCount);
                        const b = Math.round(bTotal / pixelCount);

                        colorData.push({ r, g, b });
                    }
                    return colorData;
                };

                // --- 4. Obliczanie pozycji próbkowania na Canvas ---

                // Próbkowanie na LEWEJ KRAWĘDZI (wewnątrz 5px marginesu widocznego obszaru)
                const C_SampleX_Left_Start = C_VisibleX_Start + 5;

                // Próbkowanie na PRAWEJ KRAWĘDZI (5px przed końcem widocznego obszaru)
                const C_VisibleX_End = C_VisibleX_Start + DISPLAY_WIDTH / Scale;
                const C_SampleX_Right_Start = C_VisibleX_End - SAMPLING_AREA_WIDTH - 5;

                const leftColorData = getColorsDataFromArea(C_SampleX_Left_Start);
                const rightColorData = getColorsDataFromArea(C_SampleX_Right_Start);


                // Funkcja pomocnicza do generowania stylu gradientu
                const generateStyle = (colorData) => {
                    const stops = colorData.map((data, index) => {
                        // Pozycja w kontenerze (gdzie ma się znaleźć ten kolor)
                        const positionPercent = (index / (colorData.length - 1)) * (gradientHeightPercentage * 100);

                        // ODWRÓCONY WSPÓŁCZYNNIK ALPHA (Krycie):
                        const linearAlpha = (colorData.length - 1 - index) / (colorData.length - 1);
                        const alpha = Math.pow(linearAlpha, 2);

                        const rgbaColor = toRgba(data.r, data.g, data.b, alpha);

                        return `${rgbaColor} ${positionPercent.toFixed(1)}%`;
                    });

                    const finalGradientStops = stops.join(', ');

                    // Dynamiczny gradient zanikający do dołu (GÓRNA WARSTWA)
                    const dynamicGradient = `linear-gradient(to bottom, ${finalGradientStops}, transparent ${(gradientHeightPercentage * 100).toFixed(1)}%, transparent 100%)`;

                    // Łączymy dynamiczny gradient z gradientem nakładki białej
                    return `${dynamicGradient}, ${WHITE_OVERLAY_GRADIENT}`;
                }

                // Ustawianie stylów
                leftGradientStyle.value = generateStyle(leftColorData);
                rightGradientStyle.value = generateStyle(rightColorData);

                status.value = 'Gotowe';

            } catch (error) {
                console.error("Błąd generowania gradientu:", error);
                status.value = `Wystąpił błąd: ${error.message}`;
            }
        };

        // Monitorowanie stanu załadowania obrazu
        watch(imageLoaded, (isLoaded) => {
            if (isLoaded) {
                generateGradient();
            }
        });

        // Obsługa błędu ładowania obrazu
        const handleImageError = () => {
            status.value = 'Błąd: Nie udało się załadować obrazu. Sprawdź, czy URL jest poprawny i czy obsługuje CORS.';
        };
</script>

<template>


        <!-- Kontener główny - wysokość 450px i układ flex (3 kolumny: 1/3, 1/3, 1/3) -->
        <div
            class="flex w-full mt-[56px]  overflow-hidden"
            style="height: 450px;"
        >

            <!-- 1. Kontener Lewego Gradientu (1/3 szerokości) -->
            <div class="flex-grow flex items-center justify-center relative ">
                <div v-if="leftGradientStyle"
                    class="w-full h-full"
                    :style="{ background: leftGradientStyle }"
                />
                <p v-else class="text-gray-500 font-semibold">{{ status }}</p>
            </div>

            <!-- 2. Kontener Obrazu (1/3 szerokości) -->
            <div class="w-[1250px] relative  flex items-center justify-center">

                <!-- Wyświetlanie głównego obrazu -->
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

            <!-- 3. Kontener Prawego Gradientu (1/3 szerokości) -->
            <div class="flex-grow flex items-center justify-center relative">
                <div v-if="rightGradientStyle"
                    class="w-full h-full"
                    :style="{ background: rightGradientStyle }"
                />
                <p v-else class="text-gray-500 font-semibold">{{ status }}</p>
            </div>
        </div>

</template>
