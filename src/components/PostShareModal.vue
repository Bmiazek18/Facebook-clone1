<script setup lang="ts">
import { ref, computed } from 'vue';
import Close from 'vue-material-design-icons/Close.vue';
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue';
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue';
import ShareVariant from 'vue-material-design-icons/ShareVariant.vue';
import Earth from 'vue-material-design-icons/Earth.vue';
import type { PostData } from '@/types/StoryElement';

defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits<{
  close: [];
  selectPost: [post: PostData];
}>();

// Mock posts data - in real app this would come from API/store
const mockPosts = ref([
  {
    id: 'post-1',
    authorName: 'Jan Kowalski',
    authorAvatar: 'https://i.pravatar.cc/100?u=jan',
    content: 'Właśnie wróciłem z niesamowitej podróży! 🌍✈️ Polecam wszystkim odwiedzić to miejsce.',
    imageUrl: 'https://picsum.photos/400/300?random=1',
    timestamp: Date.now() - 3600000,
    likes: 42,
    comments: 8
  },
  {
    id: 'post-2',
    authorName: 'Anna Nowak',
    authorAvatar: 'https://i.pravatar.cc/100?u=anna',
    content: 'Nowy projekt zakończony! Dziękuję wszystkim za wsparcie 💪',
    imageUrl: 'https://picsum.photos/400/300?random=2',
    timestamp: Date.now() - 7200000,
    likes: 156,
    comments: 23
  },
  {
    id: 'post-3',
    authorName: 'Piotr Wiśniewski',
    authorAvatar: 'https://i.pravatar.cc/100?u=piotr',
    content: 'Poranna kawa i dobra książka - idealny początek dnia ☕📚',
    imageUrl: undefined,
    timestamp: Date.now() - 86400000,
    likes: 89,
    comments: 12
  },
  {
    id: 'post-4',
    authorName: 'Marta Zielińska',
    authorAvatar: 'https://i.pravatar.cc/100?u=marta',
    content: 'Zobaczcie co udało mi się dzisiaj ugotować! 🍝👨‍🍳',
    imageUrl: 'https://picsum.photos/400/300?random=4',
    timestamp: Date.now() - 172800000,
    likes: 234,
    comments: 45
  }
]);

const selectedPostId = ref<string | null>(null);

const selectedPost = computed(() => {
  return mockPosts.value.find(p => p.id === selectedPostId.value);
});

const formatTimeAgo = (timestamp: number) => {
  const diff = Date.now() - timestamp;
  const hours = Math.floor(diff / 3600000);
  if (hours < 1) return 'Przed chwilą';
  if (hours < 24) return `${hours} godz. temu`;
  const days = Math.floor(hours / 24);
  return `${days} dni temu`;
};

const handleSelectPost = () => {
  if (!selectedPost.value) return;

  const postData: PostData = {
    id: selectedPost.value.id,
    authorName: selectedPost.value.authorName,
    authorAvatar: selectedPost.value.authorAvatar,
    content: selectedPost.value.content,
    imageUrl: selectedPost.value.imageUrl,
    timestamp: selectedPost.value.timestamp
  };

  emit('selectPost', postData);
  selectedPostId.value = null;
};

const close = () => {
  emit('close');
  selectedPostId.value = null;
};
</script>

<template>
  <Teleport to="body">
    <div
      v-if="isOpen"
      class="fixed inset-0 z-9999 flex items-center justify-center bg-black/60 backdrop-blur-sm"
      @click.self="close"
    >
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl mx-4 max-h-[85vh] flex flex-col overflow-hidden">
        <!-- Header -->
        <div class="flex items-center justify-between p-4 border-b border-gray-200 shrink-0">
          <h2 class="text-xl font-bold text-gray-900">Udostępnij post na relację</h2>
          <button
            @click="close"
            class="p-2 hover:bg-gray-100 rounded-full transition-colors"
          >
            <Close :size="24" class="text-gray-600" />
          </button>
        </div>

        <!-- Posts List -->
        <div class="flex-1 overflow-y-auto p-4 space-y-3">
          <p class="text-sm text-gray-500 mb-3">Wybierz post, który chcesz udostępnić:</p>

          <div
            v-for="post in mockPosts"
            :key="post.id"
            @click="selectedPostId = post.id"
            class="border-2 rounded-xl p-4 cursor-pointer transition-all hover:shadow-md"
            :class="selectedPostId === post.id ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-gray-300'"
          >
            <!-- Post Header -->
            <div class="flex items-center gap-3 mb-3">
              <img :src="post.authorAvatar" class="w-10 h-10 rounded-full object-cover" />
              <div class="flex-1 min-w-0">
                <p class="font-semibold text-gray-900 truncate">{{ post.authorName }}</p>
                <div class="flex items-center gap-1 text-xs text-gray-500">
                  <span>{{ formatTimeAgo(post.timestamp) }}</span>
                  <span>·</span>
                  <Earth :size="12" />
                </div>
              </div>
              <div v-if="selectedPostId === post.id" class="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center">
                <svg class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                </svg>
              </div>
            </div>

            <!-- Post Content -->
            <p class="text-gray-800 text-sm mb-3 line-clamp-2">{{ post.content }}</p>

            <!-- Post Image -->
            <div v-if="post.imageUrl" class="rounded-lg overflow-hidden mb-3">
              <img :src="post.imageUrl" class="w-full h-40 object-cover" />
            </div>

            <!-- Post Stats -->
            <div class="flex items-center gap-4 text-xs text-gray-500">
              <div class="flex items-center gap-1">
                <ThumbUp :size="14" class="text-blue-500" />
                <span>{{ post.likes }}</span>
              </div>
              <div class="flex items-center gap-1">
                <MessageOutline :size="14" />
                <span>{{ post.comments }} komentarzy</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Preview Section -->
        <div v-if="selectedPost" class="border-t border-gray-200 p-4 bg-gray-50 shrink-0">
          <p class="text-xs text-gray-500 mb-3">Podgląd na relacji:</p>
          <div class="bg-linear-to-br from-gray-800 to-gray-900 rounded-xl p-3 max-w-[200px] mx-auto">
            <div class="bg-white rounded-lg overflow-hidden shadow-lg">
              <div class="p-2">
                <div class="flex items-center gap-2 mb-2">
                  <img :src="selectedPost.authorAvatar" class="w-6 h-6 rounded-full" />
                  <span class="text-xs font-medium text-gray-900 truncate">{{ selectedPost.authorName }}</span>
                </div>
                <p class="text-xs text-gray-700 line-clamp-2">{{ selectedPost.content }}</p>
              </div>
              <img v-if="selectedPost.imageUrl" :src="selectedPost.imageUrl" class="w-full h-20 object-cover" />
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="p-4 border-t border-gray-200 flex justify-end gap-3 shrink-0">
          <button
            @click="close"
            class="px-5 py-2.5 text-gray-700 font-medium hover:bg-gray-100 rounded-lg transition-colors"
          >
            Anuluj
          </button>
          <button
            @click="handleSelectPost"
            :disabled="!selectedPost"
            class="px-5 py-2.5 bg-blue-500 text-white font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-blue-600 flex items-center gap-2"
          >
            <ShareVariant :size="18" />
            Udostępnij na relację
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
