<script setup lang="ts">
import { computed } from 'vue'
import { Dropdown as VDropdown, vTooltip } from 'floating-vue'
import { reactionIcons } from '@/composables/usePostReactions'
import { getUserById } from '@/data/users'
import type { Comment } from '@/types/Post'
import { useReactionConfig } from '@/composables/useReactionConfig'

const props = defineProps<{
  comment: Comment,
  totalLikes: number,
  userReaction: string | null
}>()

const { getReactionConfig } = useReactionConfig()
</script>

<template>
  <VTooltip v-if="totalLikes > 0">
    <div class="flex items-center ml-1 cursor-pointer">
      <div
        class="rounded-full p-0.5 flex items-center justify-center w-[18px] h-[18px]"
        :class="getReactionConfig(userReaction || 'like').wrapperClass"
      >
        <component
          v-if="getReactionConfig(userReaction || 'like').mode === 'icon'"
          :is="getReactionConfig(userReaction || 'like').component"
          :size="11"
          :fillColor="getReactionConfig(userReaction || 'like').color"
        />
        <span v-else class="text-[20px]">{{ getReactionConfig(userReaction || 'like').char }}</span>
      </div>
      <span class="ml-1">{{ totalLikes }}</span>
    </div>
    <template #popper>
      <div class="">
        <template v-if="totalLikes < 5">
          <div v-for="(userIds, reaction) in comment.reactions" :key="reaction">
            <div v-if="userIds.length > 0" class="flex items-center gap-2 ">
                <div
        class="rounded-full p-0.5 flex items-center justify-center w-[18px] h-[18px]"
        :class="getReactionConfig(userReaction || 'like').wrapperClass"
      >
        <component
          v-if="getReactionConfig(userReaction || 'like').mode === 'icon'"
          :is="getReactionConfig(userReaction || 'like').component"
          :size="10"
          :fillColor="getReactionConfig(userReaction || 'like').color"
        />
        <span v-else class="text-[13px]">{{ getReactionConfig(userReaction || 'like').char }}</span>
      </div>
              <div class="text-sm">
                <div v-for="userId in userIds" :key="userId">
                  {{ getUserById(userId)?.name }}
                </div>
              </div>
            </div>
          </div>
        </template>
        <template v-else>
          <div class="flex items-center gap-2">
            <div v-for="(userIds, reaction) in comment.reactions" :key="reaction">
              <div v-if="userIds.length > 0" class="flex items-center gap-1">
                <img :src="reactionIcons[reaction]?.src" class="w-4 h-4" />
                <span class="text-sm font-bold">{{ userIds.length }}</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </template>
  </VTooltip>
</template>
