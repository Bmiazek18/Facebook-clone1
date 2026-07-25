<template>
  <div class="relative w-full">
    <Dropdown class="w-full" :distance="4" :disabled="disabled" placement="bottom-start">
      <!-- Przycisk wyzwalający (Trigger) - dodano ref="triggerRef" aby móc zmierzyć jego szerokość -->
      <div :class="triggerClasses" ref="triggerRef">
        <!-- Pływająca etykieta -->
        <label
          :class="[
            'absolute left-4 z-10 origin-[0] transform transition-all duration-300 pointer-events-none',
            hasValidSelection ? 'top-2 scale-75' : 'top-1/2 -translate-y-1/2 scale-100',
            error ? 'text-[#d32f2f]' : 'text-[#606770]',
          ]"
        >
          {{ label || placeholder }}
        </label>

        <!-- Wybrana wartość -->
        <div class="flex items-center gap-2 mt-0.5 w-full">
          <component
            :is="selectedOption?.icon"
            v-if="selectedOption?.icon && hasValidSelection"
            class="text-inherit"
            :size="20"
          />
          <span
            v-if="hasValidSelection"
            class="text-[15px] text-[#1c1e21] font-medium leading-none truncate"
          >
            {{ selectedOption?.title }}
          </span>
          <span v-else class="text-[15px] opacity-0 pointer-events-none leading-none">
            Spacer
          </span>
        </div>

        <ChevronDownIcon class="text-[#1c1e21] shrink-0" :size="20" />
      </div>

      <!-- Rozwijana lista (Popper) - przypisujemy dynamiczną szerokość przez :style -->
      <template #popper="{ hide }">
        <div :class="popperClasses" :style="{ width: dropdownWidth }">
          <div
            v-for="option in selectableOptions"
            :key="option.id"
            @click="
              selectOption(option.id),
              hide()
            "
            :class="getItemClasses(option.id)"
          >
            <div
              v-if="option.icon"
              :class="[
                'p-2 rounded-full flex items-center justify-center',
                modelValue === option.id ? 'bg-white/20' : 'bg-[#f5f6f7]',
              ]"
            >
              <component :is="option.icon" :size="20" />
            </div>

            <!-- Tytuł opcji (dodano truncate, na wypadek bardzo długich tekstów w wąskim oknie) -->
            <span class="text-[15px] font-semibold flex-1 truncate">{{ option.title }}</span>

            <CheckCircleIcon
              v-if="modelValue === option.id && variant === 'classic'"
              :size="20"
              class="text-[#1877f2] ml-auto shrink-0"
            />
          </div>
        </div>
      </template>
    </Dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { Dropdown } from 'floating-vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue'
import type { FunctionalComponent } from 'vue'

export interface DropdownOption {
  id: string
  title: string
  icon?: FunctionalComponent | string
}

const props = withDefaults(
  defineProps<{
    modelValue?: string | null
    options: DropdownOption[]
    label?: string
    placeholder?: string
    disabled?: boolean
    error?: boolean
    variant?: 'classic' | 'new'
  }>(),
  {
    disabled: false,
    error: false,
    variant: 'new',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

// --- LOGIKA SZEROKOŚCI (RESIZE OBSERVER) ---
const triggerRef = ref<HTMLElement | null>(null)
const dropdownWidth = ref<string>('auto')
let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  if (triggerRef.value) {
    // 1. Ustawienie szerokości przy zamontowaniu komponentu
    dropdownWidth.value = `${triggerRef.value.offsetWidth}px`

    // 2. Nasłuchiwanie na zmiany szerokości (np. obracanie ekranu w telefonie)
    resizeObserver = new ResizeObserver((entries) => {
      for (const entry of entries) {
        dropdownWidth.value = `${(entry.target as HTMLElement).offsetWidth}px`
      }
    })
    resizeObserver.observe(triggerRef.value)
  }
})

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
})

// --- LOGIKA STANU ---
const hasValidSelection = computed(() => {
  return props.modelValue !== undefined && props.modelValue !== null && props.modelValue !== ''
})

const selectedOption = computed(() => {
  if (!hasValidSelection.value) return null
  return props.options.find((option) => option.id === props.modelValue) || null
})

const selectableOptions = computed(() => {
  return props.options.filter((opt) => opt.id !== '')
})

function selectOption(id: string) {
  if (props.disabled) return
  emit('update:modelValue', id)
}

// --- DYNAMICZNE KLASY ---
const triggerClasses = computed(() => {
  const base = 'w-full relative flex items-center justify-between transition-all'
  const disabledClass = props.disabled
    ? 'opacity-50 cursor-not-allowed bg-[#f5f6f7]'
    : 'cursor-pointer'

  if (props.variant === 'new') {
    const shape = 'px-4 pb-2.5 pt-6 border rounded-xl bg-transparent hover:bg-gray-50'
    const state = props.error
      ? 'border-[#d32f2f] ring-1 ring-[#d32f2f]'
      : 'border-[#ccd0d5] focus:border-[#1877f2] focus:ring-1 focus:ring-[#1877f2]'
    return [base, disabledClass, shape, state]
  } else {
    const shape = 'px-3 pb-2 pt-6 border rounded-[6px] bg-[#f5f6f7] hover:bg-white'
    const state = props.error
      ? 'border-[#b0281c] ring-1 ring-[#b0281c]/20'
      : 'border-[#ccd0d5] focus:border-[#1877f2]'
    return [base, disabledClass, shape, state]
  }
})

const popperClasses = computed(() => {
  // UWAGA: Usunięto sztywną klasę 'w-[380px]'
  const base =
    'bg-white shadow-[0_8px_30px_rgba(0,0,0,0.12)] border border-gray-100 max-h-[350px] overflow-y-auto'
  return props.variant === 'new' ? `${base} rounded-xl py-2` : `${base} rounded-xl p-2`
})

const getItemClasses = (id: string) => {
  const isSelected = props.modelValue === id
  const base = 'flex items-center gap-3 cursor-pointer transition-colors'

  if (props.variant === 'new') {
    const shape = 'px-4 py-3'
    const colors = isSelected ? 'bg-[#444950] text-white' : 'text-[#1c1e21] hover:bg-gray-100'
    return [base, shape, colors]
  } else {
    const shape = 'p-3 rounded-lg mb-1 last:mb-0'
    const colors = isSelected ? 'bg-[#e7f3ff] text-[#1877f2]' : 'text-[#1c1e21] hover:bg-[#f2f2f2]'
    return [base, shape, colors]
  }
}
</script>
