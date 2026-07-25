<script setup lang="ts">
import { useChatThemeStore } from '@/stores/chatTheme'
import { storeToRefs } from 'pinia'
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue'
import type { Message, LinkMessage } from '@/types/Message'

const { selectedTheme } = storeToRefs(useChatThemeStore())

const props = defineProps<{
  message: Message
}>()

function extractDomain(url: string): string {
  try {
    const hostname = new URL(url).hostname
    return hostname.startsWith('www.') ? hostname.substring(4) : hostname
  } catch {
    return url
  }
}
</script>

<template>
  <div
    class="flex flex-col overflow-hidden rounded-xl shadow-sm min-w-[250px] max-w-full border border-gray-200"
  >
    <a
      :href="message.url"
      target="_blank"
      rel="noopener noreferrer"
      :style="{ backgroundColor: selectedTheme.sentBubbleColor }"
      class="block p-3 text-white no-underline hover:underline break-all text-sm font-medium"
    >
      {{ message.url }}
    </a>

    <div class="bg-gray-100 px-3 py-2 flex items-center justify-between">
      <span class="text-black font-bold text-[15px]">
        {{ extractDomain(message.url) }}
      </span>
      <LinkVariantIcon :size="16" class="text-gray-400 opacity-50" />
    </div>
  </div>
</template>
