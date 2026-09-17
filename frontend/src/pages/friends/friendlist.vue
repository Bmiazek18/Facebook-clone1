<template>
  <div class="min-h-screen bg-theme-bg mt-[50px] flex">
    <FriendsSidebar />

    <div class="flex-1 ml-[360px] p-8">
      <div class="max-w-[1400px] mx-auto">
        <!-- WIDOK SZCZEGÓŁÓW POJEDYNCZEJ LISTY -->
        <template v-if="selectedList">
          <div class="mb-6 flex flex-col gap-4">
            <button
              @click="selectedListId = null"
              class="self-start flex items-center gap-2 text-theme-primary font-semibold text-[15px] hover:underline cursor-pointer transition-colors"
            >
              <ArrowLeftIcon :size="20" />
              <span>Wróć do wszystkich list</span>
            </button>

            <div class="bg-theme-bg-secondary p-6 rounded-2xl border border-theme-border shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div class="flex items-center gap-4">
                <div
                  class="w-14 h-14 rounded-2xl flex items-center justify-center text-white shrink-0 shadow-sm"
                  :class="getListIconBg(selectedList.icon)"
                >
                  <component :is="getListIconComponent(selectedList.icon)" :size="28" />
                </div>
                <div>
                  <div class="flex items-center gap-3">
                    <h1 class="text-[24px] font-bold text-theme-text">{{ selectedList.name }}</h1>
                    <span
                      v-if="selectedList.isSystem"
                      class="bg-theme-bg-tertiary text-theme-text-secondary text-[12px] font-semibold px-2.5 py-0.5 rounded-full border border-theme-border"
                    >
                      Domyślna
                    </span>
                  </div>
                  <p class="text-[14px] text-theme-text-secondary mt-0.5 max-w-2xl">
                    {{ selectedList.description || 'Niestandardowa lista znajomych' }}
                  </p>
                </div>
              </div>

              <div class="flex items-center gap-2.5 flex-wrap">
                <button
                  @click="openAddMembersModal(selectedList.id)"
                  class="bg-[#1877f2] hover:bg-[#166fe5] text-white px-4 py-2.5 rounded-xl font-semibold text-[15px] flex items-center gap-2 shadow-sm transition-transform active:scale-95 cursor-pointer"
                >
                  <AccountPlusIcon :size="20" />
                  <span>Dodaj znajomych</span>
                </button>

                <button
                  v-if="!selectedList.isSystem"
                  @click="handleDeleteList(selectedList.id)"
                  class="bg-red-50 hover:bg-red-100 dark:bg-red-950/40 dark:hover:bg-red-900/60 text-red-600 dark:text-red-400 px-3.5 py-2.5 rounded-xl font-semibold text-[15px] flex items-center gap-1.5 transition-colors cursor-pointer"
                >
                  <DeleteOutlineIcon :size="20" />
                  <span>Usuń listę</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Wyszukiwarka wewnątrz listy -->
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
            <div>
              <h2 class="text-[18px] font-bold text-theme-text">Członkowie listy ({{ selectedListMembers.length }})</h2>
              <p class="text-[13px] text-theme-text-secondary">Osoby przypisane do tej grupy</p>
            </div>
            <div class="w-full sm:w-72">
              <SearchInput v-model="memberSearchQuery" placeholder="Szukaj w tej liście..." />
            </div>
          </div>

          <!-- Lista osób -->
          <div v-if="filteredSelectedListMembers.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div
              v-for="friend in filteredSelectedListMembers"
              :key="friend.id"
              class="bg-theme-bg-secondary p-4 rounded-xl border border-theme-border shadow-sm flex items-center justify-between gap-3 group hover:border-theme-border-strong transition-all"
            >
              <div class="flex items-center gap-3 min-w-0">
                <img
                  :src="friend.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(friend.name)}&background=EBF4FF&color=1877F2`"
                  class="w-12 h-12 rounded-full object-cover shrink-0 border border-theme-border"
                />
                <div class="min-w-0">
                  <NuxtLink
                    :to="`/profile/${friend.id}`"
                    class="font-semibold text-[15px] text-theme-text hover:underline block truncate"
                  >
                    {{ friend.name }}
                  </NuxtLink>
                  <span v-if="friend.city || friend.school" class="text-[12px] text-theme-text-secondary block truncate">
                    {{ friend.city || friend.school }}
                  </span>
                  <span v-else-if="friend.mutual" class="text-[12px] text-theme-text-secondary block truncate">
                    {{ friend.mutual }} wspólnych znajomych
                  </span>
                </div>
              </div>

              <button
                @click="friendListsStore.removeMemberFromList(selectedList.id, friend.id)"
                title="Usuń z tej listy"
                class="p-2 text-theme-text-secondary hover:text-red-500 hover:bg-theme-bg-tertiary rounded-lg transition-colors cursor-pointer shrink-0"
              >
                <CloseIcon :size="20" />
              </button>
            </div>
          </div>

          <!-- Pusty stan listy -->
          <div
            v-else
            class="bg-theme-bg-secondary p-12 rounded-2xl border border-theme-border text-center shadow-sm"
          >
            <div class="w-16 h-16 bg-theme-bg-tertiary text-theme-text-secondary rounded-full flex items-center justify-center mx-auto mb-3">
              <AccountGroupIcon :size="32" />
            </div>
            <h3 class="text-[18px] font-bold text-theme-text mb-1">Brak osób na tej liście</h3>
            <p class="text-[14px] text-theme-text-secondary max-w-md mx-auto mb-6">
              Dodaj znajomych do listy „{{ selectedList.name }}”, aby móc z łatwością kontrolować widoczność publikacji.
            </p>
            <button
              @click="openAddMembersModal(selectedList.id)"
              class="bg-[#1877f2] hover:bg-[#166fe5] text-white px-5 py-2.5 rounded-xl font-semibold text-[15px] inline-flex items-center gap-2 shadow-sm transition-transform active:scale-95 cursor-pointer"
            >
              <AccountPlusIcon :size="20" />
              <span>Dodaj znajomych do listy</span>
            </button>
          </div>
        </template>

        <!-- WIDOK GŁÓWNY: WSZYSTKIE LISTY NIESTANDARDOWE -->
        <template v-else>
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
            <div>
              <h1 class="text-[26px] font-black text-theme-text tracking-tight">Listy niestandardowe</h1>
              <p class="text-[14px] text-theme-text-secondary mt-1">
                Zarządzaj grupami kontaktów, twórz dedykowane kręgi znajomych i ograniczaj dostęp do postów.
              </p>
            </div>
            <button
              @click="showCreateModal = true"
              class="bg-[#1877f2] hover:bg-[#166fe5] text-white px-5 py-2.5 rounded-xl font-bold text-[15px] flex items-center justify-center gap-2 shadow-sm transition-all active:scale-95 cursor-pointer shrink-0"
            >
              <PlusIcon :size="20" />
              <span>Utwórz listę</span>
            </button>
          </div>

          <!-- Siatka list -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div
              v-for="list in friendListsStore.lists"
              :key="list.id"
              @click="selectedListId = list.id"
              class="bg-theme-bg-secondary p-6 rounded-2xl border border-theme-border hover:border-theme-border-strong hover:shadow-md transition-all cursor-pointer flex flex-col justify-between group relative overflow-hidden"
            >
              <div>
                <div class="flex items-start justify-between gap-3 mb-4">
                  <div
                    class="w-12 h-12 rounded-xl flex items-center justify-center text-white shadow-sm shrink-0"
                    :class="getListIconBg(list.icon)"
                  >
                    <component :is="getListIconComponent(list.icon)" :size="24" />
                  </div>
                  <span
                    v-if="list.isSystem"
                    class="bg-theme-bg-tertiary text-theme-text-secondary text-[11px] font-bold px-2 py-0.5 rounded-full border border-theme-border"
                  >
                    Systemowa
                  </span>
                </div>

                <h3 class="text-[18px] font-bold text-theme-text group-hover:text-[#1877f2] transition-colors mb-1.5">
                  {{ list.name }}
                </h3>
                <p class="text-[13px] text-theme-text-secondary leading-relaxed line-clamp-2 mb-4">
                  {{ list.description || 'Niestandardowa lista znajomych' }}
                </p>
              </div>

              <div class="pt-4 border-t border-theme-border flex items-center justify-between">
                <div class="flex items-center gap-1.5 text-[13px] font-semibold text-theme-text-secondary">
                  <AccountGroupIcon :size="18" />
                  <span>{{ list.memberIds.length }} {{ list.memberIds.length === 1 ? 'osoba' : 'osób' }}</span>
                </div>
                <span class="text-[14px] font-bold text-[#1877f2] flex items-center gap-1 group-hover:translate-x-0.5 transition-transform">
                  Zarządzaj →
                </span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- MODAL: UTWÓRZ NOWĄ LISTĘ -->
    <BaseModal
      v-if="showCreateModal"
      title="Utwórz nową listę"
      @close="closeCreateModal"
    >
      <div class="p-6 max-w-lg mx-auto">
        <div class="mb-5">
          <label class="block text-[14px] font-bold text-theme-text mb-1.5">Nazwa listy</label>
          <input
            type="text"
            v-model="newListName"
            placeholder="np. Praca, Rodzina, Studia..."
            class="w-full bg-theme-bg-tertiary border border-theme-border rounded-xl px-4 py-2.5 text-theme-text text-[15px] focus:outline-none focus:ring-2 focus:ring-[#1877f2]"
            autofocus
            @keyup.enter="handleCreateList"
          />
        </div>

        <div class="mb-6">
          <label class="block text-[14px] font-bold text-theme-text mb-1.5">Dodaj znajomych (opcjonalnie)</label>
          <SearchInput v-model="createModalSearch" placeholder="Wyszukaj znajomych..." class="mb-3" />

          <div class="max-h-60 overflow-y-auto space-y-1.5 pr-1 border border-theme-border rounded-xl p-2 bg-theme-bg-tertiary">
            <div
              v-for="friend in filteredAllFriendsForCreate"
              :key="friend.id"
              @click="toggleSelectedForCreate(friend.id)"
              class="flex items-center justify-between p-2 rounded-lg hover:bg-theme-bg-secondary cursor-pointer transition-colors"
            >
              <div class="flex items-center gap-2.5 min-w-0">
                <img
                  :src="friend.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(friend.name)}&background=EBF4FF&color=1877F2`"
                  class="w-8 h-8 rounded-full object-cover shrink-0 border border-theme-border"
                />
                <span class="text-[14px] font-semibold text-theme-text truncate">{{ friend.name }}</span>
              </div>
              <input
                type="checkbox"
                :checked="selectedMemberIdsForCreate.includes(friend.id)"
                class="w-4 h-4 rounded text-[#1877f2] focus:ring-0 cursor-pointer pointer-events-none"
              />
            </div>
            <div v-if="filteredAllFriendsForCreate.length === 0" class="text-center py-4 text-[13px] text-theme-text-secondary">
              Brak znajomych
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-3">
          <button
            @click="closeCreateModal"
            class="px-4 py-2 rounded-xl text-theme-text hover:bg-theme-bg-tertiary font-semibold text-[15px] cursor-pointer transition-colors"
          >
            Anuluj
          </button>
          <button
            @click="handleCreateList"
            :disabled="!newListName.trim()"
            class="bg-[#1877f2] hover:bg-[#166fe5] disabled:opacity-50 disabled:cursor-not-allowed text-white px-5 py-2 rounded-xl font-bold text-[15px] shadow-sm transition-transform active:scale-95 cursor-pointer"
          >
            Utwórz
          </button>
        </div>
      </div>
    </BaseModal>

    <!-- MODAL: DODAJ / USUŃ ZNAJOMYCH Z LISTY -->
    <BaseModal
      v-if="showAddMembersModal && activeListForModal"
      :title="`Zarządzaj listą: ${activeListForModal.name}`"
      @close="showAddMembersModal = false"
    >
      <div class="p-6 max-w-lg mx-auto">
        <p class="text-[13px] text-theme-text-secondary mb-3">
          Zaznacz znajomych, którzy mają należeć do tej listy.
        </p>

        <SearchInput v-model="modalSearchQuery" placeholder="Wyszukaj znajomych..." class="mb-4" />

        <div class="max-h-80 overflow-y-auto space-y-1.5 pr-1 border border-theme-border rounded-xl p-2 bg-theme-bg-tertiary mb-5">
          <div
            v-for="friend in filteredAllFriendsForManage"
            :key="friend.id"
            @click="friendListsStore.toggleMemberInList(activeListForModal.id, friend.id)"
            class="flex items-center justify-between p-2.5 rounded-lg hover:bg-theme-bg-secondary cursor-pointer transition-colors"
          >
            <div class="flex items-center gap-3 min-w-0">
              <img
                :src="friend.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(friend.name)}&background=EBF4FF&color=1877F2`"
                class="w-9 h-9 rounded-full object-cover shrink-0 border border-theme-border"
              />
              <div class="min-w-0">
                <span class="text-[14px] font-semibold text-theme-text block truncate">{{ friend.name }}</span>
                <span v-if="friend.city" class="text-[12px] text-theme-text-secondary block truncate">{{ friend.city }}</span>
              </div>
            </div>

            <input
              type="checkbox"
              :checked="friendListsStore.isMemberOf(activeListForModal.id, friend.id)"
              class="w-5 h-5 rounded text-[#1877f2] focus:ring-0 cursor-pointer pointer-events-none"
            />
          </div>
          <div v-if="filteredAllFriendsForManage.length === 0" class="text-center py-6 text-[13px] text-theme-text-secondary">
            Brak pasujących znajomych
          </div>
        </div>

        <div class="flex justify-end">
          <button
            @click="showAddMembersModal = false"
            class="bg-[#1877f2] hover:bg-[#166fe5] text-white px-5 py-2 rounded-xl font-bold text-[15px] shadow-sm transition-transform active:scale-95 cursor-pointer"
          >
            Gotowe
          </button>
        </div>
      </div>
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useFriendListsStore } from '@/stores/friendLists'
import { usersApi } from '@/api/users'

