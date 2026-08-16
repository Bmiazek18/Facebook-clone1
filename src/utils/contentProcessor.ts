export interface ProcessedContent {
  type: 'mention' | 'hashtag' | 'text' | 'link'
  value: string
  userId?: string
  hashtag?: string
  url?: string
}

export function processContent(content: string | undefined | null): ProcessedContent[] {
  if (!content) return []

  // Regex for user mentions [@userId], hashtags #hashtag, and URLs
  const combinedRegex = /(\[@[a-zA-Z0-9-]+\]|#[\w\u00C0-\u017F]+|https?:\/\/[^\s]+)/g

  return content
    .split(combinedRegex)
    .filter((part) => part) // Filter out empty strings
    .map((part): ProcessedContent => {
      // User Mention [@userId]
      if (part.startsWith('[@') && part.endsWith(']')) {
        const userId = part.slice(2, -1)
        return {
          type: 'mention',
          value: part,
          userId: userId,
        }
      }

      // Hashtag #word
      if (part.startsWith('#')) {
        return {
          type: 'hashtag',
          value: part,
          hashtag: part.substring(1),
        }
      }

      // URL
      if (part.startsWith('https')) {
        return {
          type: 'link',
          value: part,
          url: part,
        }
      }

      // Plain text
      return {
        type: 'text',
        value: part,
      }
    })
}

const themeTitlesMap: Record<string, string> = {
  winter: 'Zimowe Królestwo',
  dune: 'Piaszczysta Planeta',
  cyberpunk: 'Neon City 2077',
  matrix: 'Kod Źródłowy',
  space: 'Gwiezdna Odyseja',
  magic: 'Szkoła Magii',
  candy: 'Różowy Świat',
  ocean: 'Głębiny Oceanu',
  jungle: 'Dzika Dżungla',
  gotham: 'Mroczny Rycerz',
  retro: 'Retro Lata 80.',
  gold: 'Wielki Gatsby',
  classic: 'Klasyczny niebieski'
}

export function formatSystemActionText(text: string): string {
  if (!text || !text.startsWith('SYSTEM_ACTION:')) return text

  const parts = text.split(':')
  const actionType = parts[1]
  const payload = parts.slice(2).join(':')

  if (actionType === 'CHANGE_THEME') {
    const title = themeTitlesMap[payload] || payload
    return `Zmieniono motyw czatu na ${title}`
  } else if (actionType === 'CHANGE_E') {
    return `Ustawiono szybką reakcję jako ${payload}`
  } else if (actionType === 'CHANGE_NICKNAME') {
    return payload ? `Zmieniono pseudonim na ${payload}` : 'Usunięto pseudonim'
  } else if (actionType === 'ADD_MEMBER') {
    return `Dodano nowego uczestnika do grupy`
  } else if (actionType === 'BACKUP_SENDER_KEY') {
    return `Udostępniono zapasowy klucz nadawcy`
  } else if (actionType === 'LEAVE_MEMBER') {
    return `Uczestnik opuścił grupę`
  } else if (actionType === 'call_ended') {
    const sec = parseInt(payload, 10)
    if (!isNaN(sec) && sec > 0) {
      const m = Math.floor(sec / 60)
      const s = Math.floor(sec % 60)
      const durStr = m > 0 && s > 0 ? `${m} min ${s} sek` : m > 0 ? `${m} min` : `${s} sek`
      return `Połączenie zakończone (${durStr})`
    }
    return 'Połączenie zakończone'
  } else if (actionType === 'call_started') {
    return 'Trwa rozmowa grupowa'
  } else if (actionType === 'call_rejected') {
    return 'Nieodebrane połączenie'
  }

  return text
}
