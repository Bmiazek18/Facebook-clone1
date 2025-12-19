<script setup lang="ts">
import { ref, computed } from 'vue';
import LazyEmojiPicker from './LazyEmojiPicker.vue';
import EmoticonHappyIcon from 'vue-material-design-icons/EmoticonHappy.vue';

interface CardBackground { id: number; class: string; textClass?: string }

const props = defineProps<{
  modelValue: string;
  bgId: number;
  backgrounds: CardBackground[];
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void;
  (e: 'update:bgId', v: number): void;
  (e: 'close'): void;
}>();

const localText = computed({
  get: () => props.modelValue,
  set: (v: string) => emit('update:modelValue', v)
});

const showPicker = ref(false);
const togglePicker = () => (showPicker.value = !showPicker.value);
const addEmoji = (e: { native: string }) => {
  emit('update:modelValue', props.modelValue + e.native);

};

const selectBg = (id: number) => {
  emit('update:bgId', id);
  if (id === 0) {
    // selecting white acts like closing in parent
    emit('close');
  }
};

const currentClass = computed(() => {
  const found = props.backgrounds.find((b) => b.id === props.bgId);
  return found ? found.class : '';
});

const currentTextClass = computed(() => {
  const found = props.backgrounds.find((b) => b.id === props.bgId);
  return found && found.textClass ? found.textClass : 'text-white';
});
</script>

<template>
  <div class="relative mb-4 bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
    <div :class="['w-full h-60 rounded-lg flex items-center justify-center relative', currentClass]">

      <div class="z-10 w-full h-full px-4 flex items-center justify-center">



        <textarea
          v-model="localText"
          class="w-full resize-none overflow-hidden bg-transparent text-center outline-none px-2 py-6 placeholder-white/50"
          :class="[currentTextClass, 'text-2xl leading-normal']"
          placeholder="Napisz tekst..."
          spellcheck="false"
        ></textarea>

      </div>

      <!-- Emoji button on the bottom-right of the gradient card -->
      <div class="absolute bottom-3 right-3 z-40">
        <button @click="togglePicker" class="bg-white p-2 rounded-full shadow text-gray-700 hover:bg-gray-50 transition">
          <EmoticonHappyIcon :size="20" />
        </button>
        <LazyEmojiPicker v-if="showPicker" class="absolute bottom-full right-0 mb-2 w-[280px] max-h-[300px] shadow-2xl z-50" @select="addEmoji" />
      </div>

      <div class="absolute bottom-3 left-0 right-0 flex items-center justify-center gap-2 z-30">
        <template v-for="bg in backgrounds" :key="bg.id">
          <button @click="selectBg(bg.id)" :class="['w-8 h-8 rounded-md overflow-hidden', bg.id === bgId ? 'ring-2 ring-white' : 'ring-0']">
            <div :class="bg.class + ' w-full h-full'" />
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
textarea::placeholder { opacity: 0.75; }
</style>
