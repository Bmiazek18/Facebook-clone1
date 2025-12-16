<script setup lang="ts">
import { ref } from 'vue';

// Sub-views rendered conditionally
import StoryPicker from '@/components/story/StoryPicker.vue';
import StoryImageEditor from '@/components/story/StoryImageEditor.vue';
import StoryTextEditor from '@/components/story/StoryTextEditor.vue';

type StoryMode = 'picker' | 'image' | 'text';

const mode = ref<StoryMode>('picker');
const selectedImage = ref<string | null>(null);

// Handlers from picker
const onSelectImage = (imageUrl: string) => {
  selectedImage.value = imageUrl;
  mode.value = 'image';
};

const onSelectText = () => {
  mode.value = 'text';
};

// Go back to picker
const onBack = () => {
  // Revoke blob URL if exists
  if (selectedImage.value?.startsWith('blob:')) {
    URL.revokeObjectURL(selectedImage.value);
  }
  selectedImage.value = null;
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
      :initial-image="selectedImage"
      @back="onBack"
    />

    <!-- Text editor -->
    <StoryTextEditor
      v-else-if="mode === 'text'"
      @back="onBack"
    />
  </div>
</template>
