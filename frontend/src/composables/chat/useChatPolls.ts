import type { Ref } from 'vue'
import type { Chat } from '@/types/Chat'
import type { ChatMessage } from '@/types/Message'

export function useChatPolls(
  messages: Ref<ChatMessage[]>,
  chats: Ref<Chat[]>,
  currentUserUuid: Ref<string>,
  chatStorage: any,
  getSymmetricConversationId: (id: string | number) => string,
  publishMqtt: (topic: string, payload: any, options?: any) => void,
) {
  async function voteChatPoll(
    chatId: string | number,
    messageId: string | number,
    optionIds: (string | number)[],
  ) {
    const cleanSenderId = String(currentUserUuid.value).replace(/^user_/, '')
    const targetMsg = messages.value.find((m) => String(m.id) === String(messageId))
    const cleanOptionIds = optionIds.map(String)

    if (targetMsg && targetMsg.pollData) {
      targetMsg.pollData.options.forEach((opt: any) => {
        if (!opt.voterIds) opt.voterIds = []
        const isSelected = cleanOptionIds.includes(String(opt.id))
        const wasVoted = opt.voterIds.includes(cleanSenderId)
        if (isSelected && !wasVoted) {
          opt.voterIds.push(cleanSenderId)
        } else if (!isSelected && wasVoted) {
          opt.voterIds = opt.voterIds.filter((id: string) => id !== cleanSenderId)
        }
        opt.votes = opt.voterIds.length
        if (voterIdIsMe(cleanSenderId)) {
          opt.votedByMe = isSelected
        }
      })
      await chatStorage.storeMessage(targetMsg).catch(console.error)
    }

    // Broadcast vote to all participants via MQTT
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    const participantIds: string[] = []
    if (chat && chat.groupMembers) {
      chat.groupMembers.forEach((m: any) => {
        const pId = String(m.userId || m.id).replace(/^user_/, '')
        if (pId) participantIds.push(pId)
      })
    } else {
      const cleanChatId = String(chatId).replace(/^user_/, '')
      participantIds.push(cleanChatId)
    }
    if (!participantIds.includes(cleanSenderId)) {
      participantIds.push(cleanSenderId)
    }

    const convId = getSymmetricConversationId(chatId)
    participantIds.forEach((pId) => {
      publishMqtt(
        `chat/messages/user/${pId}`,
        {
          type: 'poll_voted',
          conversationId: convId,
          messageId: String(messageId),
          optionIds: cleanOptionIds,
          voterId: cleanSenderId,
        },
        { qos: 1 },
      )
    })
  }

  function voterIdIsMe(cleanSenderId: string) {
    return cleanSenderId === String(currentUserUuid.value).replace(/^user_/, '')
  }

  async function addChatPollOption(
    chatId: string | number,
    messageId: string | number,
    option: any,
  ) {
    const cleanSenderId = String(currentUserUuid.value).replace(/^user_/, '')
    const targetMsg = messages.value.find((m) => String(m.id) === String(messageId))

    if (targetMsg && targetMsg.pollData) {
      const exists = targetMsg.pollData.options.some(
        (o: any) => String(o.id) === String(option.id) || o.text === option.text,
      )
      if (!exists) {
        targetMsg.pollData.options.push(option)
        await chatStorage.storeMessage(targetMsg).catch(console.error)
      }
    }

    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    const participantIds: string[] = []
    if (chat && chat.groupMembers) {
      chat.groupMembers.forEach((m: any) => {
        const pId = String(m.userId || m.id).replace(/^user_/, '')
        if (pId) participantIds.push(pId)
      })
    } else {
      const cleanChatId = String(chatId).replace(/^user_/, '')
      participantIds.push(cleanChatId)
    }
    if (!participantIds.includes(cleanSenderId)) {
      participantIds.push(cleanSenderId)
    }

    const convId = getSymmetricConversationId(chatId)
    participantIds.forEach((pId) => {
      publishMqtt(
        `chat/messages/user/${pId}`,
        {
          type: 'poll_option_added',
          conversationId: convId,
          messageId: String(messageId),
          option,
        },
        { qos: 1 },
      )
    })
  }

  return {
    voteChatPoll,
    addChatPollOption,
  }
}