import FriendsSidebar from '@/components/friends/FriendsSidebar.vue'
import SearchInput from '@/components/common/SearchInput.vue'
import BaseModal from '@/components/common/BaseModal.vue'

// Icons
import PlusIcon from 'vue-material-design-icons/Plus.vue'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue'
import AccountLockIcon from 'vue-material-design-icons/AccountLock.vue'
import FormatListBulletedIcon from 'vue-material-design-icons/FormatListBulleted.vue'
import DeleteOutlineIcon from 'vue-material-design-icons/DeleteOutline.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'

definePageMeta({
  title: 'Listy niestandardowe | Facebook',
})

const authStore = useAuthStore()
const friendListsStore = useFriendListsStore()

const allFriends = ref<any[]>([])
const selectedListId = ref<string | null>(null)
const memberSearchQuery = ref('')

// Modal tworzenia listy
const showCreateModal = ref(false)
const newListName = ref('')
const createModalSearch = ref('')
const selectedMemberIdsForCreate = ref<string[]>([])

// Modal dodawania członków
const showAddMembersModal = ref(false)
const targetListIdForModal = ref<string | null>(null)
const modalSearchQuery = ref('')

const selectedList = computed(() => {
  if (!selectedListId.value) return null
  return friendListsStore.getListById(selectedListId.value)
})

