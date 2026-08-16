<script setup lang="ts">
import { computed } from 'vue'
import PhoneMissedIcon from 'vue-material-design-icons/PhoneMissed.vue'
import PhoneIncomingIcon from 'vue-material-design-icons/PhoneIncoming.vue'
import PhoneOutgoingIcon from 'vue-material-design-icons/PhoneOutgoing.vue'
import VideoIcon from 'vue-material-design-icons/Video.vue'
import type { Message } from '@/types/Message'

const props = defineProps<{
  message: Message
  isMe: boolean // Prop passed from MessageItem.vue
}>()

const emit = defineEmits(['call-again'])

const isCallRejectedMessage = (msg: Message): boolean => msg.type === 'call_rejected'
const isCallActive = computed(() => {
  return (
    props.message.type === 'call_group' ||
    (props.message as any).subType === 'call_started' ||
    (typeof props.message.content === 'string' && props.message.content.includes('SYSTEM_ACTION:call_started'))
  )
})
const isVideoCall = (msg: Message) => {
  return (
    msg.content?.toLowerCase().includes('wideo') || msg.content?.toLowerCase().includes('video') || isCallActive.value
  )
}

const callStyle = computed(() => {
  // 1. Aktywna rozmowa grupowa / połączenie oczekujące na dołączenie
  if (isCallActive.value) {
    return {
      icon: VideoIcon,
      iconBgClass: 'bg-[#0084FF] text-white animate-pulse',
      bubbleBgClass: 'bg-[#EBF5FF] border border-[#0084FF]/30 shadow-sm',
      buttonText: 'Dołącz do rozmowy',
      buttonClass: 'bg-[#0084FF] hover:bg-[#0073E6] text-white font-bold shadow-sm'
    }
  }

  // 2. Połączenia nieodebrane / odrzucone
  if (isCallRejectedMessage(props.message)) {
    return {
      icon: PhoneMissedIcon,
      iconBgClass: props.isMe ? 'bg-[#E4E6EB] text-black' : 'bg-[#FF3B30] text-white',
      bubbleBgClass: props.isMe ? 'bg-[#F0F2F5]' : 'bg-white shadow-sm border border-gray-100/30',
      buttonText: props.isMe ? 'Zadzwoń ponownie' : 'Oddzwoń',
      buttonClass: 'bg-[#E4E6EB] hover:bg-[#D8DADF] text-black font-semibold'
    }
  }

  // 3. Połączenia zakończone / odebrane
  const isVideo = isVideoCall(props.message)
  return {
    icon: isVideo ? VideoIcon : (props.isMe ? PhoneOutgoingIcon : PhoneIncomingIcon),
    iconBgClass: 'bg-[#E4E6EB] text-black',
    bubbleBgClass: props.isMe ? 'bg-[#F0F2F5]' : 'bg-white shadow-sm border border-gray-100/30',
    buttonText: props.isMe ? 'Zadzwoń ponownie' : 'Oddzwoń',
    buttonClass: 'bg-[#E4E6EB] hover:bg-[#D8DADF] text-black font-semibold'
  }
})

// Formatowanie czasu wzorowane na Messengerze
const formatDuration = (seconds?: any): string => {
  if (seconds === undefined || seconds === null) return '0 sek'
  const sec = typeof seconds === 'number' ? seconds : parseInt(String(seconds).replace(/[^0-9]/g, ''), 10)
  if (isNaN(sec) || sec <= 0) return '0 sek'

  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)

  if (m > 0 && s > 0) return `${m} min ${s} sek`
  if (m > 0) return `${m} min`
  return `${s} sek`
}

const resolvedDuration = computed(() => {
  if (props.message.duration !== undefined && props.message.duration !== null && Number(props.message.duration) > 0) {
    return Number(props.message.duration)
  }
  if ((props.message as any).payload !== undefined && Number((props.message as any).payload) > 0) {
    return Number((props.message as any).payload)
  }
  const content = String(props.message.content || '')
  const matchEnded = content.match(/call_ended:(\d+)/)
  if (matchEnded && matchEnded[1]) {
    return Number(matchEnded[1])
  }
  const matchParen = content.match(/\((\d+)\s*min(?:\s*(\d+)\s*sek)?\)/)
  if (matchParen) {
    const mins = matchParen[1] ? parseInt(matchParen[1], 10) : 0
    const secs = matchParen[2] ? parseInt(matchParen[2], 10) : 0
    return mins * 60 + secs
  }
  const matchSek = content.match(/\((\d+)\s*sek\)/)
  if (matchSek && matchSek[1]) {
    return parseInt(matchSek[1], 10)
  }
  return Number(props.message.duration) || 0
})
</script>

<template>
  <div
    class="flex flex-col p-2.5 rounded-[18px] w-[220px] transition-colors"
    :class="callStyle.bubbleBgClass"
  >
    <!-- Górna sekcja: Ikona i Tekst -->
    <div class="flex items-start gap-2.5">
      <!-- Ikona -->
      <div
        class="w-9 h-9 rounded-full flex items-center justify-center shrink-0 transition-colors"
        :class="callStyle.iconBgClass"
      >
        <component :is="callStyle.icon" :size="20" />
      </div>

      <!-- Tekst -->
      <div class="flex flex-col text-black">
        <span class="font-semibold text-[15px] leading-tight pr-1">
          <span v-if="isCallActive">Rozmowa grupowa</span>
          <span v-else-if="isCallRejectedMessage(message)">Nieodebrane połączenie</span>
          <span v-else-if="isVideoCall(message)">Rozmowa wideo</span>
          <span v-else>Połączenie głosowe</span>
        </span>

        <span class="text-[13px] text-gray-500 mt-0.5">
          <span v-if="isCallActive" class="text-[#0084FF] font-medium flex items-center gap-1">
            <span class="w-1.5 h-1.5 rounded-full bg-[#0084FF] animate-ping inline-block"></span>
            Kliknij, aby dołączyć
          </span>
          <span v-else-if="isCallRejectedMessage(message)">
            {{ message.timestamp ? message.timestamp.slice(0, 5) : '14:22' }}
          </span>
          <span v-else>
            {{ formatDuration(resolvedDuration) }}
          </span>
        </span>
      </div>
    </div>

    <!-- Dolna sekcja: Przycisk -->
    <button
      @click="emit('call-again')"
      class="mt-2.5 w-full text-[14px] py-2 rounded-[10px] transition-all cursor-pointer flex items-center justify-center gap-1.5"
      :class="callStyle.buttonClass"
    >
      <VideoIcon v-if="isCallActive" :size="16" />
      {{ callStyle.buttonText }}
    </button>
  </div>
</template>
