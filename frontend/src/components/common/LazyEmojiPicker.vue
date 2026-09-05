<script setup lang="ts">
import { ref, onMounted, shallowRef, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import BaseModal from './BaseModal.vue'

const { t } = useI18n()
const showModal = ref(false)

const props = defineProps<{
  class?: string
  customReactions?: string[]
}>()

const emit = defineEmits<{
  select: [emoji: { native: string }]
  'modal-state': [isOpen: boolean]
  'update:customReactions': [reactions: string[]]
}>()

// --- REAKTYWNOŚĆ: LOKALNA KOPIA ---
const localReactions = ref<string[]>(props.customReactions ? [...props.customReactions] : [])

const Picker = shallowRef<any>(null)
const EmojiComponent = shallowRef<any>(null)
const emojiIndex = shallowRef<any>(null)
const isLoaded = ref(false)

const pickerWrapperRef = ref<HTMLElement | null>(null)
const customSectionRef = ref<HTMLElement | null>(null)

const activeIndex = ref<number>(0)
const selectedNative = ref<string | null>(null)

onMounted(async () => {
  const [emojiMart, { default: data }] = await Promise.all([
    import('emoji-mart-vue-fast/src'),
    import('emoji-mart-vue-fast/data/all.json'),
  ])

  const { Picker: PickerComp, EmojiIndex, Emoji: EmojiComp } = emojiMart as any
  emojiIndex.value = new EmojiIndex(data)
  Picker.value = PickerComp
  EmojiComponent.value = EmojiComp
  isLoaded.value = true
})

// Odświeżanie podświetlenia wybranego emoji w pickerze
const refreshPickerSelection = () => {
  if (!selectedNative.value) return
  nextTick(() => {
    const modalPicker = document.querySelector('.modal-inner-picker')
    if (!modalPicker) return
    modalPicker
      .querySelectorAll('.is-custom-selected')
      .forEach((el) => el.classList.remove('is-custom-selected'))
    const buttons = modalPicker.querySelectorAll('button.emoji-mart-emoji')
    buttons.forEach((btn) => {
      if (btn.getAttribute('aria-label')?.includes(selectedNative.value!)) {
        btn.classList.add('is-custom-selected')
      }
    })
  })
}

// KLIKNIĘCIE W KÓŁKO (GÓRA)
const selectEditSlot = (emoji: string, index: number) => {
  activeIndex.value = index
  selectedNative.value = emoji
  refreshPickerSelection()
}

const saveReactions = () => {
  emit('update:customReactions', [...localReactions.value])
  showModal.value = false
}

// Dodaj to pod definicją localReactions
const hasChanges = ref(false)

// Zaktualizuj handleEmojiSelect
const handleEmojiSelect = (emoji: { native: string }) => {
  localReactions.value[activeIndex.value] = emoji.native
  hasChanges.value = true // Aktywujemy przycisk zapisu
  selectedNative.value = emoji.native
  refreshPickerSelection()
}

// Zaktualizuj resetReactions
const resetReactions = () => {
  localReactions.value = ['😂', '😆', '😮', '😢', '😡', '🥹']
  hasChanges.value = true // Reset to też zmiana względem oryginału
  selectedNative.value = localReactions.value[activeIndex.value]
  refreshPickerSelection()
}

// Synchronizacja, gdyby rodzic zmienił tablicę z zewnątrz
watch(
  () => props.customReactions,
  (newVal) => {
    localReactions.value = [...newVal]
  },
  { deep: true },
)

watch(showModal, (val) => {
  emit('modal-state', val)
  if (val) {
    nextTick(() => {
      const scrollEl = document.querySelector('.modal-inner-picker .emoji-mart-scroll')
      scrollEl?.addEventListener('scroll', refreshPickerSelection, { passive: true })
      selectedNative.value = localReactions.value[activeIndex.value]
      refreshPickerSelection()
    })
  }
})

// Wstrzykiwanie sekcji do głównego pickera
watch(isLoaded, async (loaded) => {
  if (loaded) {
    await nextTick()
    setTimeout(() => {
      const pickerEl = pickerWrapperRef.value
      const scrollEl = pickerEl?.querySelector('.emoji-mart-scroll')
      const firstCategoryEl = scrollEl?.querySelector('.emoji-mart-category')
      if (firstCategoryEl && customSectionRef.value) {
        firstCategoryEl.insertAdjacentElement('beforebegin', customSectionRef.value)
        customSectionRef.value.classList.add('is-injected')
      }
    }, 100)
  }
})
</script>

<template>
  <div class="custom-messenger-picker-container" :class="props.class">
    <div v-if="!isLoaded" class="p-4 w-[320px] bg-theme-bg-secondary rounded-xl">
      <div class="animate-pulse flex flex-col gap-2">
        <div class="h-10 bg-theme-bg-tertiary rounded-full w-full"></div>
        <div class="grid grid-cols-6 gap-1 mt-2">
          <div v-for="i in 18" :key="i" class="h-10 w-10 bg-theme-bg-tertiary rounded"></div>
        </div>
      </div>
    </div>

    <div
      v-else-if="Picker && emojiIndex"
      ref="pickerWrapperRef"
      class="picker-wrapper  border border-theme-border"
    >
      <component
        :is="Picker"
        :data="emojiIndex"
        class="custom-picker"
        set="facebook"
        :perLine="6"
        :emojiSize="30"
        :showPreview="false"
        @select="(e: any) => emit('select', e)"
      />

      <div v-if="props.customReactions" ref="customSectionRef" class="custom-reactions-section">
        <div class="reactions-header">
          <span class="reactions-title">{{ t('emojiPicker.yourReactions') }}</span>
          <button @click="showModal = true" class="reactions-customize-btn">
            {{ t('emojiPicker.customize') }}
          </button>
        </div>
        <div class="reactions-list">
          <component
            :is="EmojiComponent"
            v-for="emoji in props.customReactions.slice(0, 6)"
            :key="emoji"
            :data="emojiIndex"
            :emoji="emoji"
            set="facebook"
            :size="30"
            @click.stop="emit('select', { native: emoji })"
            class="reaction-btn"
          />
        </div>
      </div>
    </div>
  </div>

  <BaseModal
    v-if="showModal"
    :title="t('emojiPicker.customizeReactions')"
    @close="showModal = false"
  >
    <div class="modal-content ov w-[450px] px-5 pb-4" @click.stop>
      <div class="selection-display pt-4">
        <div class="reactions-row">
          <div
            v-for="(emoji, index) in localReactions"
            :key="`${index}-${emoji}`"
            class="reaction-edit-btn"
            :class="{ 'is-active': activeIndex === index }"
            @click="selectEditSlot(emoji, index)"
          >
            <component
              :is="EmojiComponent"
              v-if="emojiIndex && EmojiComponent"
              :data="emojiIndex"
              :emoji="emoji"
              set="facebook"
              :size="32"
            />
          </div>
        </div>
        <p class="text-[14px] text-gray-500 mt-4 text-center">
          {{ t('emojiPicker.replaceInstruction') }}
        </p>
      </div>

      <hr class="my-5 border-gray-200" />

      <div class="modal-picker-container h-[350px]">
        <component
          v-if="Picker && emojiIndex"
          :is="Picker"
          :data="emojiIndex"
          set="facebook"
          :showPreview="false"
          :showSkinTones="false"
          :emojiSize="30"
          @select="handleEmojiSelect"
          class="modal-inner-picker"
        />
      </div>

      <div class="modal-footer flex justify-between items-center px-2">
        <button
          @click="resetReactions"
          class="text-[15px] font-semibold text-[#0064d1] hover:underline bg-transparent border-none p-0 cursor-pointer"
        >
          {{ t('emojiPicker.reset') }}
        </button>

        <button
          @click="hasChanges && saveReactions()"
          :class="[
            'px-3 py-2 rounded-xl font-semibold text-[15px] transition-colors duration-200',
            hasChanges
              ? 'bg-[#0064d1] text-white cursor-pointer hover:bg-[#0056b3]'
              : 'bg-[#e4e6eb] text-[#bcc0c4] cursor-not-allowed',
          ]"
        >
          {{ t('emojiPicker.save') }}
        </button>
      </div>
    </div>
  </BaseModal>