const activeListForModal = computed(() => {
  if (!targetListIdForModal.value) return null
  return friendListsStore.getListById(targetListIdForModal.value)
})

const selectedListMembers = computed(() => {
  if (!selectedList.value) return []
  const ids = new Set(selectedList.value.memberIds)
  return allFriends.value.filter((f) => ids.has(String(f.id)))
})

const filteredSelectedListMembers = computed(() => {
  let list = selectedListMembers.value
  if (memberSearchQuery.value.trim()) {
    const q = memberSearchQuery.value.toLowerCase()
    list = list.filter((f) => f.name.toLowerCase().includes(q))
  }
  return list
})

const filteredAllFriendsForCreate = computed(() => {
  let list = allFriends.value
  if (createModalSearch.value.trim()) {
    const q = createModalSearch.value.toLowerCase()
    list = list.filter((f) => f.name.toLowerCase().includes(q))
  }
  return list
})

const filteredAllFriendsForManage = computed(() => {
  let list = allFriends.value
  if (modalSearchQuery.value.trim()) {
    const q = modalSearchQuery.value.toLowerCase()
    list = list.filter((f) => f.name.toLowerCase().includes(q))
  }
  return list
})

const getListIconComponent = (icon?: string) => {
  switch (icon) {
    case 'star': return AccountStarIcon
    case 'lock': return AccountLockIcon
    case 'group': return AccountGroupIcon
    default: return FormatListBulletedIcon
  }
}

