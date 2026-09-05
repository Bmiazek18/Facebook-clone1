<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { usersApi } from '@/api/users'
import { useUserCache } from '@/composables/shared/useUserCache'
import { useChatStore } from '@/stores/chat'

// Import ikon z vue-material-design-icons
import AccountMultipleIcon from 'vue-material-design-icons/AccountMultiple.vue'
import HomeVariantIcon from 'vue-material-design-icons/HomeVariant.vue'
import BriefcaseVariantIcon from 'vue-material-design-icons/BriefcaseVariant.vue'
import SchoolIcon from 'vue-material-design-icons/School.vue'
import MessageTextIcon from 'vue-material-design-icons/MessageText.vue'
import AccountCheckIcon from 'vue-material-design-icons/AccountCheck.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'

interface Props {
  userId?: number | string
  name?: string
  mention?: boolean
  comment?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  userId: undefined,
  name: '',
  mention: false,
  comment: false,
  disabled: false,
})

const router = useRouter()
const chatStore = useChatStore()
const { getOrFetchUser, usersCache } = useUserCache()

const isInteracted = ref(false)
const fullUserData = ref<any>(null)
const isLoading = ref(false)

const cleanUserId = computed(() => {
  if (!props.userId) return null
  return String(props.userId).replace(/^user_/, '')
})

const user = computed(() => {
  if (fullUserData.value) return fullUserData.value
  if (!cleanUserId.value) return null
  const cached = usersCache.value[cleanUserId.value]
  if (cached) {
    return {
      id: cached.id,
      name: cached.name,
      avatar: cached.avatar || '/default-avatar.png',
      location: null,
      work: null,
      education: null,
      mutualFriendsCount: 0,
    }
  }
  return null
})

const displayName = computed(() => {
  if (props.name) return props.name
  if (user.value?.name) return user.value.name
  if (cleanUserId.value && usersCache.value[cleanUserId.value]?.name) {
    return usersCache.value[cleanUserId.value].name
  }
  return 'Użytkownik'
})

const handleViewProfile = () => {
  if (cleanUserId.value) {
    router.push(`/profile/${cleanUserId.value}`)
  }
}

const handleOpenChat = () => {
  if (cleanUserId.value) {
    chatStore.addMessageBox(cleanUserId.value)
  }
}

const fetchFullUserData = async () => {
  if (!cleanUserId.value || cleanUserId.value === '0' || cleanUserId.value === '00000000-0000-4000-8000-000000000000') return

  isLoading.value = true
  try {
    const u = await usersApi.getUserProfile(cleanUserId.value)
    if (u) {
      const fullName = [u.firstName, u.lastName].filter(Boolean).join(' ').trim() || 'Użytkownik'
      const loc = u.city || u.location || u.hometown || null
      const workInfo = u.job && u.company ? `${u.job} w: ${u.company}` : u.job || u.work || u.company || null
      const eduInfo = u.school || u.education || null

      fullUserData.value = {
        id: u.id,
        name: fullName,
        avatar: u.avatar || '/default-avatar.png',
        location: loc,
        work: workInfo,
        education: eduInfo,
        bio: u.bio || null,
        mutualFriendsCount: 0,
      }
    }
  } catch (err) {
    console.error('Failed to fetch full user for ProfilePopper:', err)
  } finally {
    isLoading.value = false
  }
}

watch(
  () => cleanUserId.value,
  (newId) => {
    if (newId) {
      getOrFetchUser(newId)
    }
  },
  { immediate: true }
)

const onShow = async () => {
  isInteracted.value = true
  if (!fullUserData.value) {
    await fetchFullUserData()
  }
}
</script>

