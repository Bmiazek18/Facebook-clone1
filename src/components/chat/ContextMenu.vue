<script setup lang="ts">
import { markRaw, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Component } from 'vue'

// Importy ikon
import EmailOutlineIcon from 'vue-material-design-icons/EmailOutline.vue'
import BellOutlineIcon from 'vue-material-design-icons/BellOutline.vue'
import AccountCircleOutlineIcon from 'vue-material-design-icons/AccountCircleOutline.vue'
import PhoneOutlineIcon from 'vue-material-design-icons/PhoneOutline.vue'
import VideoOutlineIcon from 'vue-material-design-icons/VideoOutline.vue'
import AccountCancelOutlineIcon from 'vue-material-design-icons/AccountCancelOutline.vue'
import CloseBoxOutlineIcon from 'vue-material-design-icons/CloseBoxOutline.vue'
import DeleteOutlineIcon from 'vue-material-design-icons/DeleteOutline.vue'
import AlertOutlineIcon from 'vue-material-design-icons/AlertOutline.vue'

const { t } = useI18n()

interface MenuItemConfig {
  id: number
  labelKey: string
  icon: Component
  action: string
  isDestructive?: boolean
  divider?: boolean // Logika separatora pozostaje bez zmian
}

interface MenuItemData extends MenuItemConfig {
  label: string
}

const menuItemsConfig: MenuItemConfig[] = [
  { id: 1, labelKey: 'chat.markAsUnread', icon: markRaw(EmailOutlineIcon), action: 'mark-as-unread' },
  { id: 2, labelKey: 'chat.muteNotifications', icon: markRaw(BellOutlineIcon), action: 'mute-notifications' },
  {
    id: 3,
    labelKey: 'chat.viewProfile',
    icon: markRaw(AccountCircleOutlineIcon),
    action: 'view-profile',
    divider: true // Generuje linię poniżej
  },
  { id: 4, labelKey: 'chat.voiceCall', icon: markRaw(PhoneOutlineIcon), action: 'voice-call' },
  { id: 5, labelKey: 'chat.videoCall', icon: markRaw(VideoOutlineIcon), action: 'video-call' },
  { id: 6, labelKey: 'chat.block', icon: markRaw(AccountCancelOutlineIcon), action: 'block' },
  { id: 7, labelKey: 'chat.archive', icon: markRaw(CloseBoxOutlineIcon), action: 'archive' },
  { id: 8, labelKey: 'chat.deleteChat', icon: markRaw(DeleteOutlineIcon), action: 'delete-chat' },
  { id: 9, labelKey: 'chat.report', icon: markRaw(AlertOutlineIcon), action: 'report' },
]

const menuItems = computed(() =>
  menuItemsConfig.map((item) => ({
    ...item,
    label: t(item.labelKey),
  })),
)

const emit = defineEmits<{
  (e: 'select-action', action: string): void
}>()

const handleItemClick = (item: MenuItemData) => {
  emit('select-action', item.action)
  console.log(`Akcja: ${item.action}`)
}
</script>

<template>
  <!-- Lekko zwiększony padding głównego kontenera (py-2) -->
  <div
    style="filter: drop-shadow(rgba(0, 0, 0, 0.1) 0px 4px 12px)"
    class="w-[320px] bg-white rounded-2xl z-30 py-2 border border-gray-100"
  >
    <ul role="menu" class="list-none m-0 p-0">
      <template v-for="item in menuItems" :key="item.id">
        <!-- Zbalansowane paddingi w elemencie (px-3 py-2) -->
        <li
          role="menuitem"
          @click="handleItemClick(item)"
          class="flex items-center px-3 py-2 mx-1.5 rounded-xl cursor-pointer transition-colors hover:bg-gray-100 active:bg-gray-200"
        >
          <!-- Zwiększony margines po prawej stronie ikony dla lepszego oddechu (mr-2.5) -->
          <div
            class="w-8 flex items-center justify-center mr-2.5 shrink-0"
            :class="{ 'text-red-600': item.isDestructive, 'text-gray-900': !item.isDestructive }"
          >
            <!-- Ikona powraca do standardowych 24px -->
            <component :is="item.icon" :size="24" />
          </div>

          <!-- Rozmiar tekstu przywrócony do 15px -->
          <span
            class="text-[15px] font-medium grow leading-tight"
            :class="{ 'text-red-600': item.isDestructive, 'text-gray-950': !item.isDestructive }"
          >
            {{ item.label }}
          </span>
        </li>

        <!-- Zwiększony margines wokół separatora (my-1.5) -->
        <hr v-if="item.divider" class="border-gray-200 mx-3 my-1.5" />
      </template>
    </ul>
  </div>
</template>
