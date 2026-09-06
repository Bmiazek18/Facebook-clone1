<script setup lang="ts">
import type { PageForm } from '@/types/pageCreation'

const props = defineProps<{
  form: PageForm
}>()

defineEmits<{
  'prev-step': []
  'next-step': []
}>()

const handleProfileImage = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) {
    props.form.profileImage = URL.createObjectURL(file)
  }
}

const handleCoverImage = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) {
    props.form.coverImage = URL.createObjectURL(file)
  }
}

const removeProfileImage = () => {
  props.form.profileImage = null
}

const removeCoverImage = () => {
  props.form.coverImage = null
}
</script>

<template>
  <div class="flex-1 overflow-y-auto px-4 py-2 custom-scrollbar">
    <div class="text-[13px] text-[#65676B] mb-1 font-semibold">
      Krok 2 z 5
    </div>

    <h1 class="text-[24px] font-bold leading-tight mb-2 text-[#050505]">
      Dostosuj swoją stronę
    </h1>

    <p class="text-[15px] text-[#65676B] leading-snug mb-4">
      Twoje zdjęcie profilowe to jedna z pierwszych rzeczy, które widzą inne osoby. Spróbuj użyć logo lub obrazu, który można łatwo skojarzyć z Tobą.
    </p>

    <div class="inline-flex items-center gap-1.5 bg-[#25823B] text-white px-2 py-1 rounded-[4px] text-[13px] font-semibold mb-6">
      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
        <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm1 14.5h-2v-6h2zm0-8h-2v-2h2z"></path>
      </svg>
      Pomaga polepszyć wyniki reklam
    </div>

    <!-- Dodaj zdjęcie profilowe -->
    <div class="mb-4">
      <label v-if="!form.profileImage" class="block border border-[#CED0D4] rounded-[8px] p-6 text-center cursor-pointer hover:bg-[#F0F2F5] transition-colors">
        <input type="file" accept="image/*" class="hidden" @change="handleProfileImage" />
        <div class="w-[36px] h-[36px] bg-[#E4E6EB] rounded-full flex items-center justify-center mx-auto mb-3 text-[#050505]">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M19 11h-6V5h-2v6H5v2h6v6h2v-6h6v-2z"></path>
          </svg>
        </div>
        <div class="font-bold text-[17px] text-[#050505] mb-1">Dodaj zdjęcie profilowe</div>
        <div class="text-[15px] text-[#65676B]">lub przeciągnij i upuść</div>
      </label>
      <div v-else class="relative border border-[#CED0D4] rounded-[8px] h-[160px] overflow-hidden group">
        <img :src="form.profileImage" class="w-full h-full object-cover" />
        <button @click="removeProfileImage" class="absolute top-3 right-3 w-9 h-9 bg-white rounded-full flex items-center justify-center shadow-md hover:bg-gray-100 transition-colors z-10 text-[#050505]">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Dodaj zdjęcie w tle -->
    <div class="mb-6">
      <label v-if="!form.coverImage" class="block border border-[#CED0D4] rounded-[8px] p-6 text-center cursor-pointer hover:bg-[#F0F2F5] transition-colors">
        <input type="file" accept="image/*" class="hidden" @change="handleCoverImage" />
        <div class="w-[36px] h-[36px] bg-[#E4E6EB] rounded-full flex items-center justify-center mx-auto mb-3 text-[#050505]">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M19 11h-6V5h-2v6H5v2h6v6h2v-6h6v-2z"></path>
          </svg>
        </div>
        <div class="font-bold text-[17px] text-[#050505] mb-1">Dodaj zdjęcie w tle</div>
        <div class="text-[15px] text-[#65676B]">lub przeciągnij i upuść</div>
      </label>
      <div v-else class="relative border border-[#CED0D4] rounded-[8px] h-[160px] overflow-hidden group">
        <img :src="form.coverImage" class="w-full h-full object-cover" />
        <button @click="removeCoverImage" class="absolute top-3 right-3 w-9 h-9 bg-white rounded-full flex items-center justify-center shadow-md hover:bg-gray-100 transition-colors z-10 text-[#050505]">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Przycisk działania -->
    <button class="w-full bg-[#1877F2] text-white font-semibold text-[15px] py-2 rounded-md flex items-center justify-center gap-2 hover:bg-[#166FE5] transition-colors">
      <div class="w-5 h-5 bg-white rounded flex items-center justify-center">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="#1877F2">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14.5v-5H8.5v5H7v-5H5.5v5H4v-7h7v7h-2zM19 16.5h-5v-7h5v7z"></path>
        </svg>
      </div>
      Przycisk działania
    </button>
  </div>

  <div class="px-4 py-3 bg-white shrink-0 mt-auto border-t border-[#E5E5E5] shadow-[0_-2px_4px_rgba(0,0,0,0.05)]">
    <div class="text-[15px] text-[#050505] mb-1">
      Kondycja strony: <span class="font-bold">Przeciętna</span>
    </div>
    <div class="text-[15px] text-[#050505] leading-snug mb-3">
      W porównaniu z podobnymi stronami o dużej aktywności.
    </div>
    <div class="h-1.5 w-full bg-[#E5E5E5] rounded-full mb-4 flex overflow-hidden">
      <div class="bg-[#B58A14] w-[35%] h-full rounded-full"></div>
    </div>

    <div class="flex gap-2">
      <button
        @click="$emit('prev-step')"
        class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF] transition-colors"
      >
        Wstecz
      </button>
      <button
        @click="$emit('next-step')"
        class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E7F3FF] text-[#1877F2] hover:bg-[#DBE7F2] transition-colors"
      >
        Dalej
      </button>
    </div>
  </div>
</template>
