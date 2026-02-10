<script setup lang="ts">
import { computed } from 'vue'
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue'
import type { Message, LinkMessage } from '@/types/Message'

interface Theme {
  id?: string
  sentBubbleColor?: string
}

const props = defineProps<{
  message: Message
  injectedTheme: Theme
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
      :class="[injectedTheme?.sentBubbleColor || 'bg-blue-500']"
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
