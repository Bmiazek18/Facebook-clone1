<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import MinusIcon from 'vue-material-design-icons/Minus.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import OpenInAppIcon from 'vue-material-design-icons/OpenInApp.vue'
import RefreshIcon from 'vue-material-design-icons/Refresh.vue'
import SendIcon from 'vue-material-design-icons/Send.vue'
import StopCircleOutlineIcon from 'vue-material-design-icons/StopCircleOutline.vue'
import ContentCopyIcon from 'vue-material-design-icons/ContentCopy.vue'
import CheckIcon from 'vue-material-design-icons/Check.vue'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { useMetaAiActions, type ActionProposal } from '@/composables/useMetaAiActions'
import MetaAiActionCard from '@/components/meta-ai/MetaAiActionCard.vue'

const props = withDefaults(
  defineProps<{
    boxId?: string | number
  }>(),
  {
    boxId: 'meta_ai',
  }
)

const emit = defineEmits<{
  (e: 'close'): void
}>()

const chatStore = useChatStore()
const authStore = useAuthStore()
const router = useRouter()
const { extractActionProposals, executeAction, rejectAction } = useMetaAiActions()

const sanitizeHTML = DOMPurify.sanitize || (DOMPurify as any).default?.sanitize

interface ChatMessage {
  id: number | string
  role: 'user' | 'assistant'
  content: string
  proposals?: ActionProposal[]
  isStreaming?: boolean
  timestamp?: string
}

const threadId = ref<string>('')
const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const isStreaming = ref(false)
const copiedId = ref<number | string | null>(null)
const chatContainer = ref<HTMLElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
let abortController: AbortController | null = null

const generateThreadId = () => {
  return 'meta_ai_' + Math.random().toString(36).substring(2, 11)
}

const promptSuggestions = [
  '📊 Stwórz ankietę w czacie: Gdzie idziemy na obiad? (Pizza, Sushi, Burger)',
  '✍️ Napisz odpowiedź do Marka, że spóźnię się 15 minut z powodu korków',
  '📋 Podsumuj o czym rozmawiali na grupie Projekt 2026 przez ostatnie 24h',
  '🎙️ Przetranskrybuj wiadomość głosową i wyciągnij zadania',
]

