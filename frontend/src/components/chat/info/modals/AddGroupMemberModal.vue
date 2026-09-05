<template>
  <BaseModal title="Dodaj uczestnika do grupy" @close="$emit('close')">
    <div class="p-4 flex flex-col font-sans">
      <p class="text-sm text-theme-text-muted mb-4">
        Wybierz osobę, którą chcesz dodać do tego czatu grupowego. Po dodaniu automatycznie prześlemy jej klucz szyfrujący, aby mogła odczytać dotychczasowe wiadomości.
      </p>

      <div class="max-h-[300px] overflow-y-auto custom-scrollbar">
        <ul class="space-y-1">
          <li
            v-for="user in availableContacts"
            :key="user.id"
            @click="selectUser(user)"
            class="flex items-center gap-3 px-3 py-2 hover:bg-theme-bg-hover rounded-lg cursor-pointer transition-colors"
          >
            <img
              :src="user.avatarUrl"
              class="w-10 h-10 rounded-full object-cover border border-theme-border"
            />
            <span class="text-sm font-medium text-theme-text">{{ user.name }}</span>
          </li>
        </ul>
        <div v-if="availableContacts.length === 0" class="text-center py-6 text-theme-text-muted text-sm">
          Brak dostępnych kontaktów do dodania.
        </div>
      </div>
    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'

const props = defineProps<{
  existingMemberIds: string[]
}>()

const emit = defineEmits(['close', 'select-user'])

const contacts = [
  {
    id: '7f23f5b8-87fb-4250-9ba9-6b5ed04afff0',
    name: 'dsd User',
    avatarUrl: 'https://i.pravatar.cc/150?img=1'
  },
  {
    id: 'd8604ec9-2999-4730-9409-d4c13a78a68e',
    name: 'E2EE Partner',
    avatarUrl: 'https://i.pravatar.cc/150?img=2'
  },
  {
    id: '0d4b14bc-1337-490f-ba79-27b62f4fdaf6',
    name: 'Bmiazek User',
    avatarUrl: 'https://i.pravatar.cc/150?img=3'
  },
  {
    id: '41da76f0-fc3e-362a-a939-e634bfb6a342',
    name: 'Piotr Kowalski',
    avatarUrl: 'https://i.pravatar.cc/150?img=4'
  },
  {
    id: '9a936f54-ceff-3813-9eba-fd21984efcf4',
    name: 'Tomasz Lewandowski',
    avatarUrl: 'https://i.pravatar.cc/150?img=5'
  }
]

const availableContacts = computed(() => {
  return contacts.filter(c => !props.existingMemberIds.includes(String(c.id)))
})

const selectUser = (user: any) => {
  emit('select-user', user)
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 10px;
}
</style>
