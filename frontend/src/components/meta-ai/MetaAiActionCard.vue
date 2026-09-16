<script setup lang="ts">
import { computed } from 'vue'
import ShieldCheckOutlineIcon from 'vue-material-design-icons/ShieldCheckOutline.vue'
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue'
import CloseCircleOutlineIcon from 'vue-material-design-icons/CloseCircleOutline.vue'
import PencilOutlineIcon from 'vue-material-design-icons/PencilOutline.vue'
import PaletteOutlineIcon from 'vue-material-design-icons/PaletteOutline.vue'
import CompassOutlineIcon from 'vue-material-design-icons/CompassOutline.vue'
import MessageOutlineIcon from 'vue-material-design-icons/MessageOutline.vue'
import CalendarPlusIcon from 'vue-material-design-icons/CalendarPlus.vue'
import FormatListBulletedIcon from 'vue-material-design-icons/FormatListBulleted.vue'
import ReplyOutlineIcon from 'vue-material-design-icons/ReplyOutline.vue'
import FileDocumentOutlineIcon from 'vue-material-design-icons/FileDocumentOutline.vue'
import MicrophoneOutlineIcon from 'vue-material-design-icons/MicrophoneOutline.vue'
import LoadingIcon from 'vue-material-design-icons/Loading.vue'
import type { ActionProposal } from '@/composables/useMetaAiActions'

const props = defineProps<{
  proposal: ActionProposal
}>()

const emit = defineEmits<{
  (e: 'approve', proposal: ActionProposal): void
  (e: 'reject', proposal: ActionProposal): void
}>()

const actionIcon = computed(() => {
  switch (props.proposal.action) {
    case 'create_post':
      return PencilOutlineIcon
    case 'switch_theme':
      return PaletteOutlineIcon
    case 'navigate_to':
      return CompassOutlineIcon
    case 'open_chat':
      return MessageOutlineIcon
    case 'create_event':
      return CalendarPlusIcon
    case 'create_poll':
      return FormatListBulletedIcon
    case 'draft_reply':
      return ReplyOutlineIcon
    case 'summarize_chat':
      return FileDocumentOutlineIcon
    case 'transcribe_voice':
      return MicrophoneOutlineIcon
    default:
      return ShieldCheckOutlineIcon
  }
})

const isPending = computed(() => props.proposal.status === 'pending')
const isExecuting = computed(() => props.proposal.status === 'executing')
const isExecuted = computed(() => props.proposal.status === 'executed')
const isRejected = computed(() => props.proposal.status === 'rejected')
const isFailed = computed(() => props.proposal.status === 'failed')
</script>

