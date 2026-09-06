<script setup>
import { computed, markRaw } from 'vue'

// Importy ikon z vue-material-design-icons
import ShieldAccountOutlineIcon from 'vue-material-design-icons/ShieldAccountOutline.vue'
import AccountPlusOutlineIcon from 'vue-material-design-icons/AccountPlusOutline.vue'
import CogIcon from 'vue-material-design-icons/Cog.vue'
import CommentCheckOutlineIcon from 'vue-material-design-icons/CommentCheckOutline.vue'
import ChartLineIcon from 'vue-material-design-icons/ChartLine.vue'
import StarCircleOutlineIcon from 'vue-material-design-icons/StarCircleOutline.vue'
import FlagOutlineIcon from 'vue-material-design-icons/FlagOutline.vue'
import MessageAlertOutlineIcon from 'vue-material-design-icons/MessageAlertOutline.vue'
import AlertOutlineIcon from 'vue-material-design-icons/AlertOutline.vue'
import FileDocumentCheckOutlineIcon from 'vue-material-design-icons/FileDocumentCheckOutline.vue'
import CommentOffOutlineIcon from 'vue-material-design-icons/CommentOffOutline.vue'
import NoteRemoveOutlineIcon from 'vue-material-design-icons/NoteRemoveOutline.vue'
import AccountRemoveOutlineIcon from 'vue-material-design-icons/AccountRemoveOutline.vue'
import AccountClockOutlineIcon from 'vue-material-design-icons/AccountClockOutline.vue'
import AccountLockOutlineIcon from 'vue-material-design-icons/AccountLockOutline.vue'
import PinOutlineIcon from 'vue-material-design-icons/PinOutline.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'

// Nuxt 3 auto-importuje useRoute(), więc używamy go bezpośrednio
const route = useRoute()

// Wyciągamy parametr "role" z adresu URL (np. 'admin' lub 'moderator')
const currentRoleKey = computed(() => {
  const roleParam = route.params.role || ''
  return roleParam.toLowerCase() === 'moderator' ? 'moderator' : 'admin'
})

// Dane ról (Admin i Moderator)
const rolesData = {
  admin: {
    title: 'Administrator',
    description: 'Administratorzy są odpowiedzialni za grupę i kulturę osobistą. Kontrolują oni ustawienia grupy, członkostwo, posty i komentarze. Główny administrator nie może zostać usunięty przez innych administratorów.',
    members: [
      { id: 1, name: 'Test Testowy', avatar: 'https://i.pravatar.cc/150?u=12' }
    ],
    permissions: [
      { label: 'Dodawanie / usuwanie administratorów, moderatorów i innych ról', icon: markRaw(ShieldAccountOutlineIcon) },
      { label: 'Zatwierdzanie / odrzucanie próśb o dołączenie', icon: markRaw(AccountPlusOutlineIcon) },
      { label: 'Zarządzanie ustawieniami grupy', icon: markRaw(CogIcon) },
      { label: 'Zatwierdzanie / odrzucanie / usuwanie komentarzy do postów', icon: markRaw(CommentCheckOutlineIcon) },
      { label: 'Wyświetlanie statystyk grupy', icon: markRaw(ChartLineIcon) },
      { label: 'Zarządzanie kryteriami Asystenta administratora', icon: markRaw(StarCircleOutlineIcon) },
      { label: 'Sprawdzanie materiałów oflagowanych przez Facebooka / podejmowanie działań wobec nich', icon: markRaw(FlagOutlineIcon) },
      { label: 'Sprawdzanie alertów moderacji / reagowanie na alerty', icon: markRaw(MessageAlertOutlineIcon) },
      { label: 'Sprawdź potencjalny spam', icon: markRaw(AlertOutlineIcon) },
      { label: 'Zatwierdzanie / odrzucanie oczekujących postów', icon: markRaw(FileDocumentCheckOutlineIcon) },
      { label: 'Wyłączanie opcji komentowania posta', icon: markRaw(CommentOffOutlineIcon) },
      { label: 'Usuwanie postów z grupy', icon: markRaw(NoteRemoveOutlineIcon) },
      { label: 'Usuwanie osób z grupy / blokowanie ich', icon: markRaw(AccountRemoveOutlineIcon) },
      { label: 'Zawieszanie osób w grupie', icon: markRaw(AccountClockOutlineIcon) },
      { label: 'Ograniczanie aktywności osób w grupie', icon: markRaw(AccountLockOutlineIcon) },
      { label: 'Przypinanie / odpinanie postów', icon: markRaw(PinOutlineIcon) }
    ]
  },
  moderator: {
    title: 'Moderator',
    description: 'Moderatorzy mogą zarządzać członkami grupy, postami i komentarzami, natomiast nie mogą zmieniać ogólnych ustawień grupy ani zarządzać rolami.',
    members: [], // Pusta lista by aktywować "Empty State" ze screena
    permissions: [
      { label: 'Zatwierdzanie / odrzucanie próśb o dołączenie', icon: markRaw(AccountPlusOutlineIcon) },
      { label: 'Sprawdzanie alertów moderacji / reagowanie na alerty', icon: markRaw(MessageAlertOutlineIcon) },
      { label: 'Sprawdź potencjalny spam', icon: markRaw(AlertOutlineIcon) },
      { label: 'Zatwierdzanie / odrzucanie oczekujących postów', icon: markRaw(FileDocumentCheckOutlineIcon) },
      { label: 'Zatwierdzanie / odrzucanie / usuwanie komentarzy do postów', icon: markRaw(CommentCheckOutlineIcon) },
      { label: 'Wyłączanie opcji komentowania posta', icon: markRaw(CommentOffOutlineIcon) },
      { label: 'Usuwanie postów z grupy', icon: markRaw(NoteRemoveOutlineIcon) },
      { label: 'Usuwanie osób z grupy / blokowanie ich', icon: markRaw(AccountRemoveOutlineIcon) },
      { label: 'Zawieszanie / ograniczanie aktywności osób w grupie', icon: markRaw(AccountClockOutlineIcon) },
      { label: 'Ograniczanie aktywności osób w grupie', icon: markRaw(AccountLockOutlineIcon) }
    ]
  }
}

