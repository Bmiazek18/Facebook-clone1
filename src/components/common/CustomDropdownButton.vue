<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'

export interface DropdownOption {
  label: string
  value: string
  subOptions?: DropdownOption[]
}

const props = withDefaults(defineProps<{
  label: string
  options: DropdownOption[]
  dropdownTitle?: string
  hasChevron?: boolean
  modelValue?: string | null
  badgeMode?: boolean
}>(), {
  hasChevron: true,
  modelValue: null,
  badgeMode: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | null): void
}>()

const isOpen = ref(false)
const activeSubMenu = ref<string | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)

const toggle = () => {
  isOpen.value = !isOpen.value
  activeSubMenu.value = null
}

const handleSelect = (val: string) => {
  emit('update:modelValue', val)
  isOpen.value = false
  activeSubMenu.value = null
}

const handleClear = (e?: Event) => {
  if (e) e.stopPropagation()
  emit('update:modelValue', null)
}

// Zamykanie dropdownu kliknięciem na zewnątrz
const closeDropdown = (e: MouseEvent) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
    isOpen.value = false
    activeSubMenu.value = null
  }
}

onMounted(() => document.addEventListener('click', closeDropdown))
onUnmounted(() => document.removeEventListener('click', closeDropdown))

// Helpers do sprawdzania stanu aktywnego
const getSelectedLabel = computed(() => {
  for (const opt of props.options) {
    if (opt.value === props.modelValue) return opt.label
    if (opt.subOptions) {
      const sub = opt.subOptions.find(s => s.value === props.modelValue)
      if (sub) return sub.label
    }
  }
  return props.label
})

const isRowActive = (opt: DropdownOption) => {
  if (opt.value === props.modelValue) return true
  if (opt.subOptions?.some(s => s.value === props.modelValue)) return true
  return false
}

const getActiveSubLabel = (opt: DropdownOption) => {
  return opt.subOptions?.find(s => s.value === props.modelValue)?.label
}
</script>

<template>
  <div class="relative inline-block" ref="dropdownRef">

    <!-- AKTYWNY FILTR (Niebieski przycisk) -->
    <button
      v-if="modelValue"
      @click="toggle"
      class="bg-[#e7f3ff] hover:bg-[#d8eaff] dark:bg-[#252f3d] dark:hover:bg-[#2d3a4d] transition-colors rounded-lg px-3 py-1.5 flex items-center gap-1.5 cursor-pointer"
    >
      <span class="text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff]">
        {{ badgeMode ? label : getSelectedLabel }}
      </span>

      <!-- Pigułka z liczbą (Dla trybu badgeMode, np. "Więcej filtrów 1") -->
      <span v-if="badgeMode" class="ml-0.5 flex items-center justify-center bg-[#1877f2] dark:bg-[#4599ff] text-white text-[12px] font-bold h-[18px] min-w-[18px] rounded-full px-1">
        1
      </span>

      <!-- Ikona usuwania filtru (Dla standardowego trybu) -->
      <svg
        v-else
        @click="handleClear"
        viewBox="0 0 24 24"
        fill="currentColor"
        class="w-[18px] h-[18px] text-[#1877f2] dark:text-[#4599ff] hover:opacity-80 ml-0.5"
      >
        <path d="M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z"/>
      </svg>
    </button>

    <!-- STANDARDOWY PRZYCISK DROPDOWN (Nieaktywny) -->
    <button
      v-else
      @click="toggle"
      :class="[
        'bg-[#e4e6eb] dark:bg-[#3a3b3c] hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50] transition-colors rounded-lg px-3 py-1.5 flex items-center gap-1.5 cursor-pointer text-[#050505] dark:text-[#e4e6eb]',
        isOpen ? 'bg-[#d8dadf] dark:bg-[#4e4f50]' : ''
      ]"
    >
      <span class="text-[15px] font-semibold">{{ label }}</span>
      <ChevronDownIcon v-if="hasChevron" :size="20" class="text-[#050505] dark:text-[#b0b3b8] -mr-1" />
    </button>

    <!-- Menu rozwijane -->
    <div
      v-if="isOpen"
      class="absolute top-full left-0 mt-2 w-64 bg-white dark:bg-[#242526] border border-gray-200 dark:border-[#3e4042] rounded-xl shadow-xl z-50 py-2 animate-in fade-in zoom-in-95 duration-100"
    >
      <div v-if="dropdownTitle" class="px-4 py-2 text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-1">
        {{ dropdownTitle }}
      </div>

      <div
        v-for="option in options"
        :key="option.value"
        class="relative group"
        @mouseenter="activeSubMenu = option.value"
        @mouseleave="activeSubMenu = null"
      >
        <!-- Aktywny wiersz z opcją zagnieżdżoną -->
        <div
          v-if="isRowActive(option)"
          class="w-full text-left px-4 py-2.5 text-[15px] text-[#1877f2] dark:text-[#4599ff] bg-white dark:bg-[#242526] flex items-center justify-between cursor-default"
        >
          <span>{{ option.label }}</span>
          <div class="flex items-center gap-2">
            <span>{{ getActiveSubLabel(option) || '' }}</span>
            <svg @click.stop="handleClear()" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5 cursor-pointer hover:opacity-80">
              <path d="M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z"/>
            </svg>
          </div>
        </div>

        <!-- Nieaktywny wiersz -->
        <button
          v-else
          @click="!option.subOptions && handleSelect(option.value)"
          class="w-full text-left px-4 py-2.5 text-[15px] text-[#050505] dark:text-[#e4e6eb] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors flex items-center justify-between cursor-pointer"
        >
          <span>{{ option.label }}</span>
          <ChevronRightIcon v-if="option.subOptions" :size="20" class="text-[#65676b] dark:text-[#b0b3b8]" />
        </button>

        <!-- Zagnieżdżone Submenu -->
        <div
          v-if="option.subOptions && activeSubMenu === option.value && !isRowActive(option)"
          class="absolute top-0 left-full ml-1 w-[280px] bg-white dark:bg-[#242526] border border-gray-200 dark:border-[#3e4042] rounded-xl shadow-xl z-50 py-2 animate-in fade-in zoom-in-95 duration-100"
        >
          <button
            v-for="subOption in option.subOptions"
            :key="subOption.value"
            @click="handleSelect(subOption.value)"
            class="w-full text-left px-4 py-2.5 text-[15px] text-[#050505] dark:text-[#e4e6eb] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors cursor-pointer"
          >
            {{ subOption.label }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
