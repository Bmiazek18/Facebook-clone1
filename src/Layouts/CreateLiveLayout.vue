<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue' // Added ref
import { useRoute, useRouter } from 'nuxt/app'
import { useI18n } from 'vue-i18n'
import { useLiveStore } from '@/stores/live'
import { useAuthStore } from '@/stores/auth' // Added import

// ... (IMPORTY IKON POZOSTAJĄ BEZ ZMIAN) ...
import VideoIcon from 'vue-material-design-icons/Video.vue'
import ViewDashboardIcon from 'vue-material-design-icons/ViewDashboard.vue'
import MessageAlertOutlineIcon from 'vue-material-design-icons/MessageAlertOutline.vue'
import TabletDashboardIcon from 'vue-material-design-icons/TabletDashboard.vue'
import HelpCircleOutlineIcon from 'vue-material-design-icons/HelpCircleOutline.vue'
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import LockIcon from 'vue-material-design-icons/Lock.vue'
import CustomDropdown from '@/components/common/CustomDropdown.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const liveStore = useLiveStore()
const auth = useAuthStore() // Added auth store

const isCreateLiveActive = computed(() => route.path.endsWith('/create-live'))
const isDashboardActive = computed(() => route.path.endsWith('/dashboard'))

const liveScheduleOptions = ref([
  {
    id: 'now',
    title: t('createLive.schedule.now'),
    description: t('createLive.schedule.nowDescription'),
  },
  {
    id: 'later',
    title: t('createLive.schedule.later'),
    description: t('createLive.schedule.laterDescription'),
  },
])
const selectedLiveSchedule = ref('now') // Default to 'now'