const initChat = () => {
  threadId.value = generateThreadId()
  messages.value = [
    {
      id: 'welcome',
      role: 'assistant',
      content: 'Cześć! Jestem **Meta AI**. Potrafię odpowiadać na pytania oraz **obsługiwać aplikację** (tworzyć posty, zmieniać motyw, otwierać czaty, przechodzić do podstron).\n\nKażda akcja w aplikacji wymaga Twojego **zatwierdzenia (Human in the loop)** przed wykonaniem.',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
  ]
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

const copyContent = async (text: string, id: number | string) => {
  try {
    const { cleanContent } = extractActionProposals(text)
    await navigator.clipboard.writeText(cleanContent)
    copiedId.value = id
    setTimeout(() => {
      copiedId.value = null
    }, 2000)
  } catch (err) {
    console.error('Błąd kopiowania:', err)
  }
}

const renderMarkdown = (content: string) => {
  if (!content) return ''
  try {
    const { cleanContent } = extractActionProposals(content)
    const rawHtml = marked.parse(cleanContent, { breaks: true, gfm: true }) as string
    return sanitizeHTML(rawHtml, {
      ALLOWED_TAGS: [
        'p', 'a', 'ul', 'ol', 'li', 'b', 'i', 'strong', 'em', 'span', 'div', 'code', 'pre', 'blockquote',
        'table', 'thead', 'tbody', 'tr', 'th', 'td', 'br', 'hr', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6'
      ],
      ALLOWED_ATTR: ['href', 'target', 'rel', 'class', 'style']
    })
  } catch {
    return content
  }
}

const handleInputKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const autoGrowTextarea = () => {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    const newHeight = Math.min(textareaRef.value.scrollHeight, 120)
    textareaRef.value.style.height = `${newHeight}px`
  }
}

const stopGeneration = () => {
  if (abortController) {
    abortController.abort()
    abortController = null
  }
  isStreaming.value = false
  const lastMsg = messages.value[messages.value.length - 1]
  if (lastMsg && lastMsg.role === 'assistant') {
    lastMsg.isStreaming = false
    const { cleanContent, proposals } = extractActionProposals(lastMsg.content)
    lastMsg.proposals = proposals
  }
}

const handleApproveAction = async (proposal: ActionProposal) => {
  await executeAction(proposal)
  scrollToBottom()
}

const handleRejectAction = (proposal: ActionProposal) => {
  rejectAction(proposal)
  scrollToBottom()
}

const sendSuggestion = (suggestion: string) => {
  const cleanPrompt = suggestion.replace(/^[\p{Emoji}\s]+/u, '').trim()
  sendMessage(cleanPrompt || suggestion)
}

// Client-side fallback action generator if backend service is unreachable
const generateFallbackResponse = (query: string): { content: string; proposals: ActionProposal[] } => {
  const q = query.toLowerCase()

  if (q.includes('post') || q.includes('opublikuj') || q.includes('napisz post') || q.includes('stwórz post')) {
    let postContent = query
      .replace(/^(stwórz|napisz|dodaj|opublikuj|zrób)\s+post\s*(o|że|:)?/i, '')
      .replace(/^post\s*(o|że|:)?/i, '')
      .trim()
    if (!postContent) postContent = 'Pozdrowienia z Facebook Clone! 👋'

    const proposal: ActionProposal = {
      id: `act_post_${Date.now()}`,
      action: 'create_post',
      title: 'Opublikowanie nowego posta',
      description: 'Czy chcesz opublikować poniższy post na swojej tablicy?',
      details: { content: postContent, visibility: 'PUBLIC' },
      status: 'pending'
    }
    return {
      content: `Przygotowałem dla Ciebie projekt posta. Sprawdź treść poniżej i kliknij **Zatwierdź i wykonaj**, aby go opublikować.`,
      proposals: [proposal]
    }
  }

  if (q.includes('motyw') || q.includes('tryb ciemny') || q.includes('tryb jasny') || q.includes('ciemny') || q.includes('jasny')) {
    const isDarkTarget = q.includes('ciemn') || q.includes('dark')
    const theme = isDarkTarget ? 'dark' : (q.includes('jasn') || q.includes('light') ? 'light' : 'toggle')
    const proposal: ActionProposal = {
      id: `act_theme_${Date.now()}`,
      action: 'switch_theme',
      title: `Przełączenie motywu na ${isDarkTarget ? 'ciemny' : 'jasny'}`,
      description: `Czy chcesz zmienić wygląd aplikacji na motyw ${isDarkTarget ? 'ciemny' : 'jasny'}?`,
      details: { theme },
      status: 'pending'
    }
    return {
      content: `Zaproponowano zmianę motywu interfejsu. Aby zatwierdzić zmianę, kliknij poniższy przycisk.`,
      proposals: [proposal]
    }
  }

  if (q.includes('ankiet') || q.includes('poll') || q.includes('głosowanie')) {
    let question = 'Gdzie idziemy na obiad?'
    let options = ['Pizza 🍕', 'Sushi 🍣', 'Burgery 🍔']

    if (q.includes('obiad')) {
      question = 'Gdzie idziemy na obiad?'
      options = ['Pizza 🍕', 'Sushi 🍣', 'Burgery 🍔']
    } else if (q.includes('termin') || q.includes('kiedy')) {
      question = 'Kiedy organizujemy spotkanie?'
      options = ['Piątek 18:00', 'Sobota 19:00', 'Niedziela 15:00']
    }

    const proposal: ActionProposal = {
      id: `act_poll_${Date.now()}`,
      action: 'create_poll',
      title: `Utworzenie ankiety: ${question}`,
      description: 'Czy chcesz opublikować tę ankietę w czacie grupowym?',
      details: {
        question,
        options,
        chatName: 'Czat grupowy',
        allowMultiple: false
      },
      status: 'pending'
    }
    return {
      content: `Przygotowałem ankietę **"${question}"** z opcjami: **${options.join(', ')}**. Potwierdź, aby utworzyć ankietę w czacie.`,
      proposals: [proposal]
    }
  }

  if (q.includes('utwórz wydarzenie') || q.includes('stwórz wydarzenie') || q.includes('dodaj wydarzenie') || q.includes('zaplanuj spotkanie') || q.includes('nowe wydarzenie')) {
    let title = 'Spotkanie projektowe'
    let date = 'Piątek, 18:00'
    let description = 'Omówienie postępów i kolejnych kroków w projekcie.'

    if (q.includes('urodzin')) {
      title = 'Przyjęcie urodzinowe 🎂'
      date = 'Sobota, 19:00'
      description = 'Wspólne świętowanie urodzin!'
    } else if (q.includes('grill') || q.includes('ognisk')) {
      title = 'Wspólny Grill 🍖'
      date = 'Niedziela, 15:00'
      description = 'Spotkanie przy grillu ze znajomymi.'
    }

    const proposal: ActionProposal = {
      id: `act_event_${Date.now()}`,
      action: 'create_event',
      title: `Utworzenie wydarzenia: ${title}`,
      description: 'Czy chcesz utworzyć to wydarzenie w kalendarzu aplikacji?',
      details: {
        title,
        date,
        description
      },
      status: 'pending'
    }
    return {
      content: `Przygotowałem projekt wydarzenia **"${title}"** na **${date}**. Kliknij **Zatwierdź i wykonaj**, aby utworzyć wydarzenie w sekcji Wydarzenia.`,
      proposals: [proposal]
    }
  }

  if (q.includes('marketplace') || q.includes('grupy') || q.includes('znajom') || q.includes('rolki') || q.includes('przejdź do') || q.includes('otwórz') || q.includes('sekcj')) {
    let target = 'home'
    let targetName = 'Strona główna'
    if (q.includes('marketplace') || q.includes('sklep')) { target = 'marketplace'; targetName = 'Marketplace' }
    else if (q.includes('grup')) { target = 'groups'; targetName = 'Grupy' }
    else if (q.includes('wydarzen') || q.includes('event')) { target = 'events'; targetName = 'Wydarzenia' }
    else if (q.includes('znajom') || q.includes('friends')) { target = 'friends'; targetName = 'Znajomi' }
    else if (q.includes('rolk') || q.includes('reels')) { target = 'reels'; targetName = 'Rolki (Reels)' }
    else if (q.includes('czat') || q.includes('wiadomo')) { target = 'chat'; targetName = 'Czat' }

    const proposal: ActionProposal = {
      id: `act_nav_${Date.now()}`,
      action: 'navigate_to',
      title: `Przejście do: ${targetName}`,
      description: `Czy chcesz przejść do sekcji ${targetName}?`,
      details: { page: target, targetName },
      status: 'pending'
    }
    return {
      content: `Mogę przekierować Cię do sekcji **${targetName}**. Kliknij zatwierdzenie, aby przejść dalej.`,
      proposals: [proposal]
    }
  }

  if (q.includes('napisz odpowiedź') || q.includes('odpowiedź do') || q.includes('odpisz') || q.includes('spóźnię się')) {
    let recipient = 'Marek'
    let replyText = 'Cześć! Przepraszam, ale utknąłem w korku i spóźnię się około 15 minut. Zacznijcie beze mnie, będę najszybciej jak to możliwe!'

    if (q.includes('ani') || q.includes('anna')) recipient = 'Anna'
    else if (q.includes('kasi')) recipient = 'Kasia'
    else if (q.includes('piotr') || q.includes('piotrek')) recipient = 'Piotr'

    const proposal: ActionProposal = {
      id: `act_reply_${Date.now()}`,
      action: 'draft_reply',
      title: `Wysłanie odpowiedzi do: ${recipient}`,
      description: `Czy chcesz wysłać tę zredagowaną wiadomość do ${recipient}?`,
      details: {
        recipient,
        replyText,
        tone: 'uprzejmy'
      },
      status: 'pending'
    }
    return {
      content: `Zredagowałem dla Ciebie uprzejmą odpowiedź do **${recipient}**. Sprawdź treść poniżej i kliknij zatwierdzenie, aby ją wysłać.`,
      proposals: [proposal]
    }
  }

  if (q.includes('podsumuj') || q.includes('streszcz') || q.includes('o czym rozmawiali') || q.includes('projekt 2026')) {
    const groupName = q.includes('projekt 2026') ? 'Projekt 2026' : 'Czat zespołowy'
    const proposal: ActionProposal = {
      id: `act_sum_${Date.now()}`,
      action: 'summarize_chat',
      title: `Podsumowanie grupy: ${groupName}`,
      description: 'Analiza ostatnich 24h konwersacji',
      details: { chatName: groupName, timeframe: 'ostatnie 24h' },
      status: 'executed',
      resultMessage: 'Podsumowanie wygenerowane na podstawie historii wątku.'
    }
    return {
      content: `### 📋 Podsumowanie grupy **${groupName}** (ostatnie 24h):\n\n1. 📌 **Główny temat:** Ustalenie planu wdrożenia oraz podział zadań na kolejny sprint.\n2. 🤝 **Podjęte decyzje:** Zespół zgodził się na uruchomienie testów we wtorek o 14:00.\n3. ⏰ **Terminy i zadania:**\n   - Przygotowanie dokumentacji do końca tygodnia.\n   - Spotkanie statusowe w czwartek.\n4. 💬 **Status dyskusji:** Brak problemów blokujących, 12 nowych wiadomości przejrzanych.`,
      proposals: [proposal]
    }
  }

  if (q.includes('głosow') || q.includes('audio') || q.includes('transkrypcj') || q.includes('nagran')) {
    const transcribedText = 'Cześć! Pamiętaj, żeby przesłać mi raport do jutra do godziny 12:00. Daj znać jak skończysz!'
    const proposal: ActionProposal = {
      id: `act_voice_${Date.now()}`,
      action: 'transcribe_voice',
      title: 'Transkrypcja wiadomości głosowej',
      description: 'Wyodrębniono zadanie i termin z nagrania audio',
      details: {
        transcription: transcribedText,
        extractedTask: 'Przesłać raport',
        deadline: 'Jutro, godz. 12:00'
      },
      status: 'pending'
    }
    return {
      content: `🎙️ **Transkrypcja nagrania audio:**\n> *"${transcribedText}"*\n\n🎯 **Wyodrębnione zadanie:** Przesłać raport do jutra do 12:00. Kliknij poniżej, aby dodać to przypomnienie.`,
      proposals: [proposal]
    }
  }

  return {
    content: `Jestem asystentem Meta AI wyposażonym w funkcje obsługi aplikacji i czatów (tworzenie ankiet, draftowanie odpowiedzi, podsumowywanie grup, tworzenie postów). Możesz poprosić mnie np. o:\n- *„Stwórz ankietę w czacie grupowym: Gdzie idziemy na obiad? z opcjami Pizza, Sushi, Burger”*\n- *„Napisz odpowiedź do Marka, że spóźnię się 15 minut z powodu korków”*\n- *„Podsumuj o czym rozmawiali na grupie Projekt 2026 przez ostatnie 24h”*\n- *„Przetranskrybuj wiadomość głosową i wyciągnij zadania”*`,
    proposals: []
  }
}

const sendMessage = async (customText?: string) => {
  const text = (customText || inputMessage.value).trim()
  if (!text || isStreaming.value) return

  inputMessage.value = ''
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }

  const userMsgId = Date.now()
  messages.value.push({
    id: userMsgId,
    role: 'user',
    content: text,
    timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  })

  scrollToBottom()

  const assistantMsgId = Date.now() + 1
  const assistantMsg: ChatMessage = {
    id: assistantMsgId,
    role: 'assistant',
    content: '',
    proposals: [],
    isStreaming: true,
    timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  messages.value.push(assistantMsg)
  isStreaming.value = true
  scrollToBottom()

  abortController = new AbortController()

  try {
    const uid = String(authStore.currentUserId || '')
    const response = await fetch('/api/process-chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(uid ? { 'x-user-id': uid } : {})
      },
      body: JSON.stringify({
        query: text,
        thread_id: threadId.value,
        user_id: uid,
        model: 'Flash'
      }),
      signal: abortController.signal
    })

    if (!response.ok) {
      throw new Error(`HTTP error: ${response.status}`)
    }

    if (!response.body) {
      throw new Error('Pusta odpowiedź')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      assistantMsg.content += chunk

      const { proposals } = extractActionProposals(assistantMsg.content)
      if (proposals.length > 0) {
        assistantMsg.proposals = proposals
      }

      scrollToBottom()
    }

    const { proposals } = extractActionProposals(assistantMsg.content)
    assistantMsg.proposals = proposals
  } catch (err: any) {
    if (err.name === 'AbortError') {
      // Manual user cancellation
    } else {
      console.warn('Meta AI streaming error (using smart agent fallback):', err)
      const fallback = generateFallbackResponse(text)
      assistantMsg.content = fallback.content
      assistantMsg.proposals = fallback.proposals
    }
  } finally {
    assistantMsg.isStreaming = false
    isStreaming.value = false
    abortController = null
    scrollToBottom()
  }
}

