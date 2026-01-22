  <script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import { useStoryShareStore } from '@/stores/storyShare';
  import type { PostData, ReelData } from '@/types/StoryElement';

  // Sub-views rendered conditionally
  import StoryPicker from '@/components/createStory/StoryPicker.vue';
  import StoryImageEditor from '@/components/createStory/StoryImageEditor.vue';
  import StoryTextEditor from '@/components/createStory/StoryTextEditor.vue';

  type StoryMode = 'picker' | 'image' | 'text';

  const storyShareStore = useStoryShareStore();

  const mode = ref<StoryMode>('picker');
  const selectedImage = ref<{ url: string; altText: string } | null>(null);
  const initialPost = ref<PostData | null>(null);
  const initialReel = ref<ReelData | null>(null);


  onMounted(() => {
    const pendingPost = storyShareStore.getPendingPost();
    const pendingReel = storyShareStore.getPendingReel();

    if (pendingPost) {
      initialPost.value = pendingPost;
      mode.value = 'image';
    } else if (pendingReel) {
      initialReel.value = pendingReel;
      mode.value = 'image';
    }
  });


  const onSelectImage = (imageUrl: string) => {
    selectedImage.value = { url: imageUrl, altText: '' };
    mode.value = 'image';
  };

  const onSelectText = () => {
    mode.value = 'text';
  };

  // Go back to picker
  const onBack = () => {
    // Revoke blob URL if exists
    // Responsibility for revoking moved to StoryImageEditor to avoid race condition
    // if (selectedImage.value?.url.startsWith('blob:')) {
    //   URL.revokeObjectURL(selectedImage.value.url);
    // }
    selectedImage.value = null;
    initialPost.value = null;
    initialReel.value = null;
    mode.value = 'picker';
  };
  </script>

  <template>
    <div class="h-screen w-full">
      <StoryPicker
        v-if="mode === 'picker'"
        @select-image="onSelectImage"
        @select-text="onSelectText"
      />

      <!-- Image editor -->
      <StoryImageEditor
        v-else-if="mode === 'image'"
        :initial-image="selectedImage?.url"
        :initial-post="initialPost"
        :initial-reel="initialReel"
        @back="onBack"
      />


      <StoryTextEditor
        v-else-if="mode === 'text'"
        @back="onBack"
      />
    </div>
  </template>
