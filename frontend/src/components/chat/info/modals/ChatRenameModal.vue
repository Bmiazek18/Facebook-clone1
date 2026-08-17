<!-- components/chat/modals/ChatRenameModal.vue -->
<template>
  <BaseModal title="Zmień nazwę czatu" @close="emit('close')">
    <div class="px-4 py-3">
      <label class="block text-sm font-medium text-gray-700 mb-2">Nazwa czatu</label>
      <input
        v-model="renameInput"
        type="text"
        class="w-full px-3 py-2 border border-gray-200 rounded-md focus:outline-none focus:ring-1 focus:ring-indigo-500"
      />
      <div class="mt-3 flex justify-end space-x-2">
        <button @click="emit('close')" class="px-3 py-2 rounded-md bg-gray-100 hover:bg-gray-200">
          Anuluj
        </button>
        <button @click="handleSave" class="px-3 py-2 rounded-md bg-indigo-600 text-white hover:bg-indigo-700">
          Zapisz
        </button>
      </div>
    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'

const props = defineProps<{ initialName: string }>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', newName: string): void
}>()

const renameInput = ref(props.initialName)

function handleSave() {
  const trimmed = renameInput.value.trim()
  if (trimmed) emit('save', trimmed)
  emit('close')
}
</script>
