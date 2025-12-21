<template>
  <div class="flex h-screen w-full bg-white overflow-hidden font-sans text-gray-900">

    <MessageMenu />

    <div class="flex-1 flex overflow-hidden bg-gray-200 relative p-3 gap-3">

      <div class="flex-1 flex flex-col min-w-0 relative bg-white rounded-xl shadow-sm ">
        <MessageBox ref="messageBoxRef" :boxId="chatId" mode="full" />
      </div>

      <div v-if="showInfoPanel" class="w-80 min-w-[450px] overflow-auto flex flex-col bg-white h-full rounded-xl shadow-sm ">

          <div ref="wrapperRef" class="transition-wrapper bg-white h-full">
            <transition :name="transitionName" mode="out-in" @after-enter="updateHeight">
              <div :key="panelView">
                <div v-if="panelView === 'home'" data-view class="h-full flex flex-col overflow-y-auto custom-scrollbar">

            <div class="pt-8 pb-4 flex flex-col items-center">
          <div class="relative mb-3 hover:opacity-90 cursor-pointer transition">
            <img :src="chatMeta.avatarUrl || 'https://i.pravatar.cc/150?img=12'" class="w-20 h-20 rounded-full object-cover shadow-sm" alt="Group Avatar">
          </div>
          <h2 class="text-lg font-bold text-gray-900 hover:underline cursor-pointer tracking-tight">{{ chatMeta.name }}</h2>
              <div class="flex mt-4 space-x-12 w-full justify-center px-4">
                <div class="flex flex-col items-center cursor-pointer group">
                  <div class="w-9 h-9 bg-gray-100 rounded-full flex items-center justify-center group-hover:bg-gray-200 transition mb-2">
                    <BellIcon :size="20" class="text-black" />
                  </div>
                  <span class="text-[12px] leading-tight font-medium text-gray-900 text-center">Wyciszono</span>
                </div>
                <div class="flex flex-col items-center cursor-pointer group" @click.stop="openSearchPanel">
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
                      <div @click="openRenameModal" class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <PencilIcon :size="20" class="text-black mr-3" />
                         <span class="text-[14px] font-medium text-gray-900">Zmień nazwę czatu</span>
                      </div>
                      <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2">
                         <ImageIcon :size="20" class="text-black mr-3" />
                         <span class="text-[14px] font-medium text-gray-900">Zmień zdjęcie</span>
                      </div>
               <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2"
                  @click="openThemeModal">
                 <div class="w-5 h-5 mr-3 rounded-full bg-gradient-to-br from-red-400 to-pink-600 relative flex items-center justify-center">
                   <div class="w-2 h-2 bg-black/20 rounded-full"></div>
                 </div>
                 <span class="text-[14px] font-medium text-gray-900">Zmień motyw</span>
               </div>
               <div class="px-4 py-2.5 flex items-center hover:bg-gray-100 cursor-pointer transition rounded-md mx-2"
                  @click="openEmojiModal">
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

              <ChatMediaPanel v-else-if="panelView === 'media'" data-view @close="panelView = 'home'" />
              <MessageSearch v-else-if="panelView === 'search'" data-view :boxId="chatId" @close="panelView = 'home'" @go-to-message="onSearchGoTo" />
            </div>
          </transition>
        </div>
      </div>

    </div>

    <BaseModal v-if="showRenameModal" title="Zmień nazwę czatu" @close="closeRenameModal">
      <div class="px-4 py-3">
        <label class="block text-sm font-medium text-gray-700 mb-2">Nazwa czatu</label>
        <input v-model="renameInput" type="text" class="w-full px-3 py-2 border border-gray-200 rounded-md focus:outline-none focus:ring-1 focus:ring-indigo-500" />
        <div class="mt-3 flex justify-end space-x-2">
          <button @click="closeRenameModal" class="px-3 py-2 rounded-md bg-gray-100 hover:bg-gray-200">Anuluj</button>
          <button @click="saveRename" class="px-3 py-2 rounded-md bg-indigo-600 text-white hover:bg-indigo-700">Zapisz</button>
        </div>
      </div>
    </BaseModal>
    <BaseModal v-if="showThemeModal" title="Wybierz motyw czatu" @close="closeThemeModalAndSave">
      <MessangerTheme @apply="closeThemeModalAndSave" />
    </BaseModal>
    <!-- Emoji Modal -->
    <BaseModal v-if="showEmojiModal" title="Wybierz ikonę emoji" @close="closeEmojiModal">
      <div class="flex items-center justify-between px-4 pt-2 pb-3 border-b border-gray-100">
        <div class="text-2xl">{{ convStore.selectedEmoji || '👍' }}</div>
        <button @click="closeEmojiModal" class="text-gray-500 hover:text-gray-700 text-xl leading-none">✕</button>
      </div>
      <LazyEmojiPicker @select="onEmojiSelect" />
    </BaseModal>
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
import MessageSearch from '@/components/MessageSearch.vue';
import MessageMenu from '@/components/MessageMenu.vue';
import MessageBox from '@/components/MessageBox.vue';
import { useConversationsStore } from '@/stores/conversations';
import { useRoute } from 'vue-router';
// chatSettings moved into conversations store (convStore.settings)
// use conversations store directly for theme/emoji

