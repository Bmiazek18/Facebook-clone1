<!-- components/chat/info/ChatProfileHeader.vue -->
<template>
  <div class="pt-6 pb-4 flex flex-col items-center">
    <!-- Avatar -->
    <div class="relative mb-3 hover:opacity-90 cursor-pointer transition">
      <img
        :src="avatarUrl "
        class="w-20 h-20 rounded-full object-cover shadow-sm"
        :alt="$t('chat.avatar')"
      />
    </div>

    <!-- Nazwa -->
    <h2 class="text-[17px] font-bold text-theme-text hover:underline cursor-pointer tracking-tight text-center">
      {{ name }}
    </h2>

    <!-- Status -->
    <p class="text-[13px] text-theme-text-muted mt-0.5">
      {{ timeAgo ? `Aktywna ${timeAgo}` : 'Aktywna niedawno' }}
    </p>

    <!-- Pigułka szyfrowania -->
    <div class="mt-3.5 inline-flex items-center px-3 py-1 rounded-full bg-gray-200/60 text-black font-semibold text-[13px]">
      <LockIcon :size="14" class="mr-1.5 text-black" />
      <span>{{ $t('chat.wPelniSzyfrowane') }}</span>
    </div>

    <!-- Szybkie akcje -->
    <div class="flex mt-5 space-x-6 w-full justify-center px-4">
      <!-- Profil -->
      <NuxtLink v-tooltip.top="'Profil'" to="/profile" class="flex flex-col items-center cursor-pointer group" >
        <div class="w-9 h-9 bg-gray-200/70 rounded-full flex items-center justify-center group-hover:bg-gray-300/80 transition mb-1.5">
          <AccountCircleIcon :size="22" class="text-black" />
        </div>
        <span  class="text-[13px]  text-theme-text text-center">{{ $t('chat.profil') }}</span>
      </NuxtLink>

      <!-- Wycisz -->
      <div v-tooltip.top="'Wycisz'" class="flex flex-col items-center cursor-pointer group" @click="emit('openMute')">
        <div class="w-9 h-9 bg-gray-200/70 rounded-full flex items-center justify-center group-hover:bg-gray-300/80 transition mb-1.5">
          <BellIcon :size="22" class="text-black" />
        </div>
        <span  class="text-[13px]  text-theme-text text-center">{{ $t('actions.mute') }}</span>
      </div>

      <!-- Szukaj -->
      <div v-tooltip.top="'Szukaj'" class="flex flex-col items-center cursor-pointer group" @click="emit('openSearch')">
        <div class="w-9 h-9 bg-gray-200/70 rounded-full flex items-center justify-center group-hover:bg-gray-300/80 transition mb-1.5">
          <MagnifyIcon :size="22" class="text-black" />
        </div>
        <span  class="text-[13px]  text-theme-text text-center">{{ $t('common.search') }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AccountCircleIcon from 'vue-material-design-icons/AccountCircle.vue'
import BellIcon from 'vue-material-design-icons/Bell.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import LockIcon from 'vue-material-design-icons/Lock.vue'

defineProps<{
  avatarUrl?: string
  name: string
  timeAgo?: string
}>()

const emit = defineEmits<{
  (e: 'openProfile'): void
  (e: 'openMute'): void
  (e: 'openSearch'): void
}>()
</script>
