<template>
  <div class="flex h-screen w-full bg-white overflow-hidden font-sans text-gray-900">

    <MessageMenu />

    <div class="flex-1 flex overflow-hidden bg-gray-200 relative p-3 gap-3">

      <div class="flex-1 flex flex-col min-w-0 relative bg-white rounded-xl shadow-sm ">
        <MessageBox boxId="1" mode="full" />
      </div>

      <div v-if="showInfoPanel" class="w-80 min-w-[450px] overflow-auto flex flex-col bg-white h-full rounded-xl shadow-sm ">

          <div ref="wrapperRef" class="transition-wrapper bg-white h-full">
            <transition :name="transitionName" mode="out-in" @after-enter="updateHeight">
              <div :key="panelView">
                <div v-if="panelView === 'home'" data-view class="h-full flex flex-col overflow-y-auto custom-scrollbar">

            <div class="pt-8 pb-4 flex flex-col items-center">
              <div class="relative mb-3 hover:opacity-90 cursor-pointer transition">
                 <img src="https://i.pravatar.cc/150?img=12" class="w-20 h-20 rounded-full object-cover shadow-sm" alt="Group Avatar">
              </div>
              <h2 class="text-lg font-bold text-gray-900 hover:underline cursor-pointer tracking-tight">Infa 2025</h2>
              <div class="flex mt-4 space-x-12 w-full justify-center px-4">
                <div class="flex flex-col items-center cursor-pointer group">
                  <div class="w-9 h-9 bg-gray-100 rounded-full flex items-center justify-center group-hover:bg-gray-200 transition mb-2">
                    <BellIcon :size="20" class="text-black" />
                  </div>
                  <span class="text-[12px] leading-tight font-medium text-gray-900 text-center">Wyciszono</span>
                </div>
                <div class="flex flex-col items-center cursor-pointer group">
                  <div class="w-9 h-9 bg-gray-100 rounded-full flex items-center justify-center group-hover:bg-gray-200 transition mb-2">
                    <MagnifyIcon :size="20" class="text-black" />
                  </div>
                  <span class="text-[12px] leading-tight font-medium text-gray-900 text-center">Szukaj<br>&nbsp;</span>
                </div>
              </div>
            </div>

            <div class="mt-2">
              <button @click="toggleSection('chatInfo')" class="w-full px-4 py-3 flex justify-between items-center hover:bg-gray-50 cursor-pointer transition-colors outline-none">
                <span class="font-semibold text-[14px] text-gray-900">Informacje o czacie</span>
                <ChevronUpIcon :size="20" class="text-gray-900 transition-transform duration-300" :class="{'rotate-180': !accordionState.chatInfo}" />
              </button>
              <div class="grid transition-[grid-template-rows,opacity] duration-300" :class="accordionState.chatInfo ? 'grid-rows-[1fr] opacity-100' : 'grid-rows-[0fr] opacity-0'">
                <div class="overflow-hidden">
                  <div class="px-4 py-2 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2 mb-2">
                     <PinIcon :size="20" class="text-black mr-3" />
                     <span class="text-[14px] font-medium text-gray-900">Wyświetl przypięte wiadomości</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="mt-1">
              <button @click="toggleSection('customizeChat')" class="w-full px-4 py-3 flex justify-between items-center hover:bg-gray-50 cursor-pointer transition-colors outline-none">
                <span class="font-semibold text-[14px] text-gray-900">Dostosuj czat</span>
                 <ChevronUpIcon :size="20" class="text-gray-900 transition-transform duration-300" :class="{'rotate-180': !accordionState.customizeChat}" />
              </button>
              <div class="grid transition-[grid-template-rows,opacity] duration-300" :class="accordionState.customizeChat ? 'grid-rows-[1fr] opacity-100' : 'grid-rows-[0fr] opacity-0'">
                <div class="overflow-hidden">
                   <div class="flex flex-col space-y-0.5 pb-2 mb-2">
                      <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <PencilIcon :size="20" class="text-black mr-3" />
                         <span class="text-[14px] font-medium text-gray-900">Zmień nazwę czatu</span>
                      </div>
                      <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <ImageIcon :size="20" class="text-black mr-3" />
                         <span class="text-[14px] font-medium text-gray-900">Zmień zdjęcie</span>
                      </div>
                      <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <div class="w-5 h-5 mr-3 rounded-full bg-gradient-to-br from-red-400 to-pink-600 relative flex items-center justify-center">
                            <div class="w-2 h-2 bg-black/20 rounded-full"></div>
                         </div>
                         <span class="text-[14px] font-medium text-gray-900">Zmień motyw</span>
                      </div>
                      <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <PawIcon :size="20" class="text-[#5F4B3C] mr-3" />
                         <span class="text-[14px] font-medium text-gray-900">Zmień ikonę emoji</span>
                      </div>
                      <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <FormatLetterCaseIcon :size="20" class="text-black mr-3" />
                         <span class="text-[14px] font-medium text-gray-900">Edytuj nicki</span>
                      </div>
                   </div>
                </div>
              </div>
            </div>

            <div class="mt-1">
              <button @click="toggleSection('multimedia')" class="w-full px-4 py-3 flex justify-between items-center hover:bg-gray-50 cursor-pointer transition-colors outline-none">
                <span class="font-semibold text-[14px] text-gray-900">Multimedia, pliki i linki</span>
                 <ChevronUpIcon :size="20" class="text-gray-900 transition-transform duration-300" :class="{'rotate-180': !accordionState.multimedia}" />
              </button>
              <div class="grid transition-[grid-template-rows,opacity] duration-300" :class="accordionState.multimedia ? 'grid-rows-[1fr] opacity-100' : 'grid-rows-[0fr] opacity-0'">
                <div class="overflow-hidden">
                   <div class="flex flex-col space-y-0.5 pb-2 mb-2">
                      <div
                        @click="panelView = 'media'"
                        class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2"
                      >
                         <ImageMultipleIcon :size="20" class="text-black mr-3" />
                         <span class="text-[14px] font-medium text-gray-900">Multimedia</span>
                      </div>
                      <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <FileDocumentIcon :size="20" class="text-black mr-3" />
                         <span class="text-[14px] font-medium text-gray-900">Pliki</span>
                      </div>
                      <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <LinkVariantIcon :size="20" class="text-black mr-3" />
                         <span class="text-[14px] font-medium text-gray-900">Linki</span>
                      </div>
                   </div>
                </div>
              </div>
            </div>

            <div class="mt-1">
              <button @click="toggleSection('privacy')" class="w-full px-4 py-3 flex justify-between items-center hover:bg-gray-50 cursor-pointer transition-colors outline-none">
                <span class="font-semibold text-[14px] text-gray-900">Prywatność i pomoc</span>
                 <ChevronUpIcon :size="20" class="text-gray-900 transition-transform duration-300" :class="{'rotate-180': !accordionState.privacy}" />
              </button>
              <div class="grid transition-[grid-template-rows,opacity] duration-300" :class="accordionState.privacy ? 'grid-rows-[1fr] opacity-100' : 'grid-rows-[0fr] opacity-0'">
                <div class="overflow-hidden">
                   <div class="flex flex-col space-y-0.5 pb-2 mb-2">
                      <div class="px-4 py-2 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <BellIcon :size="20" class="text-black mr-3" />
                         <div class="flex flex-col">
                            <span class="text-[14px] font-medium text-gray-900">Wznów powiadomienia</span>
                            <span class="text-[12px] text-gray-500">Wyłączone na czas nieokreślony</span>
                         </div>
                      </div>
                      <div class="px-4 py-2 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <EyeIcon :size="20" class="text-black mr-3" />
                         <div class="flex flex-col">
                            <span class="text-[14px] font-medium text-gray-900">Potwierdzenia odczytu</span>
                            <span class="text-[12px] text-gray-500">Wł.</span>
                         </div>
                      </div>
                      <div class="px-4 py-2 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <AlertOctagonIcon :size="20" class="text-black mr-3" />
                         <div class="flex flex-col">
                            <span class="text-[14px] font-medium text-gray-900">Zgłoś</span>
                            <span class="text-[12px] text-gray-500">Przekaż opinię i zgłoś konwersację</span>
                         </div>
                      </div>
                      <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <LogoutIcon :size="20" class="text-black mr-3" />
                         <span class="text-[14px] font-medium text-gray-900">Opuść grupę</span>
                      </div>
                   </div>
                </div>
              </div>
            </div>

            <div class="mt-1 pb-10">
              <button @click="toggleSection('chatMembers')" class="w-full px-4 py-3 flex justify-between items-center hover:bg-gray-50 cursor-pointer transition-colors outline-none">
                <span class="font-semibold text-[14px] text-gray-900">Uczestnicy czatu</span>
                 <ChevronUpIcon :size="20" class="text-gray-900 transition-transform duration-300" :class="{'rotate-180': !accordionState.chatMembers}" />
              </button>
              <div class="grid transition-[grid-template-rows,opacity] duration-300" :class="accordionState.chatMembers ? 'grid-rows-[1fr] opacity-100' : 'grid-rows-[0fr] opacity-0'">
                 <div class="overflow-hidden">
                   <div class="flex flex-col space-y-1 mt-1 mb-2">
                       <div v-for="(member, i) in members" :key="i" class="px-3 py-2 flex items-center hover:bg-gray-100 cursor-pointer rounded-md mx-2 group">
                          <img :src="member.avatar" class="w-9 h-9 rounded-full mr-3 object-cover">
                          <div class="flex-1 min-w-0">
                             <h4 class="text-[14px] font-medium text-gray-900">{{ member.name }}</h4>
                             <p class="text-[12px] text-gray-500 truncate">{{ member.sub }}</p>
                          </div>
                          <div class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-200 transition">
                             <DotsHorizontalIcon :size="20" class="text-gray-600" />
                          </div>
                       </div>
                    </div>
                 </div>
              </div>
            </div>
              </div>

              <ChatMediaPanel v-else data-view @close="panelView = 'home'" />
            </div>
          </transition>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import '@/assets/animations/slideTransition.css';
