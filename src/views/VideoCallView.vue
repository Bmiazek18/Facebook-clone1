<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';

// --- Ikony ---
import Microphone from 'vue-material-design-icons/Microphone.vue';
import MicrophoneOff from 'vue-material-design-icons/MicrophoneOff.vue';
import Video from 'vue-material-design-icons/Video.vue';
import VideoOff from 'vue-material-design-icons/VideoOff.vue';
import PhoneHangup from 'vue-material-design-icons/PhoneHangup.vue';
import MonitorShare from 'vue-material-design-icons/MonitorShare.vue';
import AccountPlus from 'vue-material-design-icons/AccountPlus.vue';
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';

// --- Typy danych ---
interface Participant {
  id: number;
  name: string;
  avatar: string;
  isMuted: boolean;
  isSpeaking: boolean;
  isCameraOn: boolean;
}

// --- Stan Użytkownika (Lokalny) ---
const isMicMuted = ref(false);
const isCameraOff = ref(false);
const isScreenSharing = ref(false);

// --- Flaga inicjalizacji pozycji ---
// Dzięki temu wiemy, czy komponent już obliczył pozycję startową
const isInitialized = ref(false);

// --- Stan Grupy (Remote) ---
const participants = ref<Participant[]>([
  { id: 1, name: 'Anna Nowak', avatar: 'https://i.pravatar.cc/300?img=5', isMuted: false, isSpeaking: true, isCameraOn: true },
  { id: 2, name: 'Piotr Kowalski', avatar: 'https://i.pravatar.cc/300?img=11', isMuted: true, isSpeaking: false, isCameraOn: false },
  { id: 3, name: 'Marta Wiśniewska', avatar: 'https://i.pravatar.cc/300?img=9', isMuted: false, isSpeaking: false, isCameraOn: true },
  { id: 4, name: 'Tomek Zieliński', avatar: 'https://i.pravatar.cc/300?img=3', isMuted: false, isSpeaking: true, isCameraOn: false },
]);

// --- Logika Siatki (Dynamic Grid Layout) ---
const gridLayoutClass = computed(() => {
  const count = participants.value.length;
  if (count === 1) return 'grid-cols-1';
  if (count === 2) return 'grid-cols-1 md:grid-cols-2';
  if (count <= 4) return 'grid-cols-2';
  if (count <= 6) return 'grid-cols-2 md:grid-cols-3';
  return 'grid-cols-3 md:grid-cols-4';
});

// --- Logika Drag & Drop (Twoja kamera) ---
const draggableRef = ref<HTMLElement | null>(null);
const isDragging = ref(false);
const position = reactive({ x: 0, y: 0 });
const dragOffset = reactive({ x: 0, y: 0 });

const startDrag = (event: MouseEvent) => {
  if (!draggableRef.value) return;
  isDragging.value = true;
  const rect = draggableRef.value.getBoundingClientRect();
  dragOffset.x = event.clientX - rect.left;
  dragOffset.y = event.clientY - rect.top;

  // Jeśli użytkownik zaczyna ciągnąć, upewnij się, że mamy przejętą kontrolę nad pozycją (left/top)
  isInitialized.value = true;

  window.addEventListener('mousemove', onDrag);
  window.addEventListener('mouseup', stopDrag);
};

const onDrag = (event: MouseEvent) => {
  if (!isDragging.value || !draggableRef.value) return;
  const newX = event.clientX - dragOffset.x;
  const newY = event.clientY - dragOffset.y;

  const maxX = window.innerWidth - draggableRef.value.offsetWidth;
  const maxY = window.innerHeight - draggableRef.value.offsetHeight;

  position.x = Math.max(0, Math.min(newX, maxX));
  position.y = Math.max(0, Math.min(newY, maxY));
};

const stopDrag = () => {
  isDragging.value = false;
  window.removeEventListener('mousemove', onDrag);
  window.removeEventListener('mouseup', stopDrag);
};

onMounted(() => {
  if (draggableRef.value) {
    // Obliczamy pozycję startową (odpowiadającą CSS right-6 bottom-[110px])
    // Dzięki temu po przełączeniu na absolute left/top okno nie przeskoczy
    position.x = window.innerWidth - draggableRef.value.offsetWidth - 24;
    position.y = window.innerHeight - draggableRef.value.offsetHeight - 110;

    // Ustawiamy flagę na true, aby Vue zaczęło używać style="{ left: ... }"
    // Robimy to w nextTick lub po prostu tutaj, co płynnie podmieni sterowanie z CSS na JS
    isInitialized.value = true;
  }
});

onUnmounted(() => {
  window.removeEventListener('mousemove', onDrag);
  window.removeEventListener('mouseup', stopDrag);
});

// --- Handlery ---
const toggleMic = () => isMicMuted.value = !isMicMuted.value;
const toggleCamera = () => isCameraOff.value = !isCameraOff.value;
const toggleScreenShare = () => isScreenSharing.value = !isScreenSharing.value; // Dodana brakująca funkcja
const endCall = () => alert("Opuszczono grupę");
const addParticipant = () => {
    const isCamOn = Math.random() > 0.5;
    participants.value.push({
        id: Date.now(),
        name: `Osoba ${participants.value.length + 1}`,
        avatar: `https://i.pravatar.cc/300?img=${participants.value.length + 15}`,
        isMuted: false,
        isSpeaking: false,
        isCameraOn: isCamOn
    });
};

const buttonBaseClass = "rounded-full p-3 md:p-4 transition-all duration-200 flex items-center justify-center shadow-lg cursor-pointer";
const activeBtnClass = "bg-gray-700 hover:bg-gray-600 text-white";
const inactiveBtnClass = "bg-gray-600 text-white hover:bg-gray-500";
const hangupBtnClass = "bg-red-600 hover:bg-red-700 text-white px-6 md:px-8";
</script>

