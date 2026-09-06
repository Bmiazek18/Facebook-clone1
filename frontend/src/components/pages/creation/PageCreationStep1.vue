<script setup lang="ts">
import CustomInput from '~/components/common/CustomInput.vue'
import type { PageForm } from '@/types/pageCreation'

defineProps<{
  form: PageForm
}>()

defineEmits<{
  'prev-step': []
  'next-step': []
}>()
</script>

<template>
  <div class="flex-1 overflow-y-auto px-4 py-2 custom-scrollbar">
    <div class="text-[13px] text-[#65676B] mb-1 font-semibold">{{ $t('pages.krok1Z5') }}</div>

    <h1 class="text-[24px] font-bold leading-tight mb-2 text-[#050505]">{{ $t('pages.ukonczKonfigurowanieStrony') }}</h1>

    <p class="text-[15px] text-[#65676B] leading-snug mb-4">{{ $t('pages.gotoweUtworzonoStrone') }}<span class="font-bold text-[#050505]">{{ form.pageName }}</span>{{ $t('pages.terazDodajWiecejSzczegolowych') }}</p>

    <div class="inline-flex items-center gap-1.5 bg-[#25823B] text-white px-2 py-1 rounded-[4px] text-[13px] font-semibold mb-6">
      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
        <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm1 14.5h-2v-6h2zm0-8h-2v-2h2z"></path>
      </svg>{{ $t('pages.pomagaPolepszycWynikiReklam') }}</div>

    <!-- KONTAKT -->
    <h2 class="text-[17px] font-bold text-[#050505] mb-3">{{ $t('home.contact') }}</h2>

    <div class="mb-4">
      <CustomInput
        v-model="form.website"
        :label="$t('pages.witrynaInternetowa')"
      />
    </div>

    <div class="flex gap-2 mb-4">
      <div class="w-[90px] border border-[#CED0D4] rounded-md flex items-center justify-between px-3 h-[56px] bg-[#F0F2F5] cursor-pointer hover:bg-[#E4E6EB] transition-colors shrink-0">
        <span class="text-[15px] font-medium">{{ form.phoneCode }}</span>
        <svg class="w-4 h-4 text-[#050505] fill-current" viewBox="0 0 24 24"><path d="M7 10l5 5 5-5H7z"/></svg>
      </div>
      <div class="flex-1">
        <CustomInput
          v-model="form.phone"
          :label="$t('pages.numerTelefonu')"
          type="tel"
        />
      </div>
    </div>

    <div class="mb-6">
      <CustomInput
        v-model="form.email"
        :label="$t('pages.adresEMailOpcjonalny')"
        type="email"
      />
    </div>

    <!-- LOKALIZACJA -->
    <h2 class="text-[17px] font-bold text-[#050505] mb-3">{{ $t('post.location') }}</h2>

    <div class="mb-4">
      <CustomInput
        v-model="form.address"
        :label="$t('pages.adres')"
      />
    </div>

    <div class="mb-4">
      <CustomInput
        v-model="form.city"
        :label="$t('pages.miejscowosc')"
      />
    </div>

    <div class="mb-6">
      <CustomInput
        v-model="form.zip"
        :label="$t('pages.kodPocztowy')"
      />
    </div>

    <!-- GODZINY OTWARCIA -->
    <h2 class="text-[17px] font-bold text-[#050505] mb-1">{{ $t('pages.godzinyOtwarcia') }}</h2>
    <p class="text-[15px] text-[#65676B] leading-snug mb-4">{{ $t('pages.poinformujUzytkownikowOGodzinach') }}</p>

    <div class="flex flex-col gap-4 mb-4">
      <label class="flex items-start gap-3 cursor-pointer group">
        <div class="w-5 h-5 rounded-full border-[2px] mt-0.5 flex items-center justify-center transition-colors" :class="form.hours === 'none' ? 'border-[#1877F2]' : 'border-[#8A8D91] group-hover:border-[#65676B]'">
          <div v-if="form.hours === 'none'" class="w-2.5 h-2.5 bg-[#1877F2] rounded-full"></div>
        </div>
        <div class="flex-1">
          <div class="text-[15px] font-medium text-[#050505] leading-tight mb-0.5">{{ $t('pages.niePodanoGodzinOtwarcia') }}</div>
          <div class="text-[13px] text-[#65676B] leading-snug">{{ $t('pages.nieWyswietlajZadnychGodzin') }}</div>
        </div>
        <input type="radio" value="none" v-model="form.hours" class="hidden" />
      </label>

      <label class="flex items-start gap-3 cursor-pointer group">
        <div class="w-5 h-5 rounded-full border-[2px] mt-0.5 flex items-center justify-center transition-colors" :class="form.hours === 'always' ? 'border-[#1877F2]' : 'border-[#8A8D91] group-hover:border-[#65676B]'">
          <div v-if="form.hours === 'always'" class="w-2.5 h-2.5 bg-[#1877F2] rounded-full"></div>
        </div>
        <div class="flex-1">
          <div class="text-[15px] font-medium text-[#050505] leading-tight mb-0.5">{{ $t('pages.czynneCalaDobe') }}</div>
          <div class="text-[13px] text-[#65676B] leading-snug">{{ $t('pages.otwarteCalodobowoPrzez7') }}</div>
        </div>
        <input type="radio" value="always" v-model="form.hours" class="hidden" />
      </label>

      <label class="flex items-start gap-3 cursor-pointer group">
        <div class="w-5 h-5 rounded-full border-[2px] mt-0.5 flex items-center justify-center transition-colors" :class="form.hours === 'selected' ? 'border-[#1877F2]' : 'border-[#8A8D91] group-hover:border-[#65676B]'">
          <div v-if="form.hours === 'selected'" class="w-2.5 h-2.5 bg-[#1877F2] rounded-full"></div>
        </div>
        <div class="flex-1">
          <div class="text-[15px] font-medium text-[#050505] leading-tight mb-0.5">{{ $t('pages.otwarteWWybranychGodzinach') }}</div>
          <div class="text-[13px] text-[#65676B] leading-snug">{{ $t('pages.podajOkresloneGodziny') }}</div>
        </div>
        <input type="radio" value="selected" v-model="form.hours" class="hidden" />
      </label>
    </div>
  </div>

  <div class="px-4 py-3 bg-white shrink-0 mt-auto border-t border-[#E5E5E5] shadow-[0_-2px_4px_rgba(0,0,0,0.05)]">
    <div class="text-[15px] text-[#050505] mb-1">{{ $t('pages.kondycjaStrony') }}<span class="font-bold">{{ $t('pages.wymagaDopracowania') }}</span>
    </div>
    <div class="text-[15px] text-[#050505] leading-snug mb-3">{{ $t('pages.wPorownaniuZPodobnymi') }}</div>
    <div class="h-1.5 w-full bg-[#E5E5E5] rounded-full mb-4"></div>

    <div class="flex gap-2">
      <button
        @click="$emit('prev-step')"
        class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF] transition-colors"
      >{{ $t('createLive.back') }}</button>
      <button
        @click="$emit('next-step')"
        class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E7F3FF] text-[#1877F2] hover:bg-[#DBE7F2] transition-colors"
      >{{ $t('common.next') }}</button>
    </div>
  </div>
</template>
