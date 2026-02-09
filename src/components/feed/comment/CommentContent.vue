<script setup lang="ts">
import { defineProps } from 'vue'
import { getUserById } from '@/data/users'
import type { ProcessedContent } from '@/utils/contentProcessor'
import ProfilePopper from '../profile/ProfilePopper.vue';

defineProps<{
  content: ProcessedContent[]
}>()
</script>

<template>
  <p class="text-[15px] text-theme-text">
    <template v-for="(part, index) in content" :key="index">


<ProfilePopper mention v-if="part.type === 'mention'" :user-id="part.userId" class="inline-flex"/>



      <router-link
        v-else-if="part.type === 'hashtag'"
        :to="{ name: 'hashtag', params: { hashtag: part.hashtag } }"
        class="text-blue-500 hover:underline"
      >
        {{ part.value }}
      </router-link>
      <a
        v-else-if="part.type === 'link'"
        class="text-blue-500 hover:underline"
        :href="part.value"
        target="_blank"
        rel="noopener noreferrer"
        >{{ part.value }}</a
      >
      <span v-else>{{ part.value }}</span>
    </template>
  </p>
</template>
