<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useGroupsStore } from '@/stores/groups'
import { useUserCache } from '@/composables/shared/useUserCache'

const route = useRoute()
const groupsStore = useGroupsStore()
const { getOrFetchUser } = useUserCache()

interface PendingUser {
  id: string
  name: string
  avatar: string
}

const pendingUsers = ref<PendingUser[]>([])
const isLoading = ref(true)

const loadRequests = async () => {
  isLoading.value = true
  const groupId = route.params.id as string
  if (groupId) {
    const userIds: string[] = await groupsStore.getPendingRequests(groupId)
    const users: PendingUser[] = []
    for (const id of userIds) {
      const u = await getOrFetchUser(id)
      users.push({
        id: u.id,
        name: u.name,
        avatar: u.avatar
      })
    }
    pendingUsers.value = users
  }
  isLoading.value = false
}

const handleApprove = async (userId: string) => {
  const groupId = route.params.id as string
  if (groupId) {
    const success = await groupsStore.approveGroupRequest(groupId, userId)
    if (success) {
      pendingUsers.value = pendingUsers.value.filter(u => u.id !== userId)
    }
  }
}

const handleReject = async (userId: string) => {
  const groupId = route.params.id as string
  if (groupId) {
    const success = await groupsStore.rejectGroupRequest(groupId, userId)
    if (success) {
      pendingUsers.value = pendingUsers.value.filter(u => u.id !== userId)
    }
  }
}

onMounted(() => {
  loadRequests()
})
</script>

<template>
  <div class="min-h-screen bg-theme-bg-secondary text-theme-text p-4 max-w-[680px] mx-auto border border-theme-border rounded-xl shadow-sm">
    <div class="flex items-center justify-between mb-6 pb-4 border-b border-theme-border">
      <div class="flex items-center">
        <h2 class="text-[20px] font-bold">{{ $t('groups.prosbyODolaczenie') }}</h2>
        <span class="text-[20px] font-bold text-theme-text-secondary ml-2">· {{ pendingUsers.length }}</span>
      </div>
      <button @click="loadRequests" class="text-sm font-semibold text-[#0866FF] hover:underline">{{ $t('groups.odswiez') }}</button>
    </div>

    <div v-if="isLoading" class="flex flex-col items-center justify-center py-12">
      <div class="w-8 h-8 border-4 border-[#0866FF] border-t-transparent rounded-full animate-spin"></div>
      <p class="mt-4 text-theme-text-secondary text-sm">{{ $t('groups.ladowanieProsbODolaczenie') }}</p>
    </div>

    <div v-else>
      <div v-if="pendingUsers.length === 0" class="text-center py-12">
        <div class="w-16 h-16 bg-gray-100 dark:bg-zinc-800 rounded-full flex items-center justify-center mx-auto mb-4 text-theme-text-secondary">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <h3 class="font-bold text-[17px] text-theme-text mb-1">{{ $t('groups.brakNowychProsb') }}</h3>
        <p class="text-theme-text-secondary text-sm">{{ $t('groups.wszystkieProsbyODolaczenie') }}</p>
      </div>

      <div v-else class="space-y-4">
        <div v-for="user in pendingUsers" :key="user.id" class="flex items-center justify-between p-3 bg-theme-bg border border-theme-border rounded-lg shadow-sm hover:shadow-md transition">
          <div class="flex items-center space-x-3">
            <img :src="user.avatar" class="w-12 h-12 rounded-full object-cover border border-theme-border" />
            <div>
              <span class="font-bold text-[16px] text-theme-text hover:underline cursor-pointer">
                {{ user.name }}
              </span>
              <p class="text-xs text-theme-text-secondary">{{ $t('groups.chceDolaczycDoGrupy') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <button
              @click="handleApprove(user.id)"
              class="bg-[#0866FF] hover:bg-[#0052CC] text-white px-4 py-1.5 rounded-lg text-sm font-semibold transition"
            >{{ $t('groups.zatwierdz') }}</button>
            <button
              @click="handleReject(user.id)"
              class="bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text px-4 py-1.5 rounded-lg text-sm font-semibold transition"
            >{{ $t('chat.odrzuc') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
