<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';

// --- IKONY ---
import Close from 'vue-material-design-icons/Close.vue';
import Facebook from 'vue-material-design-icons/Facebook.vue';
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue';
import Monitor from 'vue-material-design-icons/Monitor.vue';
import Cellphone from 'vue-material-design-icons/Cellphone.vue';
import Earth from 'vue-material-design-icons/Earth.vue';
import Lock from 'vue-material-design-icons/Lock.vue';
import Eye from 'vue-material-design-icons/Eye.vue';
import EyeOff from 'vue-material-design-icons/EyeOff.vue';
import AccountCircle from 'vue-material-design-icons/AccountCircle.vue';
import ImageMultiple from 'vue-material-design-icons/ImageMultiple.vue';
import AccountTag from 'vue-material-design-icons/AccountTag.vue';
import EmoticonOutline from 'vue-material-design-icons/EmoticonOutline.vue';
import Pencil from 'vue-material-design-icons/Pencil.vue';

// --- DANE I STAN ---
const groupName = ref('');
const privacy = ref('choose');   // 'choose', 'public', 'private'
const visibility = ref('visible'); // 'visible', 'hidden'
const inviteInput = ref('');
const invitedFriends = ref([]);

// Stan otwarcia dropdownów
const isPrivacyDropdownOpen = ref(false);
const isVisibilityDropdownOpen = ref(false);

// Obserwator zmian prywatności
watch(privacy, (newVal) => {
  if (newVal === 'public') {
    visibility.value = 'visible'; // Publiczna jest zawsze widoczna
  }
  isPrivacyDropdownOpen.value = false;
});

// Funkcje pomocnicze
const togglePrivacyDropdown = (e) => {
  e.stopPropagation();
  isPrivacyDropdownOpen.value = !isPrivacyDropdownOpen.value;
  isVisibilityDropdownOpen.value = false; // Zamknij drugi
};

const toggleVisibilityDropdown = (e) => {
  e.stopPropagation();
  isVisibilityDropdownOpen.value = !isVisibilityDropdownOpen.value;
  isPrivacyDropdownOpen.value = false; // Zamknij drugi
};

const selectPrivacy = (type) => {
  privacy.value = type;
  isPrivacyDropdownOpen.value = false;
};

const selectVisibility = (type) => {
  visibility.value = type;
  isVisibilityDropdownOpen.value = false;
};

const closeAllDropdowns = () => {
  isPrivacyDropdownOpen.value = false;
  isVisibilityDropdownOpen.value = false;
};

const addFriend = (name) => {
  if (!invitedFriends.value.includes(name)) invitedFriends.value.push(name);
};
const removeFriend = (name) => {
  invitedFriends.value = invitedFriends.value.filter(f => f !== name);
};

// --- COMPUTED ---
const previewName = computed(() => groupName.value.trim() || 'Nazwa grupy');
const titleColorClass = computed(() => groupName.value.trim() ? 'text-[#050505]' : 'text-[#bcc0c4]');
const subtitleText = computed(() => {
  if (privacy.value === 'public') return 'Grupa publiczna';
  if (privacy.value === 'private') return 'Grupa prywatna';
  return 'Prywatność grupy';
});
const isFormValid = computed(() => groupName.value.trim().length > 0 && privacy.value !== 'choose');

// Zasoby
const userAvatar = 'https://via.placeholder.com/40';
const coverIllustration = 'https://img.freepik.com/free-vector/people-gardening-concept-illustration_114360-647.jpg?w=1380';

</script>

