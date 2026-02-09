<script setup lang="ts">
import { computed, defineProps,ref } from 'vue';
import { usePostsStore } from '@/stores/posts';
import CreateBox from '@/components/createPost/CreateBox.vue';
import PostItem from '@/components/feed/post/PostItem.vue';
import type { Event as EventType } from '@/data/events';
import { useStickySidebar } from '@/composables/useStickySidebar';
import EventAboutDetails from '@/components/events/EventAboutDetails.vue';


const props = defineProps<{
  eventDetails: EventType | undefined
}>();

const postsStore = usePostsStore();
const rightSectionRef = ref<HTMLDivElement | null>(null)
const { stickyTop } = useStickySidebar(rightSectionRef, 56, 16)
const eventPosts = computed(() => {
  if (!props.eventDetails) return [];
  return postsStore.posts
    .filter(post => post.targetType === 'Event' && post.targetId === props.eventDetails?.id)
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
});
</script>

<template>
  <div class="grid grid-cols-1 max-w-[1200px] mx-auto lg:grid-cols-5 gap-4 ">
    <div class="lg:col-span-3 space-y-4">
      <CreateBox :event-target="eventDetails" />
      <div v-if="eventPosts.length > 0" class="space-y-4">
        <PostItem v-for="post in eventPosts" :key="post.id" :post="post" />
      </div>
      <div v-else class="bg-theme-bg-secondary rounded-lg shadow-sm p-6 text-center">
        <p class="text-theme-text-secondary">Brak postów w tej dyskusji. Bądź pierwszy!</p>
      </div>
    </div>
    <div class="lg:col-span-2">
         <div
      ref="rightSectionRef"
      class="lg:col-span-2 space-y-4 sticky z-10 self-start bg-theme"
      :style="{ top: `${stickyTop}px` }"
    >


  <EventAboutDetails :event-details="props.eventDetails" />

</div>

    </div>
  </div>
</template>
