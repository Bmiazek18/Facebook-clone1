<template>
  <div
    class="w-full md:w-[360px] mx-auto bg-theme-bg-secondary max-h-[calc(100vh-4rem)] flex flex-col overflow-hidden shadow-2xl rounded-xl"
  >
    <!-- Nagłówek -->
    <header class="pt-4 pb-3 px-4 flex justify-between items-center shrink-0">
      <h1 class="text-2xl font-bold text-theme-text">{{ $t('notifications_page.title', 'Powiadomienia') }}</h1>

      <VMenu placement="bottom-end" :distance="8" :triggers="['click']">
        <button class="flex items-center justify-center h-8 w-8 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full transition outline-none">
          <DotsHorizontalIcon class="h-6 w-6" />
        </button>
        <template #popper="{ hide }">
          <div class="w-[340px] bg-theme-bg-secondary p-1.5 rounded-2xl shadow-xl border border-theme-border flex flex-col">
            <button @click="handleAction(() => {}, hide)" class="flex items-center gap-3 w-full px-3 py-2.5 hover:bg-theme-hover rounded-xl text-theme-text text-left transition-colors">
              <Check class="h-6 w-6 text-theme-text shrink-0" />
              <span class="text-[15px] font-medium">{{ $t('notifications_page.markAllAsRead', 'Oznacz wszystkie jako przeczytane') }}</span>
            </button>
            <button @click="handleAction(() => {}, hide)" class="flex items-center gap-3 w-full px-3 py-2.5 hover:bg-theme-hover rounded-xl text-theme-text text-left transition-colors mt-0.5">
              <CogOutline class="h-6 w-6 text-theme-text shrink-0" />
              <span class="text-[15px] font-medium">{{ $t('notifications_page.settings', 'Ustawienia powiadomień') }}</span>
            </button>
            <button @click="handleAction(() => {}, hide)" class="flex items-center gap-3 w-full px-3 py-2.5 hover:bg-theme-hover rounded-xl text-theme-text text-left transition-colors mt-0.5">
              <Monitor class="h-6 w-6 text-theme-text shrink-0" />
              <span class="text-[15px] font-medium">{{ $t('notifications_page.openNotifications', 'Otwórz powiadomienia') }}</span>
            </button>
          </div>
        </template>
      </VMenu>
    </header>

    <!-- Zakładki -->
    <div class="flex px-4 pb-2 space-x-2 shrink-0 border-b border-transparent">
      <button
        @click="activeTab = 'all'"
        :class="[
          activeTab === 'all'
            ? 'bg-[#E7F3FF] dark:bg-blue-900/40 text-[#0866FF] dark:text-blue-400'
            : 'text-theme-text hover:bg-gray-100 dark:hover:bg-gray-800'
        ]"
        class="py-1.5 px-3.5 rounded-full text-[15px] font-semibold transition duration-150"
      >
        {{ $t('notifications_page.all', 'Wszystkie') }}
      </button>
      <button
        @click="activeTab = 'unread'"
        :class="[
          activeTab === 'unread'
            ? 'bg-[#E7F3FF] dark:bg-blue-900/40 text-[#0866FF] dark:text-blue-400'
            : 'text-theme-text hover:bg-gray-100 dark:hover:bg-gray-800'
        ]"
        class="py-1.5 px-3.5 rounded-full text-[15px] font-semibold transition duration-150"
      >
        {{ $t('notifications_page.unread', 'Nieprzeczytane') }}
      </button>
    </div>

    <!-- Lista powiadomień -->
    <div class="flex-1 overflow-y-auto px-2 pb-2">
      <!-- Skeleton Loading -->
      <ul v-if="loading && notifications.length === 0" class="space-y-0.5">
        <li v-for="i in 7" :key="'skeleton-' + i">
          <div class="flex items-center py-2 px-2 animate-pulse">
            <div class="h-14 w-14 bg-gray-200 dark:bg-gray-700 rounded-full shrink-0 mr-3"></div>
            <div class="flex flex-col gap-2 w-full">
              <div class="h-3.5 bg-gray-200 dark:bg-gray-700 rounded-full w-full"></div>
              <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full w-2/3"></div>
            </div>
          </div>
        </li>
      </ul>

      <template v-else>
        <!-- Sekcja: Zaproszenia do grona znajomych -->
        <div v-if="friendRequests.length > 0" class="mb-2">
          <div class="flex justify-between items-center px-2 pt-3 pb-2">
            <h2 class="text-[17px] font-bold text-theme-text">
              {{ $t('notifications_page.friendRequests', 'Zaproszenia do grona znajomych') }}
            </h2>
            <button class="text-[#0866FF] font-normal text-[15px] hover:bg-gray-50 dark:hover:bg-gray-800 px-2 py-1 rounded-md transition">
              {{ $t('notifications_page.viewAll', 'Wyświetl wszystko') }}
            </button>
          </div>

          <ul class="space-y-1">
            <li v-for="notification in friendRequests" :key="notification.id">
              <div
                @click="handleMarkAsRead(notification)"
                class="relative group flex flex-col py-2 px-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg cursor-pointer transition duration-100 outline-none"
              >
                <div class="flex items-start w-full">
                  <!-- Avatar + Badge -->
                  <div class="relative shrink-0 mr-3 mt-0.5">
                    <img
                      :src="notification.avatarUrl"
                      alt="Awatar"
                      class="h-14 w-14 rounded-full object-cover bg-gray-200 border border-black/5 dark:border-white/5"
                    />
                    <div class="absolute -bottom-1 -right-1 h-7 w-7 flex items-center justify-center rounded-full ring-2 ring-white dark:ring-[#242526] bg-[#0866FF] text-white">
                      <Account class="h-4 w-4" />
                    </div>
                  </div>

                  <!-- Treść -->
                  <div class="grow min-w-0 pr-8">
                    <p
                      class="text-[15px] leading-[1.3] text-theme-text"
                      v-html="sanitizeNotificationMessage(notification.message)"
                    ></p>
                    <span
                      class="text-[13px] block mt-0.5 transition-colors duration-200"
                      :class="notification.unread ? 'text-[#0866FF] font-semibold' : 'text-[#65676B]'"
                    >
                      {{ notification.timeAgo }}
                    </span>
                  </div>
                </div>

                <!-- Przyciski akcji na pełną szerokość (flex-1) -->
                <div class="flex items-center gap-2 pl-[68px] mt-2 pr-2" @click.stop>
                  <button
                    @click="acceptFriendRequest(notification)"
                    class="flex-1 py-1.5 bg-[#0866FF] hover:bg-blue-700 text-white font-semibold rounded-lg text-[15px] transition duration-150"
                  >
                    {{ $t('notifications_page.confirm', 'Potwierdź') }}
                  </button>
                  <button
                    @click="rejectFriendRequest(notification)"
                    class="flex-1 py-1.5 bg-[#E4E6EB] hover:bg-[#D8DADF] dark:bg-gray-700 dark:hover:bg-gray-600 text-[#050505] dark:text-white font-semibold rounded-lg text-[15px] transition duration-150"
                  >
                    {{ $t('notifications_page.delete', 'Usuń') }}
                  </button>
                </div>
              </div>
            </li>
          </ul>
        </div>

        <!-- Sekcja: Nowe / Wcześniejsze powiadomienia -->
        <div v-if="otherNotifications.length > 0">
          <div class="flex justify-between items-center px-2 pt-3 pb-2">
            <h2 class="text-[17px] font-bold text-theme-text">
              {{ $t('notifications_page.new', 'Nowe') }}
            </h2>
            <button class="text-[#0866FF] font-normal text-[15px] hover:bg-gray-50 dark:hover:bg-gray-800 px-2 py-1 rounded-md transition">
              {{ $t('notifications_page.viewAll', 'Wyświetl wszystko') }}
            </button>
          </div>

          <ul class="space-y-0.5">
            <li v-for="notification in otherNotifications" :key="notification.id">
              <button
                @click="handleMarkAsRead(notification)"
                class="relative group flex items-center w-full py-2 px-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg cursor-pointer transition duration-100 text-left outline-none"
              >
                <!-- Avatar + Badge -->
                <div class="relative shrink-0 mr-3 self-start mt-0.5">
                  <img
                    :src="notification.avatarUrl"
                    alt="Awatar"
                    class="h-14 w-14 rounded-full object-cover bg-gray-200 border border-black/5 dark:border-white/5"
                  />
                  <div
                    class="absolute -bottom-1 -right-1 h-7 w-7 flex items-center justify-center rounded-full ring-2 ring-white dark:ring-[#242526]"
                    :class="notification.typeIconBgClass"
                  >
                    <component :is="notification.typeIcon" class="h-4 w-4 text-white" />
                  </div>
                </div>

                <!-- Treść -->
                <div class="grow min-w-0 pr-10">
                  <p
                    class="text-[15px] leading-[1.3] transition-colors duration-200"
                    :class="notification.unread ? 'text-theme-text' : 'text-[#65676B]'"
                    v-html="sanitizeNotificationMessage(notification.message)"
                  ></p>
                  <span
                    class="text-[13px] block mt-0.5 transition-colors duration-200"
                    :class="notification.unread ? 'text-[#0866FF] font-semibold' : 'text-[#65676B]'"
                  >
                    {{ notification.timeAgo }}
                  </span>
                </div>

                <!-- Wskaźnik nieprzeczytania (niebieska kropka) i menu -->
                <div class="absolute right-3 top-1/2 -translate-y-1/2 flex items-center justify-end h-9 w-9">
                  <div
                    v-if="notification.unread"
                    class="w-3 h-3 bg-[#0866FF] rounded-full absolute right-1 transition-opacity duration-200"
                    :class="openDropdowns[notification.id] ? 'opacity-0' : 'group-hover:opacity-0'"
                  ></div>

                  <div @click.stop class="absolute right-0">
                    <VMenu
                      placement="bottom-end"
                      :distance="8"
                      :triggers="['click']"
                      @show="setDropdownOpen(notification.id, true)"
                      @hide="setDropdownOpen(notification.id, false)"
                    >
                      <div
                        :class="[
                          'flex items-center justify-center w-9 h-9 bg-white dark:bg-[#242526] rounded-full shadow-[0_2px_8px_rgba(0,0,0,0.12)] border border-black/5 dark:border-white/10 hover:bg-gray-100 dark:hover:bg-gray-700 transition-all duration-200 cursor-pointer',
                          openDropdowns[notification.id] ? 'opacity-100 z-20' : 'opacity-0 group-hover:opacity-100 z-10'
                        ]"
                      >
                        <DotsHorizontalIcon class="h-6 w-6 text-gray-600 dark:text-gray-300" />
                      </div>

                      <template #popper="{ hide }">
                        <div class="w-[340px] bg-theme-bg-secondary p-1.5 rounded-2xl shadow-2xl border border-theme-border flex flex-col pointer-events-auto">
                          <button @click="handleAction(() => { handleMarkAsRead(notification) }, hide)" class="flex items-center gap-3 w-full px-3 py-2.5 hover:bg-theme-hover rounded-xl text-theme-text text-left transition-colors">
                            <Check class="h-6 w-6 text-theme-text shrink-0" />
                            <span class="text-[15px] font-medium leading-snug">{{ $t('notifications_page.markAsRead', 'Oznacz jako przeczytane') }}</span>
                          </button>

                          <button @click="handleAction(() => {}, hide)" class="flex items-center gap-3 w-full px-3 py-2.5 hover:bg-theme-hover rounded-xl text-theme-text text-left transition-colors mt-0.5">
                            <CloseBoxOutline class="h-6 w-6 text-theme-text shrink-0" />
                            <span class="text-[15px] font-medium leading-snug">{{ $t('notifications_page.removeNotification', 'Usuń to powiadomienie') }}</span>
                          </button>

                          <button @click="handleAction(() => {}, hide)" class="flex items-start gap-3 w-full px-3 py-2.5 hover:bg-theme-hover rounded-xl text-theme-text text-left transition-colors mt-0.5">
                            <BellOffOutline class="h-6 w-6 text-theme-text shrink-0 mt-0.5" />
                            <span class="text-[15px] font-medium leading-tight">{{ $t('notifications_page.muteEveryone', 'Wyłącz powiadomienia @everyone z tej grupy') }}</span>
                          </button>

                          <button @click="handleAction(() => {}, hide)" class="flex items-start gap-3 w-full px-3 py-2.5 hover:bg-theme-hover rounded-xl text-theme-text text-left transition-colors mt-0.5">
                            <BellOffOutline class="h-6 w-6 text-theme-text shrink-0 mt-0.5" />
                            <span class="text-[15px] font-medium leading-tight">{{ $t('notifications_page.mutePost', 'Wyłącz powiadomienia dotyczące tego postu') }}</span>
                          </button>

                          <button @click="handleAction(() => {}, hide)" class="flex items-start gap-3 w-full px-3 py-2.5 hover:bg-theme-hover rounded-xl text-theme-text text-left transition-colors mt-0.5">
                            <BugOutline class="h-6 w-6 text-theme-text shrink-0 mt-0.5" />
                            <span class="text-[15px] font-medium leading-tight">{{ $t('notifications_page.reportBug', 'Zgłoś problem do zespołu ds. powiadomień') }}</span>
                          </button>
                        </div>
                      </template>
                    </VMenu>
                  </div>
                </div>
              </button>
            </li>
          </ul>
        </div>
      </template>

      <!-- Przycisk Zobacz więcej -->
      <div class="pt-2 px-2 mt-1 mb-1">
        <button
          class="w-full py-2 bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 rounded-lg text-[15px] font-semibold text-theme-text transition duration-150"
        >
          {{ $t('notifications_page.viewEarlier', 'Zobacz wcześniejsze powiadomienia') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, type Ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useNotificationsStore } from '@/stores/notifications'

// Ikony powiadomień
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import BellIcon from 'vue-material-design-icons/Bell.vue'
import MessageReplyText from 'vue-material-design-icons/MessageReplyText.vue'
import Account from 'vue-material-design-icons/Account.vue'
import Heart from 'vue-material-design-icons/Heart.vue'
import AccountGroup from 'vue-material-design-icons/AccountGroup.vue' // Dodana ikona grupy

// Ikony menu
import Check from 'vue-material-design-icons/Check.vue'
import CogOutline from 'vue-material-design-icons/CogOutline.vue'
import Monitor from 'vue-material-design-icons/Monitor.vue'
import CloseBoxOutline from 'vue-material-design-icons/CloseBoxOutline.vue'
import BellOffOutline from 'vue-material-design-icons/BellOffOutline.vue'
import BugOutline from 'vue-material-design-icons/BugOutline.vue'

const { t } = useI18n()
const notifStore = useNotificationsStore()

const activeTab: Ref<'all' | 'unread'> = ref('all')

const openDropdowns = ref<Record<string, boolean>>({})
const setDropdownOpen = (id: string, value: boolean) => {
  openDropdowns.value[id] = value
}

const sanitizeNotificationMessage = (msg: string): string => {
  if (!msg) return ''
  return String(msg)
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/&lt;(\/?(?:strong|b|span|i|em))&gt;/gi, '<$1>')
}

