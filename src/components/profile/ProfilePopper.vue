<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import BriefcaseIcon from 'vue-material-design-icons/Briefcase.vue'
import SchoolIcon from 'vue-material-design-icons/School.vue'
import SendIcon from 'vue-material-design-icons/Send.vue'
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import { getUserById } from '@/data/users'

interface Props {
  userId?: number | string,
  mention?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  userId: undefined,
  mention: false
})

const router = useRouter()
const isInteracted = ref(false) // Klucz do optymalizacji: nie renderujemy dopóki nie ma hovera

const user = computed(() => {
  if (!props.userId) return null
  const id = typeof props.userId === 'string' ? parseInt(props.userId) : props.userId
  return getUserById(id)
})

const handleViewProfile = () => {
  if (props.userId) {
    router.push({ name: 'userProfile', params: { userId: props.userId } })
  }
}

// Funkcja wywoływana, gdy użytkownik najeżdża na element
const onShow = () => {
  isInteracted.value = true
}
</script>

<template>
  <VMenu
    placement="top-start"
    :delay="{ show: 500, hide: 300 }"
    :distance="12"
    :skidding="0"
    container="body"
    @show="onShow"
  >
    <div
      :class="[
        'cursor-pointer hover:underline  inline-block',
        mention ? 'text-[13px]' : 'text[-15px] font-semibold'
      ]"
    >
      {{ user?.name || 'Użytkownik' }}
    </div>

    <template #popper>
      <div v-if="isInteracted && user" class="bg-theme-bg-secondary rounded-lg shadow-2xl w-[360px] overflow-hidden border border-theme-border">

        <div class="px-4 pb-4 pt-4">
          <div class="flex items-start space-x-4">
            <div class="relative shrink-0 w-20 h-20">
              <img
                :src="user.avatar || 'https://picsum.photos/id/400/80/80'"
                :alt="user.name"
                class="h-20 w-20 rounded-full object-cover border-2 border-theme-bg-secondary shadow-sm"
              />
            </div>

            <div class="grow min-w-0 pt-2">
              <h2 class="text-xl font-bold text-theme-text truncate leading-tight">
                {{ user.name }}
              </h2>

              <div v-if="user.mutualFriendsCount" class="mt-1 text-sm text-theme-text-secondary">
                {{ user.mutualFriendsCount }} wspólnych znajomych
              </div>
            </div>
          </div>

          <div class="mt-4 space-y-3">
            <div v-if="user.job" class="flex items-center space-x-3 text-theme-text">
              <BriefcaseIcon :size="20" class="text-theme-text-secondary" />
              <span class="text-sm">
                <span class="font-semibold">{{ user.job }}</span>
                <span v-if="user.company"> w {{ user.company }}</span>
              </span>
            </div>

            <div v-if="user.education" class="flex items-center space-x-3 text-theme-text">
              <SchoolIcon :size="20" class="text-theme-text-secondary" />
              <span class="text-sm">
                Studiuje {{ user.education }}
              </span>
            </div>
          </div>

          <div class="mt-6 pt-4 border-t border-theme-border flex items-center gap-2">
            <button
              @click="handleViewProfile"
              class="flex-1 flex items-center justify-center gap-2 px-4 py-2 text-sm font-semibold rounded-md bg-blue-600 text-white hover:bg-blue-700 transition active:scale-95"
            >
              <SendIcon :size="18" />
              <span>Profil</span>
            </button>

            <button
              class="flex-1 flex items-center justify-center gap-2 px-4 py-2 text-sm font-semibold rounded-md bg-theme-bg-tertiary text-theme-text hover:bg-theme-border transition active:scale-95"
            >
              <AccountPlusIcon :size="18" />
              <span>Dodaj</span>
            </button>

            <button
              class="p-2 rounded-md bg-theme-bg-tertiary text-theme-text hover:bg-theme-border transition"
            >
              <DotsHorizontalIcon :size="18" />
            </button>
          </div>
        </div>
      </div>
    </template>
  </VMenu>
</template>

<style scoped>
/* Zapobiega migotaniu poppera przy szybkim ruchu myszką */
.v-popper__popper {
  pointer-events: auto !important;
}
</style>
