<script setup lang="ts">
import ImageTag from './ImageTag.vue'
import type { ImageTagType } from '@/types/ImageTag'

defineProps<{
  images: {
    src: string
    altText?: string
    tags?: ImageTagType[]
  }[]
  postId: number
}>()
</script>

<template>
  <div class="w-full">
    <!-- 1 zdjęcie -->
    <div v-if="images.length === 1 && images[0]" class="w-full">
      <router-link
        :to="`/photo/${postId}/0`"
        class="block w-full bg-black/5 relative"
      >
        <img
          v-if="images[0]"
          class="w-full h-auto object-cover max-h-[600px] cursor-pointer"
          :src="images[0].src"
          :alt="images[0].altText || 'Post content'"
        />
        <template v-if="images[0]?.tags">
          <ImageTag
            v-for="tag in images[0].tags"
            :key="tag.id"
            :tag="tag"
            :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
            class="absolute"
          />
        </template>
      </router-link>
    </div>

    <!-- 2 zdjęcia - obok siebie -->
    <div v-else-if="images.length === 2" class="grid grid-cols-2 gap-1">
      <router-link
        v-for="(img, idx) in images"
        :key="idx"
        :to="`/photo/${postId}/${idx}`"
        class="block aspect-square bg-black/5 relative"
      >
        <img
          class="w-full h-full object-cover cursor-pointer"
          :src="img.src"
          :alt="img.altText || 'Post content'"
        />
        <template v-if="img.tags">
          <ImageTag
            v-for="tag in img.tags"
            :key="tag.id"
            :tag="tag"
            :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
            class="absolute"
          />
        </template>
      </router-link>
    </div>

    <!-- 3 zdjęcia - lewa kolumna na 2 wiersze, prawa po jednym wierszu -->
    <div v-else-if="images.length === 3" class="grid grid-cols-2 gap-1">
      <router-link
        :to="`/photo/${postId}/0`"
        class="row-span-2 bg-black/5 relative"
      >
        <img
          class="w-full h-full object-cover cursor-pointer"
          :src="images[0].src"
          :alt="images[0].altText || 'Post content'"
        />
        <template v-if="images[0].tags">
          <ImageTag
            v-for="tag in images[0].tags"
            :key="tag.id"
            :tag="tag"
            :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
            class="absolute"
          />
        </template>
      </router-link>
      <router-link
        :to="`/photo/${postId}/1`"
        class="bg-black/5 aspect-square relative"
      >
        <img
          class="w-full h-full object-cover cursor-pointer"
          :src="images[1].src"
          :alt="images[1].altText || 'Post content'"
        />
        <template v-if="images[1].tags">
          <ImageTag
            v-for="tag in images[1].tags"
            :key="tag.id"
            :tag="tag"
            :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
            class="absolute"
          />
        </template>
      </router-link>
      <router-link
        :to="`/photo/${postId}/2`"
        class="bg-black/5 aspect-square relative"
      >
        <img
          class="w-full h-full object-cover cursor-pointer"
          :src="images[2].src"
          :alt="images[2].altText || 'Post content'"
        />
        <template v-if="images[2].tags">
          <ImageTag
            v-for="tag in images[2].tags"
            :key="tag.id"
            :tag="tag"
            :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
            class="absolute"
          />
        </template>
      </router-link>
    </div>

    <!-- 4 zdjęcia - 1 na górze, 3 na dole -->
    <div v-else-if="images.length === 4" class="flex flex-col gap-1">
      <router-link
        :to="`/photo/${postId}/0`"
        class="block w-full bg-black/5 relative"
      >
        <img
          class="w-full h-auto max-h-[350px] object-cover cursor-pointer"
          :src="images[0].src"
          :alt="images[0].altText || 'Post content'"
        />
        <template v-if="images[0].tags">
          <ImageTag
            v-for="tag in images[0].tags"
            :key="tag.id"
            :tag="tag"
            :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
            class="absolute"
          />
        </template>
      </router-link>
      <div class="grid grid-cols-3 gap-1">
        <router-link
          v-for="(img, idx) in images.slice(1)"
          :key="idx"
          :to="`/photo/${postId}/${idx + 1}`"
          class="block aspect-square bg-black/5 relative"
        >
          <img
            class="w-full h-full object-cover cursor-pointer"
            :src="img.src"
            :alt="img.altText || 'Post content'"
          />
          <template v-if="img.tags">
            <ImageTag
              v-for="tag in img.tags"
              :key="tag.id"
              :tag="tag"
              :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
              class="absolute"
            />
          </template>
        </router-link>
      </div>
    </div>

    <!-- 5+ zdjęć - 2 po lewej (jedno pod drugim), 3 po prawej -->
    <div v-else class="grid grid-cols-2 gap-1">
      <!-- Lewa kolumna - 2 zdjęcia -->
      <div class="flex flex-col gap-1">
        <router-link
          :to="`/photo/${postId}/0`"
          class="flex-1 bg-black/5 relative"
        >
          <img
            class="w-full h-full object-cover cursor-pointer"
            :src="images[0].src"
            :alt="images[0].altText || 'Post content'"
          />
          <template v-if="images[0].tags">
            <ImageTag
              v-for="tag in images[0].tags"
              :key="tag.id"
              :tag="tag"
              :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
              class="absolute"
            />
          </template>
        </router-link>
        <router-link
          :to="`/photo/${postId}/1`"
          class="flex-1 bg-black/5 relative"
        >
          <img
            class="w-full h-full object-cover cursor-pointer"
            :src="images[1].src"
            :alt="images[1].altText || 'Post content'"
          />
          <template v-if="images[1].tags">
            <ImageTag
              v-for="tag in images[1].tags"
              :key="tag.id"
              :tag="tag"
              :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
              class="absolute"
            />
          </template>
        </router-link>
      </div>
      <!-- Prawa kolumna - 3 zdjęcia -->
      <div class="flex flex-col gap-1">
        <router-link
          :to="`/photo/${postId}/2`"
          class="flex-1 bg-black/5 relative"
        >
          <img
            class="w-full h-full object-cover cursor-pointer"
            :src="images[2].src"
            :alt="images[2].altText || 'Post content'"
          />
          <template v-if="images[2].tags">
            <ImageTag
              v-for="tag in images[2].tags"
              :key="tag.id"
              :tag="tag"
              :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
              class="absolute"
            />
          </template>
        </router-link>
        <router-link
          :to="`/photo/${postId}/3`"
          class="flex-1 bg-black/5 relative"
        >
          <img
            class="w-full h-full object-cover cursor-pointer"
            :src="images[3].src"
            :alt="images[3].altText || 'Post content'"
          />
          <template v-if="images[3].tags">
            <ImageTag
              v-for="tag in images[3].tags"
              :key="tag.id"
              :tag="tag"
              :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
              class="absolute"
            />
          </template>
        </router-link>
        <router-link
          :to="`/photo/${postId}/4`"
          class="flex-1 bg-black/5 relative"
        >
          <img
            class="w-full h-full object-cover cursor-pointer"
            :src="images[4].src"
            :alt="images[4].altText || 'Post content'"
          />
          <template v-if="images[4].tags">
            <ImageTag
              v-for="tag in images[4].tags"
              :key="tag.id"
              :tag="tag"
              :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
              class="absolute"
            />
          </template>
          <!-- Overlay z liczbą zdjęć dla 6+ -->
          <div
            v-if="images.length > 5"
            class="absolute inset-0 bg-black/50 flex items-center justify-center cursor-pointer"
          >
            <span class="text-white text-3xl font-bold"
              >+{{ images.length - 5 }}</span
            >
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>
