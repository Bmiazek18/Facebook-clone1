<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useStoriesStore } from '@/stores/stories';

const props = defineProps<{
  user: {
    id: number | string;
    name?: string;
    avatar?: string;
  };
  size?: number | string;
  disableLink?: boolean;
}>();

const router = useRouter();
const storiesStore = useStoriesStore();

const hasStory = computed(() => {
  if (!props.user?.id) return false;
  const userStories = storiesStore.getUserStories(props.user.id.toString());
  return !!userStories && userStories.stories.length > 0;
});

const handleClick = (e: MouseEvent) => {
  if (props.disableLink) return;
  e.stopPropagation();

  if (hasStory.value) {
    router.push({ name: 'userStories', params: { userId: props.user.id } });
  } else {
    router.push({ name: 'userProfile', params: { userId: props.user.id } });
  }
};

const avatarSize = computed(() => {
    if (typeof props.size === 'number') return `${props.size}px`;
    return props.size || '40px';
});

const ringClass = computed(() => {
  if (hasStory.value) {
    return 'ring-[3px] ring-[#1877F2] ring-offset-2';
  }
  return 'border border-gray-200'; // Default subtle border
});
</script>

<template>
  <div
    class="relative inline-block rounded-full select-none"
    :class="[
      !disableLink ? 'cursor-pointer' : '',
      ringClass
    ]"
    :style="{ width: avatarSize, height: avatarSize }"
    @click="handleClick"
  >
    <img
      class="rounded-full w-full h-full object-cover"
      :src="user.avatar || 'https://via.placeholder.com/40'"
      :alt="user.name || 'User'"
    >
  </div>
</template>