const formatTimeAgo = (createdAtStr: string) => {
  if (!createdAtStr) return t('notifications_page.time.justNow', 'Przed chwilą')
  try {
    const createdDate = new Date(createdAtStr)
    const now = new Date()
    const diffMs = now.getTime() - createdDate.getTime()
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMins / 60)
    const diffDays = Math.floor(diffHours / 24)
    const diffWeeks = Math.floor(diffDays / 7)

    if (diffMins < 1) return t('notifications_page.time.justNow', 'Przed chwilą')
    if (diffMins < 60) return t('notifications_page.time.minutesAgo', { mins: diffMins })
    if (diffHours < 24) return t('notifications_page.time.hoursAgo', { hours: diffHours })
    if (diffDays < 7) return t('notifications_page.time.daysAgo', { days: diffDays })
    return t('notifications_page.time.weeksAgo', { weeks: diffWeeks })
  } catch (e) {
    return t('notifications_page.time.recently', 'Niedawno')
  }
}

const handleAction = (callback: () => void, hide: () => void) => {
  callback()
  hide()
}

const loading = computed(() => notifStore.loading)
const notifications = computed(() => notifStore.notifications || [])

const mappedNotifications = computed(() => {
  return notifications.value.map((n: any) => {
    const timeAgo = formatTimeAgo(n.createdAt)

    let avatarUrl = ''
    if (n.sender) {
      avatarUrl = n.sender.avatar || '/default-avatar.png'
    } else {
      avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(n.title)}&background=random&color=fff`
    }

    let displayMessage = n.message
    let typeIcon: any = BellIcon
    let typeIconBgClass = 'bg-[#0866FF]'
    let isFriendRequest = false

    if (n.sender) {
      const senderName = `${n.sender.firstName} ${n.sender.lastName}`

      // Obsługa zaproszenia do znajomych
      if (n.title === 'Friend Request' || n.type === 'FRIEND_REQUEST') {
        isFriendRequest = true
        displayMessage = t('notifications_page.types.friendRequest', { name: senderName })
        typeIcon = Account
        typeIconBgClass = 'bg-[#0866FF]'
      }
      // Obsługa wzmianki / komentarza w grupie
      else if (n.title === 'Mention' || n.type === 'MENTION') {
        const groupName = n.groupName || 'Grupa'
        displayMessage = t('notifications_page.types.mentionGeneral', { name: senderName, groupName })
        typeIcon = MessageReplyText
        typeIconBgClass = 'bg-[#31A24C]'
      }
      // Obsługa reakcji (serduszko)
      else if (n.title === 'Reaction' || n.type === 'REACTION' || n.title === 'Polubienie') {
        const postSnippet = n.postSnippet || 'post'
        displayMessage = n.message && n.message.includes('polubił')
          ? t('notifications_page.types.reactionLiked', { name: senderName })
          : t('notifications_page.types.reaction', { name: senderName, snippet: postSnippet })
        typeIcon = Heart
        typeIconBgClass = 'bg-gradient-to-br from-[#FF4256] to-[#E3002A]'
      }
      // Obsługa nowego posta w grupie
      else if (n.title === 'Group Post' || n.type === 'GROUP_POST') {
        const groupName = n.groupName || 'Grupa'
        const postSnippet = n.postSnippet || 'nowy post'
        displayMessage = t('notifications_page.types.groupPost', { groupName, snippet: postSnippet })
        typeIcon = AccountGroup
        typeIconBgClass = 'bg-[#0866FF]'
      }
      // Zaakceptowane zaproszenie
      else if (n.title === 'Friend Request Accepted' || n.type === 'FRIEND_REQUEST_ACCEPTED') {
        displayMessage = t('notifications_page.types.friendRequestAccepted', { name: senderName })
      }
    }

    return {
      id: String(n.id),
      avatarUrl: avatarUrl,
      typeIcon: typeIcon,
      typeIconBgClass: typeIconBgClass,
      message: displayMessage,
      timeAgo: timeAgo,
      unread: !n.read,
      isFriendRequest: isFriendRequest,
      raw: n
    }
  })
})

const filteredNotifications = computed(() => {
  if (activeTab.value === 'unread') {
    return mappedNotifications.value.filter((n) => n.unread)
  }
  return mappedNotifications.value
})

const friendRequests = computed(() => {
  return filteredNotifications.value.filter((n) => n.isFriendRequest)
})

const otherNotifications = computed(() => {
  return filteredNotifications.value.filter((n) => !n.isFriendRequest)
})

const handleMarkAsRead = async (notification: any) => {
  if (!notification.unread) return
  await notifStore.markAsRead(String(notification.id))
}

const acceptFriendRequest = async (notification: any) => {
  await handleMarkAsRead(notification)
}

const rejectFriendRequest = async (notification: any) => {
  await handleMarkAsRead(notification)
}
</script>
