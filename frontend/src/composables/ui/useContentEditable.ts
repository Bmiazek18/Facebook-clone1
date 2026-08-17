import { ref, watch, nextTick, computed } from 'vue'
import type { Ref } from 'vue'
import { useUserSearch } from '@/composables/shared/useUserSearch'
import { getAllUsers, type User } from '@/utils/users'

export function useContentEditable(
  contentEditableDiv: Ref<HTMLDivElement | null>,
  postContent: Ref<string>,
) {
  const isLocalUpdate = ref(false)

  const searchTerm = ref<string | null>(null)
  const { matchingUsers } = useUserSearch(searchTerm)
  const showUserDropdown = computed(
    () => matchingUsers.value.length > 0 && searchTerm.value !== null,
  )

  // --- KLUCZOWA LOGIKA PARSOWANIA TREŚCI ---
  function onContentInput() {
    if (!contentEditableDiv.value) return
    isLocalUpdate.value = true

    let newContent = ''
    const nodes = contentEditableDiv.value.childNodes

    nodes.forEach((node, index) => {
      if (node.nodeType === Node.TEXT_NODE) {
        newContent += node.textContent
      } else if (node.nodeType === Node.ELEMENT_NODE) {
        const el = node as HTMLElement

        if (el.dataset.userId) {
          // Wykrywanie naszych tagów użytkowników
          newContent += `[@${el.dataset.userId}]`
        } else if (el.tagName === 'BR') {
          // Standardowy łamacz linii
          newContent += '\n'
        } else if (el.tagName === 'DIV' || el.tagName === 'P') {
          // Przeglądarki często pakują nowe linie w divy.
          // Jeśli to nie jest pierwszy element, dodaj enter przed treścią diva.
          if (index > 0) newContent += '\n'
          newContent += el.innerText.replace(/\n$/, '')
        } else {
          newContent += el.innerText
        }
      }
    })

    postContent.value = newContent
    handleUserTagging()

    // Krótkie opóźnienie, aby uniknąć pętli watcherów
    setTimeout(() => {
      isLocalUpdate.value = false
    }, 10)
  }

  function renderContentEditable() {
    if (!contentEditableDiv.value) return

    const text = postContent.value || ''

    // Sanityzacja HTML i zamiana \n na <br>
    let htmlContent = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\n/g, '<br>')

    const allUsers = getAllUsers()

    // Renderowanie tagów użytkowników [@id] -> Span
    htmlContent = htmlContent.replace(/\[@([a-zA-Z0-9-]+)\]/g, (match, userId) => {
      const user = allUsers.find((u) => String(u.id) === userId)
      if (user) {
        return `<span contenteditable="false" class="bg-blue-100 text-blue-600 font-semibold px-1 rounded mx-0.5" data-user-id="${user.id}">@${user.name}</span>`
      }
      return match
    })

    // Renderowanie linków
    htmlContent = htmlContent.replace(/(https?:\/\/[^\s<]+)/g, (match) => {
      return `<a href="${match}" target="_blank" rel="noopener noreferrer" contenteditable="false" class="text-blue-600 hover:underline cursor-pointer font-medium">${match}</a>`
    })

    // Aktualizujemy DOM tylko jeśli treść się różni
    if (contentEditableDiv.value.innerHTML !== htmlContent) {
      contentEditableDiv.value.innerHTML = htmlContent
      // Dodajemy niewidoczny znak na końcu, jeśli kończy się na <br>, aby kursor nie utknął
      if (htmlContent.endsWith('<br>')) {
        contentEditableDiv.value.innerHTML += '&#8203;'
      }
    }
  }

  function moveCursorToEnd() {
    nextTick(() => {
      if (contentEditableDiv.value) {
        contentEditableDiv.value.focus()
        const selection = window.getSelection()
        const range = document.createRange()
        range.selectNodeContents(contentEditableDiv.value)
        range.collapse(false)
        selection?.removeAllRanges()
        selection?.addRange(range)
      }
    })
  }

  function handleUserTagging() {
    const selection = window.getSelection()
    if (selection && selection.rangeCount > 0) {
      const range = selection.getRangeAt(0)
      if (range.startContainer.nodeType === Node.TEXT_NODE) {
        const textNode = range.startContainer
        const textContent = textNode.textContent || ''
        const textBeforeCaret = textContent.substring(0, range.startOffset)
        const match = textBeforeCaret.match(/@([^\s]*)$/)

        if (match) {
          searchTerm.value = match[1]
        } else {
          searchTerm.value = null
        }
      } else {
        searchTerm.value = null
      }
    }
  }

  function selectUser(user: User) {
    if (!contentEditableDiv.value) return

    // Zamiana @term na [@id] w postContent
    const currentText = postContent.value
    const newContent = currentText.replace(/@([^\s]*)$/, `[@${user.id}] `)
    postContent.value = newContent
    searchTerm.value = null

    // Po wyborze usera renderujemy ponownie, by pokazać ładny niebieski badge
    nextTick(() => {
      renderContentEditable()
      moveCursorToEnd()
    })
  }

  function addEmoji(emoji: any) {
    postContent.value += emoji.native
    nextTick(() => {
      renderContentEditable()
      moveCursorToEnd()
    })
  }

  return {
    onContentInput,
    matchingUsers,
    showUserDropdown,
    selectUser,
    addEmoji,
    renderContentEditable,
    moveCursorToEnd,
  }
}