import { ref, watch, computed, nextTick } from 'vue';
import { useSlideTransition } from '@/composables/useSlideTransition';
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue';
import BellIcon from 'vue-material-design-icons/Bell.vue';
import ChevronUpIcon from 'vue-material-design-icons/ChevronUp.vue';
import PinIcon from 'vue-material-design-icons/Pin.vue';
import PencilIcon from 'vue-material-design-icons/Pencil.vue';
import PawIcon from 'vue-material-design-icons/Paw.vue';
import FormatLetterCaseIcon from 'vue-material-design-icons/FormatLetterCase.vue';
import ImageIcon from 'vue-material-design-icons/Image.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import ImageMultipleIcon from 'vue-material-design-icons/ImageMultiple.vue';
import FileDocumentIcon from 'vue-material-design-icons/FileDocument.vue';
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue';
import EyeIcon from 'vue-material-design-icons/Eye.vue';
import AlertOctagonIcon from 'vue-material-design-icons/AlertOctagon.vue';
import LogoutIcon from 'vue-material-design-icons/Logout.vue';

// Nowa ikona powrotu
import ChatMediaPanel from '@/components/ChatMediaPanel.vue';

import MessageMenu from '@/components/MessageMenu.vue';
import MessageBox from '@/components/MessageBox.vue';

