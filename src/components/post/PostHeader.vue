<script setup lang="ts">
import { useRouter } from 'vue-router'
import Earth from 'vue-material-design-icons/Earth.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Close from 'vue-material-design-icons/Close.vue'
import ProfilePopper from '../ProfilePopper.vue'
import { useTheme } from '@/composables/useTheme'
import type { User } from '@/data/users'
import type { PostLocation } from '@/components/PostCreator.vue'

const props = defineProps<{
  authorName: string
  authorAvatar: string
  authorId?: number
  date?: string
  taggedUsers?: User[]
  location?: PostLocation
}>()

defineEmits<{
  (e: 'menu'): void
  (e: 'close'): void
}>()

const { isDark } = useTheme()
const router = useRouter()

const handleAvatarClick = () => {
  if (props.authorId) {
    router.push({ name: 'userProfile', params: { userId: props.authorId } })
  }
}
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
          <Earth :size="12" fillColor="#65676B" v-tooltip="'Publiczne'" />
        </div>
      </div>

      <div class="flex items-center -mr-2">
        <button @click="$emit('menu')" class="rounded-full p-2 hover:bg-theme-hover transition-colors">
          <DotsHorizontal :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
        </button>
        <button @click="$emit('close')" class="rounded-full p-2 hover:bg-theme-hover transition-colors">
          <Close :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
        </button>
      </div>
    </div>
  </div>
</template>
