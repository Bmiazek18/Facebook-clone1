<script setup lang="ts">
import { ref, onMounted, shallowRef } from 'vue';

const emit = defineEmits<{
  select: [emoji: { native: string }];
}>();

defineProps<{
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
  <div v-if="!isLoaded" class="p-4 bg-white rounded-lg shadow-2xl">
    <div class="animate-pulse flex flex-col gap-2">
      <div class="h-8 bg-gray-200 rounded w-full"></div>
      <div class="grid grid-cols-8 gap-1">
        <div v-for="i in 40" :key="i" class="h-8 w-8 bg-gray-200 rounded"></div>
      </div>
    </div>
  </div>
  <component 
    v-else-if="Picker && emojiIndex"
    :is="Picker"
    :data="emojiIndex"
    :class="$props.class"
    set="facebook"
    :perLine="8"
    color="oklch(48.8% 0.243 264.376)"
    :showPreview="false"
    @select="onSelect"
  />
</template>

<style>
@import 'emoji-mart-vue-fast/css/emoji-mart.css';
</style>