onUnmounted(() => {
  liveStore.stopStream()
})
</script>
<template>
  <div
    class="flex h-screen bg-theme-bg mt-14 text-theme-text   overflow-hidden antialiased leading-normal"
  >
    <aside
      class="w-90 bg-theme-bg-secondary border-r border-theme-border flex flex-col h-full overflow-y-auto shrink-0 z-20 custom-scrollbar"
    >
      <div class="px-4 pt-5 pb-2">
        <div class="flex justify-between items-start mb-4">
          <h1 class="text-[24px] font-bold leading-tight tracking-tight text-theme-text">
            Utwórz transmisję<br />wideo na żywo
          </h1>
          <div class="flex items-center gap-2">
            <div
              class="w-9 h-9 hover:bg-theme-hover rounded-full flex items-center justify-center cursor-pointer transition-colors text-theme-text-secondary"
            >
              <HelpCircleOutlineIcon :size="24" />
            </div>
            <div
              class="w-9 h-9 hover:bg-theme-hover rounded-full flex items-center justify-center cursor-pointer transition-colors text-theme-text-secondary"
            >
              <TabletDashboardIcon :size="20" />
            </div>
          </div>
        </div>

        <div class="flex items-center gap-3 mb-6">
          <div class="h-2 flex-1 bg-theme-bg-subtle rounded-full overflow-hidden">
            <div class="h-full bg-[#1877F2] w-[33%] rounded-full"></div>
          </div>
          <span class="text-[#1877F2] font-semibold text-[15px]">1/3</span>
        </div>

        <div class="space-y-5 mb-6">
          <div class="flex items-start gap-3 cursor-pointer group">
            <CheckCircleIcon
              class="text-[#31A24C] shrink-0 mt-0.5"
              :size="24"
              v-if="liveStore.activeStream"
            />
            <div
              v-else
              class="w-6 h-6 rounded-full border-2 border-theme-border shrink-0 mt-0.5 group-hover:border-theme-text"
            ></div>
            <span class="text-[15px] font-medium text-theme-text leading-snug"
              >Połącz źródło wideo</span
            >
          </div>

          <div class="flex items-start gap-3 cursor-pointer">
            <CheckCircleIcon class="text-[#31A24C] shrink-0 mt-0.5" :size="24" />
            <span class="text-[15px] font-medium text-theme-text leading-snug"
              >Uzupełnij szczegółowe informacje dotyczące posta</span
            >
          </div>

          <div class="flex items-start gap-3 cursor-pointer group">
            <div
              class="w-6 h-6 rounded-full border-2 border-theme-border shrink-0 mt-0.5 group-hover:border-theme-text"
            ></div>
            <span class="text-[15px] font-medium text-theme-text leading-snug"
              >Rozpocznij transmisję na żywo</span
            >
          </div>
        </div>

        <hr class="border-theme-border mb-5" />

        <div class="flex items-center gap-3 mb-5">
          <img
            :src="auth.currentUser?.avatar"
            :alt="auth.currentUser?.name"
            class="w-10 h-10 rounded-full border border-theme-border"
          />
          <div class="leading-snug">
            <div class="font-semibold text-[15px] text-theme-text">
              {{ auth.currentUser?.name }}
            </div>
            <div class="text-[13px] text-theme-text-secondary">Organizator — Twój profil</div>
          </div>
        </div>

        <div class="space-y-3 mb-3 w-full">
          <div
            class="border border-theme-border rounded-lg px-3 py-2 cursor-pointer hover:bg-theme-hover transition-colors flex justify-between items-center bg-theme-bg-secondary relative"
          >
            <div>
              <div class="text-[11px] text-theme-text-secondary mb-0.5">
                Wskaż docelową lokalizację posta
              </div>
              <div class="font-medium text-[15px] text-theme-text">Opublikuj w profilu</div>
            </div>
            <ChevronDownIcon :size="20" class="text-theme-text" />
          </div>

          <CustomDropdown
            v-model="selectedLiveSchedule"
            :options="liveScheduleOptions"
            label="Kiedy rozpoczniesz transmisję na żywo?"
          />
        </div>

        <div
          class="inline-flex items-center px-3 py-1.5 bg-theme-bg-subtle hover:bg-theme-hover rounded-md text-[15px] font-semibold text-theme-text cursor-pointer transition-colors mb-4 w-max"
        >
          <LockIcon :size="14" class="mr-2 text-theme-text-secondary" />
          Tylko ja
        </div>
      </div>

      <nav
        v-if="isCreateLiveActive || isDashboardActive"
        class="flex-1 overflow-y-auto px-2 custom-scrollbar"
      >
        <ul class="flex flex-col space-y-1">
          <li
            class="flex items-center px-2 py-2 rounded-lg cursor-pointer transition-colors"
            :class="{
              'bg-theme-primary-subtle': isCreateLiveActive,
              'hover:bg-theme-hover': !isCreateLiveActive,
            }"
            @click="router.push('/live/produce/create-live')"
          >
            <div
              class="w-10 h-10 flex items-center justify-center rounded-full mr-3 shrink-0"
              :class="
                isCreateLiveActive
                  ? 'bg-theme-primary text-white'
                  : 'bg-theme-bg-subtle text-theme-text group-hover:bg-theme-hover'
              "
            >
              <VideoIcon :size="20" />
            </div>
            <span
              class="font-semibold text-[15px]"
              :class="isCreateLiveActive ? 'text-theme-primary' : 'text-theme-text'"
              >Konfiguracja transmisji</span
            >
          </li>

          <li
            class="flex items-center px-2 py-2 rounded-lg cursor-pointer transition-colors"
            :class="{
              'bg-theme-primary-subtle': isDashboardActive,
              'hover:bg-theme-hover': !isDashboardActive,
            }"
            @click="router.push('/live/produce/dashboard')"
          >
            <div
              class="w-10 h-10 flex items-center justify-center rounded-full mr-3 shrink-0"
              :class="
                isDashboardActive
                  ? 'bg-theme-primary text-white'
                  : 'bg-theme-bg-subtle text-theme-text group-hover:bg-theme-hover'
              "
            >
              <ViewDashboardIcon :size="20" />
            </div>
            <span
              class="font-semibold text-[15px]"
              :class="isDashboardActive ? 'text-theme-primary' : 'text-theme-text'"
              >Pulpit</span
            >
          </li>

          <li
            class="flex items-center px-2 py-2 rounded-lg cursor-pointer hover:bg-theme-hover transition-colors group"
          >
            <div
              class="w-10 h-10 flex items-center justify-center rounded-full bg-theme-bg-subtle text-theme-text mr-3 shrink-0 group-hover:bg-theme-hover"
            >
              <MessageAlertOutlineIcon :size="20" />
            </div>
            <span class="font-semibold text-[15px] text-theme-text">Przekaż opinię</span>
          </li>
        </ul>
      </nav>
      <div
        class="p-4 border-t border-theme-border flex items-center gap-3 bg-theme-bg-secondary z-30 mt-auto"
      >
        <button
          class="px-5 py-2.5 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text rounded-md font-semibold text-[15px] transition-colors"
        >
          Wstecz
        </button>
        <button
          class="flex-1 px-4 py-2.5 bg-theme-bg-subtle text-theme-text-secondary rounded-md font-semibold text-[15px] cursor-not-allowed text-center truncate"
        >
          Rozpocznij transmisję na żywo
        </button>
      </div>
    </aside>

    <main class="flex-1 overflow-hidden relative">
      <NuxtPage />
    </main>
  </div>
</template>

<style scoped>
/* Stylizacja scrollbarów, aby pasowały do designu */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #bcc0c4;
  border-radius: 4px;
  border: 2px solid transparent;
  background-clip: content-box;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: #a8abaf;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
</style>
