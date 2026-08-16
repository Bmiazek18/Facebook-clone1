<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useGroupsStore } from '@/stores/groups'
import { useUserCache } from '@/composables/shared/useUserCache'
import { useAuthStore } from '@/stores/auth'
import { useApolloClient } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import { GroupRole } from '@/types/Group'

const route = useRoute()
const groupsStore = useGroupsStore()
const { getOrFetchUser } = useUserCache()
const authStore = useAuthStore()

interface GroupMemberData {
  id: string
  name: string
  avatar: string
  role: GroupRole
  joinedAt: string
  joinedDateText: string
  isFriend: boolean
}

const admins = ref<GroupMemberData[]>([])
const allMembersSorted = ref<GroupMemberData[]>([])
const totalMembersCount = ref(0)
const isLoading = ref(true)
const searchQuery = ref('')

const sentRequests = ref<Set<string>>(new Set())

const SEND_FRIEND_REQUEST = gql`
  mutation SendFriendRequest($senderId: ID!, $receiverId: ID!) {
    sendFriendRequest(senderId: $senderId, receiverId: $receiverId) {
      success
      message
    }
  }
`

const formatTimeAgo = (isoString: string) => {
  if (!isoString) return 'niedawno'
  try {
    const date = new Date(isoString)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMin = Math.floor(diffMs / 60000)
    if (diffMin < 1) return 'przed chwilą'
    if (diffMin < 60) return `${diffMin} min temu`
    const diffHours = Math.floor(diffMin / 60)
    if (diffHours < 24) return `${diffHours} godz. temu`
    const diffDays = Math.floor(diffHours / 24)
    if (diffDays === 1) return 'wczoraj'
    return `${diffDays} dni temu`
  } catch (e) {
    return 'niedawno'
  }
}

const handleSendFriendRequest = async (targetUserId: string) => {
  try {
    const result = await apolloClient.mutate({
      mutation: SEND_FRIEND_REQUEST,
      variables: {
        senderId: authStore.currentUserId,
        receiverId: targetUserId
      }
    })
    if (result.data?.sendFriendRequest?.success) {
      sentRequests.value.add(targetUserId)
    }
  } catch (e) {
    console.error('Failed to send friend request:', e)
  }
}

const loadMembers = async () => {
  isLoading.value = true
  const groupId = route.params.id as string
  if (groupId) {
    const rawMembers = await groupsStore.fetchGroupMembers(groupId)
    totalMembersCount.value = rawMembers.length

    const processed: GroupMemberData[] = []
    for (const m of rawMembers) {
      const u = await getOrFetchUser(m.userId)
      processed.push({
        id: u.id,
        name: u.name,
        avatar: u.avatar,
        role: m.role,
        joinedAt: m.joinedAt,
        joinedDateText: formatTimeAgo(m.joinedAt),
        isFriend: !!m.isFriend
      })
    }

    // Admins and moderators
    admins.value = processed.filter(u => u.role.toUpperCase() === 'ADMIN')

    // All members sorted from newest (joinedAt desc)
    allMembersSorted.value = [...processed].sort((a, b) => {
      const timeA = new Date(a.joinedAt || 0).getTime()
      const timeB = new Date(b.joinedAt || 0).getTime()
      return timeB - timeA
    })
  }
  isLoading.value = false
}

const filteredMembers = computed(() => {
  if (!searchQuery.value) return allMembersSorted.value
  const q = searchQuery.value.toLowerCase()
  return allMembersSorted.value.filter(m => m.name.toLowerCase().includes(q))
})

const isCurrentUserAdmin = computed(() => {
  return admins.value.some(a => String(a.id) === String(authStore.currentUserId))
})

const handleRemoveMember = async (targetUserId: string) => {
  const isTargetAdmin = admins.value.some(a => String(a.id) === String(targetUserId))
  if (isTargetAdmin && admins.value.length <= 1) {
    alert('Nie można usunąć jedynego administratora grupy. Mianuj najpierw innego administratora.')
    return
  }
  const groupId = route.params.id as string
  if (groupId && confirm('Czy na pewno chcesz usunąć tego członka z grupy?')) {
    const success = await groupsStore.removeGroupMember(groupId, targetUserId)
    if (success) {
      loadMembers()
    }
  }
}

const handleUpdateRole = async (targetUserId: string, newRole: GroupRole) => {
  if (newRole !== GroupRole.ADMIN && admins.value.length <= 1 && admins.value.some(a => String(a.id) === String(targetUserId))) {
    alert('Nie można odebrać uprawnień jedynemu administratorowi grupy. Mianuj najpierw innego administratora.')
    return
  }
  const groupId = route.params.id as string
  if (groupId) {
    const success = await groupsStore.updateGroupMemberRole(groupId, targetUserId, newRole)
    if (success) {
      loadMembers()
    }
  }
}

