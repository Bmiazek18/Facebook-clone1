<script setup lang="ts">
import { computed } from 'vue'
import { useConversationsStore } from '@/stores/conversations'

const emit = defineEmits<{
  (e: 'apply'): void
}>()

const conv = useConversationsStore();

const themes = conv.themes;
const selectedThemeId = computed({ get: () => conv.selectedThemeId, set: (v: string) => conv.setSelectedTheme(v) });
const selectedTheme = computed(() => conv.selectedTheme);
const setSelectedTheme = (id: string) => conv.setSelectedTheme(id);

const selectTheme = (id: string) => setSelectedTheme(id)

const applyTheme = () => {
  // selectedTheme is already reactive in conversations store
  emit('apply')
}
</script>

<template>


    <div class="bg-white rounded-xl shadow-2xl w-full max-w-5xl overflow-hidden flex flex-col h-[750px] md:h-[650px] border border-gray-200">

      <div class="flex flex-1 overflow-hidden">

        <div class="w-full md:w-5/12 lg:w-1/3 border-r border-gray-100 flex flex-col z-20 bg-white">

          <div class="p-4 border-b border-gray-100 bg-white z-10">
            <h2 class="text-xl font-bold text-gray-800">Galeria Motywów</h2>
            <p class="text-xs text-gray-500 mt-1">Wybierz jeden z {{ themes.length }} stylów</p>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar p-2 space-y-1">
            <div
              v-for="theme in themes"
              :key="theme.id"
              @click="selectTheme(theme.id)"
              class="flex items-center p-3 rounded-xl cursor-pointer transition-all duration-200 group border border-transparent"
              :class="selectedThemeId === theme.id ? 'bg-slate-50 border-slate-200 shadow-sm' : 'hover:bg-gray-50'"
            >
               <div class="relative w-12 h-12 shrink-0 mr-4 transition-transform duration-200" :class="selectedThemeId === theme.id ? 'scale-110' : ''">
                <img :src="theme.image" alt="" class="w-full h-full rounded-full object-cover shadow-sm ring-2 ring-offset-2 ring-transparent" :class="selectedThemeId === theme.id ? 'ring-blue-400' : ''" />
              </div>

              <div class="flex-1 min-w-0">
                <h3 class="font-semibold text-gray-900 truncate text-[15px]">
                  {{ theme.title }}
                </h3>
                <p v-if="theme.subtitle" class="text-xs text-gray-500 truncate mt-0.5">
                  {{ theme.subtitle }}
                </p>
              </div>

              <div class="ml-2 w-6 h-6 flex items-center justify-center">
                 <div v-if="selectedThemeId === theme.id" class="text-blue-500 transform scale-100 transition-transform">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6">
                      <path fill-rule="evenodd" d="M2.25 12c0-5.385 4.365-9.75 9.75-9.75s9.75 4.365 9.75 9.75-4.365 9.75-9.75 9.75S2.25 17.385 2.25 12zm13.36-1.814a.75.75 0 10-1.22-.872l-3.236 4.53L9.53 12.22a.75.75 0 00-1.06 1.06l2.25 2.25a.75.75 0 001.14-.094l3.75-5.25z" clip-rule="evenodd" />
                    </svg>
                 </div>
                 <div v-else class="w-5 h-5 rounded-full border-2 border-gray-300 group-hover:border-gray-400"></div>
              </div>
            </div>
          </div>

          <div class="p-4 border-t border-gray-100 bg-gray-50">
            <button class="w-full py-2.5 px-4 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 font-semibold rounded-lg transition-colors text-sm shadow-sm">
              Anuluj zmiany
            </button>
          </div>
        </div>

        <div class="hidden md:flex md:w-7/12 lg:w-2/3 flex-col relative overflow-hidden bg-gray-900">

             <div class="absolute inset-0 z-0">
             <img
              :src="selectedTheme?.backgroundImage"
              alt="Theme Background"
              class="w-full h-full object-cover filter blur-[3px] scale-105 transition-all duration-700 transform"
            />
          </div>

          <div
            class="absolute inset-0 z-0 transition-all duration-500"
            :class="selectedTheme?.gradientClass"
          ></div>

          <div class="relative z-10 flex-1 flex flex-col justify-center p-8 space-y-4">

            <div
              class="self-end max-w-[80%] rounded-2xl rounded-tr-sm p-3.5 text-white text-[15px] leading-snug shadow-lg transition-colors duration-300 border border-white/10"
              :class="selectedTheme?.sentBubbleColor"
            >
              Testujemy motyw: <strong>{{ selectedTheme?.title }}</strong>. Jak Ci się podoba ten klimat?
            </div>

            <div class="flex items-end justify-end gap-2">
              <div class="w-6 h-6 rounded-full overflow-hidden shrink-0 order-2 bg-gray-200 shadow-sm border border-white/30">
                 <img src="https://ui-avatars.com/api/?name=Ty&background=random&color=fff" alt="" class="w-full h-full object-cover">
              </div>
              <div
                class="order-1 max-w-[80%] rounded-2xl rounded-br-sm p-3.5 text-white text-[15px] leading-snug shadow-lg transition-colors duration-300 border border-white/10"
                :class="selectedTheme?.sentBubbleColor"
              >
                Kolor dymków idealnie pasuje do tła!
              </div>
            </div>

            <div class="self-start max-w-[70%] rounded-2xl rounded-tl-sm bg-white/90 backdrop-blur-md p-3.5 text-gray-900 text-[15px] leading-snug shadow-lg mt-6 border border-white/50">
              Jest super! Zostawiamy ten, czy szukamy dalej? 👀
            </div>

            <div class="w-full text-center text-xs font-medium text-white/90 my-2 drop-shadow-md shadow-black">
              10:45
            </div>

            <div class="flex items-end justify-end gap-2">
              <div
                class="order-1 max-w-[80%] rounded-2xl rounded-br-sm p-3.5 text-white text-[15px] leading-snug shadow-lg transition-colors duration-300 border border-white/10 font-medium"
                :class="selectedTheme?.sentBubbleColor"
              >
                Kliknij przycisk poniżej, aby zatwierdzić.
              </div>
            </div>

          </div>

          <div class="relative z-10 p-4 border-t border-white/10 bg-black/20 backdrop-blur-md">
            <button
              class="w-full py-3 px-4 text-white font-semibold rounded-lg transition-all duration-300 shadow-lg hover:shadow-xl hover:scale-[1.01]"
              :class="selectedTheme?.sentBubbleColor"
              @click="applyTheme"
            >
              Ustaw motyw: {{ selectedTheme?.title }}
            </button>
          </div>

        </div>
      </div>

    </div>

</template>

<style scoped>
/* Stylizacja Scrollbara */
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #e2e8f0;
  border-radius: 20px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: #cbd5e1;
}

/* Płynne przejścia kolorów */
.transition-colors {
  transition-property: background-color, border-color, color, opacity;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
