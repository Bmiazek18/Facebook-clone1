<script setup lang="ts">
import { ref, onMounted, shallowRef } from 'vue';

const emit = defineEmits<{
  select: [emoji: { native: string }];
}>();

const props = defineProps<{
  class?: string;
}>();

const Picker = shallowRef<ReturnType<typeof import('vue')['defineComponent']> | null>(null);
const emojiIndex = shallowRef<InstanceType<typeof import('emoji-mart-vue-fast/src')['EmojiIndex']> | null>(null);
const isLoaded = ref(false);

onMounted(async () => {
  const [{ Picker: PickerComponent, EmojiIndex }, { default: data }] = await Promise.all([
    import('emoji-mart-vue-fast/src'),
    import('emoji-mart-vue-fast/data/all.json'),
  ]);

  emojiIndex.value = new EmojiIndex(data);
  Picker.value = PickerComponent;
  isLoaded.value = true;
});

const onSelect = (e: { native: string }) => {
  emit('select', e);
};
</script>

<template>
  <div v-if="!isLoaded" class="p-4 bg-white rounded-lg shadow-2xl w-[350px]">
    <div class="animate-pulse flex flex-col gap-2">
      <div class="h-10 bg-gray-100 rounded-full w-full"></div>
      <div class="grid grid-cols-8 gap-1 mt-2">
        <div v-for="i in 40" :key="i" class="h-8 w-8 bg-gray-100 rounded"></div>
      </div>
    </div>
  </div>

  <component
    v-else-if="Picker && emojiIndex"
    :is="Picker"
    :data="emojiIndex"
    :class="['custom-picker', props.class]"
    set="facebook"
    :perLine="8"
    :showPreview="false"
    :showSkinTones="false"
    :style="{ width: '320px' }"
    :i18n="{
      search: 'Wyszukaj emoji',
      categories: { search: 'Wyniki wyszukiwania', recent: 'Ostatnie' }
    }"
    @select="onSelect"
  />
</template>

<style >
@import 'emoji-mart-vue-fast/css/emoji-mart.css';

/* Kontener główny */
.custom-picker.emoji-mart {
  height: 300px !important;
  border: none !important;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  font-family: inherit;
}

/* Sekcja wyszukiwania */
.emoji-mart-search {
  padding: 6px 12px 8px 12px !important;
  position: relative;
}

.emoji-mart-search input {
  background-color: #f1f2f4 !important;
  border: none !important;
  border-radius: 20px !important;
  padding: 10px 16px 10px 42px !important;
  font-size: 15px !important;
  outline: none !important;
  color: #1c1e21;
}

/* Ikona lupy SVG wyśrodkowana */
.emoji-mart-search::before {
  content: "";
  position: absolute;
  left: 26px;
  top: 50%;
  transform: translateY(calc(-50% )); /* +4px koryguje padding górny kontenera */
  width: 18px;
  height: 18px;
  background-color: #65676b;
  -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='black' stroke-width='2.5'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' d='M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z' /%3E%3C/svg%3E") no-repeat center;
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='black' stroke-width='2.5'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' d='M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z' /%3E%3C/svg%3E") no-repeat center;
  mask-size: contain;
  -webkit-mask-size: contain;
  pointer-events: none;
  z-index: 10;
}

/* Dolny pasek kategorii */
.emoji-mart-bar {
  border: none !important;
  background: white;
}

.emoji-mart-bar-anchors {
  order: 2; /* Przenosi ikony na dół */
border-radius: 0 !important;
border-top: rgb(226, 215, 215) 0.5px solid !important;
}

.emoji-mart-anchor-icon {
  color: #65676b !important;
}

.emoji-mart-anchor-selected {
  color: #0084ff !important; /* Kolor aktywnej kategorii */
}

.emoji-mart-anchor-bar {
  background-color: #0084ff !important;

}

/* Ukrycie niepotrzebnych etykiet */
.emoji-mart-category-label  {
  background-color: transparent !important;
  font-weight: 100;
  font-size: 13px !important;
  color: rgb(101, 104, 108);;


}
</style>