onMounted(() => {
  loadMembers()
})
</script>

<template>
  <div class="min-h-screen bg-transparent p-4 flex justify-center">
    <!-- Główny kontener wzorowany na karcie z Facebooka -->
    <div class="w-full max-w-[680px] bg-white dark:bg-[#242526] rounded-lg shadow-sm p-4">

      <!-- Nagłówek i wyszukiwarka -->
      <section class="mb-4">
        <div class="flex items-center text-[20px] font-bold text-[#050505] dark:text-[#E4E6EB] mb-1">
          <h2>Członkowie</h2>
          <span class="text-[#65676B] dark:text-[#B0B3B8] font-normal ml-1">· {{ totalMembersCount }}</span>
        </div>
        <p class="text-[15px] text-[#65676B] dark:text-[#B0B3B8] mb-4">
          Tutaj będą widoczne nowe osoby i strony, które dołączą do grupy.
          <a href="#" class="text-[#050505] dark:text-[#E4E6EB] font-semibold hover:underline">Dowiedz się więcej</a>
        </p>

        <div class="relative flex items-center bg-[#F0F2F5] dark:bg-[#3A3B3C] rounded-full px-3 py-2">
          <svg class="w-[18px] h-[18px] text-[#65676B] dark:text-[#B0B3B8] ml-1" fill="currentColor" viewBox="0 0 24 24">
            <path d="M10 2.5a7.5 7.5 0 0 1 5.96 12.04l4.74 4.75a1 1 0 0 1-1.42 1.42l-4.75-4.74A7.5 7.5 0 1 1 10 2.5zm0 2a5.5 5.5 0 1 0 0 11 5.5 5.5 0 0 0 0-11z" />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Znajdź członka grupy"
            class="bg-transparent border-none outline-none w-full ml-2 text-[15px] text-[#050505] dark:text-[#E4E6EB] placeholder-[#65676B] dark:placeholder-[#B0B3B8]"
          />
        </div>
      </section>

      <!-- Stan ładowania - SKELETON -->
      <div v-if="isLoading">
        <hr class="my-4 border-[#CED0D4] dark:border-[#3E4042]" />

        <!-- Skeleton: Administratorzy -->
        <section class="mb-4">
          <div class="flex items-center mb-5">
            <div class="h-[20px] w-48 bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded animate-pulse"></div>
          </div>

          <div class="space-y-4">
            <!-- 2 elementy administratorów jako placeholder -->
            <div v-for="i in 2" :key="'admin-skel-'+i" class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-[60px] h-[60px] rounded-full bg-[#E4E6EB] dark:bg-[#3A3B3C] animate-pulse flex-shrink-0"></div>
                <div class="flex flex-col gap-2.5 w-32 md:w-48">
                  <div class="h-3.5 bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded animate-pulse w-3/4"></div>
                  <div class="h-4 bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded animate-pulse w-24"></div>
                  <div class="h-3 bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded animate-pulse w-full"></div>
                </div>
              </div>
              <div class="w-[140px] h-[36px] bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded-md animate-pulse hidden sm:block"></div>
            </div>
          </div>

          <div class="w-full mt-4 h-9 bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded-md animate-pulse"></div>
        </section>

        <hr class="my-4 border-[#CED0D4] dark:border-[#3E4042]" />

        <!-- Skeleton: Nowi członkowie -->
        <section>
          <div class="mb-5">
            <div class="h-[20px] w-40 bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded animate-pulse mb-3"></div>
            <div class="h-3 w-full max-w-md bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded animate-pulse mb-2"></div>
            <div class="h-3 w-5/6 max-w-sm bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded animate-pulse"></div>
          </div>

          <div class="space-y-4">
            <!-- 3 elementy członków jako placeholder -->
            <div v-for="i in 3" :key="'member-skel-'+i" class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-[60px] h-[60px] rounded-full bg-[#E4E6EB] dark:bg-[#3A3B3C] animate-pulse flex-shrink-0"></div>
                <div class="flex flex-col gap-2.5 w-32 md:w-48">
                  <div class="h-3.5 bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded animate-pulse w-4/5"></div>
                  <div class="h-3 bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded animate-pulse w-3/5"></div>
                </div>
              </div>
              <div class="w-[120px] h-[36px] bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded-md animate-pulse hidden sm:block"></div>
            </div>
          </div>

          <div class="w-full mt-4 h-9 bg-[#E4E6EB] dark:bg-[#3A3B3C] rounded-md animate-pulse"></div>
        </section>
      </div>

      <!-- Załadowana treść -->
      <div v-else>
        <!-- Linia oddzielająca -->
        <hr class="my-4 border-[#CED0D4] dark:border-[#3E4042]" />

        <!-- Administratorzy i moderatorzy -->
        <section v-if="admins.length > 0" class="mb-4">
          <div class="flex items-center text-[17px] font-bold text-[#050505] dark:text-[#E4E6EB] mb-4">
            <h3>Administratorzy i moderatorzy:</h3>
            <span class="text-[#65676B] dark:text-[#B0B3B8] font-normal ml-1">· {{ admins.length }}</span>
          </div>

          <div class="space-y-4">
            <div v-for="admin in admins" :key="admin.id" class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <img :src="admin.avatar" class="w-[60px] h-[60px] rounded-full object-cover border border-gray-100 dark:border-zinc-700" />
                <div class="flex flex-col justify-center">
                  <div class="font-semibold text-[15px] text-[#050505] dark:text-[#E4E6EB] hover:underline cursor-pointer leading-tight">
                    {{ admin.name }}
                  </div>
                  <div class="flex flex-wrap items-center gap-1.5 mt-1">
                    <span class="bg-[#E7F3FF] dark:bg-[#263951] text-[#1877F2] dark:text-[#2D88FF] text-[13px] px-1.5 py-0.5 rounded font-medium flex items-center">
                      Administrator
                    </span>
                  </div>
                  <!-- Przykładowy tekst opisu (np. uczelnia, miasto) -->
                  <div class="text-[13px] text-[#65676B] dark:text-[#B0B3B8] mt-1 leading-tight">
                    Dołączył(a) {{ admin.joinedDateText }}
                  </div>
                </div>
              </div>

              <div v-if="admin.id !== authStore.currentUserId" class="flex items-center gap-2">
                <!-- Admin controls -->
                <template v-if="isCurrentUserAdmin">
                  <button
                    @click="handleUpdateRole(admin.id, GroupRole.MEMBER)"
                    class="bg-gray-200 dark:bg-zinc-700 hover:bg-gray-300 dark:hover:bg-zinc-600 text-[#050505] dark:text-[#E4E6EB] px-2.5 py-1.5 rounded-md font-semibold text-[13px] transition flex items-center gap-1 cursor-pointer"
                    title="Odbierz uprawnienia administratora"
                  >
                    Odbierz admina
                  </button>
                  <button
                    @click="handleRemoveMember(admin.id)"
                    class="bg-red-50 dark:bg-red-950/40 hover:bg-red-100 dark:hover:bg-red-900/60 text-red-600 dark:text-red-400 px-2.5 py-1.5 rounded-md font-semibold text-[13px] transition flex items-center gap-1 cursor-pointer"
                    title="Usuń członka z grupy"
                  >
                    <svg class="w-3.5 h-3.5 fill-currentColor" viewBox="0 0 24 24">
                      <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                    </svg>
                    Usuń
                  </button>
                </template>

                <button
                  v-if="admin.isFriend"
                  disabled
                  class="bg-gray-100 dark:bg-zinc-800 text-gray-400 dark:text-zinc-500 px-3 py-1.5 rounded-md font-semibold text-[15px] flex items-center gap-1.5 cursor-not-allowed"
                >
                  <svg class="w-4 h-4 fill-currentColor" viewBox="0 0 24 24">
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                  </svg>
                  Znajomy
                </button>
                <button
                  v-else-if="sentRequests.has(admin.id)"
                  disabled
                  class="bg-[#E7F3FF] dark:bg-[#263951] text-[#1877F2] dark:text-[#2D88FF] px-3 py-1.5 rounded-md font-semibold text-[15px] flex items-center gap-1.5 cursor-not-allowed"
                >
                  Wysłano zaproszenie
                </button>
                <button
                  v-else
                  @click="handleSendFriendRequest(admin.id)"
                  class="bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] text-[#050505] dark:text-[#E4E6EB] px-3 py-1.5 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5 cursor-pointer"
                >
                  <svg class="w-4 h-4 fill-currentColor" viewBox="0 0 24 24">
                    <path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                  </svg>
                  Dodaj znajomego
                </button>
              </div>
            </div>
          </div>

          <button class="w-full mt-4 bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] text-[#050505] dark:text-[#E4E6EB] font-semibold py-2 rounded-md text-[15px] transition">
            Wyświetl wszystkich
          </button>
        </section>

        <!-- Linia oddzielająca -->
        <hr class="my-4 border-[#CED0D4] dark:border-[#3E4042]" />

        <!-- Nowi członkowie grupy -->
        <section>
          <div class="mb-4">
            <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#E4E6EB]">Nowi członkowie grupy</h3>
            <p class="text-[13px] text-[#65676B] dark:text-[#B0B3B8] mt-1 leading-snug">
              Ta lista obejmuje osoby, które dołączyły do grupy, a także osoby wyświetlające podgląd tej grupy. Każda zaproszona i zatwierdzona osoba może wyświetlać podgląd zawartości grupy.
            </p>
          </div>

          <div v-if="filteredMembers.length === 0" class="text-center py-6 text-[#65676B] dark:text-[#B0B3B8] text-[15px]">
            Brak członków pasujących do wyszukiwania.
          </div>

          <div v-else class="space-y-4">
            <div v-for="(member, index) in filteredMembers" :key="member.id" class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <img :src="member.avatar" class="w-[60px] h-[60px] rounded-full object-cover border border-gray-100 dark:border-zinc-700" />
                <div class="flex flex-col justify-center">
                  <div class="font-semibold text-[15px] text-[#050505] dark:text-[#E4E6EB] hover:underline cursor-pointer leading-tight">
                    {{ member.name }}
                  </div>
                  <div class="text-[13px] text-[#65676B] dark:text-[#B0B3B8] mt-1 leading-tight">
                    Dołączenie {{ member.joinedDateText }}
                  </div>
                </div>
              </div>

              <div v-if="member.id !== authStore.currentUserId" class="flex items-center gap-2">
                <!-- Admin controls -->
                <template v-if="isCurrentUserAdmin">
                  <button
                    v-if="member.role !== GroupRole.ADMIN"
                    @click="handleUpdateRole(member.id, GroupRole.ADMIN)"
                    class="bg-[#E7F3FF] dark:bg-[#263951] hover:bg-[#DBEAFE] dark:hover:bg-[#1E293B] text-[#1877F2] dark:text-[#2D88FF] px-2.5 py-1.5 rounded-md font-semibold text-[13px] transition flex items-center gap-1 cursor-pointer"
                    title="Mianuj administratorem grupy"
                  >
                    Mianuj adminem
                  </button>
                  <button
                    v-else
                    @click="handleUpdateRole(member.id, GroupRole.MEMBER)"
                    class="bg-gray-200 dark:bg-zinc-700 hover:bg-gray-300 dark:hover:bg-zinc-600 text-[#050505] dark:text-[#E4E6EB] px-2.5 py-1.5 rounded-md font-semibold text-[13px] transition flex items-center gap-1 cursor-pointer"
                    title="Odbierz uprawnienia administratora"
                  >
                    Odbierz admina
                  </button>
                  <button
                    @click="handleRemoveMember(member.id)"
                    class="bg-red-50 dark:bg-red-950/40 hover:bg-red-100 dark:hover:bg-red-900/60 text-red-600 dark:text-red-400 px-2.5 py-1.5 rounded-md font-semibold text-[13px] transition flex items-center gap-1 cursor-pointer"
                    title="Usuń członka z grupy"
                  >
                    <svg class="w-3.5 h-3.5 fill-currentColor" viewBox="0 0 24 24">
                      <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                    </svg>
                    Usuń
                  </button>
                </template>

                <button
                  v-if="member.isFriend"
                  disabled
                  class="bg-gray-100 dark:bg-zinc-800 text-gray-400 dark:text-zinc-500 px-3 py-1.5 rounded-md font-semibold text-[15px] flex items-center gap-1.5 cursor-not-allowed"
                >
                  <svg class="w-4 h-4 fill-currentColor" viewBox="0 0 24 24">
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                  </svg>
                  Znajomy
                </button>
                <button
                  v-else-if="sentRequests.has(member.id)"
                  disabled
                  class="bg-[#E7F3FF] dark:bg-[#263951] text-[#1877F2] dark:text-[#2D88FF] px-3 py-1.5 rounded-md font-semibold text-[15px] flex items-center gap-1.5 cursor-not-allowed"
                >
                  Wysłano zaproszenie
                </button>
                <button
                  v-else
                  @click="handleSendFriendRequest(member.id)"
                  class="bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] text-[#050505] dark:text-[#E4E6EB] px-3 py-1.5 rounded-md font-semibold text-[15px] transition flex items-center gap-1.5 cursor-pointer"
                >
                  <svg class="w-4 h-4 fill-currentColor" viewBox="0 0 24 24">
                    <path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                  </svg>
                  Dodaj znajomego
                </button>
              </div>
            </div>
          </div>

          <button v-if="filteredMembers.length > 5" class="w-full mt-4 bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] text-[#050505] dark:text-[#E4E6EB] font-semibold py-2 rounded-md text-[15px] transition">
            Wyświetl wszystkich
          </button>
        </section>

      </div>
    </div>
  </div>
</template>
