import { ref, computed } from 'vue';
import type { Message, ImageMessage, VideoMessage } from '@/types/Message';

export interface MediaItem {
    id: number;
    sender: string;
    type: 'image' | 'gif' | 'video';
    content: string;
    time: number;
    imageUrl?: string;
    videoUrl?: string;
}

export function useLightbox(messagesList: globalThis.Ref<Message[]>) {
    const isLightboxOpen = ref(false);
    const currentMediaIndex = ref(0);

    const filteredMedia = computed<MediaItem[]>(() => {
        const result: MediaItem[] = [];

        for (const msg of messagesList.value) {
            if (msg.type === 'video') {
                result.push({ id: msg.id, sender: msg.sender, type: 'video', content: msg.content, time: msg.time, videoUrl: (msg as VideoMessage).videoUrl });
            } else if (msg.type === 'image' || msg.type === 'gif') {
                const imgMsg = msg as ImageMessage & { mediaUrls?: string[] };
                if (imgMsg.mediaUrls?.length) {
                    imgMsg.mediaUrls.forEach(url => result.push({ id: imgMsg.id, sender: imgMsg.sender, type: imgMsg.type, content: imgMsg.content, time: imgMsg.time, imageUrl: url }));
                } else if (imgMsg.imageUrl) {
                    result.push({ id: imgMsg.id, sender: img.sender, type: imgMsg.type, content: imgMsg.content, time: imgMsg.time, imageUrl: imgMsg.imageUrl });
                }
            }
        }
        return result;
    });

    const openLightbox = (url: string) => {
        const idx = filteredMedia.value.findIndex(m => m.videoUrl === url || m.imageUrl === url);
        if (idx !== -1) {
            currentMediaIndex.value = idx;
            isLightboxOpen.value = true;
        }
    };

    return {
        isLightboxOpen,
        currentMediaIndex,
        filteredMedia,
        openLightbox
    };
}
