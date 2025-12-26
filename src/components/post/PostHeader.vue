<script setup lang="ts">
import { computed, type DefineComponent } from 'vue'
import { useRouter } from 'vue-router'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Close from 'vue-material-design-icons/Close.vue'
import ProfilePopper from '../ProfilePopper.vue'
import { useTheme } from '@/composables/useTheme'
import type { User } from '@/data/users'
import type { PostLocation } from '@/types/Post'
import LockIcon from 'vue-material-design-icons/Lock.vue'
import EarthIcon from 'vue-material-design-icons/Earth.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue'
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue'
import { Dropdown as VDropdown } from 'floating-vue';
import 'floating-vue/dist/style.css';
import PostSettingPopper from './PostSettingPopper.vue';

const props = defineProps<{
  authorName: string
  authorAvatar: string
  authorId?: number
  date?: string
  taggedUsers?: User[]
  location?: PostLocation
  privacy?: string
  postId?: string; // Add postId prop
}>()

const _emit = defineEmits<{
  (e: 'menu'): void
  (e: 'close'): void
  (e: 'editPost', postId: number): void;
  (e: 'deletePost', postId: number): void;
  (e: 'hidePost', postId: number): void;
}>()

const { isDark } = useTheme()
const router = useRouter()

const handleAvatarClick = () => {
  if (props.authorId) {
    router.push({ name: 'userProfile', params: { userId: props.authorId } })
  }
}

const privacyInfo = computed(() => {
  type Info = { label: string; icon: DefineComponent | null };
  const map: Record<string, Info> = {
    only_me: { label: 'Tylko ja', icon: LockIcon },
    public: { label: 'Publiczne', icon: EarthIcon },
    friends: { label: 'Znajomi', icon: AccountGroupIcon },
    friends_except: { label: 'Znajomi z wyjątkiem...', icon: AccountMultipleMinusIcon },
    specific_friends: { label: 'Konkretni znajomi', icon: AccountStarIcon },
  };
  if (!props.privacy) return { label: 'Publiczne', icon: EarthIcon };
  return map[props.privacy] || { label: props.privacy, icon: null };
});
</script>

<template>
  <div class="px-4 pt-3 pb-1">
    <div class="flex items-start">
      <button @click="handleAvatarClick" class="mr-2.5 rounded-full hover:opacity-80 transition-opacity">
        <img
          class="rounded-full w-10 h-10 object-cover cursor-pointer"
          :src="authorAvatar"
          :alt="authorName"
        >
      </button>

      <div class="flex-1 min-w-0 mt-0.5">
        <div class="text-theme-text text-[15px] leading-tight">
          <ProfilePopper :name="authorName" :user-id="authorId" class="font-semibold hover:underline cursor-pointer" />
          <template v-if="taggedUsers && taggedUsers.length">
            <span class="font-normal text-gray-600"> z: </span>
            <template v-for="(user, idx) in taggedUsers" :key="user.id">
              <span v-if="idx > 0">, </span>
              <ProfilePopper :name="user.name" :user-id="user.id" class="font-semibold hover:underline cursor-pointer" />
            </template>
          </template>
          <template v-if="location">
            <span class="font-normal text-gray-600"> jest w: </span>
            <span class="font-semibold">{{ location.title }}</span>
          </template>
        </div>
        <div class="flex items-center text-[13px] text-theme-text-secondary mt-0.5">
          <span class="hover:underline cursor-pointer">{{ date || '17 grudnia' }}</span>
          <span class="mx-1">·</span>
          <component :is="privacyInfo.icon" :size="12" :fillColor="isDark ? '#E4E6EB' : '#65676B'" v-tooltip="privacyInfo.label" />
        </div>
      </div>

      <div class="flex items-center -mr-2">
        <VDropdown placement="bottom-end" :triggers="['click']">
          <button @click="_emit('menu')" class="rounded-full p-2 hover:bg-theme-hover transition-colors">
            <DotsHorizontal :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
          </button>
          <template #popper>
            <PostSettingPopper
              v-if="postId"
              :post-id="postId"
              :author-id="authorId ?? 0"

            />
          </template>
        </VDropdown>
        <button @click="_emit('close')" class="rounded-full p-2 hover:bg-theme-hover transition-colors">
          <Close :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
        </button>
      </div>
    </div>
  </div>
</template>