</template>

<style>
@import 'emoji-mart-vue-fast/css/emoji-mart.css';

.custom-messenger-picker-container {
  width: 320px;
  background: var(--color-bg-secondary);

  border-radius: 40px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.picker-wrapper {
  height: 310px;
  border-radius: 40px;
}
/* Style dla przycisku Zapisz */
.btn-save-disabled {
  background-color: #e4e6eb !important;
  color: #bcc0c4 !important;
  cursor: not-allowed !important;
}
.modal-inner-picker .emoji-mart-category {
  display: grid !important;
  grid-template-columns: repeat(8, 1fr) !important;
}

.modal-inner-picker .emoji-mart-category-label {
  grid-column: span 8 !important;
}
.btn-save-active {
  background-color: #1883f5 !important;
  color: white !important;
  cursor: pointer !important;
}

/* Resetuj jako link tekstowy */
.btn-reset-text {
  color: #0064d1;
  background: none;
  border: none;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
}
.emoji-mart-bar-anchors {
  border-radius: 0 0 20px 20px !important;
}
.modal-inner-picker.emoji-mart,
.custom-picker.emoji-mart {
  height: 100% !important;
  width: 100% !important;
  border: none !important;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-secondary);
  font-family: inherit;
  border-radius: 10px;
}

.emoji-mart-search {
  margin-top: 12px !important;
  margin: 0 6px;
  position: relative;
  z-index: 20;
}

