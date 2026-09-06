<script setup lang="ts">
interface GalleryItem {
  id: string | number
  imageUrl: string
  name?: string
  mutualFriendsCount?: number // <-- Dodane pole (opcjonalne)
}

const props = defineProps<{
  title: string
  subtitle?: string
  actionText?: string
  items: GalleryItem[]
}>()

const emit = defineEmits<{
  (e: 'click-action'): void
  (e: 'click-item', item: GalleryItem): void
}>()
</script>

<template>
  <div class="bg-theme-bg-secondary p-4 mt-4 rounded-lg shadow-md">
    <div class="flex justify-between items-baseline mb-2">
      <div>
        <h2 class="text-xl font-bold text-theme-text">{{ title }}</h2>
        <div v-if="subtitle" class="text-sm text-theme-text-secondary mt-0.5">
          {{ subtitle }}
        </div>
      </div>
      <button
        v-if="actionText"
        @click="emit('click-action')"
        class="text-blue-500 text-[15px] hover:underline cursor-pointer bg-transparent border-none p-0 outline-none"
      >
        {{ actionText }}
      </button>
    </div>

    <div class="grid grid-cols-3 gap-2.5 mt-4">
      <div
        v-for="item in items"
        :key="item.id"
        @click="emit('click-item', item)"
        class="cursor-pointer group flex flex-col"
      >
        <div class="w-full aspect-square rounded-lg overflow-hidden bg-theme-bg-tertiary">
          <img
            :src="item.imageUrl"
            class="w-full h-full object-cover transform group-hover:brightness-95 transition-transform duration-200"
            :alt="item.name || title"
            loading="lazy"
          />
        </div>

        <div
          v-if="item.name"
          class="mt-1.5 text-[13px] font-semibold text-theme-text group-hover:underline truncate"
          :title="item.name"
        >
          {{ item.name }}
        </div>

        <div
          v-if="item.mutualFriendsCount !== undefined"
          class="text-[13px] text-theme-text-secondary text-wrap"
        >{{ $t('profile.itemMutualfriendscountWspolnychZnajomych') }}</div>
      </div>
    </div>
  </div>
</template>