<template>
  <VMenu
    placement="top-start"
    :delay="{ show: 400, hide: 250 }"
    :distance="12"
    :skidding="0"
    container="body"
    :disabled="disabled || !cleanUserId || cleanUserId === '0'"
    @show="onShow"
  >
    <slot>
      <div
        @click="handleViewProfile"
        :class="[
          'cursor-pointer hover:underline inline-block leading-5 w-fit theme-text',
          comment
            ? 'text-[15px] font-medium'
            : mention
              ? 'text-[13px]'
              : 'text-[17px] font-semibold',
        ]"
      >
        {{ displayName }}
      </div>
    </slot>

    <template #popper="{ hide }">
      <div
        v-if="isInteracted && user"
        class="relative bg-white dark:bg-[#242526] rounded-2xl shadow-[0_12px_28px_0_rgba(0,0,0,0.2),0_2px_4px_0_rgba(0,0,0,0.1)] w-[380px] p-4 text-[#050505] dark:text-white border border-gray-200 dark:border-gray-700/80 antialiased"
      >
        <!-- Przycisk zamknięcia -->
        <button
          @click="hide"
          class="absolute top-3 right-3 p-1.5 bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] transition-colors rounded-full text-[#050505] dark:text-white"
        >
          <CloseIcon :size="18" />
        </button>

        <!-- Nagłówek ze zdjęciem i informacjami -->
        <div class="flex gap-3.5 items-start">
          <div class="shrink-0 w-[84px] h-[84px]">
            <img
              :src="user.avatar || '/default-avatar.png'"
              :alt="user.name"
              class="h-[84px] w-[84px] rounded-full object-cover border border-black/5 dark:border-white/10"
            />
          </div>

          <div class="grow min-w-0 pr-6 space-y-1">
            <h2
              @click="handleViewProfile"
              class="text-[19px] font-bold text-gray-900 dark:text-white leading-tight hover:underline cursor-pointer truncate"
            >
              {{ user.name }}
            </h2>

            <!-- Bio jeśli istnieje -->
            <p v-if="user.bio" class="text-xs text-gray-500 dark:text-gray-400 line-clamp-2 leading-snug">
              {{ user.bio }}
            </p>

            <!-- Miejsce zamieszkania (tylko jeśli istnieje prawdziwe) -->
            <div
              v-if="user.location"
              class="flex items-center gap-2 text-[13px] text-gray-600 dark:text-gray-300"
            >
              <HomeVariantIcon :size="16" class="text-gray-400 shrink-0" />
              <span class="truncate">
                <span>Mieszka w:</span>
                <span class="font-semibold ml-1 text-gray-900 dark:text-white">{{ user.location }}</span>
              </span>
            </div>

            <!-- Praca (jeśli istnieje) -->
            <div
              v-if="user.work"
              class="flex items-center gap-2 text-[13px] text-gray-600 dark:text-gray-300"
            >
              <BriefcaseVariantIcon :size="16" class="text-gray-400 shrink-0" />
              <span class="truncate">
                <span>Pracuje w:</span>
                <span class="font-semibold ml-1 text-gray-900 dark:text-white">{{ user.work }}</span>
              </span>
            </div>

            <!-- Edukacja (jeśli istnieje) -->
            <div
              v-if="user.education"
              class="flex items-center gap-2 text-[13px] text-gray-600 dark:text-gray-300"
            >
              <SchoolIcon :size="16" class="text-gray-400 shrink-0" />
              <span class="truncate">
                <span>Szkoła:</span>
                <span class="font-semibold ml-1 text-gray-900 dark:text-white">{{ user.education }}</span>
              </span>
            </div>
          </div>
        </div>

        <!-- Przyciski akcji -->
        <div class="mt-4 flex items-center gap-2">
          <button
            @click="handleViewProfile"
            class="flex items-center justify-center gap-1.5 px-3 h-[36px] text-sm font-semibold rounded-xl bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] text-[#050505] dark:text-white transition active:scale-[0.98]"
          >
            <AccountCheckIcon :size="16" />
            <span>Profil</span>
          </button>

          <button
            @click="handleOpenChat"
            class="flex-1 flex items-center justify-center gap-2 px-4 h-[36px] text-sm font-semibold rounded-xl bg-[#1877F2] hover:bg-[#1771E6] text-white transition shadow-sm active:scale-[0.98]"
          >
            <MessageTextIcon :size="16" />
            <span>Wyślij wiadomość</span>
          </button>

          <button
            @click="handleViewProfile"
            class="flex items-center justify-center px-2.5 h-[36px] rounded-xl bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] text-[#050505] dark:text-white transition active:scale-[0.98]"
          >
            <DotsHorizontalIcon :size="18" />
          </button>
        </div>
      </div>
    </template>
  </VMenu>
</template>

<style scoped>
.v-popper__popper {
  pointer-events: auto !important;
}
</style>
