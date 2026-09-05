<script setup lang="ts">
import { ref, computed, inject, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'nuxt/app'
import { useI18n } from 'vue-i18n'
import FriendsSection from '@/components/friends/FriendsSection.vue'
import { usersApi } from '@/api/users'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const profileUser: any = inject('profileUser')
const isOwner: any = inject('isOwner', ref(false))

const hasWork = computed(() => {
  if (!profileUser?.value) return false
  return !!(profileUser.value.job || profileUser.value.company)
})

const hasEducation = computed(() => {
  if (!profileUser?.value) return false
  return !!(profileUser.value.school || profileUser.value.education || profileUser.value.highSchool)
})

const hasPersonalDetails = computed(() => {
  if (!profileUser?.value) return false
  return !!(
    profileUser.value.phone ||
    profileUser.value.email ||
    profileUser.value.website ||
    profileUser.value.gender ||
    profileUser.value.birthDate ||
    profileUser.value.relationshipStatus ||
    profileUser.value.location ||
    profileUser.value.hometown
  )
})

const activeTab = computed(() => {
  const segments = route.path.replace(/^\//, '').split('/')
  const tabSegment = route.params.userId ? segments[3] : segments[2]
  return tabSegment || 'overview'
})

const menuItems = computed(() =>
  [
    { id: 'directory_intro', label: 'profile.info.overview', visible: true },
    {
      id: 'directory_personal_details',
      label: 'profile.info.directory_personal_details',
      visible: isOwner.value || hasPersonalDetails.value,
    },
    { id: 'directory_work', label: 'profile.info.work', visible: isOwner.value || hasWork.value },
    {
      id: 'directory_education',
      label: 'profile.info.education',
      visible: isOwner.value || hasEducation.value,
    },
    { id: 'directory_activites', label: 'profile.info.hobby', visible: true },
  ].filter((item) => item.visible),
)

function setActiveTab(tabKey: string) {
  const userId = route.params.userId
  const basePath = userId ? `/profile/${userId}/info` : '/profile/info'
  router.push(`${basePath}/${tabKey}`)
}

const friendsList = ref<any[]>([])
const isLoadingFriends = ref(false)

const loadFriends = async () => {
  const targetUserId = (profileUser?.value?.id || route.params.userId) as string
  if (!targetUserId) return
  isLoadingFriends.value = true
  try {
    const list = await usersApi.getFriends(targetUserId)
    if (list && Array.isArray(list) && list.length > 0) {
      friendsList.value = list.map((f: any) => ({
        id: f.id,
        name: [f.firstName, f.lastName].filter(Boolean).join(' ') || 'Użytkownik',
        avatar: f.avatar || '',
        birthDate: f.birthDate || '',
        city: f.city || '',
        location: f.location || '',
        hometown: f.hometown || '',
        school: f.school || '',
        highSchool: f.highSchool || '',
        work: f.work || f.job || '',
        isFriend: true,
        mutual: f.mutualCount ?? 0,
        imageId: 35,
      }))
    } else {
      friendsList.value = []
    }
  } catch (err) {
    console.error('Failed to load friends in ProfileInfoTab:', err)
    friendsList.value = []
  } finally {
    isLoadingFriends.value = false
  }
}

onMounted(() => {
  loadFriends()
})

watch(() => profileUser?.value?.id || route.params.userId, () => {
  loadFriends()
})
</script>

<template>
  <div class="w-full mt-4 overflow-auto">
    <div
      v-if="profileUser"
      class="flex bg-theme-bg-secondary px-[6px] py-[16px] rounded-lg shadow-lg min-h-100"
    >
      <div class="w-1/4 border-r border-gray-200">
        <h2 class="text-xl font-semibold mb-4 text-theme-text ml-2">
          {{ $t('profile.info.about') }}
        </h2>
        <ul class="space-y-1 text-theme-text-secondary">
          <li
            v-for="item in menuItems"
            :key="item.id"
            @click="setActiveTab(item.id)"
            class="p-2 rounded-lg cursor-pointer transition-colors text-[15px]"
            :class="activeTab === item.id ? 'bg-blue-50 text-blue-600 ' : 'hover:bg-gray-100'"
          >
            {{
              item.id === 'details'
                ? t('profile.info.details_about', { name: profileUser.name.split(' ')[0] })
                : t(item.label)
            }}
          </li>
        </ul>
      </div>

      <div class="w-3/4 pl-[16px] pb-[32px] pr-[32px] text-theme-text-secondary">
        <NuxtPage :profile-user="profileUser" />
      </div>
    </div>

    <div v-else class="p-8 text-center text-gray-500 bg-theme-bg-secondary rounded-lg shadow-lg">
      {{ $t('profile.info.userNotFound') }}
    </div>

    <FriendsSection
      :friends-list="friendsList"
      :is-full-view="false"
      class="mt-4 border-none shadow-none p-0 bg-transparent"
    />
  </div>
</template>