const currentRole = computed(() => rolesData[currentRoleKey.value])
</script>

<template>
  <div class="min-h-screen bg-[#18191a] text-[#e4e6eb] font-sans flex flex-col relative selection:bg-blue-600 pb-20">

    <!-- Pasek Nagłówka -->
    <div class="bg-[#242526] border-b border-[#3e4042] px-6 py-4 flex items-center justify-center shadow-sm sticky top-0 z-20">
      <h1 class="text-xl font-bold text-[#e4e6eb]">
        {{ currentRole.title }}
      </h1>
    </div>

    <!-- Główny Kontener -->
    <div class="max-w-7xl mx-auto w-full p-4 sm:p-6 flex flex-col lg:flex-row gap-4 items-start">

      <!-- Lewa Kolumna: Członkowie (Members) -->
      <div class="w-full lg:w-3/5 bg-[#242526] border border-[#3e4042] rounded-xl p-4 shadow-sm min-h-[300px]">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-[17px] font-bold">{{ $t('groups.czlonkowiePelniacyTeRole') }}</h2>
          <button class="text-[15px] font-semibold text-[#4599FF] hover:bg-[#3a3b3c] px-3 py-1.5 rounded-md transition-colors">{{ $t('call.dodaj') }}</button>
        </div>

        <!-- Stan ze zdefiniowanymi członkami (np. Admin) -->
        <div v-if="currentRole.members.length > 0" class="space-y-2 mt-4">
          <div
            v-for="member in currentRole.members"
            :key="member.id"
            class="flex items-center justify-between p-2 rounded-lg hover:bg-[#3a3b3c] transition-colors group cursor-pointer"
          >
            <div class="flex items-center gap-3">
              <img :src="member.avatar" :alt="$t('chat.avatar')" class="w-10 h-10 rounded-full bg-gray-500 object-cover" />
              <span class="text-[15px] font-semibold">{{ member.name }}</span>
            </div>
            <button class="p-2 rounded-full hover:bg-[#4e4f50] transition-colors text-[#b0b3b8] group-hover:text-[#e4e6eb]">
              <DotsHorizontalIcon :size="20" />
            </button>
          </div>
        </div>

        <!-- Empty State (Brak członków, np. Moderator) -->
        <div v-else class="flex flex-col items-center justify-center h-full pt-10 pb-6 text-center">
          <svg width="120" height="100" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg" class="mb-4">
            <!-- Pierwsza postać -->
            <rect x="30" y="30" width="16" height="20" rx="3" fill="#699bf7" />
            <path d="M38 35C38 35 34 42 38 45C42 42 38 35 38 35Z" fill="#e4e6eb" opacity="0.8" />
            <path d="M22 65C22 55 26 50 38 50C50 50 54 55 54 65L54 75L22 75L22 65Z" fill="#b0b3b8" />
            <!-- Druga postać -->
            <circle cx="62" cy="48" r="10" fill="#4599ff" />
            <path d="M52 48C52 42 57 38 62 38C67 38 72 42 72 48C72 48 52 48 52 48Z" fill="#b0b3b8" />
            <path d="M42 75C42 62 48 55 62 55C76 55 82 62 82 75L82 75L42 75L42 75Z" fill="#75777a" />
          </svg>

          <h3 class="text-[17px] font-bold text-[#e4e6eb] mb-1">{{ $t('groups.jeszczeNiktNiePelni') }}</h3>
          <p class="text-[15px] text-[#b0b3b8] max-w-md">{{ $t('groups.sprawdzKtoJestZainteresowany') }}</p>
          <button class="mt-5 bg-[#2d88ff] hover:bg-[#1a73e8] transition-colors text-white font-semibold text-[15px] px-6 py-2 rounded-md">{{ $t('groups.dodajCzlonkowGrupy') }}</button>
        </div>
      </div>

      <!-- Prawa Kolumna: Opis i Uprawnienia -->
      <div class="w-full lg:w-2/5 flex flex-col gap-4">

        <div class="bg-[#242526] border border-[#3e4042] rounded-xl p-4 shadow-sm">
          <h2 class="text-[17px] font-bold mb-2">{{ $t('createLive.description') }}</h2>
          <p class="text-[15px] text-[#b0b3b8] leading-relaxed">
            {{ currentRole.description }}
          </p>
        </div>

        <div class="bg-[#242526] border border-[#3e4042] rounded-xl p-4 shadow-sm">
          <h2 class="text-[17px] font-bold mb-4">{{ $t('groups.uprawnienia') }}</h2>
          <div class="space-y-4">
            <div
              v-for="(permission, index) in currentRole.permissions"
              :key="index"
              class="flex items-start gap-4"
            >
              <div class="w-9 h-9 rounded-full bg-[#3a3b3c] flex items-center justify-center shrink-0">
                <component :is="permission.icon" :size="20" class="text-[#e4e6eb]" />
              </div>
              <div class="text-[15px] text-[#e4e6eb] font-medium leading-snug mt-1.5">
                {{ permission.label }}
              </div>
            </div>
          </div>
        </div>

      </div>

    </div>



  </div>
</template>
