<script setup lang="ts">
import MediaItem from '@/components/feed/MediaItem.vue'
import type { ImageTagType } from '@/types/ImageTag'

defineProps<{
  media: {
    src: string
    altText?: string
    tags?: ImageTagType[]
  }[]
  postId: number
}>()
</script>

<template>
  <div class="w-full">
    <!-- 1 media -->
    <div v-if="media.length === 1" class="w-full">
      <MediaItem
        v-if="media[0]"
        :media="media[0]"
        :post-id="postId"
        :index="0"
        class="block w-full bg-black/5 relative"
      />
    </div>

    <!-- 2 media - obok siebie -->
    <div v-else-if="media.length === 2" class="grid grid-cols-2 gap-1">
      <MediaItem
        v-for="(item, idx) in media"
        :key="idx"
        :media="item"
        :post-id="postId"
        :index="idx"
        class="block aspect-square bg-black/5 relative"
      />
    </div>

    <!-- 3 media - lewa kolumna na 2 wiersze, prawa po jednym wierszu -->
    <div v-else-if="media.length === 3" class="grid grid-cols-2 gap-1">
      <MediaItem
        v-if="media[0]"
        :media="media[0]"
        :post-id="postId"
        :index="0"
        class="col-span-2 bg-black/5 relative"
      />
      <MediaItem
        v-if="media[1]"
        :media="media[1]"
        :post-id="postId"
        :index="1"
        class="bg-black/5 aspect-square relative"
      />
      <MediaItem
        v-if="media[2]"
        :media="media[2]"
        :post-id="postId"
        :index="2"
        class="bg-black/5 aspect-square relative"
      />
    </div>

    <!-- 4 media - 1 na górze, 3 na dole -->
    <div v-else-if="media.length === 4" class="flex flex-col gap-1">
      <MediaItem
        v-if="media[0]"
        :media="media[0]"
        :post-id="postId"
        :index="0"
        class="block w-full bg-black/5 relative"
      />
      <div class="grid grid-cols-3 gap-1">
        <MediaItem
          v-for="(item, idx) in media.slice(1)"
          :key="idx"
          :media="item"
          :post-id="postId"
          :index="idx + 1"
          class="block aspect-square bg-black/5 relative"
        />
      </div>
    </div>

    <!-- 5+ media - 2 po lewej (jedno pod drugim), 3 po prawej -->
    <div v-else class="grid grid-cols-2 gap-1">
      <!-- Lewa kolumna - 2 media -->
      <div class="flex flex-col gap-1">
        <MediaItem
          v-if="media[0]"
          :media="media[0]"
          :post-id="postId"
          :index="0"
          class="flex-1 bg-black/5 relative"
        />
        <MediaItem
          v-if="media[1]"
          :media="media[1]"
          :post-id="postId"
          :index="1"
          class="flex-1 bg-black/5 relative"
        />
      </div>
      <!-- Prawa kolumna - 3 media -->
      <div class="flex flex-col gap-1">
        <MediaItem
          v-if="media[2]"
          :media="media[2]"
          :post-id="postId"
          :index="2"
          class="flex-1 bg-black/5 relative"
        />
        <MediaItem
          v-if="media[3]"
          :media="media[3]"
          :post-id="postId"
          :index="3"
          class="flex-1 bg-black/5 relative"
        />
        <router-link
          v-if="media[4]"
          :to="`/photo/${postId}/4`"
          class="flex-1 bg-black/5 relative"
        >
          <MediaItem
            :media="media[4]"
            :post-id="postId"
            :index="4"
            class="flex-1 bg-black/5 relative"
          />
          <!-- Overlay z liczbą media dla 6+ -->
          <div
            v-if="media.length > 5"
            class="absolute inset-0 bg-black/50 flex items-center justify-center cursor-pointer"
          >
            <span class="text-white text-3xl font-bold"
              >+{{ media.length - 5 }}</span
            >
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>
