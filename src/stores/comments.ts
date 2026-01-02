import { defineStore } from 'pinia'
import { ref } from 'vue'

interface ReplyingToUser {
  id: number
  name: string
}

export const useCommentsStore = defineStore('comments', () => {
  const replyingToUser = ref<ReplyingToUser | null>(null)
  const activeReplyInput = ref<number | null>(null) // To which comment we are replying

  function setReplyingTo(user: ReplyingToUser, commentId: number) {
    replyingToUser.value = user
    activeReplyInput.value = commentId
  }

  function clearReplyingTo() {
    replyingToUser.value = null
    activeReplyInput.value = null
  }

  return { replyingToUser, activeReplyInput, setReplyingTo, clearReplyingTo }
})
