<template>
  <div class="bg-white dark:bg-[#242526] rounded-xl p-4 shadow-sm border border-gray-200 dark:border-[#3e4042]">
    <div class="mb-4">
      <h2 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] leading-snug">{{ featuresSection.title }}</h2>
      <p class="text-[14px] text-[#65676b] dark:text-[#b0b3b8] mt-0.5 leading-snug">
        {{ featuresSection.subtitle }}
      </p>
    </div>

    <div
      v-for="(category, catIndex) in featuresSection.categories"
      :key="catIndex"
      class="mb-6 last:mb-0"
    >
      <h3 class="text-[16px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-2">{{ category.name }}</h3>

      <div class="flex flex-col">
        <button
          v-for="(item, itemIndex) in category.items"
          :key="itemIndex"
          @click="toggleEdit(item.id)"
          :class="[
            'flex items-start gap-3 py-3 hover:bg-gray-100 dark:hover:bg-[#3a3b3c]/50 transition-colors -mx-4 px-4 group text-left cursor-pointer',
            activeEditId && activeEditId !== item.id ? 'opacity-40 pointer-events-none' : ''
          ]"
        >
          <div :class="`w-9 h-9 rounded-full flex items-center justify-center text-white shrink-0 mt-0.5 ${item.bg}`">
            <component :is="item.icon" :size="20" />
          </div>

          <div class="flex-1 pr-2">
            <div class="text-[15px] font-semibold text-[#050505] dark:text-[#e4e6eb] leading-snug">
              {{ item.title }}
            </div>
            <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] mt-0.5 leading-snug">
              {{ item.desc }}
            </div>
          </div>

          <div class="shrink-0 flex items-center justify-center text-[#65676b] dark:text-[#b0b3b8] group-hover:text-[#050505] dark:group-hover:text-[#e4e6eb] mt-1">
            <PencilIcon :size="20" />
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { markRaw } from 'vue'

// Importy ikon
import PencilIcon from 'vue-material-design-icons/Pencil.vue'
import ChartBarIcon from 'vue-material-design-icons/ChartBar.vue'
import GifIcon from 'vue-material-design-icons/ImageMultiple.vue'
import FolderIcon from 'vue-material-design-icons/Folder.vue'
import PlayCircleIcon from 'vue-material-design-icons/PlayCircle.vue'
import VideoIcon from 'vue-material-design-icons/Video.vue'
import CalendarIcon from 'vue-material-design-icons/Calendar.vue'
import StarCircleIcon from 'vue-material-design-icons/StarCircle.vue'

const props = defineProps<{
  activeEditId: string | null
}>()

const emit = defineEmits<{
  (e: 'update:activeEditId', id: string | null): void
}>()

const toggleEdit = (id: string) => {
  if (props.activeEditId === id) {
    emit('update:activeEditId', null)
  } else {
    emit('update:activeEditId', id)
  }
}

const featuresSection = {
  title: 'Dodano do grupy',
  subtitle: 'Funkcje dają możliwości korzystania z grup w pełnym zakresie.',
  categories: [
    {
      name: 'Formaty posta',
      items: [
        { id: 'feat_poll', icon: markRaw(ChartBarIcon), bg: 'bg-[#f4c22b]', title: 'Ankieta', desc: 'Pozwól użytkownikom tworzyć ankiety w tej społeczności.' },
        { id: 'feat_gif', icon: markRaw(GifIcon), bg: 'bg-[#2db5a3]', title: 'GIF', desc: 'Zezwalaj członkom na publikowanie obrazów GIF.' },
        { id: 'feat_files', icon: markRaw(FolderIcon), bg: 'bg-[#2d88ff]', title: 'Pliki', desc: 'Zezwól użytkownikom na udostępnianie plików w społeczności.' },
        { id: 'feat_reels', icon: markRaw(PlayCircleIcon), bg: 'bg-[#e75349]', title: 'Rolki', desc: 'Oglądaj i twórz krótkie, zabawne filmy dopasowane do Twojej społeczności.' },
        { id: 'feat_live', icon: markRaw(VideoIcon), bg: 'bg-[#e75349]', title: 'Transmisje wideo na żywo', desc: 'Transmituj na żywo.' },
        { id: 'feat_events', icon: markRaw(CalendarIcon), bg: 'bg-[#e75349]', title: 'Wydarzenia', desc: 'Planuj wydarzenia online i wydarzenia umożliwiające kontakt na żywo.' },
      ]
    },
    {
      name: 'Inne funkcje',
      items: [
        { id: 'feat_contribution', icon: markRaw(StarCircleIcon), bg: 'bg-[#8958ea]', title: 'Wkład', desc: 'Pozwól członkom zdobywać punkty i odznaki za ich wkład w grupę.' }
      ]
    }
  ]
}
</script>
