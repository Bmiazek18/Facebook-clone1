<template>
  <div class="flex h-[calc(100vh-56px)] mt-[54px] bg-black overflow-hidden font-sans relative">

    <div
      ref="scrollContainer"
      class="flex-1 overflow-y-scroll snap-y snap-mandatory scrollbar-hide h-full transition-all duration-300"
      :class="{ 'mr-[350px] md:mr-[400px] lg:mr-[490px]': isCommentsOpen }"
    >
      <ReelItem
        v-for="(reel, index) in reels"
        :key="reel.id"
        :reel="reel"
        :data-id="reel.id"
        :ref="el => setReelRef(el, index)"
        :isActive="activeReelId === reel.id"
        :isMuted="isGlobalMuted"
        :globalVolume="globalVolume"
        :isCommentsOpen="isCommentsOpen"
        @toggle-comments="isCommentsOpen = !isCommentsOpen"
        @update-mute="isGlobalMuted = $event"
        @update-volume="globalVolume = $event"
        @open-share="handleOpenShare"
      />
    </div>

    <div
      class="fixed top-1/2 -translate-y-1/2 hidden sm:flex flex-col gap-4 z-30 transition-all duration-300"
      :class="{
        'right-[366px] md:right-[416px] lg:right-[506px]': isCommentsOpen,
        'right-4': !isCommentsOpen
      }"
    >
      <button @click="goToReel('up')" :disabled="activeIndex <= 0" class="w-10 h-10 md:w-12 md:h-12 bg-[#3a3b3c] hover:bg-[#4e4f50] rounded-full flex items-center justify-center text-white disabled:opacity-30 border border-gray-700">
        <ChevronUpIcon :size="28" />
      </button>
      <button @click="goToReel('down')" :disabled="!reels || activeIndex >= reels.length - 1" class="w-10 h-10 md:w-12 md:h-12 bg-[#3a3b3c] hover:bg-[#4e4f50] rounded-full flex items-center justify-center text-white disabled:opacity-30 border border-gray-700">
        <ChevronDownIcon :size="28" />
      </button>
    </div>

    <ReelInfoPanel v-if="isCommentsOpen && currentReel" :reel="currentReel" class="fixed right-0 w-full sm:w-[350px] md:w-[400px] lg:w-[490px] h-full z-40 animate-slide-in" />

    <BaseModal v-if="showShareModal" :title="shareModalTitle" @close="showShareModal = false">
      <StoryShareModal :reel="selectedReelToShare" @close="showShareModal = false" />
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useReelsStore } from '@/stores/reels';
import ReelItem from '../components/feed/reel/ReelItem.vue';
import ReelInfoPanel from '@/components/feed/reel/ReelInfoPanel.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import StoryShareModal from '@/components/feed/stories/StoryShareModal.vue';
import ChevronUpIcon from 'vue-material-design-icons/ChevronUp.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';

const reelsStore = useReelsStore();
const reels = computed(() => reelsStore.reels);

const activeReelId = ref<string | null>(null);
const isGlobalMuted = ref(true);
const globalVolume = ref(1);
const isCommentsOpen = ref(false);
const showShareModal = ref(false);
const shareModalTitle = ref('Udostępnij');
const selectedReelToShare = ref(null);
const reelRefs = ref<any[]>([]);

const activeIndex = computed(() => reels.value.findIndex(r => r.id === activeReelId.value));
const currentReel = computed(() => reels.value[activeIndex.value] || reels.value[0]);

let observer: IntersectionObserver | null = null;

const setReelRef = (el: any, index: number) => {
  if (el) reelRefs.value[index] = el.$el || el;
};

const handleOpenShare = (reel: any) => {
  selectedReelToShare.value = reel;
  showShareModal.value = true;
};

const goToReel = (direction: 'up' | 'down') => {
  const newIndex = direction === 'up' ? activeIndex.value - 1 : activeIndex.value + 1;
  if (newIndex >= 0 && newIndex < reels.value.length) {
    reelRefs.value[newIndex]?.scrollIntoView({ behavior: 'smooth' });
  }
};

onMounted(() => {
  observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) activeReelId.value = entry.target.getAttribute('data-id');
    });
  }, { threshold: 0.6 });

  nextTick(() => {
    reelRefs.value.forEach(el => el && observer?.observe(el));
  });
});

onUnmounted(() => observer?.disconnect());
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }
.animate-slide-in { animation: slideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
</style>