.emoji-mart-search input {
  background-color: var(--color-bg-tertiary) !important;
  border: none !important;
  border-radius: 20px !important;
  padding: 8px 16px 8px 36px !important;
  font-size: 15px !important;
  outline: none !important;
  color: var(--color-text) !important;
  width: 100%;
}
/* 1. Wymuszamy układ siatki na kontenerze, który trzyma emoji */
.emoji-mart-category {
  display: grid !important;
  /* Tworzymy 6 równych kolumn */
  grid-template-columns: repeat(6, 1fr) !important;
  /* Usuwamy wszelkie flexy/floats które mogą tam być */
  display: grid !important;
  width: 100% !important;
}

/* 2. Resetujemy każde emoji, by nie rozpychało rzędu */
.emoji-mart-emoji {
  width: auto !important;
  max-width: 100% !important;
  display: block !important;
  padding: 4px 0 !important;
}
.emoji-mart-category .emoji-mart-emoji:hover:before,
.emoji-mart-emoji-selected:before {
  background-color: #f6f3f4 !important;
  border-radius: 8px !important;
}

/* 3. Przycisk wewnątrz emoji musi wypełnić komórkę grida */
.emoji-mart-emoji button {
  width: 100% !important;
  padding: 5px 0 !important;
  height: 40px !important; /* Stała wysokość ułatwia przeliczanie kategorii */
}

/* 4. Nagłówek kategorii MUSI zajmować wszystkie 6 kolumn */
.emoji-mart-category-label {
  grid-column: span 6 !important;
  width: 100% !important;
  display: block !important;
  background: var(--color-bg-secondary) !important;
}

/* 5. Usuwamy puste elementy wewnątrz listy, które mogą psuć grid */
.emoji-mart-category-list > div:not(.emoji-mart-emoji):not(.emoji-mart-category-label) {
  display: none !important;
}
.custom-reactions-section.is-injected {
  /* Upewnij się, że Twoja sekcja też jest traktowana jako element Grida
     zajmujący całą szerokość przed pierwszą kategorią */
  grid-column: span 6 !important;
  display: block !important;
}
/* Styl dla emoji, które pasuje do wybranego ID */
/* Musimy użyć dynamicznego stylu lub klasy wstrzykniętej,
   ale najprościej zadziała to przez nadpisanie stylu wybranego elementu */
.is-custom-selected {
  pointer-events: none; /* Zapobiega klikalności, bo to tylko wizualne podświetlenie */
  border-radius: 8px !important;
  background-color: #ced0d4 !important;
}

.emoji-mart-search::before {
  content: '';
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  background-color: var(--color-text-secondary) !important;
  -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='black' stroke-width='2.5'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' d='M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z' /%3E%3C/svg%3E")
    no-repeat center;
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='black' stroke-width='2.5'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' d='M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z' /%3E%3C/svg%3E")
    no-repeat center;
  mask-size: contain;
  -webkit-mask-size: contain;
  pointer-events: none;
  z-index: 10;
}
.emoji-mart-search input {
  background-color: #f1f2f5 !important;
}
.emoji-mart-scroll {
  flex: 1;
  padding-top: 4px !important;
}

.emoji-mart-bar {
  border: none !important;
  background: var(--color-bg-secondary) !important;
}

.emoji-mart-bar-anchors {
  order: 2;
  border-top: 1px solid var(--color-border) !important;
  padding: 4px 0 !important;
}
/* Kontener ikony musi być bazą dla tooltipa */
.emoji-mart-anchor {
  position: relative !important;
  overflow: visible !important;
  border: none !important; /* Ważne: tooltip nie może zostać przycięty */
}
.emoji-mart-anchor-bar {
  display: none !important;
}
/* Tworzenie dymka tooltipa */
.emoji-mart-anchor:hover::after {
  content: attr(data-title);
  position: absolute;
  bottom: 100%; /* Nad ikoną */
  left: 50%;
  transform: translateX(-50%); /* Odstęp od ikony */
  z-index: 9999;
  background-color: var(--color-tooltip-bg) !important;
  color: var(--color-tooltip-text) !important;
  border: 1px solid var(--color-tooltip-border) !important;

  padding: 8px 14px !important;
  border-radius: 12px !important; /* Na nowym SS dymek jest bardziej "obły" */

  /* Typografia */
  font-size: 13px !important;
  font-weight: 400;
  line-height: 1.4;
  white-space: nowrap;
}

