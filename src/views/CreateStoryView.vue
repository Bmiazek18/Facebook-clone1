<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useTempStoryStore } from '@/stores/tempStory';

// Sub-views rendered conditionally
import StoryPicker from '@/components/story/StoryPicker.vue';
import StoryImageEditor from '@/components/story/StoryImageEditor.vue';
import StoryTextEditor from '@/components/story/StoryTextEditor.vue';

type StoryMode = 'picker' | 'image' | 'text';

const tempStore = useTempStoryStore();
const mode = ref<StoryMode>('picker');

// Check if image was passed from picker
onMounted(() => {
  if (tempStore.selectedImage) {
    mode.value = 'image';
  }
});

// Handlers from picker
const onSelectImage = (imageUrl: string) => {
  tempStore.selectedImage = imageUrl;
  mode.value = 'image';
};

const onSelectText = () => {
  mode.value = 'text';
};

// Go back to picker
const onBack = () => {
  tempStore.clear();
  mode.value = 'picker';
};
</script>

<template>
  <div class="h-screen w-full">
    <!-- Picker: choose image or text story -->
    <StoryPicker
      v-if="mode === 'picker'"
      @select-image="onSelectImage"
      @select-text="onSelectText"
    />

    <!-- Image editor -->
    <StoryImageEditor
      v-else-if="mode === 'image'"
      :initial-image="tempStore.selectedImage"
      @back="onBack"
    />

    <!-- Text editor -->
    <StoryTextEditor
      v-else-if="mode === 'text'"
      @back="onBack"
    />
  </div>
</template>
