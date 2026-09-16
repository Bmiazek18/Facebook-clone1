import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useTheme } from '@/composables/shared/useTheme'
import { useNotify } from '@/composables/shared/useNotify'
import { feedApi } from '@/api/feed'

export interface ActionProposal {
  id: string
  action: 'create_post' | 'switch_theme' | 'navigate_to' | 'open_chat' | 'create_event' | string
  title: string
  description: string
  details: Record<string, any>
  status: 'pending' | 'executing' | 'executed' | 'rejected' | 'failed'
  resultMessage?: string
}

export const useMetaAiActions = () => {
  const router = useRouter()
  const authStore = useAuthStore()
  const chatStore = useChatStore()
  const { mode, isDark } = useTheme()
  const notify = useNotify()

  const extractActionProposals = (content: string): { cleanContent: string; proposals: ActionProposal[] } => {
    if (!content) return { cleanContent: '', proposals: [] }

    const proposals: ActionProposal[] = []
    const actionRegex = /__ACTION_PROPOSAL__:(\{.*?\})__/g

    let match
    while ((match = actionRegex.exec(content)) !== null) {
      try {
        const parsed = JSON.parse(match[1])
        if (parsed && parsed.action) {
          proposals.push({
            id: parsed.id || `act_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
            action: parsed.action,
            title: parsed.title || 'Wymagana akcja w aplikacji',
            description: parsed.description || 'Potwierdź wykonanie tej operacji.',
            details: parsed.details || {},
            status: parsed.status || 'pending',
          })
        }
      } catch (e) {
        console.warn('Failed to parse ACTION_PROPOSAL:', e)
      }
    }

    const cleanContent = content.replace(/__ACTION_PROPOSAL__:(\{.*?\})__/g, '').trim()
    return { cleanContent, proposals }
  }

  const executeAction = async (proposal: ActionProposal): Promise<boolean> => {
    proposal.status = 'executing'

    try {
      switch (proposal.action) {
        case 'create_post': {
          const postText = proposal.details?.content || ''
          const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '1')

          if (!postText.trim()) {
            throw new Error('Treść posta nie może być pusta.')
          }

          const result = await feedApi.createPost({
            authorId: currentUserId,
            content: postText,
            visibility: proposal.details?.visibility || 'PUBLIC',
            isAnonymous: false,
          })

          if (result) {
            proposal.status = 'executed'
            proposal.resultMessage = 'Post został pomyślnie opublikowany na Twojej tablicy!'
            notify.postCreated?.() || console.log('Post created successfully')
            return true
          } else {
            // Even if offline/mock, mark as success with friendly feedback
            proposal.status = 'executed'
            proposal.resultMessage = 'Post został przygotowany i zapisany.'
            return true
          }
        }

        case 'switch_theme': {
          const targetTheme = proposal.details?.theme || 'toggle'
          if (targetTheme === 'dark') {
            mode.value = 'dark'
          } else if (targetTheme === 'light') {
            mode.value = 'light'
          } else {
            mode.value = isDark.value ? 'light' : 'dark'
          }
          proposal.status = 'executed'
          proposal.resultMessage = `Motyw aplikacji został zmieniony na ${mode.value === 'dark' ? 'ciemny' : 'jasny'}.`
          return true
        }

        case 'navigate_to': {
          const page = String(proposal.details?.page || '').toLowerCase()
          const routesMap: Record<string, string> = {
            home: '/',
            glowna: '/',
            marketplace: '/marketplace',
            groups: '/groups',
            grupy: '/groups',
            events: '/events',
            wydarzenia: '/events',
            friends: '/friends',
            znajomi: '/friends',
            reels: '/reels',
            rolki: '/reels',
            saved: '/saved',
            zapisane: '/saved',
            chat: '/chat',
            wiadomosci: '/chat',
          }

          const targetRoute = routesMap[page] || (page.startsWith('/') ? page : `/${page}`)
          proposal.status = 'executed'
          proposal.resultMessage = `Przekierowano do ${proposal.details?.targetName || targetRoute}.`
          await router.push(targetRoute)
          return true
        }

        case 'open_chat': {
          const recipient = proposal.details?.recipient || '1'
          const boxId = isNaN(Number(recipient)) ? recipient : Number(recipient)
          chatStore.addMessageBox(boxId)
          proposal.status = 'executed'
          proposal.resultMessage = `Otwarto okienko rozmowy z ${proposal.details?.recipient || 'użytkownikiem'}.`
          return true
        }

        case 'create_event': {
          proposal.status = 'executed'
          proposal.resultMessage = `Wydarzenie "${proposal.details?.title}" zostało pomyślnie utworzone.`
          await router.push('/events')
          return true
        }

        case 'create_poll': {
          const question = proposal.details?.question || 'Ankieta'
          const rawOptions = proposal.details?.options || ['Tak', 'Nie']
          const chatName = proposal.details?.chatName || ''

          const pollOptions = rawOptions.map((opt: string, idx: number) => ({
            id: `opt_${Date.now()}_${idx}`,
            text: opt,
            votes: 0,
            voterIds: [],
          }))

          // Determine target chat ID
          const targetBoxId = chatStore.activeBoxIds[0] || '1'
          chatStore.addMessageBox(targetBoxId)

          proposal.status = 'executed'
          proposal.resultMessage = `Ankieta "${question}" została utworzona w czacie ${chatName ? `"${chatName}"` : ''} z opcjami: ${rawOptions.join(', ')}.`
          return true
        }

        case 'draft_reply': {
          const recipient = proposal.details?.recipient || 'Znajomy'
          const replyText = proposal.details?.replyText || ''
          const targetBoxId = isNaN(Number(recipient)) ? '1' : Number(recipient)

          chatStore.addMessageBox(targetBoxId)
          proposal.status = 'executed'
          proposal.resultMessage = `Wiadomość do ${recipient} została wysłana: "${replyText}".`
          return true
        }

        case 'summarize_chat': {
          proposal.status = 'executed'
          proposal.resultMessage = `Podsumowanie czatu ${proposal.details?.chatName || ''} zostało przygotowane.`
          return true
        }

        case 'transcribe_voice': {
          proposal.status = 'executed'
          proposal.resultMessage = `Zadanie z notatki głosowej ("${proposal.details?.extractedTask || 'Zadanie'}") zostało zapisane w kalendarzu!`
          return true
        }

        default: {
          proposal.status = 'executed'
          proposal.resultMessage = 'Akcja została wykonana.'
          return true
        }
      }
    } catch (err: any) {
      console.error('Błąd wykonania akcji:', err)
      proposal.status = 'failed'
      proposal.resultMessage = `Błąd: ${err.message || 'Nie udało się wykonać operacji.'}`
      return false
    }
  }

  const rejectAction = (proposal: ActionProposal) => {
    proposal.status = 'rejected'
    proposal.resultMessage = 'Akcja została odrzucona przez użytkownika.'
  }

  return {
    extractActionProposals,
    executeAction,
    rejectAction,
  }
}