<template>
  <div class="flex h-screen bg-[#F0F2F5] font-sans text-[#050505] overflow-hidden" @click="closeAllDropdowns">

    <aside class="w-[360px] bg-white flex flex-col shadow-[4px_0_24px_rgba(0,0,0,0.08)] z-20 shrink-0 h-full border-r border-[#dadde1]" @click.stop>

      <div class="h-[60px] px-4 flex items-center border-b border-[#dadde1]">
        <button class="mr-3 p-2 bg-[#F0F2F5] hover:bg-[#d8dadf] rounded-full transition-colors">
          <Close :size="20" class="text-black" />
        </button>
        <div class="text-[13px] text-[#65676B] font-medium ml-1">Grupy › Utwórz grupę</div>
      </div>

      <div class="flex-1 overflow-y-auto px-4 py-5 custom-scrollbar relative">
        <h1 class="text-[24px] font-bold leading-tight mb-4">Utwórz grupę</h1>

        <div class="flex items-center mb-6 p-1">
          <img :src="userAvatar" class="w-9 h-9 rounded-full mr-3 border border-gray-200">
          <div class="leading-tight">
            <div class="font-semibold text-[15px]">Bartosz Miazek</div>
            <div class="text-[13px] text-[#65676B]">Administrator</div>
          </div>
        </div>

        <div class="mb-4">
          <input
            type="text"
            v-model="groupName"
            placeholder="Nazwa grupy"
            class="w-full h-[56px] px-4 border rounded-lg focus:outline-none transition-all placeholder-gray-500 text-[15px]"
            :class="groupName ? 'border-[#1877F2] ring-1 ring-[#1877F2]' : 'border-[#ced0d4] hover:border-[#8d949e]'"
          >
        </div>

        <div class="mb-4 relative">
            <div
                @click="togglePrivacyDropdown"
                class="w-full h-[56px] border rounded-lg flex items-center px-3 cursor-pointer bg-white relative transition-all"
                :class="[
                   isPrivacyDropdownOpen ? 'border-[#1877F2] ring-1 ring-[#1877F2]' : 'border-[#ced0d4] hover:border-[#8d949e]',
                   privacy !== 'choose' ? 'border-[#1877F2]' : ''
                ]"
            >
                <div v-if="privacy === 'choose'" class="text-[15px] text-[#65676B] px-1">Wybierz ustawienie prywatności</div>
                <div v-else class="flex flex-col justify-center w-full px-1">
                     <span class="text-[12px] text-[#1877F2] -mb-1 mt-1">Wybierz ustawienie prywatności</span>
                     <div class="flex items-center text-[15px] font-medium text-[#050505]">
                        <Earth v-if="privacy === 'public'" :size="20" class="mr-2" />
                        <Lock v-else :size="20" class="mr-2" />
                        {{ privacy === 'public' ? 'publiczna' : 'Prywatna' }}
                     </div>
                </div>
                <ChevronDown class="absolute right-4 text-[#606770]" />
            </div>

            <div v-if="isPrivacyDropdownOpen" class="absolute top-full left-0 w-full bg-white shadow-[0_12px_28px_0_rgba(0,0,0,0.2),0_2px_4px_0_rgba(0,0,0,0.1)] rounded-lg mt-1 z-50 p-2 overflow-hidden border border-[#dadde1]">
                <div @click="selectPrivacy('public')" class="p-2 flex items-start rounded-lg hover:bg-[#F2F2F2] cursor-pointer transition-colors mb-1">
                    <div class="w-9 h-9 rounded-full bg-[#E4E6EB] flex items-center justify-center mr-3 mt-1 shrink-0">
                        <Earth :size="24" class="text-[#050505]" />
                    </div>
                    <div class="flex-1 mr-2">
                        <div class="font-semibold text-[15px] text-[#050505]">publiczna</div>
                        <div class="text-[13px] text-[#65676B] leading-snug">
                            Każdy może sprawdzić listę członków grupy i zobaczyć ich posty.
                        </div>
                        <div class="text-[12px] text-[#65676B] leading-snug mt-1">
                             W zależności od wielkości...
                        </div>
                    </div>
                    <div class="mt-3">
                        <div class="w-5 h-5 rounded-full border flex items-center justify-center" :class="privacy === 'public' ? 'border-[#1877F2]' : 'border-[#65676B]'">
                             <div v-if="privacy === 'public'" class="w-2.5 h-2.5 rounded-full bg-[#1877F2]"></div>
                        </div>
                    </div>
                </div>
                <div @click="selectPrivacy('private')" class="p-2 flex items-start rounded-lg hover:bg-[#F2F2F2] cursor-pointer transition-colors">
                    <div class="w-9 h-9 rounded-full bg-[#E4E6EB] flex items-center justify-center mr-3 mt-1 shrink-0">
                        <Lock :size="24" class="text-[#050505]" />
                    </div>
                    <div class="flex-1 mr-2">
                        <div class="font-semibold text-[15px] text-[#050505]">Prywatna</div>
                        <div class="text-[13px] text-[#65676B] leading-snug">
                            Tylko członkowie grupy mogą sprawdzić listę członków grupy i zobaczyć ich posty.
                        </div>
                        <div class="text-[12px] text-[#65676B] leading-snug mt-1">
                             Możesz później zmienić status grupy na publiczną.
                        </div>
                    </div>
                    <div class="mt-3">
                         <div class="w-5 h-5 rounded-full border flex items-center justify-center" :class="privacy === 'private' ? 'border-[#1877F2]' : 'border-[#65676B]'">
                             <div v-if="privacy === 'private'" class="w-2.5 h-2.5 rounded-full bg-[#1877F2]"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="privacy === 'private'" class="mb-4 text-[12px] text-[#65676B] px-1 leading-snug">
           Tylko członkowie grupy mogą sprawdzić listę członków grupy i zobaczyć ich posty. Status grupy możesz później zmienić na publiczną. <span class="text-[#1877F2] cursor-pointer hover:underline">Dowiedz się więcej...</span>
        </div>

        <div v-if="privacy === 'private'" class="mb-4 relative">
            <div
                @click="toggleVisibilityDropdown"
                class="w-full h-[56px] border rounded-lg flex items-center px-3 cursor-pointer bg-white relative transition-all"
                :class="[
                   isVisibilityDropdownOpen ? 'border-[#1877F2] ring-1 ring-[#1877F2]' : 'border-[#ced0d4] hover:border-[#8d949e]',
                   // Jeśli otwarta lub zamknięta, ale widoczna, dodaj styl active (blue border jak na screenie)
                   'border-[#1877F2]'
                ]"
            >
                 <div class="flex flex-col justify-center w-full px-1">
                     <span class="text-[12px] text-[#1877F2] -mb-1 mt-1">Widoczność</span>
                     <div class="flex items-center text-[15px] font-medium text-[#050505]">
                        <Eye v-if="visibility === 'visible'" :size="20" class="mr-2" />
                        <EyeOff v-else :size="20" class="mr-2" />
                        {{ visibility === 'visible' ? 'Widoczna' : 'Ukryta' }}
                     </div>
                </div>
                <ChevronDown class="absolute right-4 text-[#606770]" />
            </div>

            <div v-if="isVisibilityDropdownOpen" class="absolute top-full left-0 w-full bg-white shadow-[0_12px_28px_0_rgba(0,0,0,0.2),0_2px_4px_0_rgba(0,0,0,0.1)] rounded-lg mt-1 z-50 p-2 overflow-hidden border border-[#dadde1]">

                <div @click="selectVisibility('visible')" class="p-2 flex items-start rounded-lg hover:bg-[#F2F2F2] cursor-pointer transition-colors mb-1">
                    <div class="w-9 h-9 rounded-full bg-[#E4E6EB] flex items-center justify-center mr-3 mt-1 shrink-0">
                        <Eye :size="24" class="text-[#050505]" />
                    </div>
                    <div class="flex-1 mr-2 flex flex-col justify-center min-h-[36px]">
                        <div class="font-semibold text-[15px] text-[#050505]">Widoczna</div>
                        <div class="text-[13px] text-[#65676B] leading-snug">
                            Każdy może znaleźć tę grupę.
                        </div>
                    </div>
                    <div class="mt-3">
                        <div class="w-5 h-5 rounded-full border flex items-center justify-center" :class="visibility === 'visible' ? 'border-[#1877F2]' : 'border-[#65676B]'">
                             <div v-if="visibility === 'visible'" class="w-2.5 h-2.5 rounded-full bg-[#1877F2]"></div>
                        </div>
                    </div>
                </div>

                <div @click="selectVisibility('hidden')" class="p-2 flex items-start rounded-lg hover:bg-[#F2F2F2] cursor-pointer transition-colors">
                    <div class="w-9 h-9 rounded-full bg-[#E4E6EB] flex items-center justify-center mr-3 mt-1 shrink-0">
                        <EyeOff :size="24" class="text-[#050505]" />
                    </div>
                    <div class="flex-1 mr-2 flex flex-col justify-center min-h-[36px]">
                        <div class="font-semibold text-[15px] text-[#050505]">Ukryta</div>
                        <div class="text-[13px] text-[#65676B] leading-snug">
                            Tylko członkowie mogą znaleźć tę grupę.
                        </div>
                    </div>
                    <div class="mt-3">
                         <div class="w-5 h-5 rounded-full border flex items-center justify-center" :class="visibility === 'hidden' ? 'border-[#1877F2]' : 'border-[#65676B]'">
                             <div v-if="visibility === 'hidden'" class="w-2.5 h-2.5 rounded-full bg-[#1877F2]"></div>
                        </div>
                    </div>
                </div>

            </div>
        </div>

        <div class="mb-2">
           <div class="w-full min-h-[56px] px-2 py-1 border border-[#ced0d4] rounded-lg focus-within:border-[#1877F2] focus-within:ring-1 focus-within:ring-[#1877F2] hover:border-[#8d949e] flex flex-wrap items-center gap-1 bg-white">
              <div v-for="friend in invitedFriends" :key="friend" class="bg-[#E7F3FF] text-[#1877F2] px-2 py-1 rounded-[4px] flex items-center text-[14px] font-semibold">
                  <img :src="userAvatar" class="w-5 h-5 rounded-full mr-1.5">
                  {{ friend }}
                  <Close :size="16" class="ml-1 cursor-pointer hover:text-blue-800" @click="removeFriend(friend)" />
              </div>
              <input
                type="text"
                v-model="inviteInput"
                placeholder="Zaproś znajomych (opcjonalne)"
                class="flex-1 h-[40px] px-2 outline-none text-[15px] min-w-[120px]"
              >
           </div>
        </div>
        <p class="text-[12px] text-[#65676B] px-1 leading-snug">
           Propozycje:
           <span @click="addFriend('Mateusz Bieniek')" class="text-[#1877F2] font-semibold cursor-pointer hover:underline">Mateusz Bieniek</span>,
           <span @click="addFriend('Kuba Trzaskowski')" class="text-[#1877F2] font-semibold cursor-pointer hover:underline">Kuba Trzaskowski</span>...
        </p>

      </div>

      <div class="p-4 border-t border-[#dadde1] shadow-[0_-2px_4px_rgba(0,0,0,0.05)] bg-white z-10">
        <button
          class="w-full h-9 rounded-md font-semibold text-[15px] flex items-center justify-center transition-colors"
          :class="isFormValid ? 'bg-[#1877F2] text-white hover:bg-[#166fe5]' : 'bg-[#E4E6EB] text-[#BCC0C4] cursor-not-allowed'"
          :disabled="!isFormValid"
        >
          Utwórz
        </button>
      </div>
    </aside>


    <main class="flex-1 flex justify-center items-start pt-8 pb-8 px-8 overflow-y-auto bg-[#F0F2F5]" @click="closeAllDropdowns">

      <div class="w-full max-w-[980px] bg-white rounded-lg shadow-sm border border-[#dadde1] overflow-hidden flex flex-col">

        <div class="flex justify-between items-center px-4 h-[60px] border-b border-[#dadde1] bg-white">
            <span class="font-bold text-[16px] text-[#050505]">Podgląd na komputerze</span>
            <div class="flex items-center gap-2 text-[#65676B]">
                <button class="p-2 hover:bg-[#F2F2F2] rounded-full transition text-[#1877F2]"><Monitor :size="24" /></button>
                <button class="p-2 hover:bg-[#F2F2F2] rounded-full transition"><Cellphone :size="24" /></button>
            </div>
        </div>

        <div class="flex-1 flex flex-col">

            <div class="w-full px-4 pt-4 bg-white">
                <div class="w-full aspect-[2.5/1] bg-[#F0F2F5] rounded-lg overflow-hidden relative">
                    <img :src="coverIllustration" class="w-full h-full object-cover filter grayscale opacity-90">
                </div>
            </div>

            <div class="px-8 pt-4 pb-0 bg-white">
                <h1 class="text-[28px] font-bold mb-1 leading-snug break-words transition-colors" :class="titleColorClass">
                     {{ previewName }}
                </h1>

                <div class="text-[#65676B] text-[15px] font-medium flex items-center mt-1">
                    <span class="flex items-center">
                        <Lock v-if="privacy === 'private'" :size="14" class="mr-1.5" />
                        <Earth v-else-if="privacy === 'public'" :size="14" class="mr-1.5" />
                        <span>{{ subtitleText }}</span>
                    </span>
                    <span class="mx-1.5">·</span>
                    <span class="font-bold text-black">1 członek</span>
                </div>

                <div class="flex mt-6 border-t border-[#dadde1]">
                    <div class="px-4 py-4 text-[#1877F2] border-b-[3px] border-[#1877F2] font-semibold text-[15px] cursor-pointer">Informacje</div>
                    <div class="px-4 py-4 text-[#65676B] font-semibold text-[15px] hover:bg-[#F2F2F2] rounded-md mx-1 my-1 cursor-pointer">Posty</div>
                    <div class="px-4 py-4 text-[#65676B] font-semibold text-[15px] hover:bg-[#F2F2F2] rounded-md mx-1 my-1 cursor-pointer">Członkowie</div>
                    <div class="px-4 py-4 text-[#65676B] font-semibold text-[15px] hover:bg-[#F2F2F2] rounded-md mx-1 my-1 cursor-pointer">Wydarzenia</div>
                </div>
            </div>

            <div class="bg-[#F0F2F5] p-4 flex gap-4 min-h-[300px]">

        <div class="flex-1">
    <div class="bg-white rounded-lg shadow-sm border border-[#dadde1] px-4 py-3">
        <div class="flex items-center gap-2 mb-3">
            <AccountCircle :size="40" class="text-[#E4E6EB]" />

            <div class="bg-[#F0F2F5] rounded-full flex-1 px-3 py-2 text-[#BEC3C9] text-[15px] text-left pl-4 cursor-default">
                Co słychać?
            </div>
        </div>

        <div class="border-t border-[#dadde1] pt-2 flex justify-between px-2">

            <div class="flex items-center justify-center flex-1 py-2 cursor-default">
                <ImageMultiple class="text-[#BEC3C9] mr-2" :size="24" />
                <span class="text-[#BEC3C9] font-semibold text-[14px]">Zdjęcie/film</span>
            </div>

            <div class="flex items-center justify-center flex-1 py-2 cursor-default">
                <AccountTag class="text-[#BEC3C9] mr-2" :size="24" />
                <span class="text-[#BEC3C9] font-semibold text-[14px]">Oznacz osoby</span>
            </div>

            <div class="flex items-center justify-center flex-1 py-2 cursor-default">
                <EmoticonOutline class="text-[#BEC3C9] mr-2" :size="24" />
                <span class="text-[#BEC3C9] font-semibold text-[14px]">Feeling/activity</span>
            </div>

        </div>
    </div>