<template>
  <div
    class="my-2 rounded-xl border border-blue-500/30 bg-gradient-to-br from-blue-500/5 via-indigo-500/5 to-purple-500/5 dark:from-blue-950/20 dark:via-indigo-950/20 dark:to-purple-950/20 p-3 shadow-sm transition-all text-xs sm:text-sm text-theme-text"
  >
    <!-- Header: HITL Safety Badge -->
    <div class="flex items-center justify-between pb-2 mb-2 border-b border-theme-border/50">
      <div class="flex items-center gap-1.5 text-blue-600 dark:text-blue-400 font-semibold text-[11px] tracking-wide uppercase">
        <ShieldCheckOutlineIcon :size="15" />
        <span>Human in the loop</span>
      </div>

      <!-- Status Pill -->
      <span
        v-if="isPending"
        class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-300"
      >
        Wymaga zgody
      </span>
      <span
        v-else-if="isExecuting"
        class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300 flex items-center gap-1"
      >
        <LoadingIcon :size="11" class="animate-spin" /> Wykonywanie...
      </span>
      <span
        v-else-if="isExecuted"
        class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-300 flex items-center gap-1"
      >
        <CheckCircleIcon :size="11" /> Wykonano
      </span>
      <span
        v-else-if="isRejected"
        class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-100 text-rose-700 dark:bg-rose-900/40 dark:text-rose-300 flex items-center gap-1"
      >
        <CloseCircleOutlineIcon :size="11" /> Odrzucono
      </span>
      <span
        v-else-if="isFailed"
        class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300"
      >
        Błąd
      </span>
    </div>

    <!-- Title & Description -->
    <div class="flex items-start gap-2 mb-2">
      <div class="p-1.5 rounded-lg bg-blue-500/10 text-blue-600 dark:text-blue-400 shrink-0 mt-0.5">
        <component :is="actionIcon" :size="18" />
      </div>
      <div class="flex flex-col min-w-0">
        <span class="font-bold text-theme-text text-sm leading-tight">
          {{ proposal.title }}
        </span>
        <span class="text-xs text-theme-text-secondary mt-0.5">
          {{ proposal.description }}
        </span>
      </div>
    </div>

    <!-- Payload Details Preview -->
    <div class="p-2.5 rounded-lg bg-theme-bg/80 border border-theme-border/60 text-xs mb-3 space-y-1.5">
      <!-- Create Post Preview -->
      <div v-if="proposal.action === 'create_post'" class="flex flex-col gap-1">
        <span class="font-semibold text-theme-text-secondary text-[11px] uppercase tracking-wider">Treść do opublikowania:</span>
        <blockquote class="pl-2 border-l-2 border-blue-500 text-theme-text italic bg-blue-50/50 dark:bg-blue-950/30 p-1.5 rounded-r">
          "{{ proposal.details.content }}"
        </blockquote>
        <div class="text-[10px] text-theme-text-secondary mt-0.5">
          Widoczność: <span class="font-medium text-theme-text">{{ proposal.details.visibility || 'Publiczny' }}</span>
        </div>
      </div>

      <!-- Create Poll Preview -->
      <div v-else-if="proposal.action === 'create_poll'" class="flex flex-col gap-1.5">
        <span class="font-semibold text-theme-text-secondary text-[11px] uppercase tracking-wider">Pytanie ankiety:</span>
        <div class="font-bold text-theme-text">{{ proposal.details.question }}</div>
        <div class="flex flex-wrap gap-1 mt-1">
          <span
            v-for="(opt, oIdx) in proposal.details.options"
            :key="oIdx"
            class="px-2 py-0.5 rounded-md bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-200 text-xs font-medium"
          >
            📊 {{ opt }}
          </span>
        </div>
      </div>

      <!-- Draft Reply Preview -->
      <div v-else-if="proposal.action === 'draft_reply'" class="flex flex-col gap-1">
        <div class="flex items-center justify-between text-[11px] text-theme-text-secondary">
          <span>Odbiorca: <strong class="text-theme-text">{{ proposal.details.recipient }}</strong></span>
          <span class="px-1.5 py-0.2 rounded bg-theme-hover font-medium">Ton: {{ proposal.details.tone || 'uprzejmy' }}</span>
        </div>
        <blockquote class="pl-2 border-l-2 border-blue-500 text-theme-text bg-blue-50/50 dark:bg-blue-950/30 p-2 rounded-r italic">
          "{{ proposal.details.replyText }}"
        </blockquote>
      </div>

      <!-- Voice Transcription Preview -->
      <div v-else-if="proposal.action === 'transcribe_voice'" class="flex flex-col gap-1.5">
        <span class="font-semibold text-theme-text-secondary text-[11px] uppercase tracking-wider">Treść transkrypcji:</span>
        <blockquote class="pl-2 border-l-2 border-indigo-500 text-theme-text bg-indigo-50/50 dark:bg-indigo-950/30 p-2 rounded-r italic">
          "{{ proposal.details.transcription }}"
        </blockquote>
        <div v-if="proposal.details.extractedTask" class="flex items-center justify-between text-xs pt-1">
          <span class="text-theme-text-secondary">🎯 Wyodrębnione zadanie:</span>
          <span class="font-bold text-theme-text">{{ proposal.details.extractedTask }}</span>
        </div>
        <div v-if="proposal.details.deadline" class="flex items-center justify-between text-xs">
          <span class="text-theme-text-secondary">⏰ Termin:</span>
          <span class="font-semibold text-amber-600 dark:text-amber-400">{{ proposal.details.deadline }}</span>
        </div>
      </div>

      <!-- Switch Theme Preview -->
      <div v-else-if="proposal.action === 'switch_theme'" class="flex items-center justify-between">
        <span class="text-theme-text-secondary">Wybrany motyw:</span>
        <span class="font-bold uppercase text-blue-600 dark:text-blue-400">{{ proposal.details.theme }}</span>
      </div>

      <!-- Navigation Preview -->
      <div v-else-if="proposal.action === 'navigate_to'" class="flex items-center justify-between">
        <span class="text-theme-text-secondary">Docelowa podstrona:</span>
        <span class="font-bold text-theme-text px-2 py-0.5 rounded bg-theme-hover">{{ proposal.details.targetName || proposal.details.page }}</span>
      </div>

      <!-- Open Chat Preview -->
      <div v-else-if="proposal.action === 'open_chat'" class="flex items-center justify-between">
        <span class="text-theme-text-secondary">Rozmówca:</span>
        <span class="font-bold text-theme-text">{{ proposal.details.recipient }}</span>
      </div>

      <!-- Create Event Preview -->
      <div v-else-if="proposal.action === 'create_event'" class="flex flex-col gap-1.5">
        <span class="font-semibold text-theme-text-secondary text-[11px] uppercase tracking-wider">Szczegóły wydarzenia:</span>
        <div class="flex items-center justify-between">
          <span class="font-bold text-theme-text text-sm">{{ proposal.details.title }}</span>
          <span v-if="proposal.details.date" class="px-2 py-0.5 rounded-md bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-200 text-xs font-semibold">
            📅 {{ proposal.details.date }}
          </span>
        </div>
        <p v-if="proposal.details.description" class="text-xs text-theme-text-secondary italic">
          {{ proposal.details.description }}
        </p>
      </div>

      <!-- Generic Details -->
      <div v-else v-for="(val, key) in proposal.details" :key="key" class="flex justify-between">
        <span class="text-theme-text-secondary">{{ key }}:</span>
        <span class="font-medium text-theme-text">{{ val }}</span>
      </div>
    </div>

    <!-- Execution Feedback Message -->
    <div
      v-if="proposal.resultMessage && !isPending"
      :class="[
        'mb-2 p-2 rounded-lg text-xs',
        isExecuted ? 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-300 border border-emerald-500/20' : '',
        isRejected ? 'bg-rose-500/10 text-rose-700 dark:text-rose-300 border border-rose-500/20' : '',
        isFailed ? 'bg-red-500/10 text-red-700 dark:text-red-300 border border-red-500/20' : '',
      ]"
    >
      {{ proposal.resultMessage }}
    </div>

    <!-- Buttons: Zatwierdź / Odrzuć (Tylko gdy Pending) -->
    <div v-if="isPending" class="flex items-center gap-2 pt-1">
      <button
        @click="emit('approve', proposal)"
        class="flex-1 py-1.5 px-3 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs transition-all shadow-xs flex items-center justify-center gap-1 cursor-pointer active:scale-98"
      >
        <CheckCircleIcon :size="15" />
        <span>{{ proposal.action === 'draft_reply' ? 'Zatwierdź i wyślij' : 'Zatwierdź i wykonaj' }}</span>
      </button>

      <button
        @click="emit('reject', proposal)"
        class="py-1.5 px-3 rounded-lg bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-theme-text font-medium text-xs transition-all cursor-pointer active:scale-98 flex items-center gap-1"
      >
        <CloseCircleOutlineIcon :size="15" />
        <span>Odrzuć</span>
      </button>
    </div>
  </div>
</template>
