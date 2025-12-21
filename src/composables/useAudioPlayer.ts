import { ref } from 'vue';
import type { Message } from '@/types/Message';

export interface AudioState {
    isPlaying: boolean;
    duration: number;
    currentTime: number;
}

export function useAudioPlayer(boxId?: string | number) {
    const audioStates = ref<Record<number, AudioState>>({});

    const toggleAudioPlayback = (message: Message) => {
        const audioId = message.id;
        // Używamy złożonego id zgodnego z MessageItem: audio-<boxId>-<msgId>
        const domId = `audio-${boxId ?? '0'}-${audioId}`;
        const audioElement = document.getElementById(domId) as HTMLAudioElement | null;

        if (!audioElement) return;

        // Pauzowanie innych
        Object.keys(audioStates.value).forEach((key) => {
            const id = Number(key);
            if (id !== audioId && audioStates.value[id]?.isPlaying) {
                const otherAudio = document.getElementById(`audio-${id}`) as HTMLAudioElement;
                if (otherAudio) otherAudio.pause();
                audioStates.value[id].isPlaying = false;
            }
        });

        // Inicjalizacja stanu jeśli nie istnieje
        if (!audioStates.value[audioId]) {
            audioStates.value[audioId] = { isPlaying: false, duration: audioElement.duration || 0, currentTime: 0 };
        }

        const state = audioStates.value[audioId];

        if (state.isPlaying) {
            audioElement.pause();
            state.isPlaying = false;
        } else {
            audioElement.play().catch(console.error);
            state.isPlaying = true;

            // Event listenery powinny być dodawane raz, ale w tym modelu (external control) jest to trudne.
            // Zabezpieczamy przed dublowaniem
            audioElement.ontimeupdate = () => { state.currentTime = audioElement.currentTime; };
            audioElement.onended = () => { state.isPlaying = false; state.currentTime = 0; };
        }
    };

    return {
        audioStates,
        toggleAudioPlayback
    };
}