</div>

                <div class="w-[340px] hidden md:block">
                    <div class="bg-white rounded-lg shadow-sm border border-[#dadde1] p-4">
                        <h3 class="font-bold text-[17px] text-[#050505] mb-3">Informacje</h3>

                        <div v-if="privacy === 'choose'" class="h-10"></div>

                        <div v-else class="space-y-4">
                            <div class="flex items-start">
                                <Lock v-if="privacy === 'private'" :size="20" class="text-[#050505] mr-3 mt-0.5 shrink-0" />
                                <Earth v-else :size="20" class="text-[#050505] mr-3 mt-0.5 shrink-0" />
                                <div>
                                    <div class="font-semibold text-[15px] text-[#050505] mb-0.5">
                                        {{ privacy === 'private' ? 'Prywatna' : 'Publiczna' }}
                                    </div>
                                    <div class="text-[13px] text-[#050505] leading-snug">
                                        {{ privacy === 'private'
                                           ? 'Tylko członkowie grupy mogą sprawdzić listę członków grupy i zobaczyć ich posty.'
                                           : 'Każdy może zobaczyć, kto należy do grupy i co w niej publikuje.' }}
                                    </div>
                                </div>
                            </div>

                            <div class="flex items-start">
                                <Eye v-if="visibility === 'visible'" :size="20" class="text-[#050505] mr-3 mt-0.5 shrink-0" />
                                <EyeOff v-else :size="20" class="text-[#050505] mr-3 mt-0.5 shrink-0" />
                                <div>
                                    <div class="font-semibold text-[15px] text-[#050505] mb-0.5">
                                        {{ visibility === 'visible' ? 'Widoczna' : 'Ukryta' }}
                                    </div>
                                    <div class="text-[13px] text-[#050505] leading-snug">
                                        {{ visibility === 'visible'
                                           ? 'Każdy może znaleźć tę grupę.'
                                           : 'Tylko członkowie mogą znaleźć tę grupę.' }}
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>

            </div>
        </div>
      </div>
    </main>

    <div class="fixed bottom-6 right-6 z-50">
      <button class="bg-white p-3 rounded-full shadow-lg border border-[#dadde1] hover:bg-[#F2F2F2]">
        <Pencil :size="20" class="text-[#050505]" />
      </button>
    </div>

  </div>
</template>