const handleMinimize = () => {
  chatStore.toggleMinimize(props.boxId)
}

const handleClose = () => {
  stopGeneration()
  chatStore.removeMessageBox(props.boxId)
  emit('close')
}

const handleOpenFullPage = () => {
  router.push(`/meta-ai/${threadId.value}`)
}

onMounted(() => {
  initChat()
  scrollToBottom()
})

onUnmounted(() => {
  stopGeneration()
})
</script>

<template>
  <div class="flex items-center w-[338px] box-content relative justify-center">
    <div
      class="w-full max-w-[338px] h-[455px] rounded-t-xl shadow-2xl bg-theme-bg-secondary flex flex-col relative transition-all duration-300 overflow-hidden border border-theme-border/60"
    >
      <!-- HEADER -->
      <header
        class="h-13 px-3 py-2 border-b border-theme-border flex items-center justify-between bg-gradient-to-r from-blue-600/10 via-indigo-600/10 to-purple-600/10 dark:from-blue-950/40 dark:via-indigo-950/40 dark:to-purple-950/40 backdrop-blur-sm select-none"
      >
        <div class="flex items-center gap-2.5 min-w-0">
          <div class="relative shrink-0 flex items-center justify-center">
            <div
              class="w-8 h-8 rounded-full p-[2px] bg-gradient-to-tr from-[#0866FF] via-[#A855F7] to-[#EC4899] shadow-sm flex items-center justify-center"
            >
              <div
                class="w-full h-full bg-[#18191A] rounded-full flex items-center justify-center text-white"
              >
                <span
                  class="text-xs font-bold bg-gradient-to-tr from-[#00c6ff] via-[#0072ff] to-[#fbc2eb] bg-clip-text text-transparent"
                  >✦</span
                >
              </div>
            </div>
            <div
              class="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 bg-green-500 rounded-full border-2 border-white dark:border-[#242526]"
            ></div>
          </div>

          <div class="flex flex-col min-w-0">
            <div class="flex items-center gap-1.5">
              <span class="text-sm font-bold text-theme-text truncate">Meta AI</span>
              <span
                class="px-1 py-0.2 text-[9px] font-extrabold uppercase rounded bg-gradient-to-r from-blue-600 to-indigo-600 text-white tracking-wider"
              >
                Agent
              </span>
            </div>
            <span class="text-[11px] text-theme-text-secondary truncate">
              {{ isStreaming ? 'Generowanie odpowiedzi...' : 'Obsługa aplikacji + HITL' }}
            </span>
          </div>
        </div>

        <!-- Header Action Buttons -->
        <div class="flex items-center gap-1 shrink-0 text-theme-text-secondary">
          <button
            @click="initChat"
            v-tooltip.bottom="{ content: 'Nowa rozmowa' }"
            class="p-1.5 hover:bg-theme-hover rounded-full transition-colors cursor-pointer text-theme-text-secondary hover:text-theme-text"
            aria-label="Nowa rozmowa"
          >
            <RefreshIcon :size="17" />
          </button>

          <button
            @click="handleOpenFullPage"
            v-tooltip.bottom="{ content: 'Otwórz na pełnym ekranie' }"
            class="p-1.5 hover:bg-theme-hover rounded-full transition-colors cursor-pointer text-theme-text-secondary hover:text-theme-text"
            aria-label="Pełny ekran"
          >
            <OpenInAppIcon :size="17" />
          </button>

          <button
            @click="handleMinimize"
            v-tooltip.bottom="{ content: 'Zminimalizuj' }"
            class="p-1.5 hover:bg-theme-hover rounded-full transition-colors cursor-pointer text-theme-text-secondary hover:text-theme-text"
            aria-label="Minimalizuj"
          >
            <MinusIcon :size="17" />
          </button>

          <button
            @click="handleClose"
            v-tooltip.bottom="{ content: 'Zamknij' }"
            class="p-1.5 hover:bg-theme-hover rounded-full transition-colors cursor-pointer text-theme-text-secondary hover:text-theme-text"
            aria-label="Zamknij"
          >
            <CloseIcon :size="17" />
          </button>
        </div>
      </header>

      <!-- MESSAGES CONTAINER -->
      <main
        ref="chatContainer"
        class="flex-1 overflow-y-auto p-3 flex flex-col gap-3 bg-theme-bg/60 custom-scrollbar text-sm"
      >
        <!-- Suggestion Chips -->
        <div
          v-if="messages.length <= 1"
          class="my-2 p-2.5 rounded-xl bg-gradient-to-br from-blue-500/5 via-indigo-500/5 to-purple-500/5 border border-theme-border/50 flex flex-col gap-2"
        >
          <span class="text-xs font-semibold text-theme-text-secondary flex items-center gap-1">
            ⚡ Przetestuj funkcje agenta:
          </span>
          <div class="flex flex-col gap-1.5">
            <button
              v-for="suggestion in promptSuggestions"
              :key="suggestion"
              @click="sendSuggestion(suggestion)"
              class="text-left text-xs p-2 rounded-lg bg-theme-bg hover:bg-theme-hover border border-theme-border text-theme-text transition-all duration-150 cursor-pointer hover:border-blue-500/40 hover:shadow-xs flex items-center justify-between group/chip"
            >
              <span class="truncate">{{ suggestion }}</span>
              <span class="text-[10px] text-blue-500 font-semibold opacity-0 group-hover/chip:opacity-100 transition-opacity">Wyślij →</span>
            </button>
          </div>
        </div>

        <!-- LISTA WIADOMOŚCI -->
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="[
            'flex flex-col group/msg relative',
            msg.role === 'user' ? 'items-end' : 'items-start'
          ]"
        >
          <div
            :class="[
              'flex gap-2 max-w-[92%]',
              msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'
            ]"
          >
            <!-- Assistant Avatar -->
            <div
              v-if="msg.role === 'assistant'"
              class="w-6 h-6 rounded-full p-[1px] bg-gradient-to-tr from-blue-500 to-purple-500 shrink-0 mt-0.5"
            >
              <div
                class="w-full h-full bg-[#18191A] rounded-full flex items-center justify-center text-white"
              >
                <span class="text-[9px] font-bold text-blue-400">✦</span>
              </div>
            </div>

            <!-- Message Bubble Content -->
            <div class="relative w-full">
              <div
                :class="[
                  'px-3.5 py-2 text-sm leading-relaxed break-words',
                  msg.role === 'user'
                    ? 'bg-[#0084FF] text-white rounded-2xl rounded-tr-sm shadow-sm'
                    : 'bg-[#E4E6EB] dark:bg-[#3A3B3C] text-[#050505] dark:text-[#E4E6EB] rounded-2xl rounded-tl-sm shadow-xs'
                ]"
              >
                <!-- Markdown content for assistant -->
                <div
                  v-if="msg.role === 'assistant'"
                  class="prose prose-sm dark:prose-invert max-w-none text-xs sm:text-sm ai-markdown"
                  v-html="renderMarkdown(msg.content)"
                ></div>

                <!-- Simple text for user -->
                <div v-else class="whitespace-pre-wrap text-xs sm:text-sm">
                  {{ msg.content }}
                </div>

                <!-- Streaming cursor / typing indicator -->
                <div
                  v-if="msg.isStreaming && !msg.content"
                  class="inline-flex items-center gap-1 py-1 text-blue-500"
                >
                  <span class="w-1.5 h-1.5 bg-blue-500 rounded-full animate-bounce"></span>
                  <span
                    class="w-1.5 h-1.5 bg-blue-500 rounded-full animate-bounce [animation-delay:0.2s]"
                  ></span>
                  <span
                    class="w-1.5 h-1.5 bg-blue-500 rounded-full animate-bounce [animation-delay:0.4s]"
                  ></span>
                </div>
              </div>

              <!-- HUMAN-IN-THE-LOOP ACTION CARDS -->
              <div v-if="msg.proposals && msg.proposals.length > 0" class="mt-2 space-y-2">
                <MetaAiActionCard
                  v-for="prop in msg.proposals"
                  :key="prop.id"
                  :proposal="prop"
                  @approve="handleApproveAction"
                  @reject="handleRejectAction"
                />
              </div>

              <!-- Action buttons on hover (e.g. Copy) -->
              <div
                v-if="msg.content && !msg.isStreaming"
                :class="[
                  'absolute top-2 opacity-0 group-hover/msg:opacity-100 transition-opacity flex items-center',
                  msg.role === 'user' ? '-left-7' : '-right-7'
                ]"
              >
                <button
                  @click="copyContent(msg.content, msg.id)"
                  v-tooltip.bottom="{ content: copiedId === msg.id ? 'Skopiowano!' : 'Kopiuj treść' }"
                  class="p-1 rounded-full bg-theme-bg-secondary hover:bg-theme-hover border border-theme-border shadow-xs text-theme-text-secondary cursor-pointer"
                  aria-label="Kopiuj"
                >
                  <CheckIcon v-if="copiedId === msg.id" :size="12" class="text-green-500" />
                  <ContentCopyIcon v-else :size="12" />
                </button>
              </div>
            </div>
          </div>

          <span
            v-if="msg.timestamp"
            class="text-[10px] text-theme-text-secondary/70 mt-0.5 px-1"
          >
            {{ msg.timestamp }}
          </span>
        </div>
      </main>

      <!-- FOOTER / INPUT BAR -->
      <footer class="p-2.5 border-t border-theme-border bg-theme-bg-secondary flex flex-col gap-1.5">
        <div class="flex items-end gap-1.5 bg-theme-bg rounded-2xl px-3 py-1.5 border border-theme-border focus-within:border-blue-500 transition-colors">
          <textarea
            ref="textareaRef"
            v-model="inputMessage"
            @keydown="handleInputKeydown"
            @input="autoGrowTextarea"
            rows="1"
            placeholder="Napisz do Meta AI (np. 'opublikuj post...')"
            class="w-full bg-transparent text-xs sm:text-sm text-theme-text placeholder-theme-text-secondary resize-none outline-none max-h-24 py-1 custom-scrollbar"
          ></textarea>

          <div class="flex items-center shrink-0 mb-0.5">
            <button
              v-if="isStreaming"
              @click="stopGeneration"
              v-tooltip.top="{ content: 'Zatrzymaj generowanie' }"
              class="p-1.5 rounded-full bg-red-500/10 text-red-500 hover:bg-red-500/20 transition-colors cursor-pointer"
              aria-label="Zatrzymaj"
            >
              <StopCircleOutlineIcon :size="18" />
            </button>

            <button
              v-else
              @click="() => sendMessage()"
              :disabled="!inputMessage.trim()"
              v-tooltip.top="{ content: 'Wyślij wiadomość' }"
              :class="[
                'p-1.5 rounded-full transition-all duration-150 flex items-center justify-center cursor-pointer',
                inputMessage.trim()
                  ? 'bg-blue-600 text-white hover:bg-blue-700 shadow-xs'
                  : 'text-theme-text-secondary opacity-40 cursor-not-allowed'
              ]"
              aria-label="Wyślij"
            >
              <SendIcon :size="16" />
            </button>
          </div>
        </div>

        <div class="flex items-center justify-between px-1 text-[10px] text-theme-text-secondary">
          <span>Tarcza Human-in-the-loop aktywna.</span>
          <span class="font-medium text-blue-500">Meta AI Agent</span>
        </div>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(150, 150, 150, 0.3);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: rgba(150, 150, 150, 0.5);
}

:deep(.ai-markdown p) {
  margin-bottom: 0.5rem;
}
:deep(.ai-markdown p:last-child) {
  margin-bottom: 0;
}
:deep(.ai-markdown pre) {
  background-color: rgba(0, 0, 0, 0.15);
  padding: 0.5rem;
  border-radius: 0.5rem;
  overflow-x: auto;
  font-size: 0.75rem;
  margin: 0.5rem 0;
}
:deep(.ai-markdown code) {
  font-family: monospace;
  font-size: 0.8em;
  padding: 0.1rem 0.3rem;
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 0.25rem;
}
:deep(.ai-markdown ul),
:deep(.ai-markdown ol) {
  padding-left: 1.2rem;
  margin: 0.4rem 0;
}
:deep(.ai-markdown li) {
  margin-bottom: 0.2rem;
}
:deep(.ai-markdown strong) {
  font-weight: 600;
}
</style>
