<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute } from 'vue-router';

// Komponenty wewnętrzne (zakładam, że istnieją w Twoim projekcie)
import ImageWithGradient from '@/components/media/ImageWithGradient.vue';
import PostItem from '@/components/feed/post/PostItem.vue';
import CreateBox from '@/components/createPost/CreateBox.vue';

// Store
import { usePostsStore } from '@/stores/posts';
import { useGroupsStore } from '@/stores/groups';
import type { Group as GroupType } from '@/types/Group';

// --- IKONY (vue-material-design-icons) ---
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue';
import ShareVariantIcon from 'vue-material-design-icons/ShareVariant.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import InformationIcon from 'vue-material-design-icons/Information.vue';

import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'; // Ważne: dodano
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'; // Ważne: dodano
import PlusIcon from 'vue-material-design-icons/Plus.vue';

import { useStickySidebar } from '@/composables/useStickySidebar';
import { useI18n } from 'vue-i18n';


const { t } = useI18n();

// --- LOGIKA "FACEBOOK DUAL STICKY" (Prawy pasek) ---
const rightSectionRef = ref<HTMLDivElement | null>(null);
const { stickyTop } = useStickySidebar(rightSectionRef, 125, 16);

// --- STICKY HEADER LOGIC ---
const tabsContainerRef = ref<HTMLElement | null>(null);
const isTabsFixed = ref(false);

const handleScroll = () => {
    if (tabsContainerRef.value) {
        // Jeśli element zakładek dotyka górnego paska nawigacji (ok. 56px)
        const rect = tabsContainerRef.value.getBoundingClientRect();
        isTabsFixed.value = rect.top <= 56;
    }
};

onMounted(() => {

    window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
});
// --- DANE (Store) ---
const route = useRoute();
const groupsStore = useGroupsStore();

const groupDetails = computed<GroupType | undefined>(() => {
  const id = route.params.id as string;
  return groupsStore.getGroupById(id);
});

const postsStore = usePostsStore();
const groupPosts = computed(() => {
    const groupId = route.params.id as string;
    return postsStore.posts.filter(post => post.groupId === groupId);
});

const handleDeletePost = (postId: string) => {
    postsStore.removePost(postId);
};

// --- UI LOGIC ---
const isDescriptionExpanded = ref(false);
const truncatedDescription = computed(() => {
  const description = groupDetails.value?.description || '';
  const maxLength = 200;
  if (description.length > maxLength && !isDescriptionExpanded.value) {
    return description.substring(0, maxLength) + '...';
  }
  return description;
});




</script>

