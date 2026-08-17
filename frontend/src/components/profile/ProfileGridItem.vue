<template>
  <!-- KARTA 1: POJEDYNCZY POST -->
  <div
    v-if="item.type === 'single'"
    @click="emit('click-single', item.data)"
    class="bg-theme-bg-secondary rounded-xl overflow-hidden flex flex-col h-full border border-theme-border hover:shadow-md transition-all cursor-pointer group"
  >
    <div class="aspect-square w-full overflow-hidden bg-gray-100 border-b border-theme-border">
      <img
        v-if="item.data.media && item.data.media.length > 0"
        :src="item.data.media[0].src"
        class="w-full h-full object-cover"
      />
      <div v-else class="w-full h-full flex items-center justify-center text-gray-400 text-sm">
        Brak zdjęcia
      </div>
    </div>
    <div class="p-3 mt-auto flex items-center gap-3">
      <img
        :src="item.data.authorAvatar || userImage"
        class="w-10 h-10 rounded-full border border-gray-200 object-cover shrink-0"
      />
      <div class="flex flex-col min-w-0">
        <span class="text-[14px] font-semibold text-theme-text line-clamp-1">
          {{ item.data.content || userName }}
        </span>
        <div class="text-[12px] text-gray-500 mt-0.5 font-medium flex items-center gap-1">
          <span class="truncate">
            {{ formatDate(item.data.date) }}
          </span>
          <svg class="w-3 h-3 fill-current ml-0.5 shrink-0" viewBox="0 0 16 16">
            <path
              d="M8 1a2 2 0 0 1 2 2v4H6V3a2 2 0 0 1 2-2zm3 6V3a3 3 0 0 0-6 0v4a2 2 0 0 0-2 2v5a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"
            />
          </svg>
        </div>
      </div>
    </div>
  </div>

  <!-- KARTA 2: ZGRUPOWANE POSTY Z TABLICY -->
  <div
    v-else-if="item.type === 'aggregated'"
    @click="emit('click-group', item.posts, 'Posty na osi czasu')"
    class="bg-theme-bg-secondary rounded-xl overflow-hidden flex flex-col h-full border border-theme-border hover:shadow-md transition-all cursor-pointer group"
  >
    <div
      class="aspect-square w-full bg-gray-300 flex items-center justify-center border-b border-theme-border"
    >
      <div class="flex -space-x-4">
        <img
          v-for="(avatar, i) in item.avatars"
          :key="i"
          :src="avatar"
          class="w-16 h-16 rounded-full border-2 border-transparent shadow-sm object-cover"
          :class="{ 'z-10': i === 0, 'z-20': i === 1, 'z-30': i === 2, 'z-40': i === 3 }"
        />
      </div>
    </div>
    <div class="p-3 mt-auto flex items-center gap-3">
      <img
        :src="item.avatars[0]"
        class="w-10 h-10 rounded-full border border-gray-200 object-cover shrink-0"
      />
      <div class="flex flex-col">
        <span class="text-[14px] font-semibold text-theme-text line-clamp-2 leading-tight">
          {{ item.count }} znajomych opublikowało post
          {{ isOwner ? 'na Twojej osi czasu.' : `na osi czasu użytkownika ${userName}.` }}
        </span>
        <div class="text-[12px] text-gray-500 mt-1 font-medium">
          {{ formatMonthDay(item.date) }}
        </div>
      </div>
    </div>
  </div>

  <!-- KARTA 3: ZGRUPOWANE POSTY URODZINOWE -->
  <div
    v-else-if="item.type === 'birthday'"
    @click="emit('click-group', item.posts, 'Posty urodzinowe')"
    class="bg-theme-bg-secondary rounded-xl overflow-hidden flex flex-col h-full border border-theme-border hover:shadow-md transition-all cursor-pointer group"
  >
    <div
      class="aspect-square w-full bg-pink-50 flex items-center justify-center border-b border-theme-border relative"
    >
      <div class="flex -space-x-4">
        <img
          v-for="(avatar, i) in item.avatars"
          :key="i"
          :src="avatar"
          class="w-16 h-16 rounded-full border-2 border-white shadow-md object-cover"
          :class="{ 'z-10': i === 0, 'z-20': i === 1, 'z-30': i === 2, 'z-40': i === 3 }"
        />
      </div>
      <div class="absolute top-2 right-2 text-2xl">🎂</div>
    </div>
    <div class="p-3 mt-auto flex items-center gap-3">
      <div class="flex flex-col">
        <span class="text-[14px] font-semibold text-theme-text line-clamp-2 leading-tight">
          {{ item.count }} znajomych złożyło
          {{
            isOwner ? 'Ci życzenia urodzinowe.' : `życzenia urodzinowe użytkownikowi ${userName}.`
          }}
        </span>
        <div class="text-[12px] text-gray-500 mt-1 font-medium">
          {{ formatMonthDay(item.date) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  item: {
    type: 'single' | 'aggregated' | 'birthday'
    data?: any
    count?: number
    date?: string
    avatars?: string[]
    posts?: any[]
  }
  userName: string
  userImage: string
  isOwner: boolean
}>()

const emit = defineEmits<{
  (e: 'click-single', post: any): void
  (e: 'click-group', posts: any[], title: string): void
}>()

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const dateObj = new Date(dateStr)
  return `${dateObj.toLocaleDateString('pl-PL', { day: 'numeric', month: 'long' })} o ${dateObj.toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' })}`
}

const formatMonthDay = (dateStr?: string) => {
  if (!dateStr) return ''
  const dateObj = new Date(dateStr)
  return dateObj.toLocaleDateString('pl-PL', { day: 'numeric', month: 'long' })
}
</script>
