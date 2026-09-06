<script setup lang="ts">
// Import ikony gwiazdki z vue-material-design-icons
import StarIcon from 'vue-material-design-icons/Star.vue'

// Props z nazwą grupy (domyślnie "test")
const props = withDefaults(
  defineProps<{
    groupName?: string
  }>(),
  {
    groupName: 'test'
  }
)

const emit = defineEmits<{
  (e: 'view-all'): void
}>()
</script>

<template>
  <div class="inline-block font-sans select-none">
    <!-- Komponent z floating-vue -->
    <VDropdown
      placement="bottom-start"
      :triggers="['hover', 'focus']"
      :delay="{ show: 0, hide: 150 }"
      :distance="6"
      popper-class="admin-badge-popper"
    >
      <!-- Etykieta Administratora (Element wyzwalający) -->
      <button
        type="button"
        class="inline-flex items-center px-2 py-0.5 rounded-md bg-[#e7f3ff] dark:bg-[#252f3d] text-[#1877f2] dark:text-[#4599ff] text-[13px]  dark:hover:bg-[#2d3a4d] transition-colors cursor-pointer"
      >{{ $t('feed.administrator') }}</button>

      <!-- Zawartość Popovera (Popper) -->
      <template #popper="{ hide }">
        <div class="w-[400px] bg-white dark:bg-[#242526] text-[#050505] dark:text-[#e4e6eb] rounded-2xl p-4 shadow-2xl border border-gray-100 dark:border-[#3e4042]">
          <div class="flex items-start gap-3.5 mb-4">
            <!-- Odznaka gwiazdki -->
            <div class="relative w-12 h-12 rounded-full bg-[#fbe74c] flex items-center justify-center shrink-0 shadow-sm">
              <div class="w-8 h-8 rounded-full bg-[#1877f2] flex items-center justify-center text-white">
                <StarIcon :size="20" />
              </div>
            </div>

            <!-- Nagłówek i Opis -->
            <div class="flex-1 min-w-0">
              <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] leading-tight mb-1">{{ $t('feed.administrator') }}</h3>
              <p class="text-[14px] text-[#050505] dark:text-[#e4e6eb] leading-snug">{{ $t('feed.zarzadzaszCzlonkostwemUprawnieniamiModeratora') }}</p>

             <img
                src="@/assets/images/default_avatar.png"
                :alt="$t('feed.informacjaOOdznaceAdministratora')"
                class="w-9 h-9 mt-3 rounded-full border border-gray-200 dark:border-[#3e4042]"
              />
            </div>
          </div>

          <!-- Przycisk "Wyświetl wszystkich administratorów" -->
          <button
            type="button"
            @click="() => { emit('view-all'); hide(); }"
            class="w-full bg-[#e4e6eb] hover:bg-[#d8dadf] dark:bg-[#3a3b3c] dark:hover:bg-[#4e4f50] text-[#050505] dark:text-[#e4e6eb] text-[15px] font-semibold py-2.5 rounded-xl transition-colors cursor-pointer"
          >{{ $t('feed.wyswietlWszystkichAdministratorow') }}</button>
        </div>
      </template>
    </VDropdown>
  </div>
</template>

<style>
/* Resettujemy domyślne tło i paddingi floating-vue, aby Tailwind przejął 100% kontroli */
.v-popper--theme-dropdown.admin-badge-popper .v-popper__inner {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
  border-radius: 1rem !important;
}

.v-popper--theme-dropdown.admin-badge-popper .v-popper__arrow-container {
  display: none !important; /* Chowa strzałkę wskazującą */
}
</style>