<template>
  <div class="flex min-h-screen pb-10 bg-theme-bg text-theme-text">
    <div class="flex-1">

      <div class="w-full bg-theme-bg-secondary">
         <ImageWithGradient
                :image-url=" groupDetails?.images "
                :initial-width="1250"
                :initial-height="350"
                class="w-full h-full object-cover"
              />
        <div class="max-w-[1200px] mx-auto px-0 md:px-4 lg:px-0">


 <div
            v-if="isTabsFixed"
            class="fixed top-[50px] left-0 right-0 h-[60px] bg-theme-bg-secondary shadow-theme-shadow border-b border-theme-border z-30 animate-slide-down flex items-center"
        >
            <div class="max-w-[1200px] flex items-center justify-between w-full mx-auto px-4 lg:px-0">
                <div class="flex items-center space-x-3">
 <img :src="groupDetails?.image" class=" h-10 w-10 rounded-md">
                    <div class="text-[17px] text-theme-text font-bold leading-5">
                        {{ groupDetails?.name }}
                    </div>
                </div>
                  <div class="flex items-center gap-2 w-full md:w-auto">
                    <button class="bg-[#0866FF] hover:bg-[#0052CC] text-white h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5">
                        <PlusIcon :size="18" /> {{ t('groups.invite') }}
                    </button>

                    <button class="h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5 bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text">
                        <ShareVariantIcon :size="18" /> {{ t('common.share') }}
                    </button>

                    <button class="h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5 bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text">
                        <CheckCircleIcon :size="18" /> {{ t('groups.joined') }} <ChevronDownIcon :size="18" />
                    </button>

                    <button class="h-9 w-12 rounded-md font-semibold transition flex items-center justify-center bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text">
                        <ChevronDownIcon :size="20"/>
                    </button>

                </div>
            </div>
        </div>
          <div class="px-4 pb-0 relative bg-theme-bg-secondary">
            <div class="max-w-[1200px] mx-auto">

                <div class="mb-4">

                <h1 class="text-[28px] font-bold leading-tight text-theme-text">
                    {{ groupDetails?.name || t('groups.groupName') }}
                </h1>

                <div class="text-[15px] mt-1 font-semibold flex items-center gap-1.5 text-theme-text-secondary">
                    <span class="flex items-center gap-1">
                        <AccountGroupIcon v-if="groupDetails?.privacy !== 'public'" :size="16" />
                        <EarthIcon v-else :size="16" />
                        <span>{{ groupDetails?.privacy === 'public' ? t('groups.public') : t('groups.private') }}</span>
                    </span>
                    <span class="text-[6px] align-middle">•</span>
                    <span class="font-semibold  hover:underline cursor-pointer">
                    {{ groupDetails?.members || '0' }} {{ t('groups.members') }}
                    </span>
                </div>
                </div>

                <div class="flex flex-col md:flex-row justify-between items-end gap-4 mb-4">

                <div   class="flex items-center -space-x-2 overflow-hidden py-1 pl-1">
                    <img
                        v-for="i in 8"
                        :key="i"
                        :src="`https://i.pravatar.cc/150?img=${i + 10}`"
                        alt="Member"
                        class="w-[38px] h-[38px] rounded-full border-2 border-theme-border cursor-pointer hover:z-10 relative"
                    />
                </div>

                <div   class="flex items-center gap-2 w-full md:w-auto">
                    <button class="bg-[#0866FF] hover:bg-[#0052CC] text-white h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5">
                        <PlusIcon :size="18" /> {{ t('groups.invite') }}
                    </button>

                    <button class="h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5 bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text">
                        <ShareVariantIcon :size="18" /> {{ t('common.share') }}
                    </button>

                    <button class="h-9 px-3 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5 bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text">
                        <CheckCircleIcon :size="18" /> {{ t('groups.joined') }} <ChevronDownIcon :size="18" />
                    </button>

                    <button class="h-9 w-12 rounded-md font-semibold transition flex items-center justify-center bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text">
                        <ChevronDownIcon :size="20"/>
                    </button>

                </div>
                </div>

                <div ref="tabsContainerRef" class="border-t flex items-center justify-between h-[60px] border-theme-border">

                <div class="flex h-full overflow-x-auto no-scrollbar">
                    <button class="px-4 h-full font-semibold text-[15px] rounded-lg  flex items-center transition-colors whitespace-nowrap text-theme-text-secondary hover:bg-theme-hover pb-[3px]">
                        {{ t('groups.info') }}
                    </button>

                    <button class="text-[#0866FF] px-4 h-full font-semibold text-[15px] border-b-[3px] border-[#0866FF] flex items-center transition-colors whitespace-nowrap">
                        {{ t('groups.discussion') }}
                    </button>

                    <button class="px-4 h-full font-semibold text-[15px] rounded-lg  flex items-center transition-colors whitespace-nowrap text-theme-text-secondary hover:bg-theme-hover pb-[3px]">
                        {{ t('groups.members') }}
                    </button>
                    <button class="px-4 h-full font-semibold text-[15px] rounded-lg  flex items-center transition-colors whitespace-nowrap text-theme-text-secondary hover:bg-theme-hover pb-[3px]">
                        {{ t('groups.events') }}
                    </button>
                    <button class="px-4 h-full font-semibold text-[15px] rounded-lg  flex items-center transition-colors whitespace-nowrap text-theme-text-secondary hover:bg-theme-hover pb-[3px]">
                        {{ t('groups.media') }}
                    </button>
                    <button class="px-4 h-full font-semibold text-[15px] rounded-lg  flex items-center transition-colors whitespace-nowrap text-theme-text-secondary hover:bg-theme-hover pb-[3px]">
                        {{ t('groups.files') }}
                    </button>
                </div>

                <div class="flex items-center gap-1 pl-2 shrink-0">
                    <button class="p-2 rounded-full transition w-9 h-9 flex items-center justify-center bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text">
                        <MagnifyIcon :size="20"/>
                    </button>
                    <button class="p-2 rounded-full transition w-9 h-9 flex items-center justify-center bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text">
                        <DotsHorizontalIcon :size="20"/>
                    </button>
                </div>

                </div>

            </div>
          </div>
        </div>
      </div>

      <div class="w-full max-w-[1200px] mx-auto px-4 lg:px-0 mt-4">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-4">

          <div class="lg:col-span-7 space-y-4">
            <CreateBox class="rounded-lg shadow-sm bg-theme-bg-secondary border border-theme-border" />

            <PostItem
                v-for="post in groupPosts"
                :key="post.id"
                :post="post"
                isGroup
                class="rounded-lg shadow-sm bg-theme-bg-secondary border border-theme-border"
                @delete="handleDeletePost"
            />

            <div v-if="groupPosts.length === 0" class="text-center py-10 rounded-lg shadow-sm bg-theme-bg-secondary text-theme-text-secondary">
               {{ t('groups.noPosts') }}
            </div>
          </div>

          <div
            ref="rightSectionRef"
            class="lg:col-span-5 space-y-4 sticky self-start"
            :style="{ top: `${stickyTop}px` }"
          >
            <div class="rounded-lg shadow-sm p-4 bg-theme-bg-secondary border border-theme-border">
              <div class="flex justify-between items-center mb-3">
                 <h2 class="text-[17px] font-bold text-theme-text">{{ t('groups.information') }}</h2>
              </div>

              <ul class="space-y-4">
                <li class="flex items-start gap-3">
                  <div class="mt-1"><InformationIcon class="text-theme-text-secondary" :size="20" /></div>
                  <div class="text-[15px] leading-snug text-theme-text">
                    {{ truncatedDescription }}
                    <span
                      v-if="(groupDetails?.description || '').length > 200"
                      @click="isDescriptionExpanded = !isDescriptionExpanded"
                      class="font-semibold cursor-pointer hover:underline ml-1 text-theme-primary"
                    >
                      {{ isDescriptionExpanded ? t('groups.hide') : t('groups.seeMore') }}
                    </span>
                  </div>
                </li>

                <li class="flex items-center gap-3">
                  <div>
                      <EarthIcon v-if="groupDetails?.privacy === 'public'" class="text-theme-text-secondary" :size="20" />
                      <AccountGroupIcon v-else class="text-theme-text-secondary" :size="20" />
                  </div>
                  <div>
                      <div class="text-[17px] font-semibold text-theme-text">
                          {{ groupDetails?.privacy === 'public' ? t('groups.public') : t('groups.private') }}
                      </div>
                      <div class="text-[13px] text-theme-text-secondary">
                          {{ groupDetails?.privacy === 'public' ? t('groups.publicDescription') : t('groups.privateDescription') }}
                      </div>
                  </div>
                </li>

                <li class="flex items-center gap-3">
                  <div><AccountGroupIcon class="text-theme-text-secondary" :size="20" /></div>
                  <div>
                      <div class="text-[17px] font-semibold text-theme-text">
                          {{ t('groups.visibility') }}
                      </div>
                      <div class="text-[13px] text-theme-text-secondary">
                          {{ t('groups.visibleDescription') }}
                      </div>
                  </div>
                </li>


              </ul>
            </div>
            </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Ukrywanie scrollbara w zakładkach, ale zachowanie funkcjonalności */
.no-scrollbar::-webkit-scrollbar {
    display: none;
}
.no-scrollbar {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

/* Stylizacja paska przewijania dla całej strony (opcjonalnie) */
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: #f0f2f5;
}
::-webkit-scrollbar-thumb {
  background: #bcc0c4;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #999;
}
@keyframes slide-down {
    from {
        opacity: 0;
        transform: translateY(-100%);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.animate-slide-down {
    animation: slide-down 0.3s ease-out;
}
</style>

