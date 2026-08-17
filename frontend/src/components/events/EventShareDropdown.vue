<template>
  <div class="relative inline-block">
    <VDropdown :distance="0" placement="bottom">
      <button
        class="flex items-center justify-center gap-1.5 bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505] border-none rounded-md px-3 h-9 text-[15px] font-semibold cursor-pointer transition-colors dark:bg-theme-bg-subtle dark:text-theme-text dark:hover:bg-theme-hover"
      >
        <ShareVariantIcon class="w-4 h-4 flex items-center justify-center" />
        {{ notTitle ? 'Udostępnij' : '' }}
      </button>

      <template #popper="{ hide }">
        <ul
          class="flex flex-col p-2 min-w-[320px] m-0 list-none bg-white dark:bg-theme-bg-secondary rounded-xl shadow-lg border border-white/70 dark:border-theme-border"
        >
          <li v-for="option in shareOptions" :key="option.id">
            <button
              @click="
                option.action(event);
                hide();
              "
              class="flex items-center gap-3.5 p-2 hover:bg-[#f2f2f2] dark:hover:bg-theme-hover rounded-lg text-left transition-colors w-full cursor-pointer border-none bg-transparent"
            >
              <component :is="option.icon" class="w-5 h-5 text-[#050505] dark:text-theme-text" />
              <span class="text-[15px] text-[#050505] dark:text-theme-text">{{ option.label }}</span>
            </button>
          </li>
        </ul>
      </template>
    </VDropdown>

    <!-- Share Post Modal -->
    <BaseModal
      v-if="showCreatePostModal"
      @close="closeCreatePostModal"
      :title="'Udostępnij wydarzenie'"
    >
      <CreatePost
        :shared-event-id="event.id"
        :target-id="modalTargetId"
        :target-type="modalTargetType"
        @close="closeCreatePostModal"
        @publish="handlePublishPost"
      />
    </BaseModal>

    <!-- Send in Messenger Modal -->
    <BaseModal
      v-if="showMessengerModal"
      @close="showMessengerModal = false"
      :title="'Wyślij w Messengerze'"
    >
      <ShareAsMessageModal @close="showMessengerModal = false" />
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef } from 'vue'
import { Dropdown as VDropdown } from 'floating-vue'
import 'floating-vue/dist/style.css'

import ShareVariantIcon from 'vue-material-design-icons/ShareVariant.vue'
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue'
import SquareEditOutlineIcon from 'vue-material-design-icons/SquareEditOutline.vue'
import FacebookMessengerIcon from 'vue-material-design-icons/FacebookMessenger.vue'
import AccountGroupOutlineIcon from 'vue-material-design-icons/AccountGroupOutline.vue'
import AccountMultipleOutlineIcon from 'vue-material-design-icons/AccountMultipleOutline.vue'

import BaseModal from '@/components/common/BaseModal.vue'
import CreatePost from '@/components/create/createPost/CreateModal.vue'
import ShareAsMessageModal from '@/components/feed/ShareAsMessageModal.vue'
import { useNotify } from '@/composables/shared/useNotify'
import { useGroupsStore } from '@/stores/groups'

const props = defineProps({
  event: {
    type: Object,
    required: true,
  },
  notTitle: {
    type: Boolean,
    default: false,
  }
})

const notify = useNotify()
const groupsStore = useGroupsStore()

const showCreatePostModal = ref(false)
const showMessengerModal = ref(false)
const modalTargetType = ref<'User' | 'Group' | 'Event' | 'event' | null>(null)
const modalTargetId = ref<string | null>(null)

const closeCreatePostModal = () => {
  showCreatePostModal.value = false
  modalTargetType.value = null
  modalTargetId.value = null
}

const handlePublishPost = (content: string) => {
  notify.success('Wydarzenie zostało udostępnione!')
  closeCreatePostModal()
}

// Akcje z menu "Udostępnij"
const copyLink = (event: any) => {
  if (typeof window !== 'undefined') {
    const url = `${window.location.origin}/event/${event.id}`
    navigator.clipboard.writeText(url)
      .then(() => {
        notify.success('Skopiowano link do schowka!')
      })
      .catch((err) => {
        console.error('Błąd kopiowania linku:', err)
        notify.error('Nie udało się skopiować linku.')
      })
  }
}

const shareToFeed = (event: any) => {
  modalTargetType.value = 'event' // lowercase 'event' to share as event card in feed
  modalTargetId.value = event.id
  showCreatePostModal.value = true
}

const sendInMessenger = (event: any) => {
  showMessengerModal.value = true
}

const shareInGroup = (event: any) => {
  const defaultGroup = groupsStore.groups?.[0]
  modalTargetType.value = 'Group'
  modalTargetId.value = defaultGroup ? defaultGroup.id : '1'
  showCreatePostModal.value = true
}

const shareToFriendProfile = (event: any) => {
  modalTargetType.value = 'User'
  modalTargetId.value = '2' // default user/friend ID
  showCreatePostModal.value = true
}

const shareOptions = shallowRef([
  { id: 'copy_link', label: 'Kopiuj link do wydarzenia', icon: LinkVariantIcon, action: copyLink },
  {
    id: 'share_feed',
    label: 'Udostępnij w Aktualnościach',
    icon: SquareEditOutlineIcon,
    action: shareToFeed,
  },
  {
    id: 'send_messenger',
    label: 'Wyślij w Messengerze',
    icon: FacebookMessengerIcon,
    action: sendInMessenger,
  },
  {
    id: 'share_group',
    label: 'Udostępnij w grupie',
    icon: AccountGroupOutlineIcon,
    action: shareInGroup,
  },
  {
    id: 'share_friend',
    label: 'Udostępnij w profilu znajomego',
    icon: AccountMultipleOutlineIcon,
    action: shareToFriendProfile,
  },
])
</script>
