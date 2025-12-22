import { ref, reactive } from 'vue';
import type { StoryElement as StoryElementType, BackgroundSettings } from '@/types/StoryElement';
import { calculateSnaps, type Guide } from '@/utils/snapping';

export function useStoryElementInteraction(storyElements: ref<StoryElementType[]>, bgDimensions: { width: number, height: number }) {
    const activeDragId = ref<string | null>(null);
    const activeResizeId = ref<string | null>(null);
    const activeRotateId = ref<string | null>(null);
    const editingId = ref<string | null>(null);
    const croppingId = ref<string | null>(null);
    const dragStart = reactive({ x: 0, y: 0 });
    const elementStart = reactive({ x: 0, y: 0, w: 0, h: 0, rotation: 0, cropX: 0, cropY: 0 });
    const activeGuides = ref<Guide[]>([]);
    const SNAP_THRESHOLD = 12;
    const selectedElementId = ref<string | null>(null);


    const startDrag = (event: MouseEvent, element: StoryElementType) => {
        selectedElementId.value = element.id;
        if (editingId.value === element.id || activeResizeId.value || activeRotateId.value) return;

        if (croppingId.value === element.id && element.type === 'image') {
            activeDragId.value = 'CROP_MOVE';
            dragStart.x = event.clientX; dragStart.y = event.clientY;
            elementStart.cropX = element.cropX || 0; elementStart.cropY = element.cropY || 0;
        } else {
            if (croppingId.value) return;
            activeDragId.value = element.id;
            dragStart.x = event.clientX; dragStart.y = event.clientY;
            elementStart.x = element.x; elementStart.y = element.y;
            const target = event.currentTarget as HTMLElement;
            elementStart.w = target.offsetWidth; elementStart.h = target.offsetHeight;
            element.width = target.offsetWidth;
            element.height = target.offsetHeight;
        }
        window.addEventListener('mousemove', onMouseMove);
        window.addEventListener('mouseup', stopInteraction);
    };

    const startResize = (event: MouseEvent, element: StoryElementType) => {
        event.stopPropagation(); event.preventDefault();
        activeResizeId.value = element.id; selectedElementId.value = element.id;
        dragStart.x = event.clientX;
        elementStart.w = element.width || 200;
        elementStart.h = element.height || 200;
        window.addEventListener('mousemove', onMouseMove);
        window.addEventListener('mouseup', stopInteraction);
    };

    const onMouseMove = (event: MouseEvent) => {
        if (activeDragId.value === 'CROP_MOVE' && croppingId.value) {
            const element = storyElements.value.find((el: StoryElementType) => el.id === croppingId.value);
            if (element && element.type === 'image') {
                element.cropX = elementStart.cropX + (event.clientX - dragStart.x);
                element.cropY = elementStart.cropY + (event.clientY - dragStart.y);
            }
            return;
        }

        if (activeDragId.value && activeDragId.value !== 'CROP_MOVE') {
            const element = storyElements.value.find((el: StoryElementType) => el.id === activeDragId.value);
            if (element) {
                let newX = elementStart.x + (event.clientX - dragStart.x);
                let newY = elementStart.y + (event.clientY - dragStart.y);

                const elementWithDimensions = {
                    ...element,
                    width: elementStart.w || element.width || 100,
                    height: elementStart.h || element.height || 100,
                };
                const { snappedX, snappedY, guides } = calculateSnaps(
                    elementWithDimensions, newX, newY,
                    storyElements.value.map(el => ({ ...el, width: el.width || 100, height: el.height || 100 })),
                    { threshold: SNAP_THRESHOLD, canvasWidth: bgDimensions.width, canvasHeight: bgDimensions.height }
                );
                activeGuides.value = guides;

                // If element is text or music image, apply specific clamping, otherwise apply snapping
                if (element.type === 'text' || (element.type === 'image' && element.musicArtist)) {
                    const mainImage = storyElements.value.find(el => el.id === 'main-image');
                    if (mainImage) {
                        // Clamp horizontally to main image boundaries
                        const minX = mainImage.x;
                        const maxX = mainImage.x + mainImage.width - elementStart.w;
                        newX = Math.max(minX, Math.min(newX, maxX));

                        // Clamp vertically to above main image boundaries
                        const minY = 0;
                        const maxY = Math.max(minY, mainImage.y - elementStart.h); // Ensure maxY is not negative
                        newY = Math.max(minY, Math.min(newY, maxY));
                    } else {
                         // Fallback to clamping within bgDimensions if main image is not found
                        if (newX < 0) newX = 0;
                        else if (newX + elementStart.w > bgDimensions.width) newX = bgDimensions.width - elementStart.w;
                        if (newY < 0) newY = 0;
                        else if (newY + elementStart.h > bgDimensions.height) newY = bgDimensions.height - elementStart.h;
                    }
                } else { // For non-text elements, apply snapping
                    newX = snappedX;
                    newY = snappedY;
                }
                element.x = newX; element.y = newY;
            }
        }

        if (activeResizeId.value) {
            const element = storyElements.value.find((el: StoryElementType) => el.id === activeResizeId.value);
            if (element) {
                const newSize = Math.max(50, elementStart.w + (event.clientX - dragStart.x));
                element.width = newSize;
                if (element.height) element.height = newSize;
                element.scale = 1;
            }
        }

        if (activeRotateId.value) {
            const element = storyElements.value.find((el: StoryElementType) => el.id === activeRotateId.value);
            if (element) {
                const angleRad = Math.atan2(event.clientY - dragStart.y, event.clientX - dragStart.x);
                element.rotation = (angleRad * 180 / Math.PI) + 90;
            }
        }
    };

    const stopInteraction = () => {
        activeDragId.value = null;
        activeResizeId.value = null;
        activeRotateId.value = null;
        activeGuides.value = [];
        window.removeEventListener('mousemove', onMouseMove);
        window.removeEventListener('mouseup', stopInteraction);
    };

    const enableEdit = (id: string) => { editingId.value = id; activeDragId.value = null; };
    const disableEdit = () => { editingId.value = null; };
    const onBackgroundClick = () => { disableEdit(); croppingId.value = null; selectedElementId.value = null; };
    const toggleCrop = (id: string) => {
        if (croppingId.value === id) croppingId.value = null;
        else { croppingId.value = id; editingId.value = null; selectedElementId.value = id; }
    };

    const rotateElement90 = () => {
        const selectedElement = storyElements.value.find((el: StoryElementType) => el.id === selectedElementId.value);
        if (selectedElement) selectedElement.rotation = (selectedElement.rotation + 90) % 360;
    };


    return {
        activeDragId,
        activeResizeId,
        activeRotateId,
        editingId,
        croppingId,
        dragStart,
        elementStart,
        activeGuides,
        selectedElementId,
        startDrag,
        startResize,
        onMouseMove,
        stopInteraction,
        enableEdit,
        disableEdit,
        onBackgroundClick,
        toggleCrop,
        rotateElement90
    };
}
