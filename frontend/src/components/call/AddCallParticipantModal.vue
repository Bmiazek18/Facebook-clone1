<script setup lang="ts">
import { ref, computed } from 'vue'

export interface ContactItem {
  id: string
  name: string
  avatarUrl: string
}

const props = defineProps<{
  isOpen: boolean
  contacts: ContactItem[]
  currentParticipants: string[]
  isAdding: boolean
}>()

const emit = defineEmits<{
  'close': []
  'invite': [user: ContactItem]
}>()

const searchQuery = ref('')

const availableContacts = computed(() => {
  const current = props.currentParticipants.map(id => String(id).toLowerCase())
  return props.contacts.filter(c => {
    const cleanId = String(c.id).toLowerCase()
    const matchesParticipant = current.includes(cleanId)
    const matchesQuery = !searchQuery.value || c.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    return !matchesParticipant && matchesQuery
  })
})
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-[200] bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 animate-in fade-in duration-200"
  >
    <div class="bg-[#242526] border border-zinc-700/80 rounded-2xl w-full max-w-md overflow-hidden shadow-2xl text-white">
      <!-- Modal header -->
      <div class="flex items-center justify-between p-4 border-b border-zinc-700/60">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded-full bg-[#0084FF]/20 flex items-center justify-center text-[#0084FF]">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
          </div>
          <h3 class="font-semibold text-lg">Dodaj osoby do rozmowy</h3>
        </div>
        <button
          @click="emit('close')"
          class="w-8 h-8 rounded-full bg-zinc-800 hover:bg-zinc-700 flex items-center justify-center text-zinc-400 hover:text-white transition-colors cursor-pointer"
        >
          ✕
        </button>
      </div>

      <!-- Search input -->
      <div class="p-4 pb-2">
        <div class="relative">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Wyszukaj znajomych..."
            class="w-full bg-[#3A3B3C] text-white placeholder-zinc-400 text-sm px-4 py-2.5 rounded-xl border border-transparent focus:border-[#0084FF] focus:outline-none transition-all"
          />
        </div>
        <p class="text-xs text-zinc-400 mt-2">
          Dodanie osoby natychmiast utworzy grupę i wyśle wiadomość z przyciskiem dołączenia do rozmowy.
        </p>
      </div>

      <!-- Contacts list -->
      <div class="max-h-[300px] overflow-y-auto p-4 pt-2 space-y-2 custom-scrollbar">
        <div
          v-for="user in availableContacts"
          :key="user.id"
          class="flex items-center justify-between p-2.5 hover:bg-zinc-800/80 rounded-xl transition-colors group"
        >
          <div class="flex items-center gap-3">
            <img :src="user.avatarUrl" class="w-10 h-10 rounded-full object-cover border border-zinc-700" />
            <div>
              <div class="text-sm font-medium text-white">{{ user.name }}</div>
              <div class="text-xs text-zinc-400">Dostępny do połączenia</div>
            </div>
          </div>

          <button
            @click="emit('invite', user)"
            :disabled="isAdding"
            class="bg-[#0084FF] hover:bg-[#0073E6] disabled:opacity-50 text-white font-medium text-xs px-3.5 py-1.5 rounded-lg transition-colors flex items-center gap-1.5 shadow-sm cursor-pointer"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Dodaj
          </button>
        </div>

        <div v-if="availableContacts.length === 0" class="text-center py-8 text-zinc-500 text-sm">
          Brak dostępnych kontaktów do dodania.
        </div>
      </div>
    </div>
  </div>
</template>
