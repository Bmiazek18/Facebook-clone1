<script setup lang="ts">
import { ref } from 'vue';

// --- IKONY ---
// Upewnij się, że masz zainstalowane: npm install vue-material-design-icons
import VideoIcon from 'vue-material-design-icons/Video.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import ViewDashboardIcon from 'vue-material-design-icons/ViewDashboard.vue';
import CogIcon from 'vue-material-design-icons/Cog.vue';
import StarIcon from 'vue-material-design-icons/Star.vue';
import WebcamIcon from 'vue-material-design-icons/Webcam.vue';
import KeyVariantIcon from 'vue-material-design-icons/KeyVariant.vue';
import CameraIcon from 'vue-material-design-icons/Camera.vue';
import MicrophoneIcon from 'vue-material-design-icons/Microphone.vue';
import MonitorShareIcon from 'vue-material-design-icons/MonitorShare.vue';
import CameraOffIcon from 'vue-material-design-icons/CameraOff.vue';
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import EmoticonIcon from 'vue-material-design-icons/Emoticon.vue';
import AccountMultipleIcon from 'vue-material-design-icons/AccountMultiple.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import HelpCircleOutlineIcon from 'vue-material-design-icons/HelpCircleOutline.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import HeartIcon from 'vue-material-design-icons/Heart.vue';
import ArrowExpandAllIcon from 'vue-material-design-icons/ArrowExpandAll.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import ClipboardTextOutlineIcon from 'vue-material-design-icons/ClipboardTextOutline.vue';
import DeleteOutlineIcon from 'vue-material-design-icons/DeleteOutline.vue';
import CheckIcon from 'vue-material-design-icons/Check.vue';
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue';
import InformationOutlineIcon from 'vue-material-design-icons/InformationOutline.vue';
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue';
import EyeIcon from 'vue-material-design-icons/Eye.vue';
import MessageTextIcon from 'vue-material-design-icons/MessageText.vue';
import ThumbUpIcon from 'vue-material-design-icons/ThumbUp.vue';
import ShareIcon from 'vue-material-design-icons/Share.vue';
import MovieOpenIcon from 'vue-material-design-icons/MovieOpen.vue';

// --- TYPY ---
type Tab = 'setup' | 'dashboard';
type VideoSource = 'webcam' | 'software';

// --- STAN ---
// Zmień na 'setup', aby zobaczyć pierwszy ekran, lub 'dashboard' dla drugiego
const activeTab = ref<Tab>('setup');
const selectedSource = ref<VideoSource>('webcam');

// Dane formularza
const title = ref('');
const description = ref('');
const shareToStory = ref(true);
const gameSearch = ref('');

// Dane ankiet
const pollQuestion = ref('');
const pollOption1 = ref('');
const pollOption2 = ref('');

// Opcje selectów
const cameraOptions = ['Wybierz źródło multimediów', 'Kamera FaceTime HD'];
const micOptions = ['Wybierz źródło multimediów', 'Mikrofon domyślny'];
</script>

