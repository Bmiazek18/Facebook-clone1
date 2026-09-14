<template>
  <div
    class="w-full bg-theme-bg-secondary border border-theme-border rounded-xl overflow-hidden shadow-sm flex flex-col transition-shadow duration-200"
  >
    <div class="relative w-full aspect-square shrink-0">
      <img
        :src="person.imageUrl || DefaultAvatar"
        :alt="person.name"
        class="w-full h-full object-cover cursor-pointer"
      />

      <button
        v-if="hasXButton"
        @click="$emit('remove', person.id)"
        class="absolute top-3 right-3 p-2 bg-black/50 hover:bg-black/70 rounded-full text-white transition backdrop-blur-sm"
      >
        <CloseIcon :size="20" fillColor="white" />
      </button>
    </div>

    <div class="p-2.5 flex flex-col grow">
      <h3
        class="text-[17px] leading-tight font-semibold text-theme-text mb-1 cursor-pointer hover:underline truncate"
      >
        {{ person.name }}
      </h3>

      <div class="mb-2">
        <div v-if="person.commonFriends > 0" class="flex items-center text-[13px] text-theme-text-secondary">
          <div class="flex shrink-0 mr-2">
            <div
              class="w-5 h-5 rounded-full bg-theme-border flex items-center justify-center overflow-hidden"
            >
              <img :src="person.imageUrl || DefaultAvatar" class="w-full h-full object-cover" />
            </div>
          </div>

          <span class="truncate">{{ commonFriendsLabel }}</span>
        </div>

        <!-- Fallback, gdy brak wspólnych znajomych -->
        <div v-else class="flex items-center text-[13px] text-theme-text-secondary">
          <span>{{ $t('profile.noCommonFriends') }}</span>
        </div>
      </div>

      <div class="mt-auto flex flex-col gap-2">
        <button
          v-if="variant === 'request'"
          @click="$emit('confirm', person.id)"
          class="w-full bg-theme-primary hover:bg-theme-primary-hover text-white font-bold text-[15px] py-1.5 rounded-lg transition-colors"
        >{{ $t('notifications_page.confirm') }}</button>

        <button
          v-else
          @click="$emit('add', person.id)"
          class="w-full bg-theme-primary-subtle hover:bg-theme-primary-subtle-hover text-theme-primary font-bold text-[12px] py-1 rounded-lg transition-colors flex items-center justify-center"
        >
          <AccountPlusIcon :size="16" class="mr-1.5" />{{ $t('feed.dodajZnajomego') }}</button>

        <button
          v-if="!hasXButton"
          @click="$emit('delete', person.id)"
          class="w-full bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text font-bold text-[15px] py-1.5 rounded-lg transition-colors"
        >{{ $t('notifications_page.delete') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue'
import type { Person } from '@/types/Person'
import DefaultAvatar from '@/assets/images/default_avatar.png'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    person: Person
    variant?: 'request' | 'suggestion'
    hasXButton?: boolean
  }>(),
  {
    variant: 'suggestion',
    hasXButton: false,
  },
)

defineEmits<{
  (e: 'remove', id: string | number): void
  (e: 'confirm', id: string | number): void
  (e: 'delete', id: string | number): void
  (e: 'add', id: string | number): void
}>()

const commonFriendsLabel = computed(() => {
  const count = props.person.commonFriends || 0
  if (count === 1) {
    return t('friends.oneCommonFriend')
  }
  return t('friends.personCommonfriendsWspolnychZnajomych', { count })
})
</script>