/* Strzałka pod tooltipem */
.emoji-mart-anchor:hover::before {
  content: '';
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-bottom: 3px;

  border-width: 5px;
  border-style: solid;
  border-color: #333 transparent transparent transparent;
  z-index: 999;
  pointer-events: none;
}
.emoji-mart-anchor-icon {
  color: var(--color-text-secondary) !important;
}
.emoji-mart-anchor-selected {
  color: var(--color-primary) !important;
}
.emoji-mart-anchor-bar {
  background-color: var(--color-primary) !important;
}
.emoji-mart-category-label {
  background-color: var(--color-bg-secondary) !important;
  font-weight: 400 !important;
  font-size: 13px !important;
  color: var(--color-text-secondary) !important;
}

/* --- NASZA SEKCJA --- */
.custom-reactions-section {
  display: none;
}

/* Teraz po prostu układa się jako pierwszy blok na górze listy emoji */
.custom-reactions-section.is-injected {
  display: block;
  padding: 8px;
  background-color: var(--color-bg-secondary);
}

.reactions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.reactions-title {
  font-size: 13px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

.reactions-customize-btn {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.reactions-customize-btn:hover {
  text-decoration: underline;
}

.reactions-list {
  display: flex;
  padding: 0 4px;
  justify-content: space-between;
  align-items: center;
}

.reaction-btn {
  font-size: 30px;
  background: none;
  border: none;
  cursor: pointer;
  transition: transform 0.2s;
  padding: 0;
  line-height: 1;
}

.reaction-btn:hover {
  transform: scale(1.15);
}
/* Ukrywa całą kategorię "Ostatnie" w obszarze scrollowania */
/* Ukrywa ikonę zegara (Ostatnio używane) na dolnym pasku */
.emoji-mart-anchor[aria-label='Recent'],
.emoji-mart-anchor[aria-label='Ostatnio używane'],
.emoji-mart-anchor[aria-label='frequently_used'] {
  display: none !important;
}

/* Ukrywa nagłówek i listę sekcji "Ostatnio używane" w obszarze scrollowania */
.emoji-mart-category[aria-label='Recent'],
.emoji-mart-category[aria-label='Ostatnio używane'],
.emoji-mart-category[aria-label='Często używane'],
.emoji-mart-category[aria-label='Frequently Used'] {
  display: none !important;
}
.selection-display {
  text-align: center;
  padding: 10px 0;
}

.reactions-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.reaction-edit-btn {
  font-size: 26px;
  width: 38px;
  height: 38px;

  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 2px solid transparent;
  background: #f0f2f5;
  cursor: pointer;
  transition: all 0.2s;
}

.reaction-edit-btn.is-active {
  background: #e7f3ff;
  border-color: #0084ff;
}

.modal-divider {
  border: none;
  border-top: 1px solid var(--color-border);
  margin: 0;
}

.modal-picker-container {
  height: 350px;
}

/* Ukrywamy zbędne elementy wewnątrz modala */
.modal-inner-picker.emoji-mart {
  border: none !important;
  width: 100% !important;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px;
}

.btn-reset {
  color: var(--color-primary);
  font-weight: 600;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px 16px;
}

.btn-save {
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary); /* Zmień na czarny/niebieski gdy nastąpi zmiana */
  font-weight: 600;
  border: none;
  border-radius: 6px;
  padding: 8px 24px;
  cursor: not-allowed;
}

/* Jeśli chcesz, by przycisk "Zapisz" był aktywny po zmianach: */
.btn-save.active {
  background: var(--color-primary);
  color: white;
  cursor: pointer;
}
.selection-display {
  display: flex;
  flex-direction: column;
  align-items: center;

  background: var(--color-bg-secondary);
}

.reactions-row {
  display: flex;
  justify-content: center;
  align-items: center;
  /* Ikony blisko siebie jak na zdjęciu */
  gap: 2px;

  height: 44px;
}

.reaction-edit-btn {
  background: transparent;
  border: 2px solid transparent;
  padding: 1px; /* Zapewnia miejsce na tło wokół emoji */
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 14px; /* Zaokrąglony prostokąt (pill) */
  display: flex;
  align-items: center;
  justify-content: center;

  /* Efekt dla ikon nieaktywnych: mocno wyblakłe */
  filter: grayscale(1);
  opacity: 0.25;
}

.reaction-edit-btn.is-active {
  /* Tło i ramka dla aktywnej ikony */
  background-color: #ced0d4; /* Jasnoszare tło */
  border-color: #ced0d4; /* Subtelna szara ramka */

  /* Powrót do koloru i oryginalnej skali */
  filter: grayscale(0);
  opacity: 1;
}

.reaction-edit-btn .emoji-icon {
  line-height: 1;
  display: block;
}
</style>