<template>
  <div class="flex h-screen bg-[#F0F2F5] text-[#050505] font-sans overflow-hidden antialiased leading-normal">

    <aside class="w-[360px] bg-white shadow-[1px_0_0_0_#CED0D4] flex flex-col h-full overflow-y-auto shrink-0 z-20 custom-scrollbar">

      <div class="px-4 pt-5 pb-4 border-b border-[#CED0D4]">
        <div class="flex justify-between items-start mb-6">
          <h1 class="text-2xl font-bold leading-tight tracking-tight">Utwórz transmisję<br>wideo na żywo</h1>
          <div class="flex items-center gap-2 relative -top-1 right-1">
            <div class="w-9 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-full flex items-center justify-center cursor-pointer transition-colors">
               <HelpCircleOutlineIcon :size="24" class="text-[#050505]" />
            </div>
            <div class="w-9 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-full flex items-center justify-center cursor-pointer transition-colors">
               <CloseIcon :size="20" class="text-[#050505]" />
            </div>
          </div>
        </div>

        <div class="relative pl-4 space-y-6">
            <div class="absolute left-[8px] top-2.5 bottom-5 w-[2px] bg-[#E4E6EB] z-0"></div>
            <div class="absolute left-[8px] top-2.5 h-[75px] w-[2px] bg-[#1877F2] z-10"></div>

            <div class="relative flex items-center z-20">
                <div class="absolute -left-[11px] bg-white rounded-full p-[2px]">
                   <div class="w-3 h-3 rounded-full bg-[#1877F2]"></div>
                </div>
                <span class="text-[15px] ml-5 text-[#65676B]">Połącz źródło wideo</span>
            </div>

            <div class="relative flex items-center z-20">
                <div class="absolute -left-[14px] bg-white rounded-full p-[1px]">
                    <CheckCircleIcon class="text-[#31A24C]" :size="22" />
                </div>
                <span class="text-[15px] font-semibold ml-5 text-[#050505]">Uzupełnij szczegółowe informacje dotyczące posta</span>
            </div>

            <div class="relative flex items-center z-20">
                <div class="absolute -left-[11px] bg-white p-[2px] rounded-full">
                    <div class="w-3 h-3 rounded-full border-[2px] border-[#CED0D4] bg-white"></div>
                </div>
                <span class="text-[15px] ml-5 text-[#65676B]">Rozpocznij transmisję na żywo</span>
            </div>
        </div>
      </div>

      <div class="px-4 py-4 border-b border-[#CED0D4] flex items-center gap-3">
        <img src="https://i.pravatar.cc/150?img=68" alt="Avatar" class="w-10 h-10 rounded-full border border-[#CED0D4]">
        <div class="leading-snug">
          <div class="font-semibold text-[15px]">Bartosz Miazek</div>
          <div class="text-[13px] text-[#65676B]">Organizator — Twój profil</div>
        </div>
      </div>

      <div class="p-4 space-y-3 border-b border-[#CED0D4]">
        <div class="bg-[#F0F2F5] hover:bg-[#E4E6EB] rounded-lg px-3 py-2 flex justify-between items-center cursor-pointer transition-colors group">
            <div>
                <div class="text-[12px] text-[#65676B]">Wskaż docelową lokalizację posta</div>
                <div class="font-semibold text-[15px]">Opublikuj w profilu</div>
            </div>
            <ChevronDownIcon :size="24" class="text-[#050505]" />
        </div>
        <div class="bg-[#F0F2F5] hover:bg-[#E4E6EB] rounded-lg px-3 py-2 flex justify-between items-center cursor-pointer transition-colors group">
            <div>
                <div class="text-[12px] text-[#65676B]">Kiedy rozpoczniesz transmisję na żywo?</div>
                <div class="font-semibold text-[15px]">Teraz</div>
            </div>
            <ChevronDownIcon :size="24" class="text-[#050505]" />
        </div>
        <div class="inline-flex items-center px-3 py-1.5 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md text-[15px] font-semibold text-[#050505] cursor-pointer transition-colors">
            <EarthIcon :size="16" class="mr-2 text-[#65676B]" />
            Publiczne
        </div>
      </div>

      <nav class="flex-1 overflow-y-auto py-2 custom-scrollbar">
        <ul class="flex flex-col space-y-1 px-2">
            <li
                @click="activeTab = 'setup'"
                class="flex items-center px-2 rounded-lg cursor-pointer h-12 transition-colors font-semibold text-[15px]"
                :class="activeTab === 'setup' ? 'bg-[#E7F3FF] text-[#1877F2]' : 'hover:bg-[#F2F2F2] text-[#65676B] hover:text-[#050505]'"
            >
                <div class="w-9 h-9 flex items-center justify-center mr-3 transition-colors" :class="activeTab === 'setup' ? 'rounded-full bg-[#1877F2] text-white' : ''">
                    <VideoIcon :size="20" />
                </div>
                <span>Konfiguracja transmisji</span>
            </li>

            <li
                @click="activeTab = 'dashboard'"
                class="flex items-center px-2 rounded-lg cursor-pointer h-12 transition-colors font-medium text-[15px]"
                :class="activeTab === 'dashboard' ? 'bg-[#E7F3FF] text-[#1877F2] font-semibold' : 'hover:bg-[#F2F2F2] text-[#65676B] hover:text-[#050505]'"
            >
                <div class="w-9 h-9 flex items-center justify-center mr-3 transition-colors" :class="activeTab === 'dashboard' ? 'rounded-full bg-[#1877F2] text-white' : ''">
                    <ViewDashboardIcon :size="20" />
                </div>
                <span>Pulpit</span>
            </li>

            <li class="flex items-center justify-between px-2 rounded-lg hover:bg-[#F2F2F2] text-[#65676B] hover:text-[#050505] cursor-pointer h-12 transition-colors font-medium text-[15px]">
                <div class="flex items-center">
                    <div class="w-9 h-9 flex items-center justify-center mr-3"><CogIcon :size="24" /></div>
                    <span>Ustawienia</span>
                </div>
                <ChevronDownIcon :size="20" />
            </li>
            <li class="flex items-center justify-between px-2 rounded-lg hover:bg-[#F2F2F2] text-[#65676B] hover:text-[#050505] cursor-pointer h-12 transition-colors font-medium text-[15px]">
                <div class="flex items-center">
                    <div class="w-9 h-9 flex items-center justify-center mr-3"><StarIcon :size="24" /></div>
                    <span>Interaktywność</span>
                </div>
                <ChevronDownIcon :size="20" />
            </li>
        </ul>
      </nav>

      <div class="p-4 border-t border-[#CED0D4] flex items-center gap-3 bg-white">
         <button class="px-5 py-2 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] rounded-md font-semibold text-[15px] transition-colors">Wstecz</button>
         <button class="flex-1 px-4 py-2 bg-[#E4E6EB] text-[#BCC0C4] rounded-md font-semibold text-[15px] cursor-not-allowed">Rozpocznij transmisję na żywo</button>
      </div>
    </aside>


    <main class="flex-1 overflow-hidden relative">

        <div v-if="activeTab === 'setup'" class="h-full overflow-y-auto p-8 flex justify-center gap-6 custom-scrollbar">

            <div class="flex-1 flex flex-col gap-5 max-w-[720px] min-w-[500px]">

                <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4] p-5">
                    <h2 class="font-bold text-[17px] mb-4 flex items-center gap-2">
                        Wybierz źródło wideo
                        <div class="bg-[#1877F2] rounded-full text-white w-4 h-4 flex items-center justify-center text-xs font-bold cursor-help select-none">?</div>
                    </h2>
                    <div class="grid grid-cols-2 gap-4">
                        <div
                            @click="selectedSource = 'webcam'"
                            class="relative rounded-lg p-6 flex flex-col items-center justify-center gap-3 cursor-pointer transition-all border-[2px] h-36"
                            :class="selectedSource === 'webcam' ? 'border-[#1877F2] bg-[#E7F3FF]' : 'border-[#CED0D4] hover:bg-[#F2F2F2]'"
                        >
                            <div class="w-14 h-14 rounded-full flex items-center justify-center shadow-sm transition-colors" :class="selectedSource === 'webcam' ? 'bg-[#1877F2] text-white' : 'text-[#65676B] bg-[#E4E6EB]'">
                                <WebcamIcon :size="32" />
                            </div>
                            <span class="font-semibold text-[15px]" :class="selectedSource === 'webcam' ? 'text-[#1877F2]' : 'text-[#050505]'">Kamera internetowa</span>
                        </div>

                        <div
                            @click="selectedSource = 'software'"
                            class="relative rounded-lg p-6 flex flex-col items-center justify-center gap-3 cursor-pointer transition-all border-[2px] h-36"
                            :class="selectedSource === 'software' ? 'border-[#1877F2] bg-[#E7F3FF]' : 'border-[#CED0D4] hover:bg-[#F2F2F2]'"
                        >
                             <div class="w-14 h-14 rounded-full flex items-center justify-center transition-colors" :class="selectedSource === 'software' ? 'bg-[#1877F2] text-white' : 'text-[#65676B] bg-[#E4E6EB]'">
                                <KeyVariantIcon :size="28" />
                            </div>
                            <span class="font-semibold text-[15px] text-center leading-snug" :class="selectedSource === 'software' ? 'text-[#1877F2]' : 'text-[#050505]'">oprogramowanie do transmisji strumieniowej</span>
                        </div>
                    </div>
                </div>

                <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4] p-5 space-y-5">
                    <div class="mb-2">
                        <h2 class="font-bold text-[17px]">Elementy sterujące kamerą</h2>
                        <p class="text-[13px] text-[#65676B] mt-1">Przed rozpoczęciem transmisji na żywo sprawdź, czy wejścia kamery i mikrofonu działają prawidłowo.</p>
                    </div>

                    <div class="space-y-4">
                        <div class="relative group">
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#050505]">
                               <CameraIcon :size="22"/>
                            </div>
                            <select class="block w-full pl-11 pr-10 py-3.5 text-[15px] border border-transparent bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#1877F2] font-semibold appearance-none cursor-pointer transition-colors">
                                <option v-for="opt in cameraOptions" :key="opt">{{ opt }}</option>
                            </select>
                            <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none text-[#050505]">
                                <ChevronDownIcon :size="24"/>
                            </div>
                        </div>

                         <div class="relative group">
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#050505]">
                               <MicrophoneIcon :size="22"/>
                            </div>
                            <select class="block w-full pl-11 pr-10 py-3.5 text-[15px] border border-transparent bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#1877F2] font-semibold appearance-none cursor-pointer transition-colors">
                                <option v-for="opt in micOptions" :key="opt">{{ opt }}</option>
                            </select>
                             <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none text-[#050505]">
                                <ChevronDownIcon :size="24"/>
                            </div>
                        </div>
                    </div>

                    <button class="w-full flex items-center justify-center gap-2 py-2.5 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-lg font-semibold text-[15px] text-[#050505] transition-colors">
                        <MonitorShareIcon :size="22" />
                        Rozpocznij udostępnianie ekranu
                    </button>
                </div>

                <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4] p-5 pb-3">
                    <h2 class="font-bold text-[17px] mb-3">Film</h2>
                    <div class="relative w-full aspect-video bg-black rounded-lg flex flex-col items-center justify-center text-white p-6 text-center overflow-hidden">
                        <div class="absolute top-3 left-3 bg-[#242526]/80 backdrop-blur-sm px-2.5 py-1.5 rounded-md text-[13px] font-semibold text-[#E4E6EB] flex items-center">
                            <div class="w-2 h-2 rounded-full bg-[#65676B] mr-2"></div>
                            Pulpit
                        </div>
                        <div class="flex flex-col items-center justify-center">
                            <CameraOffIcon :size="56" class="text-[#B0B3B8] mb-4" />
                            <p class="font-bold text-[17px] text-[#E4E6EB]">Aparat nie jest obsługiwany przez obecną przeglądarkę</p>
                        </div>
                    </div>
                     <div class="flex justify-between items-center mt-3 pt-1">
                         <span class="text-[15px] font-semibold text-[#050505]">Rozwiń podgląd filmu</span>
                          <div class="w-9 h-9 flex items-center justify-center hover:bg-[#F2F2F2] rounded-full cursor-pointer transition-colors">
                            <ArrowExpandAllIcon :size="22" class="text-[#65676B]" />
                          </div>
                     </div>
                </div>
            </div>

            <div class="w-[360px] flex flex-col gap-4 shrink-0">
                 <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4] p-4">
                    <h2 class="font-bold text-[17px] mb-4 text-[#050505]">Dodaj informacje dotyczące posta</h2>

                    <div class="flex items-start gap-3 mb-5 p-1">
                         <div class="flex items-center h-6">
                            <input id="story" v-model="shareToStory" type="checkbox" class="w-5 h-5 text-[#1877F2] border-[#CED0D4] rounded focus:ring-[#1877F2] cursor-pointer">
                        </div>
                        <div class="ml-1">
                            <label for="story" class="font-semibold text-[15px] text-[#050505] cursor-pointer">Udostępnij w relacji</label>
                            <p class="text-[#65676B] text-[13px] mt-0.5 leading-snug">Transmisja wideo na żywo zostanie również dodana do relacji.</p>
                        </div>
                    </div>

                    <div class="space-y-4">
                        <input
                            v-model="title"
                            type="text"
                            placeholder="Tytuł (opcjonalnie)"
                            class="block w-full border border-[#CED0D4] rounded-md px-4 py-3.5 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] outline-none hover:border-[#8A8D91] transition-colors"
                        >
                        <div class="relative">
                            <textarea
                                v-model="description"
                                rows="4"
                                placeholder="Opis"
                                class="block w-full border border-[#CED0D4] rounded-md px-4 py-3.5 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] outline-none hover:border-[#8A8D91] transition-colors resize-none pb-12"
                            ></textarea>
                            <div class="absolute bottom-3 right-3 flex gap-3 items-center">
                                 <AccountMultipleIcon class="text-[#42B72A] cursor-pointer hover:brightness-95 transition-all" :size="24" title="Oznacz znajomych" />
                                 <MapMarkerIcon class="text-[#F02849] cursor-pointer hover:brightness-95 transition-all" :size="24" title="Zamelduj się" />
                                 <EmoticonIcon class="text-[#F7B928] cursor-pointer hover:brightness-95 transition-all" :size="24" title="Nastroj/aktywność" />
                                 <div class="w-6 h-6 bg-[#E11D48] rounded-full flex items-center justify-center text-white cursor-pointer hover:brightness-95 shadow-sm" title="Wspieraj organizacje non-profit">
                                    <HeartIcon :size="14" />
                                 </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="activeTab === 'dashboard'" class="h-full flex flex-row bg-[#F0F2F5]">

            <div class="flex-1 p-6 overflow-y-auto custom-scrollbar">
                <div class="flex gap-5 min-w-[850px] mx-auto max-w-[1200px]">

                    <div class="flex-1 flex flex-col gap-5">

                        <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                            <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                                <h2 class="font-bold text-[17px]">Film</h2>
                                <ChevronDownIcon class="text-[#65676B] cursor-pointer" :size="24" />
                            </div>
                            <div class="p-4">
                                <div class="relative w-full aspect-video bg-black rounded-lg flex flex-col items-center justify-center text-white mb-4 overflow-hidden">
                                    <div class="absolute top-3 left-3 bg-[#242526]/80 backdrop-blur-sm px-2 py-1 rounded-md text-[12px] font-semibold text-[#E4E6EB] flex items-center">
                                        <div class="w-2 h-2 rounded-full bg-[#65676B] mr-2"></div>
                                        Pulpit
                                    </div>
                                    <CameraOffIcon :size="48" class="text-[#B0B3B8] mb-3" />
                                    <p class="font-bold text-[15px] text-[#E4E6EB] text-center px-4 leading-snug">Aparat nie jest obsługiwany przez obecną przeglądarkę</p>
                                </div>
                                <div class="flex justify-between items-center border border-[#CED0D4] rounded-md px-4 py-2.5 bg-white hover:bg-[#F0F2F5] cursor-pointer transition-colors mb-4">
                                    <span class="text-[15px] font-semibold">Rozwiń podgląd filmu</span>
                                    <ArrowExpandAllIcon :size="20" class="text-[#65676B]" />
                                </div>
                                <div class="flex items-center gap-2 text-[#050505] font-semibold text-[15px] cursor-pointer hover:bg-[#F2F2F2] p-2 rounded-md -ml-2 w-max transition-colors">
                                    <div class="bg-[#E4E6EB] p-1.5 rounded-full">
                                         <ClipboardTextOutlineIcon :size="20"/>
                                    </div>
                                    Dzienniki zdarzeń
                                </div>
                            </div>
                        </div>

                        <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                             <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                                <h2 class="font-bold text-[17px]">Live activity</h2>
                                <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                            </div>
                            <div class="p-5">
                                <div class="flex gap-4 mb-6">
                                    <div class="flex-1 bg-[#F0F2F5] rounded-lg p-4 relative">
                                        <div class="flex justify-between items-start mb-1">
                                            <span class="text-[13px] font-semibold text-[#65676B]">Obecni widzowie</span>
                                            <InformationOutlineIcon :size="16" class="text-[#65676B] cursor-help" />
                                        </div>
                                        <div class="text-2xl font-bold text-[#050505]">--</div>
                                    </div>
                                     <div class="flex-1 bg-[#F0F2F5] rounded-lg p-4 relative">
                                        <div class="flex justify-between items-start mb-1">
                                            <span class="text-[13px] font-semibold text-[#65676B]">Bieżące komentarze</span>
                                            <InformationOutlineIcon :size="16" class="text-[#65676B] cursor-help" />
                                        </div>
                                        <div class="text-2xl font-bold text-[#050505]">--</div>
                                    </div>
                                </div>

                                <div class="relative h-32 border-b border-[#E4E6EB] flex items-end mb-6">
                                    <div class="w-full h-px bg-[#F0F2F5] absolute top-0"></div>
                                    <div class="w-full h-px bg-[#F0F2F5] absolute top-1/3"></div>
                                    <div class="w-full h-px bg-[#F0F2F5] absolute top-2/3"></div>

                                    <div class="absolute left-0 -bottom-6 text-[11px] text-[#65676B] font-medium">0</div>
                                    <div class="absolute right-0 -bottom-6 text-[11px] text-[#65676B] font-medium">0</div>
                                    <div class="w-full text-center -bottom-6 absolute text-[11px] text-[#65676B] font-medium">00:00</div>
                                </div>

                                <div class="flex gap-6 mt-8">
                                    <div class="flex items-center gap-2">
                                        <div class="w-3 h-3 rounded-[2px] bg-[#1877F2]"></div>
                                        <span class="text-[13px] font-medium text-[#050505]">Viewers</span>
                                    </div>
                                    <div class="flex items-center gap-2">
                                        <div class="w-3 h-3 rounded-[2px] bg-[#93C5FD]"></div>
                                        <span class="text-[13px] font-medium text-[#050505]">Comments</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                             <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                                <h2 class="font-bold text-[17px]">Statystyki</h2>
                                <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                            </div>
                            <div class="p-5 grid grid-cols-2 gap-y-5 gap-x-4">
                                <div class="flex items-center gap-3">
                                    <EyeIcon class="text-[#65676B]" :size="24"/>
                                    <div>
                                        <div class="font-bold text-[17px] leading-tight">0</div>
                                        <div class="text-[13px] text-[#65676B]">Widzowie</div>
                                    </div>
                                </div>
                                <div class="flex items-center gap-3">
                                    <MessageTextIcon class="text-[#42B72A]" :size="24"/>
                                    <div>
                                        <div class="font-bold text-[17px] leading-tight">0</div>
                                        <div class="text-[13px] text-[#65676B]">Komentarze</div>
                                    </div>
                                </div>
                                <div class="flex items-center gap-3">
                                    <ThumbUpIcon class="text-[#1877F2]" :size="24"/>
                                    <div>
                                        <div class="font-bold text-[17px] leading-tight">0</div>
                                        <div class="text-[13px] text-[#65676B]">Reakcje</div>
                                    </div>
                                </div>
                                <div class="flex items-center gap-3">
                                    <ShareIcon class="text-[#1877F2]" :size="24"/>
                                    <div>
                                        <div class="font-bold text-[17px] leading-tight">0</div>
                                        <div class="text-[13px] text-[#65676B]">Udostępnienia</div>
                                    </div>
                                </div>
                                <div class="flex items-center gap-3 col-span-2">
                                    <MovieOpenIcon class="text-[#F02849]" :size="24"/>
                                    <div>
                                        <div class="font-bold text-[17px] leading-tight">0</div>
                                        <div class="text-[13px] text-[#65676B]">Klipy</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                         <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                             <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                                <h2 class="font-bold text-[17px]">Wskaźniki transmisji strumieniowej</h2>
                                <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                            </div>
                            <div class="p-5 pb-3">
                                <div class="space-y-5 mb-6">
                                    <div class="flex justify-between items-end border-b border-[#E4E6EB] pb-2">
                                        <span class="text-[13px] text-[#65676B] w-28 text-right leading-snug">Szybkość<br>transmisji<br>wideo</span>
                                        <span class="font-bold text-[17px]">---</span>
                                    </div>
                                    <div class="flex justify-between items-end border-b border-[#E4E6EB] pb-2">
                                        <span class="text-[13px] text-[#65676B] w-28 text-right leading-snug">Klatki na<br>sekundę</span>
                                        <span class="font-bold text-[17px]">---</span>
                                    </div>
                                    <div class="flex justify-between items-end border-b border-[#E4E6EB] pb-2">
                                        <span class="text-[13px] text-[#65676B] w-28 text-right leading-snug">Szybkość<br>transmisji<br>audio</span>
                                        <span class="font-bold text-[17px]">---</span>
                                    </div>
                                </div>

                                <div class="grid grid-cols-4 gap-2 text-center text-[12px] text-[#65676B]">
                                    <div>
                                        <div class="font-bold text-[15px] text-[#050505] mb-0.5">1080p</div>
                                        <div class="leading-tight">Maksymalna rozdzielczość</div>
                                    </div>
                                     <div>
                                        <div class="font-bold text-[15px] text-[#050505] mb-0.5">8 godz.</div>
                                        <div class="leading-tight">Maksymalny czas trwania</div>
                                    </div>
                                     <div>
                                        <div class="font-bold text-[15px] text-[#050505] mb-0.5">0 x 0</div>
                                        <div class="leading-tight">Rozdzielczość filmu</div>
                                    </div>
                                     <div>
                                        <div class="font-bold text-[15px] text-[#050505] mb-0.5">---</div>
                                        <div class="leading-tight">Format audio</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                             <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                                <h2 class="font-bold text-[17px]">Zarządzaj na drugim urządzeniu</h2>
                                <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                            </div>
                            <div class="p-5">
                                <p class="text-[13px] text-[#050505] mb-4 leading-snug">Udostępnij moderatorom link do transmisji wideo na żywo. W celu łatwego dostępu zostanie ona otwarta w narzędziu Live Producer.</p>
                                <div class="flex bg-[#F0F2F5] rounded-md overflow-hidden border border-transparent hover:border-[#CED0D4] transition-colors h-10">
                                    <div class="flex-1 px-3 flex items-center text-[13px] text-[#050505] truncate select-all bg-[#F0F2F5]">
                                        https://www.facebook.com/live/producer/1584...
                                    </div>
                                    <button class="bg-[#E4E6EB] hover:bg-[#D8DADF] px-5 font-semibold text-[13px] text-[#050505] transition-colors border-l border-[#CED0D4]">Kopiuj</button>
                                </div>
                            </div>
                        </div>

                    </div>

                    <div class="flex-1 flex flex-col gap-5">

                        <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                             <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                                <h2 class="font-bold text-[17px]">Ankiety</h2>
                                <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                            </div>
                            <div class="p-5">
                                <div class="text-[15px] font-semibold mb-3">Utwórz ankietę</div>
                                <label class="block text-[13px] font-semibold text-[#050505] mb-1.5">Pytanie</label>
                                <input v-model="pollQuestion" type="text" placeholder="Pytanie" class="block w-full bg-[#F0F2F5] border-transparent rounded-md px-3 py-3 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] mb-4 hover:bg-[#E4E6EB] transition-colors">

                                <label class="block text-[13px] font-semibold text-[#050505] mb-1.5">Opcje</label>
                                <div class="space-y-2.5 mb-4">
                                    <div class="flex items-center gap-2">
                                        <div class="relative flex-1">
                                            <input v-model="pollOption1" type="text" placeholder="Opcja" class="block w-full bg-[#F0F2F5] border-transparent rounded-md px-3 py-3 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] hover:bg-[#E4E6EB] transition-colors">
                                            <CheckIcon class="absolute right-3 top-3 text-[#BCC0C4]" :size="20"/>
                                        </div>
                                        <div class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-[#F2F2F2] cursor-pointer transition-colors">
                                            <DeleteOutlineIcon class="text-[#65676B]" :size="24" />
                                        </div>
                                    </div>
                                     <div class="flex items-center gap-2">
                                        <div class="relative flex-1">
                                            <input v-model="pollOption2" type="text" placeholder="Opcja" class="block w-full bg-[#F0F2F5] border-transparent rounded-md px-3 py-3 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] hover:bg-[#E4E6EB] transition-colors">
                                            <CheckIcon class="absolute right-3 top-3 text-[#BCC0C4]" :size="20"/>
                                        </div>
                                        <div class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-[#F2F2F2] cursor-pointer transition-colors">
                                            <DeleteOutlineIcon class="text-[#65676B]" :size="24" />
                                        </div>
                                    </div>
                                </div>

                                <div class="text-[#1877F2] text-[15px] font-semibold cursor-pointer hover:bg-[#F2F2F2] mb-5 inline-flex items-center justify-center border-[2px] border-dashed border-[#CED0D4] rounded-full px-4 py-1.5 w-full text-center transition-colors">
                                    Dodaj opcję
                                </div>

                                <div class="flex gap-3 mb-3">
                                    <button class="flex-1 bg-[#E4E6EB] text-[#BCC0C4] py-2.5 rounded-md font-semibold text-[15px] cursor-not-allowed">Wyczyść</button>
                                    <button class="flex-1 bg-[#E4E6EB] text-[#BCC0C4] py-2.5 rounded-md font-semibold text-[15px] cursor-not-allowed">Zapisz</button>
                                </div>
                                <button class="w-full bg-[#E7F3FF] text-[#1877F2] py-2.5 rounded-md font-semibold text-[15px] hover:bg-[#DBE7F2] transition-colors">Utwórz ankietę</button>
                            </div>
                        </div>

                         <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                             <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                                <h2 class="font-bold text-[17px]">Informacje o poście</h2>
                                <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                            </div>
                            <div class="p-5 space-y-4">
                                <input v-model="title" type="text" placeholder="Tytuł (opcjonalnie)" class="block w-full border border-[#CED0D4] rounded-md px-4 py-3.5 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] outline-none hover:border-[#8A8D91] transition-colors">

                                <div class="relative">
                                    <textarea v-model="description" rows="3" placeholder="Napisz coś o tej transmisji wideo na żywo..." class="block w-full border border-[#CED0D4] rounded-md px-4 py-3.5 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] outline-none resize-none pb-12 hover:border-[#8A8D91] transition-colors"></textarea>
                                    <EmoticonIcon class="absolute top-3 right-3 text-[#65676B] cursor-pointer hover:text-[#050505]" :size="24"/>
                                     <div class="absolute bottom-3 right-3 flex gap-3 items-center">
                                         <AccountMultipleIcon class="text-[#42B72A] cursor-pointer hover:brightness-95" :size="24" />
                                         <MapMarkerIcon class="text-[#F02849] cursor-pointer hover:brightness-95" :size="24" />
                                         <EmoticonIcon class="text-[#F7B928] cursor-pointer hover:brightness-95" :size="24" />
                                         <div class="w-6 h-6 bg-[#E11D48] rounded-full flex items-center justify-center text-white cursor-pointer hover:brightness-95 shadow-sm"><HeartIcon :size="14" /></div>
                                    </div>
                                </div>
                                <div>
                                    <label class="block text-[13px] font-semibold text-[#050505] mb-1.5">Gra</label>
                                    <div class="relative group">
                                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#65676B] group-focus-within:text-[#1877F2]">
                                            <MagnifyIcon :size="22"/>
                                        </div>
                                        <input v-model="gameSearch" type="text" placeholder="Oznacz grę (np. Fortnite)" class="block w-full bg-[#F0F2F5] border-transparent rounded-md pl-11 pr-3 py-3 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] hover:bg-[#E4E6EB] transition-colors">
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4] h-72">
                             <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                                <h2 class="font-bold text-[17px]">Alerty</h2>
                                <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                            </div>
                            <div class="p-5 flex items-center justify-center h-full">
                                <span class="text-[#65676B] text-[15px]">Brak nowych alertów</span>
                            </div>
                        </div>

                         <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                             <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                                <h2 class="font-bold text-[17px]">Link do podglądu</h2>
                                <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                            </div>
                            <div class="p-5">
                                <p class="text-[13px] text-[#65676B] mb-3">Kliknij link poniżej, aby zobaczyć to, co widzowie</p>
                                <div class="flex items-center bg-[#F0F2F5] rounded-md p-2 border border-transparent hover:border-[#CED0D4] transition-colors cursor-pointer group">
                                    <div class="bg-[#E4E6EB] p-2 rounded-full mr-3 text-[#65676B] group-hover:bg-[#D8DADF] transition-colors">
                                         <LinkVariantIcon :size="20"/>
                                    </div>
                                    <div class="flex-1 truncate text-[13px] text-[#65676B] mr-2 leading-snug font-medium">
                                        https://www.facebook.com/bartos...
                                    </div>
                                    <button class="text-[#1877F2] font-semibold text-[15px] hover:underline px-2">Wyświetl post</button>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>

            <div class="w-[360px] bg-white border-l border-[#CED0D4] flex flex-col shrink-0 shadow-sm z-10">
                <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center bg-white">
                    <h2 class="font-bold text-[20px]">Komentarze</h2>
                    <div class="w-9 h-9 flex items-center justify-center rounded-full hover:bg-[#F2F2F2] cursor-pointer transition-colors">
                        <ArrowExpandAllIcon class="text-[#65676B]" :size="24"/>
                    </div>
                </div>
                <div class="flex-1 flex flex-col items-center justify-center text-[#65676B] p-6 text-center bg-[#F0F2F5]">
                    <p class="font-medium text-[15px]">Brak komentarzy</p>
                </div>
            </div>
        </div>

    </main>
  </div>
</template>

<style scoped>
/* Stylizacja scrollbarów, aby pasowały do designu */
.custom-scrollbar::-webkit-scrollbar { width: 8px; height: 8px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #BCC0C4; border-radius: 4px; border: 2px solid transparent; background-clip: content-box; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background-color: #A8ABAF; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }

/* Reset domyślnych stylów inputów w niektórych przeglądarkach */
input[type="text"], textarea, select {
    outline: none;
    box-shadow: none !important; /* Nadpisuje domyślny ring Tailwinda */
}

/* Ukrywa domyślną strzałkę selecta */
select {
  -webkit-appearance: none;
  -moz-appearance: none;
  text-indent: 1px;
  text-overflow: '';
}
</style>
