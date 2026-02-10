<script setup lang="ts">
import { computed } from 'vue'
import PhoneMissedIcon from 'vue-material-design-icons/PhoneMissed.vue'
import PhoneIcon from 'vue-material-design-icons/Phone.vue'
import VideoIcon from 'vue-material-design-icons/Video.vue'
import type { Message } from '@/types/Message'

const props = defineProps<{
  message: Message
  isMe: boolean // Prop passed from MessageItem.vue
}>()

const isCallRejectedMessage = (msg: Message): boolean => msg.type === 'call_rejected'
const isVideoCall = (msg: Message) => {
  return (
    msg.content?.toLowerCase().includes('wideo') || msg.content?.toLowerCase().includes('video')
  )
}

const callStyle = computed(() => {
  if (isCallRejectedMessage(props.message)) {
    return {
      icon: PhoneMissedIcon,
      iconBgClass: !props.isMe ? 'bg-red-500 text-white' : 'bg-gray-200 text-gray-500',
    }
  }
  const isVideo = isVideoCall(props.message)
  return {
    icon: isVideo ? VideoIcon : PhoneIcon,
    iconBgClass: 'bg-gray-200 text-gray-800',
  }
})

const formatSeconds = (seconds: number): string => {
  if (isNaN(seconds) || seconds < 0) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}
</script>

<template>
  <div
    class="flex items-center p-3 bg-white rounded-2xl shadow-sm min-w-[240px] border border-gray-100"
  >
    <div
      class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 mr-3"
      :class="callStyle.iconBgClass"
    >
      <component :is="callStyle.icon" :size="20" />
    </div>

    <div class="flex flex-col text-gray-800">
      <span class="font-bold text-[15px] leading-tight">
        <span v-if="isCallRejectedMessage(message)">Nieodebrane połączenie głosowe</span>
        <span v-else-if="isVideoCall(message)">Rozmowa wideo</span>
        <span v-else>Połączenie głosowe</span>
      </span>

      <span class="text-xs text-gray-500 mt-1">
        <span v-if="isCallRejectedMessage(message)">
          {{ message.timestamp ? message.timestamp.slice(0, 5) : '16:13' }}
        </span>
        <span v-else>
          {{ formatSeconds(message.duration) }}
        </span>
      </span>
    </div>
  </div>
</template>