<template>
  <div class="flex flex-col h-screen w-full bg-gray-900 text-white overflow-hidden relative font-sans select-none">

    <header class="h-16 px-4 flex justify-between items-center bg-gray-900 border-b border-gray-800 z-20">
      <div>
        <h2 class="text-lg font-bold">Spotkanie Zespołu</h2>
        <span class="text-xs text-gray-400 flex items-center gap-1">
           <span class="w-2 h-2 rounded-full bg-green-500"></span>
           {{ participants.length + 1 }} uczestników
        </span>
      </div>
      <button @click="addParticipant" class="bg-gray-700 hover:bg-gray-600 px-3 py-1.5 rounded-md text-sm font-medium flex items-center gap-2 transition">
         <AccountPlus :size="18" />
         <span class="hidden md:inline">Dodaj</span>
      </button>
    </header>

    <main class="flex-1 p-2 md:p-4 overflow-y-auto flex items-center justify-center bg-black">

      <div :class="['grid gap-2 md:gap-4 w-full h-full max-w-6xl', gridLayoutClass]" style="max-height: 100%;">
        <div
          v-for="person in participants"
          :key="person.id"
          class="relative bg-gray-800 rounded-xl overflow-hidden shadow-lg group transition-all duration-300 flex flex-col items-center justify-center"
          :class="[
            person.isSpeaking && person.isCameraOn ? 'border-2 border-green-500' : 'border border-gray-700'
          ]"
        >
          <template v-if="person.isCameraOn">
            <img :src="person.avatar" class="w-full h-full object-cover" />
            <div class="absolute bottom-0 left-0 w-full h-20 bg-gradient-to-t from-black/80 to-transparent"></div>
          </template>

          <template v-else>
            <div class="absolute inset-0 bg-gray-800 flex items-center justify-center">
                <div class="relative">
                    <img
                        :src="person.avatar"
                        class="w-20 h-20 md:w-28 md:h-28 rounded-full object-cover shadow-xl"
                        :class="person.isSpeaking ? 'ring-4 ring-green-500 ring-offset-4 ring-offset-gray-800' : ''"
                    />
                    <div v-if="person.isSpeaking" class="absolute -bottom-1 -right-1 bg-green-500 rounded-full p-1 border-2 border-gray-800">
                        <Microphone :size="12" class="text-white" />
                    </div>
                </div>
            </div>
          </template>

          <div class="absolute bottom-3 left-3 z-10">
            <span class="text-sm font-semibold text-white text-shadow drop-shadow-md">
                {{ person.name }}
            </span>
          </div>

          <div v-if="person.isMuted" class="absolute top-3 right-3 bg-red-600/90 p-1.5 rounded-full backdrop-blur-sm z-10">
             <MicrophoneOff :size="16" />
          </div>

          <button class="absolute top-3 right-3 p-1.5 rounded-full bg-black/40 hover:bg-black/60 opacity-0 group-hover:opacity-100 transition text-white z-10" v-if="!person.isMuted">
             <DotsHorizontal :size="20" />
          </button>
        </div>
      </div>
    </main>

    <div
      ref="draggableRef"
      @mousedown="startDrag"
      :style="isInitialized ? { left: position.x + 'px', top: position.y + 'px' } : {}"
      class="absolute w-32 h-48 md:w-48 md:h-32 bg-gray-900 rounded-lg overflow-hidden border border-gray-600 shadow-2xl z-50 cursor-move right-6 bottom-[110px]"
      :class="{ 'transition-none': isDragging, 'transition-all duration-300': !isDragging }"
    >
      <template v-if="!isCameraOff">
         <img src="https://i.pravatar.cc/300?img=12" class="w-full h-full object-cover pointer-events-none transform -scale-x-100" />
         <span class="absolute bottom-1 left-2 text-xs font-medium text-gray-300 bg-black/50 px-1 rounded">Ty</span>
      </template>
      <template v-else>
         <div class="w-full h-full flex flex-col items-center justify-center bg-gray-800 pointer-events-none">
            <img src="https://i.pravatar.cc/300?img=12" class="w-16 h-16 rounded-full opacity-70 mb-2" />
            <span class="text-xs text-gray-400">Kamera wył.</span>
         </div>
      </template>

      <div v-if="isMicMuted" class="absolute top-2 right-2 bg-red-600 p-1 rounded-full pointer-events-none">
          <MicrophoneOff :size="12" />
      </div>
    </div>

    <footer class="h-20 bg-gray-900 border-t border-gray-800 flex items-center justify-center gap-3 md:gap-5 z-40 px-4">
      <button @click="toggleMic" :class="[buttonBaseClass, isMicMuted ? activeBtnClass : inactiveBtnClass]">
        <component :is="isMicMuted ? MicrophoneOff : Microphone" :size="24" />
      </button>
      <button @click="toggleCamera" :class="[buttonBaseClass, isCameraOff ? activeBtnClass : inactiveBtnClass]">
        <component :is="isCameraOff ? VideoOff : Video" :size="24" />
      </button>
      <button @click="endCall" :class="[buttonBaseClass, hangupBtnClass]">
        <PhoneHangup :size="28" />
      </button>
      <button @click="toggleScreenShare" :class="[buttonBaseClass, isScreenSharing ? 'bg-blue-600 text-white' : inactiveBtnClass]">
        <MonitorShare :size="24" />
      </button>
    </footer>

  </div>
</template>

<style scoped>
.text-shadow { text-shadow: 0 1px 2px rgba(0,0,0,0.8); }
</style>