const getListIconBg = (icon?: string) => {
  switch (icon) {
    case 'star': return 'bg-gradient-to-br from-amber-400 to-orange-500'
    case 'lock': return 'bg-gradient-to-br from-rose-500 to-red-600'
    case 'group': return 'bg-gradient-to-br from-blue-500 to-indigo-600'
    default: return 'bg-gradient-to-br from-emerald-500 to-teal-600'
  }
}

const fetchAllFriends = async () => {
  if (!authStore.currentUserId) return
  try {
    const list = await usersApi.getFriends(String(authStore.currentUserId), 'ALL')
    if (list && Array.isArray(list)) {
      allFriends.value = list.map((f: any) => ({
        id: String(f.id),
        name: [f.firstName, f.lastName].filter(Boolean).join(' ') || 'Użytkownik',
        avatar: f.avatar || '',
        city: f.city || f.location || '',
        school: f.highSchool || f.school || '',
        mutual: f.mutualCount ?? 0,
      }))
    }
  } catch (err) {
    console.error('Failed to load friends for custom lists:', err)
  }
}

const openAddMembersModal = (listId: string) => {
  targetListIdForModal.value = listId
  modalSearchQuery.value = ''
  showAddMembersModal.value = true
}

const closeCreateModal = () => {
  showCreateModal.value = false
  newListName.value = ''
  createModalSearch.value = ''
  selectedMemberIdsForCreate.value = []
}

const toggleSelectedForCreate = (id: string) => {
  if (selectedMemberIdsForCreate.value.includes(id)) {
    selectedMemberIdsForCreate.value = selectedMemberIdsForCreate.value.filter((i) => i !== id)
  } else {
    selectedMemberIdsForCreate.value.push(id)
  }
}

const handleCreateList = () => {
  if (!newListName.value.trim()) return
  const created = friendListsStore.createList(newListName.value, selectedMemberIdsForCreate.value)
  closeCreateModal()
  selectedListId.value = created.id
}

const handleDeleteList = (listId: string) => {
  if (confirm('Czy na pewno chcesz usunąć tę listę znajomych?')) {
    friendListsStore.deleteList(listId)
    selectedListId.value = null
  }
}

onMounted(() => {
  friendListsStore.loadLists()
  fetchAllFriends()
})
</script>