// accept optional route prop chatId so this view can render messages for a given chat
const routeProps = withDefaults(defineProps<{ chatId?: string | number }>(), { chatId: undefined });
const route = useRoute();
const chatId = computed(() => Number(route.params.chatId ?? routeProps.chatId ?? ''));
const convStore = useConversationsStore();


// chat metadata (name/avatar) shown in the right panel and header
const chatMeta = computed(() => convStore.chats.find(c => c.id === chatId.value) ?? { id: chatId.value, name: `Czat ${chatId.value}`, avatarUrl: '', lastMessage: '', timeAgo: '', unread: false, isActive: false });

// apply chat-specific theme and emoji when chat changes
watch(chatId, (newId) => {
  const s = convStore.settings.find(x => x.chatId === Number(newId));
  if (s?.themeId !== undefined) {
    // chatSettings.themeId stores a numeric index; map it to the real theme id string
    const idx = Number(s.themeId);
    const themesArr = convStore.themes as { id: string }[] | undefined;
    const mappedId = themesArr && themesArr[idx]?.id ? themesArr[idx].id : (themesArr && themesArr[0]?.id) ?? String(s.themeId);
    convStore.setSelectedTheme(mappedId);
  }
  if (s?.emoji) convStore.setSelectedEmoji(s.emoji);
});

import BaseModal from '@/components/BaseModal.vue';
import MessangerTheme from '@/components/MessangerTheme.vue';
import LazyEmojiPicker from '@/components/LazyEmojiPicker.vue';

const showInfoPanel = ref(true);

// 'home' - główny widok z akordeonem
// 'media' - widok galerii
const panelView = ref<'home' | 'media' | 'search'>('home');

const accordionState = ref({
  chatInfo: true,
  customizeChat: false,
  multimedia: false,
  privacy: false,
  chatMembers: false,
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
const messageBoxRef = ref<InstanceType<typeof MessageBox> | null>(null);

const previousPanelView = ref<'home' | 'media'|'search'|''>(panelView.value);

function openSearchPanel() {
  panelView.value = 'search';
}

function onSearchGoTo(payload: { id: number; chatId?: string | number }) {
  // close right panel and scroll to message in MessageBox
  panelView.value = 'home';
  nextTick(() => {
    try {
      messageBoxRef.value?.scrollToMessage(payload.id);
    } catch {
      // ignore errors
    }
  });
}

// Modal state for theme selection
const showThemeModal = ref(false);
const openThemeModal = () => { showThemeModal.value = true; };
// persist selected theme for this chat when closing modal
const closeThemeModalAndSave = () => {
  try {
    // save current selected theme id as numeric index in chat settings
    convStore.setChatThemeById(chatId.value, convStore.selectedThemeId as string);
  } catch {
    // noop
  }
  showThemeModal.value = false;
};

// Emoji picker modal
const showEmojiModal = ref(false);
const openEmojiModal = () => { showEmojiModal.value = true; };
const closeEmojiModal = () => { showEmojiModal.value = false; };

const onEmojiSelect = (e: { native: string }) => {
  // persist per-chat emoji and update global selected emoji
  const emoji = e.native;
  try {
    convStore.setChatEmoji(chatId.value, emoji);
  } catch {
    // ignore if store not available
  }
  convStore.setSelectedEmoji(emoji);
  closeEmojiModal();
}

// Rename chat modal
const showRenameModal = ref(false);
const renameInput = ref('');
function openRenameModal() {
  renameInput.value = chatMeta.value?.name ?? `Czat ${chatId.value}`;
  showRenameModal.value = true;
}
function closeRenameModal() {
  showRenameModal.value = false;
}
function saveRename() {
  const trimmed = String(renameInput.value ?? '').trim();
  if (trimmed) {
    convStore.updateChatName(chatId.value, trimmed);
  }
  closeRenameModal();
}

const transitionName = computed(() => {
  // assign an order so direction can be determined programmatically
  const order: Record<string, number> = { home: 0, media: 1, search: 2 };
  const prev = previousPanelView.value || 'home';
  const curr = panelView.value || 'home';
  const prevIdx = order[prev] ?? 0;
  const currIdx = order[curr] ?? 0;

  return currIdx >= prevIdx ? 'slide-left' : 'slide-right';
});

watch(panelView, (newVal, oldVal) => {
  // remember the old view (allow all three states)
  previousPanelView.value = (oldVal as 'home' | 'media' | 'search' | '') ?? '';
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
