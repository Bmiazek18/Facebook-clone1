<script setup lang="ts">
import { ref, computed } from 'vue';

interface FilterOption {
  id: string;
  title: string;
  description: string;
}

const options: FilterOption[] = [
  {
    id: 'all',
    title: 'Wszystkie komentarze',
    description: 'Wyświetl wszystkie komentarze, w tym potencjalny spam.',
  },
  {
    id: 'relevant',
    title: 'Najtrafniejsze',
    description: 'Wyświetl najpierw komentarze znajomych i komentarze o największej aktywności.',
  },
  {
    id: 'newest',
    title: 'Najnowsze',
    description: 'Wyświetl wszystkie komentarze, rozpoczynając od najnowszych.',
  },
];

// --- Stan ---
const selectedId = ref<string>('relevant');

const currentLabel = computed(() => {
  return options.find(o => o.id === selectedId.value)?.title || 'Sortowanie';
});


const selectOption = (id: string, hide: () => void) => {
  selectedId.value = id;
  hide();
};
</script>

<template>
  <VDropdown
    placement="bottom-start"
    :distance="12"
    arrow
    :skidding="0"
    class="font-sans w-fit"
  >
    <button
      class="flex items-center space-x-1 text-theme-text-secondary font-semibold text-sm hover:underline focus:outline-none"
    >
      <span>{{ currentLabel }}</span>
      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 fill-current" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
      </svg>
    </button>

    <template #popper="{ hide }">
      <div class="w-[340px] p-2 text-left">
        <ul>
          <li
            v-for="option in options"
            :key="option.id"
            @click="selectOption(option.id, hide)"
            class="cursor-pointer px-3 py-2 rounded-lg transition-colors duration-150"
            :class="[
              selectedId === option.id
                ? 'bg-theme-hover'
                : 'hover:bg-gray-50'
            ]"
          >
            <div class="text-[15px] font-semibold text-theme-text leading-tight mb-0.5">
              {{ option.title }}
            </div>
            <div class="text-[13px] text-theme-text-secondary leading-snug">
              {{ option.description }}
            </div>
          </li>
        </ul>
      </div>
    </template>
  </VDropdown>
</template>

<style>

.v-popper--theme-dropdown .v-popper__inner {
  background: var(--color-bg-secondary);
  border-radius: 0.75rem; /* rounded-xl w Tailwind */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* Cień FB */
  border: 1px solid var(--color-border);
  padding: 0;
}

.v-popper--theme-dropdown .v-popper__arrow-inner {
  border-color: var(--color-bg-secondary); /* Kolor strzałki */
}
.v-popper--theme-dropdown .v-popper__arrow-outer {
  border-color: var(--color-border); /* Kolor obramowania strzałki */
}
</style>
