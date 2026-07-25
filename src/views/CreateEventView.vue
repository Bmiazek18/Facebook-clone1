<script setup lang="ts">
import CustomInput from '@/components/common/CustomInput.vue'
import { useAuthStore } from '@/stores/auth'
import { reactive, computed, ref } from 'vue'
import Information from 'vue-material-design-icons/Information.vue'
// --- TYPY I STAN ---
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
type ViewMode = 'desktop' | 'mobile'

interface EventForm {
  eventName: string
  startDate: string
  startTime: string
  privacy: string
  description: string
}

const viewMode = ref<ViewMode>('desktop')
const usePastEvent = ref(false) // Stan przełącznika "Wypełnij przy użyciu danych..."

const form = reactive<EventForm>({
  eventName: '',
  startDate: '2025-12-23', // Data ze screena
  startTime: '00:47', // Godzina ze screena
  privacy: 'Prywatne',
  description: '',
})

// --- COMPUTED PROPERTIES ---

// Format daty dla podglądu: "wtorek, 23 grudnia 2025"
const formattedDateFull = computed(() => {
  if (!form.startDate) return ''
  const date = new Date(form.startDate)
  return date.toLocaleDateString('pl-PL', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
})

// Format daty dla Inputa (symulacja formatu "23 gru 2025")
const formattedDateShort = computed(() => {
  if (!form.startDate) return ''
  const date = new Date(form.startDate)
  const day = date.getDate()
  const year = date.getFullYear()
  // Pobranie skróconego miesiąca
  const month = date.toLocaleDateString('pl-PL', { month: 'short' })
  return `${day} ${month} ${year}`
})

const dayNumber = computed(() => {
  if (!form.startDate) return '23'
  return new Date(form.startDate).getDate()
})

const previewTitle = computed(() =>
  viewMode.value === 'mobile' ? 'Podgląd na urządzeniu mobilnym' : 'Podgląd na komputerze',
)

const auth = useAuthStore()
const containerClass = computed(() =>
  viewMode.value === 'mobile'
    ? 'max-w-[375px] min-h-[600px] border-x border-gray-200 shadow-sm'
    : 'max-w-[850px] shadow-[0_1px_2px_rgba(0,0,0,0.1)] border border-[#dbdbdb]',
)
</script>

<template>
  <div
    class="flex h-screen w-full bg-[#F0F2F5]   text-[#050505] overflow-hidden antialiased"
  >
    <aside
      class="w-[360px] shrink-0 flex flex-col bg-white shadow-[2px_0_5px_rgba(0,0,0,0.05)] h-full z-20 relative border-r border-[#E5E5E5]"
    >
      <div class="h-[52px] flex items-center px-4 border-b border-[#E5E5E5] shrink-0">
        <div
          class="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100 cursor-pointer text-[#65676B]"
        >
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
            <path
              d="M18.47 5.53a.75.75 0 010 1.06L13.06 12l5.41 5.41a.75.75 0 11-1.06 1.06L12 13.06l-5.41 5.41a.75.75 0 01-1.06-1.06L10.94 12 5.53 6.59a.75.75 0 011.06-1.06L12 10.94l5.41-5.41a.75.75 0 011.06 0z"
            ></path>
          </svg>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto px-4 py-5 custom-scrollbar">
        <div class="text-[13px] text-[#65676B] mb-1">
          <NuxtLink to="/" class="font-semibold">{{ t('common.homePage') }}</NuxtLink>
          <span class="mx-0.5">›</span> {{ t('createEvent.liveVideoEvent') }}
        </div>

        <h1 class="text-[24px] font-bold leading-tight mb-4">
          {{ t('createEvent.createEvent') }}
        </h1>

        <p class="text-[15px] text-[#65676B] leading-normal mb-6">
          {{ t('createEvent.description') }}
        </p>

        <div class="flex items-center mb-6">
          <img
            :src="auth.currentUser?.avatar || 'https://i.pravatar.cc/150?u=bartosz'"
            class="w-10 h-10 rounded-full mr-3 border border-[#E5E5E5]"
          />
          <div class="leading-tight">
            <div class="font-semibold text-[15px]">{{ auth.currentUser?.name }}</div>
            <div class="text-[13px] text-[#65676B]">{{ t('createEvent.organizerProfile') }}</div>
          </div>
        </div>

        <div class="mb-6">
          <div class="flex justify-between items-center mb-2">
            <label class="font-semibold text-[17px]">{{ t('createEvent.coverPhoto') }}</label>
            <div
              v-tooltip.top="t('createEvent.coverPhotoTooltip')"
              class="text-[#65676B] cursor-pointer hover:bg-gray-100 p-1 rounded-full"
            >
              <Information :size="20" />
            </div>
          </div>

          <div
            class="w-full aspect-[1.9/1] bg-white border border-[#CED0D4] rounded-lg flex flex-col items-center justify-center gap-2 mb-4"
          >
            <button
              class="bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold px-4 py-2 rounded-md flex items-center gap-2 transition-colors text-[15px]"
            >
              <svg viewBox="0 0 16 16" width="16" height="16" fill="currentColor">
                <path
                  d="M4 11a1 1 0 112 0v1a1 1 0 11-2 0v-1zm6 0a1 1 0 112 0v1a1 1 0 11-2 0v-1zM7 5a1 1 0 112 0v1a1 1 0 11-2 0V5z"
                ></path>
                <path
                  d="M7 2a3 3 0 00-3 3v2a3 3 0 00-3 3v4a3 3 0 003 3h8a3 3 0 003-3v-4a3 3 0 00-3-3V5a3 3 0 00-3-3H7zm1 1.5a1.5 1.5 0 011.5 1.5v2h-3V5A1.5 1.5 0 018 3.5zm-5.5 7A1.5 1.5 0 014 9h8a1.5 1.5 0 011.5 1.5v4A1.5 1.5 0 0112 16H4a1.5 1.5 0 01-1.5-1.5v-4z"
                ></path>
              </svg>
              {{ t('createEvent.uploadCoverPhoto') }}
            </button>
            <button
              class="bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold px-4 py-2 rounded-md flex items-center gap-2 transition-colors text-[15px]"
            >
              <svg viewBox="0 0 16 16" width="16" height="16" fill="currentColor">
                <path
                  d="M2 4a2 2 0 012-2h8a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V4zm2-0.5A0.5 0.5 0 003.5 4v8a0.5 0.5 0 000.5 0.5h8a0.5 0.5 0 000.5-0.5V4a0.5 0.5 0 00-0.5-0.5H4z"
                ></path>
                <path
                  d="M4 5.5a0.5 0 010.5-0.5h1a0.5 0.5 0 010.5 0.5v1a0.5 0.5 0 01-0.5 0.5h-1a0.5 0.5 0 01-0.5-0.5v-1zM4 9a0.5 0.5 0 010.5-0.5h1a0.5 0.5 0 010.5 0.5v1a0.5 0.5 0 01-0.5 0.5h-1a0.5 0.5 0 01-0.5-0.5v-1zM8 5.5a0.5 0.5 0 010.5-0.5h3a0.5 0.5 0 010.5 0.5v1a0.5 0.5 0 01-0.5 0.5h-3a0.5 0.5 0 01-0.5-0.5v-1zM8 9a0.5 0.5 0 010.5-0.5h3a0.5 0.5 0 010.5 0.5v1a0.5 0.5 0 01-0.5 0.5h-3a0.5 0.5 0 01-0.5-0.5v-1z"
                ></path>
              </svg>
              {{ t('createEvent.selectIllustration') }}
            </button>
          </div>

          <div class="flex gap-2 overflow-hidden">
            <div
              v-for="n in 6"
              :key="n"
              class="w-[44px] h-[44px] rounded-lg bg-gray-200 shrink-0 overflow-hidden cursor-pointer hover:opacity-80 transition"
            >
              <img
                :src="`https://picsum.photos/seed/ev${n}/100`"
                class="w-full h-full object-cover"
              />
            </div>
            <div
              class="w-[44px] h-[44px] rounded-lg bg-[#E4E6EB] shrink-0 flex items-center justify-center cursor-pointer hover:bg-[#D8DADF]"
            >
              <svg viewBox="0 0 20 20" width="20" height="20" fill="currentColor">
                <path d="M5 10a2 2 0 114 0 2 2 0 01-4 0zm7 0a2 2 0 114 0 2 2 0 01-4 0z"></path>
              </svg>
            </div>
          </div>
        </div>

        <div class="flex items-start justify-between gap-4 pt-4">
          <div class="flex-1">
            <h3 class="font-bold text-[17px] text-[#050505] leading-snug">
              {{ t('createEvent.fillWithPastEventData') }}
            </h3>
            <p class="text-[15px] text-[#65676B] mt-1 leading-normal">
              {{ t('createEvent.fillWithPastEventDataDescription') }}
            </p>
          </div>
          <label class="relative inline-flex items-center cursor-pointer mt-1 shrink-0">
            <input type="checkbox" v-model="usePastEvent" class="sr-only peer" />
            <div
              class="w-[44px] h-[24px] bg-[#BCC0C4] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-5 peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#1877F2]"
            ></div>
          </label>
        </div>
        <CustomInput
          v-model="form.eventName"
          :label="t('createEvent.eventName')"
          :id="'eventTitle'"
          class="mb-4"
        />
      </div>

      <div class="p-4 border-t border-[#E5E5E5] bg-white flex flex-col gap-3 shrink-0">
        <div class="w-full flex gap-1 h-1.5 rounded-full overflow-hidden">
          <div class="w-1/2 bg-[#1877F2] rounded-full"></div>
          <div class="w-1/2 bg-[#F0F2F5] rounded-full"></div>
        </div>

        <div class="flex items-center gap-2 mt-1">
          <button
            class="px-5 h-9 rounded-md font-semibold text-[15px] bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF] transition-colors"
          >
            {{ t('common.back') }}
          </button>

          <button
            class="flex-1 h-9 rounded-md font-semibold text-[15px] bg-[#E4E6EB] text-[#BCC0C4] cursor-not-allowed"
          >
            {{ t('common.next') }}
          </button>
        </div>
      </div>
    </aside>

    <main
      class="flex-1 flex flex-col bg-[#F0F2F5] mt-[56px] items-center overflow-hidden p-6 md:p-8"
    >
      <div
        :class="[
          'bg-white rounded-[12px] shadow-sm flex px-4 pb-4 flex-col overflow-hidden transition-all duration-300 ease-in-out w-full h-full',
          viewMode === 'mobile' ? 'max-w-[420px]' : 'max-w-[965px]',
        ]"
      >
        <div class="flex items-center justify-between py-4 bg-white shrink-0">
          <span class="font-semibold text-[#050505] text-[15px]">{{ previewTitle }}</span>

          <div class="flex gap-2 text-[#65676B]">
            <button
              @click="viewMode = 'desktop'"
              :class="[
                'w-9 h-9 rounded-full flex items-center justify-center transition',
                viewMode === 'desktop' ? 'text-[#1877F2] bg-[#E7F3FF]' : 'hover:bg-gray-100',
              ]"
            >
              <svg
                class="w-5 h-5"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                viewBox="0 0 24 24"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
              </svg>
            </button>

            <button
              @click="viewMode = 'mobile'"
              :class="[
                'w-9 h-9 rounded-full flex items-center justify-center transition',
                viewMode === 'mobile' ? 'text-[#1877F2] bg-[#E7F3FF]' : 'hover:bg-gray-100',
              ]"
            >
              <svg
                class="w-5 h-5"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                viewBox="0 0 24 24"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
                <line x1="12" y1="18" x2="12.01" y2="18"></line>
              </svg>
            </button>
          </div>
        </div>

        <div
          class="flex-1 border border-[#E5E5E5] rounded-lg overflow-y-auto custom-scrollbar bg-[#F0F2F5]"
        >
          <template v-if="viewMode === 'desktop'">
            <div class="bg-white">
              <div
                v-if="form.coverImage"
                class="w-full h-[350px] bg-gray-100 relative overflow-hidden"
              >
                <img
                  :src="form.coverImage"
                  class="w-full h-full object-cover"
                  :style="{ objectPosition: `50% ${coverPosition}%` }"
                />
              </div>

              <div class="px-5 pt-6 pb-4">
                <div
                  class="w-[72px] h-[76px] rounded-xl border border-[#CED0D4] overflow-hidden flex flex-col mb-4 bg-white shadow-sm"
                >
                  <div class="h-[14px] bg-[#D93025] w-full"></div>
                  <div
                    class="flex-1 flex items-center justify-center font-bold text-[32px] text-[#050505] leading-none mt-1"
                  >
                    {{ dayNumber }}
                  </div>
                </div>

                <div class="flex justify-between items-end mb-6">
                  <div>
                    <div class="text-[#D93025] font-bold text-[15px] mb-1 uppercase tracking-tight">
                      {{ formattedDateFull }} {{ form.startTime }} {{ t('createEvent.timezone') }}
                    </div>
                    <h2
                      :class="[
                        'font-bold text-[36px] leading-tight mb-1',
                        form.eventName ? 'text-[#050505]' : 'text-[#BCC0C4]',
                      ]"
                    >
                      {{ form.eventName || t('createEvent.eventName') }}
                    </h2>
                    <div class="text-[#65676B] font-semibold text-[15px]">
                      {{ t('createEvent.facebookLive') }}
                    </div>
                  </div>
                  <button
                    class="bg-[#1877F2] text-white font-semibold text-[15px] px-4 py-2 rounded-md hover:bg-[#166FE5] shrink-0 mb-1"
                  >
                    {{ t('createEvent.joinLiveStream') }}
                  </button>
                </div>

                <div class="bg-[#F0F2F5] rounded-xl p-3 flex items-center justify-between mb-4">
                  <div class="flex items-center gap-2">
                    <img
                      :src="auth.currentUser?.avatar || 'https://i.pravatar.cc/150?u=bartosz'"
                      class="w-[35px] h-[35px] rounded-full border border-[#CED0D4]"
                    />
                    <span class="text-[15px] font-bold"
                      >{{ auth.currentUser?.name }}
                      <span class="font-normal text-[#050505]">{{
                        t('createEvent.invitesYou')
                      }}</span></span
                    >
                  </div>
                  <div class="flex gap-2">
                    <button
                      class="px-3 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md font-semibold text-[14px]"
                    >
                      {{ t('createEvent.willAttend') }}
                    </button>
                    <button
                      class="px-3 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md font-semibold text-[14px]"
                    >
                      {{ t('createEvent.maybe') }}
                    </button>
                    <button
                      class="px-3 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md font-semibold text-[14px]"
                    >
                      {{ t('createEvent.invite') }}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div class="p-6">
              <div
                class="bg-white rounded-[12px] border border-[#CED0D4] p-5 max-w-[680px] shadow-sm"
              >
                <h3 class="text-[20px] font-bold text-[#050505] mb-3">
                  {{ t('createEvent.details') }}
                </h3>
                <p class="text-[#65676B] text-[15px] whitespace-pre-line mb-4">
                  {{ form.description || t('createEvent.noDescription') }}
                </p>
                <div class="flex gap-2">
                  <span class="px-3 py-1 bg-[#E4E6EB] rounded-md text-[14px] font-semibold">{{
                    t('createEvent.online')
                  }}</span>
                  <span class="px-3 py-1 bg-[#E4E6EB] rounded-md text-[14px] font-semibold">{{
                    t('createEvent.liveVideo')
                  }}</span>
                </div>
              </div>
            </div>
          </template>

          <template v-else> </template>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* Pasek przewijania */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #bcc0c4;
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #8d949e;
}
</style>
