<script setup lang="ts">
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
// Importujemy komponent Dropdownu z biblioteki
import { Dropdown as VDropdown } from 'floating-vue'
import 'floating-vue/dist/style.css' // bazowe style dla pozycjonowania

defineProps<{
  friend: {
    name: string
    mutual: number
    isFriend: boolean
    imageId: number
  }
}>()
</script>

<template>
  <div class="w-full md:w-1/2 px-2 mb-4">
    <div
      class="flex items-center justify-between p-3 bg-theme-bg-secondary shadow-sm h-full rounded-lg transition-shadow duration-200"
    >
      <div class="flex items-center">
        <img
          :src="`https://picsum.photos/id/${friend.imageId}/100/100`"
          class="w-19 h-19 rounded-lg mr-3 object-cover border border-theme-border"
          :alt="`Zdjęcie ${friend.name}`"
        />
        <div>
          <ProfilePopper :user-id="1" />
          <div
            class="text-[13px] font-medium text-theme-text-secondary hover:underline cursor-pointer"
          >
            {{ friend.mutual }} wspólnych znajomych
          </div>
        </div>
      </div>

      <div class="flex items-center space-x-2">
        <!-- UŻYCIE VDropdown zamiast ręcznej logiki -->
        <VDropdown
          v-if="friend.isFriend"
          placement="bottom-end"
          :distance="8"
          :triggers="['click']"
        >
          <!-- Trigger: Przycisk otwierający -->
          <button
            class="w-8 h-8 flex items-center justify-center hover:bg-theme-hover rounded-full text-theme-text transition-colors"
          >
            <DotsHorizontal :size="20" />
          </button>

          <!-- Slot #popper: Zawartość menu ze Zrzutu ekranu 2026-07-7 o 18.14.32.png -->
          <template #popper="{ hide }">
            <div
              class="w-72 bg-white dark:bg-zinc-900 py-2 border border-gray-100 dark:border-zinc-800 rounded-xl shadow-xl"
            >
              <!-- Opcja: Ulubione -->
              <button
                @click="hide()"
                class="w-full flex items-center gap-3 px-3 py-2.5 text-left text-[15px] font-semibold text-gray-900 dark:text-gray-100 hover:bg-gray-100 dark:hover:bg-zinc-800 transition-colors"
              >
                <svg
                  class="w-6 h-6 text-gray-700 dark:text-gray-300 shrink-0"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.198 5.474a.562.562 0 01-.812.59l-4.721-2.735a.563.563 0 00-.541 0L4.17 21l1.197-5.474a.563.563 0 00-.182-.557l-4.204-3.602a.562.562 0 01.32-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z"
                  />
                </svg>
                <span>Ulubione</span>
              </button>

              <!-- Opcja: Edytuj listę znajomych -->
              <button
                @click="hide()"
                class="w-full flex items-center gap-3 px-3 py-2.5 text-left text-[15px] font-semibold text-gray-900 dark:text-gray-100 hover:bg-gray-100 dark:hover:bg-zinc-800 transition-colors"
              >
                <svg
                  class="w-6 h-6 text-gray-700 dark:text-gray-300 shrink-0"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10"
                  />
                </svg>
                <span>Edytuj listę znajomych</span>
              </button>

              <!-- Opcja: Przestań obserwować -->
              <button
                @click="hide()"
                class="w-full flex items-center gap-3 px-3 py-2.5 text-left text-[15px] font-semibold text-gray-900 dark:text-gray-100 hover:bg-gray-100 dark:hover:bg-zinc-800 transition-colors"
              >
                <svg
                  class="w-6 h-6 text-gray-700 dark:text-gray-300 shrink-0"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M9.75 9.75l4.5 4.5m0-4.5l-4.5 4.5M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                <span>Przestań obserwować</span>
              </button>

              <!-- Opcja: Usuń z grona znajomych -->
              <button
                @click="hide()"
                class="w-full flex items-center gap-3 px-3 py-2.5 text-left text-[15px] font-semibold text-red-600 hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors"
              >
                <svg
                  class="w-6 h-6 text-red-600 shrink-0"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M22 10.5h-6m-2.25-1.5a3 3 0 11-6 0 3 3 0 016 0zm-11.25 9.75c0-3.175 2.146-5.85 5.147-6.634A6.002 6.002 0 006 15c0 3.314 2.686 6 6 6 1.012 0 1.957-.251 2.793-.694"
                  />
                </svg>
                <span>Usuń z grona znajomych</span>
              </button>
            </div>
          </template>
        </VDropdown>

        <button
          v-else
          class="bg-theme-primary hover:bg-theme-primary-hover cursor-pointer text-white text-sm px-3 py-2 rounded-lg font-semibold whitespace-nowrap"
        >
          Dodaj znajomego
        </button>
      </div>
    </div>
  </div>
</template>

<style>
/* Resetujemy domyślne tło i paddingi z poppera biblioteki, aby zachować czysty design Tailwind */
.v-popper__inner {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}
.v-popper__arrow-container {
  display: none !important; /* Ukrywa strzałkę wskazującą, tak jak na Facebooku */
}
</style>
