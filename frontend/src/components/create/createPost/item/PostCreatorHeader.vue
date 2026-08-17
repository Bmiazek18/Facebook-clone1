<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCreatePostStore } from '@/stores/createPost'
import { useAuthStore } from '@/stores/auth'
import AvatarImage from '@/components/common/AvatarImage.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import LockIcon from 'vue-material-design-icons/Lock.vue'
import EarthIcon from 'vue-material-design-icons/Earth.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue'
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue'

const emit = defineEmits<{
  (e: 'openFeelingSelector'): void
  (e: 'navigatePrivacy'): void
}>()

const { t } = useI18n()
const createPostStore = useCreatePostStore()
const authStore = useAuthStore()

const currentUser = computed(() => authStore.currentUser)

const isAnonymous = computed(() => !!createPostStore.postData.isAnonymous)
const taggedUsers = computed(() => createPostStore.postData.taggedUsers)
const selectedFeeling = computed(() => createPostStore.postData.feeling)
const selectedActivity = computed(() => createPostStore.postData.activity)
const selectedLocation = computed(() => createPostStore.postData.location)
const selectedPrivacy = computed(() => createPostStore.postData.privacy)

const displayAvatar = computed(() => {
  if (isAnonymous.value) return '/img/anonymous-avatar.png'
  return currentUser.value?.avatar || (currentUser.value as any)?.avatarId || '/default-avatar.png'
})

const displayName = computed(() => {
  return isAnonymous.value
    ? t('post.anonymousUser') || 'Anonim'
    : currentUser.value?.name || `${(currentUser.value as any)?.firstName || ''} ${(currentUser.value as any)?.lastName || ''}`.trim()
})

const privacyInfo = computed(() => {
  const map: Record<string, { label: string; icon: any }> = {
    only_me: { label: t('post.only_me'), icon: LockIcon },
    public: { label: t('post.public'), icon: EarthIcon },
    friends: { label: t('post.friends'), icon: AccountGroupIcon },
    friends_except: { label: t('post.friends_except'), icon: AccountMultipleMinusIcon },
    specific_friends: { label: t('post.specific_friends'), icon: AccountStarIcon },
  }
  return map[selectedPrivacy.value] || { label: t('post.only_me'), icon: LockIcon }
})
</script>

<template>
  <div class="flex items-center mb-4">
    <AvatarImage :src="displayAvatar" :alt="displayName" class="mr-3" />

    <div class="flex flex-col">
      <div class="text-[15px] leading-tight mb-1 text-theme-text">
        <span class="font-medium">{{ displayName }}</span>

        <template v-if="!isAnonymous && taggedUsers?.length">
          <span class="font-normal text-theme-text-secondary"> {{ t('post.with') }} </span>
          <span class="font-medium">{{ taggedUsers.map((u) => u.name).join(', ') }}</span>
        </template>

        <template v-if="selectedFeeling">
          <span class="font-normal text-theme-text-secondary"> {{ t('post.feelingWith') }} </span>
          <button @click="emit('openFeelingSelector')" class="font-bold hover:underline">
            {{ selectedFeeling.label }} {{ selectedFeeling.emoji }}
          </button>
        </template>

        <template v-if="selectedActivity">
          <button
            @click="emit('openFeelingSelector')"
            class="font-normal text-theme-text-secondary"
          >
            - {{ selectedActivity.parent?.slice(0, -3) || '' }}
            <span class="font-semibold hover:underline">{{ selectedActivity.item.label }}</span>
            {{ selectedActivity.item.emoji }}
          </button>
        </template>

        <template v-if="selectedLocation">
          <span class="font-normal text-theme-text-secondary">{{ t('post.isAt') }}</span>
          <span class="font-semibold hover:underline cursor-pointer">
            {{ selectedLocation.title }}
          </span>
        </template>
      </div>

      <div
        v-if="!isAnonymous"
        class="flex items-center bg-theme-bg-tertiary px-2 py-1 rounded-md text-xs font-semibold text-theme-text w-fit cursor-pointer hover:bg-theme-bg-hover transition-colors"
        @click="emit('navigatePrivacy')"
      >
        <component v-if="privacyInfo.icon" :is="privacyInfo.icon" :size="12" class="mr-1" />
        <span>{{ privacyInfo.label }}</span>
        <ChevronDownIcon :size="12" class="ml-1" />
      </div>
    </div>
  </div>
</template>