const showInfoPanel = ref(true);

// 'home' - główny widok z akordeonem
// 'media' - widok galerii
const panelView = ref<'home' | 'media'>('home');

const accordionState = ref({
  chatInfo: true,
  customizeChat: true,
  multimedia: true,
  privacy: true,
  chatMembers: true,
});

function toggleSection(key: 'chatInfo' | 'customizeChat' | 'multimedia' | 'privacy' | 'chatMembers') {
  accordionState.value[key] = !accordionState.value[key];
}

const members = ref([
  { avatar: 'https://i.pravatar.cc/150?img=4', name: 'Anna Kowalska', sub: 'Admin' },
  { avatar: 'https://i.pravatar.cc/150?img=5', name: 'Jan Nowak', sub: 'Użytkownik' },
  { avatar: 'https://i.pravatar.cc/150?img=6', name: 'Ewa Zielińska', sub: 'Moderator' },
]);

// Slide transition wrapper (handles measured height and resize)
const { wrapperRef, updateHeight } = useSlideTransition();

const previousPanelView = ref<'home' | 'media'>(panelView.value);

const transitionName = computed(() => {
  if (previousPanelView.value === 'home' && panelView.value === 'media') return 'slide-left';
  if (previousPanelView.value === 'media' && panelView.value === 'home') return 'slide-right';
  return 'slide-left';
});

watch(panelView, (newVal, oldVal) => {
  previousPanelView.value = oldVal as 'home' | 'media';
  // ensure wrapper height updates after DOM changes
  nextTick(() => updateHeight());
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 20px;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.3);
}
</style>
