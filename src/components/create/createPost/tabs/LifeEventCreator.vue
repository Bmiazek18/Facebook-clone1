<script setup lang="ts">
// Importy ikon
import BriefcaseOutline from 'vue-material-design-icons/BriefcaseOutline.vue'
import SchoolOutline from 'vue-material-design-icons/SchoolOutline.vue'
import HeartMultipleOutline from 'vue-material-design-icons/HeartMultipleOutline.vue'
import HomeOutline from 'vue-material-design-icons/HomeOutline.vue'
import TreeOutline from 'vue-material-design-icons/TreeOutline.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import StarOutline from 'vue-material-design-icons/StarOutline.vue'
import HeartBoxOutline from 'vue-material-design-icons/HeartBoxOutline.vue'
import TrophyOutline from 'vue-material-design-icons/TrophyOutline.vue'
import Candle from 'vue-material-design-icons/Candle.vue'
import FlagOutline from 'vue-material-design-icons/FlagOutline.vue'

const categories = [
  { name: 'Praca', icon: BriefcaseOutline },
  { name: 'Edukacja', icon: SchoolOutline },
  { name: 'Związek', icon: HeartMultipleOutline },
  { name: 'Dom i życie', icon: HomeOutline },
  { name: 'Rodzina', icon: TreeOutline },
  { name: 'Podróż', icon: Earth },
  { name: 'Zainteresowania\ni aktywności', icon: StarOutline },
  { name: 'Wellness', icon: HeartBoxOutline },
  { name: 'Kamienie\nmilowe i\nosiągnięcia', icon: TrophyOutline },
  { name: 'Wspomnienie', icon: Candle },
  { name: 'Utwórz własną', icon: FlagOutline },
]

const emit = defineEmits(['select-category'])

const handleSelect = (categoryName: string) => {
  emit('select-category', categoryName)
}
</script>

<template>
  <div
    class="w-full h-[160px] bg-blue-50 relative flex justify-center items-center overflow-hidden"
  >
    <span class="text-blue-300 text-sm font-medium">Grafika: Balon i Tort</span>
  </div>

  <div class="flex flex-col items-center">
    <div class="text-center pt-6 pb-5 px-6">
      <h1 class="text-[24px] font-bold text-black mb-1.5">Wydarzenia z życia</h1>
      <p class="text-[15px] text-gray-500">Udostępniaj ważne chwile z życia i zapamiętuj je.</p>
    </div>

    <div class="w-full px-8">
      <div class="border-t border-gray-200"></div>
    </div>

    <div class="w-full pt-8 pb-14 px-8 flex flex-col items-center">
      <h2 class="text-[15px] font-bold text-black uppercase tracking-wide mb-10">
        Wybierz kategorię
      </h2>

      <div class="grid grid-cols-4 gap-x-2 gap-y-10 w-full max-w-[650px]">
        <div
          v-for="(item, index) in categories"
          :key="index"
          @click="handleSelect(item.name)"
          class="flex flex-col items-center text-center cursor-pointer group"
        >
          <div
            class="w-14 h-14 flex items-center justify-center mb-2.5 rounded-full transition-all duration-200 group-hover:bg-gray-100"
          >
            <component
              :is="item.icon"
              :size="34"
              class="text-gray-800 group-hover:text-black transition-colors"
            />
          </div>

          <span class="text-[14px] text-gray-900 whitespace-pre-line leading-tight">
            {{ item.name }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
