<template>
  <div class="bg-transparent flex justify-center items-start">
    <!-- Karta główna widżetu -->
    <div class="w-full bg-white dark:bg-[#242526] rounded-lg border border-gray-200 dark:border-zinc-700 shadow-sm p-4">
      <!-- Nagłówek -->
      <div class="text-[20px] font-bold text-[#050505] dark:text-[#E4E6EB] mb-3 flex items-center">
        Członkowie <span class="text-[#65676B] dark:text-[#B0B3B8] font-normal ml-1">· {{ groupDetails?.members || membersList.length }}</span>
      </div>

      <!-- Delikatna linia oddzielająca -->
      <hr class="border-[#CED0D4] dark:border-[#3E4042] mb-4" />

      <!-- Sekcja 1: Znajomi / Członkowie -->
      <div class="mb-5" v-if="membersList.length > 0">
        <div class="flex items-center -space-x-1.5 mb-2 overflow-hidden">
          <img
            v-for="member in membersList.slice(0, 10)"
            :key="'member-' + member.id"
            :src="member.avatar"
            class="w-[36px] h-[36px] rounded-full border-2 border-white dark:border-[#242526] object-cover relative z-10"
            :alt="member.name"
            :title="member.name"
          />
        </div>
        <p class="text-[15px] text-[#050505] dark:text-[#E4E6EB]">
          W grupie jest <span class="font-semibold">{{ membersList.length }} członków</span> społeczności.
        </p>
      </div>

      <!-- Sekcja 2: Administracja i moderatorzy -->
      <div class="mb-4" v-if="admins.length > 0">
        <div class="flex items-center -space-x-1.5 mb-2 overflow-hidden">
          <img
            v-for="admin in admins"
            :key="'admin-' + admin.id"
            :src="admin.avatar"
            class="w-[36px] h-[36px] rounded-full border-2 border-white dark:border-[#242526] object-cover relative z-10"
            :alt="admin.name"
            :title="admin.name"
          />
        </div>
        <p class="text-[15px] text-[#050505] dark:text-[#E4E6EB]">
          Administratorem grupy jest <span class="font-semibold">{{ admins.map(a => a.name).join(', ') }}</span>.
        </p>
      </div>
      <div class="mb-4" v-else>
        <p class="text-[15px] text-[#050505] dark:text-[#E4E6EB]">
          Brak przypisanych administratorów. Administracja i moderatorzy dbają o bezpieczeństwo w tej grupie.
        </p>
      </div>

      <!-- Przycisk "Wyświetl wszystkich" -->
      <button class="w-full mt-2 bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] text-[#050505] dark:text-[#E4E6EB] font-semibold text-[15px] py-2 rounded-md transition-colors">
        Wyświetl wszystkich
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useGroupsStore } from '@/stores/groups'
import { useUserCache } from '@/composables/shared/useUserCache'
import type { Group } from '@/types/Group'

const props = defineProps<{
  groupDetails?: Group
}>()

const groupsStore = useGroupsStore()
const { getOrFetchUser, usersCache } = useUserCache()
const rawMembers = ref<{ userId: string; role: string }[]>([])

const fetchMembers = async () => {
  if (props.groupDetails?.id) {
    const members = await groupsStore.fetchGroupMembers(props.groupDetails.id)
    rawMembers.value = members
    // Trigger fetch for user details in background
    for (const m of members) {
      getOrFetchUser(m.userId)
    }
  }
}

onMounted(() => {
  fetchMembers()
})

watch(() => props.groupDetails?.id, () => {
  fetchMembers()
})

const admins = computed(() => {
  return rawMembers.value
    .filter(m => m.role === 'ADMIN')
    .map(m => {
      const user = usersCache.value[m.userId]
      return {
        id: m.userId,
        name: user?.name || 'Administrator',
        avatar: user?.avatar || '/default-avatar.png'
      }
    })
})

const membersList = computed(() => {
  return rawMembers.value
    .map(m => {
      const user = usersCache.value[m.userId]
      return {
        id: m.userId,
        name: user?.name || 'Członek',
        avatar: user?.avatar || '/default-avatar.png'
      }
    })
})
</script>
