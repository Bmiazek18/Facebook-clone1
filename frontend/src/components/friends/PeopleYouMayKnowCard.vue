<template>
  <div
    class="w-full bg-theme-bg-secondary border border-theme-border rounded-xl overflow-hidden shadow-sm flex flex-col transition-shadow duration-200"
  >
    <div class="relative w-full aspect-square shrink-0">
      <img
        :src="DefaultAvatar"
        :alt="person.name"
        class="w-full h-full cursor-pointer"
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

      <!-- Używamy działającego u Ciebie VTooltip -->
      <div class="mb-2">
        <VTooltip v-if="person.commonFriends > 0" @show="fetchFriends">
          <!-- Element, na który najeżdżamy -->
          <div class="flex items-center text-[13px] text-theme-text-secondary cursor-pointer">
            <div class="flex shrink-0 mr-2">
              <div
                class="w-5 h-5 rounded-full bg-theme-border flex items-center justify-center overflow-hidden"
              >
                <img :src="person.imageUrl || DefaultAvatar" class="w-full h-full" />
              </div>
            </div>

            <span class="truncate hover:underline">{{ $t('friends.personCommonfriendsWspolnychZnajomych') }}</span>
          </div>

          <!-- Zawartość Tooltipa (Popper) -->
          <template #popper>
            <div class="flex flex-col   ">


              <LoadingSpinner v-if="loading" :size="25" />




            </div>
          </template>
        </VTooltip>

        <!-- Fallback, gdy brak wspólnych znajomych (bez tooltipa) -->
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
import { ref, computed } from 'vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue'
import type { Person } from '@/types/Person'
import LoadingSpinner from '../common/LoadingSpinner.vue'
import DefaultAvatar from '@/assets/images/default_avatar.png'

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

const hasRequested = ref(false)
const loading = ref(true)

// Funkcja odpalana eventem @show z komponentu VTooltip
const fetchFriends = () => {
  // Jeśli już wysłano żądanie, nic nie rób
  if (hasRequested.value) return

  // Oznaczamy, że żądanie zostało rozpoczęte
  hasRequested.value = true
  loading.value = true

  setTimeout(() => {
    loading.value = false
  }, 5000)
}


</script>
