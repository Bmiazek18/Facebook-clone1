import { ref } from 'vue';
import type { Message } from '@/types/Message';

export interface AudioState {
  isPlaying: boolean;
  duration: number;
  currentTime: number;
}


const audioStates = ref<Record<number, AudioState>>({});
const currentActiveId = ref<number | null>(null);

export function useAudioPlayer(boxId?: string | number) {

  const seekAudio = (message: Message, seekTime: number) => {
    const audioId = message.id;
    const domId = `audio-${boxId ?? '0'}-${audioId}`;
    const audioElement = document.getElementById(domId) as HTMLAudioElement | null;

    if (audioElement) {
      audioElement.currentTime = seekTime;
      if (!audioStates.value[audioId]) {
        audioStates.value[audioId] = {
          isPlaying: false,
          duration: message.duration || audioElement.duration || 0,
          currentTime: seekTime
        };
      } else {
        audioStates.value[audioId].currentTime = seekTime;
      }
    }
  };

  const toggleAudioPlayback = (message: Message) => {
    const audioId = message.id;
    const domId = `audio-${boxId ?? '0'}-${audioId}`;
    const audioElement = document.getElementById(domId) as HTMLAudioElement | null;

    if (!audioElement) return;

    // 1. ZATRZYMANIE POPRZEDNIEGO AUDIO
    // Jeśli gra cokolwiek innego, znajdź to i zatrzymaj
    if (currentActiveId.value !== null && currentActiveId.value !== audioId) {
      const prevAudioId = currentActiveId.value;

      // Szukamy elementu w DOM (zakładając ten sam boxId lub musiałbyś go też trzymać w stanie)
      const prevDomId = `audio-${boxId ?? '0'}-${prevAudioId}`;
      const prevAudioElement = document.getElementById(prevDomId) as HTMLAudioElement;

      if (prevAudioElement) {
        prevAudioElement.pause();
        // Nie resetujemy czasu do 0, chyba że chcesz, by zawsze zaczynało od nowa
      }

      if (audioStates.value[prevAudioId]) {
        audioStates.value[prevAudioId].isPlaying = false;
      }
    }

    // 2. INICJALIZACJA STANU DLA NOWEGO ID
    if (!audioStates.value[audioId]) {
      audioStates.value[audioId] = {
        isPlaying: false,
        duration: message.duration || audioElement.duration || 0,
        currentTime: 0
      };
    }

    const state = audioStates.value[audioId];

    // 3. LOGIKA PLAY / PAUSE
    if (state.isPlaying) {
      audioElement.pause();
      state.isPlaying = false;
      currentActiveId.value = null; // Nic już nie gra
    } else {
      audioElement.play().catch(console.error);
      state.isPlaying = true;
      currentActiveId.value = audioId; // Ustawiamy jako aktywne

      if (!audioElement.dataset.listenersAttached) {
        audioElement.ontimeupdate = () => {
          state.currentTime = audioElement.currentTime;
        };
        audioElement.onended = () => {
          state.isPlaying = false;
          state.currentTime = 0;
          if (currentActiveId.value === audioId) {
            currentActiveId.value = null;
          }
        };
        audioElement.dataset.listenersAttached = 'true';
      }
    }
  };

  return {
    audioStates,
    currentActiveId,
    toggleAudioPlayback,
    seekAudio
  }}
