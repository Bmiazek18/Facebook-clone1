<script setup lang="ts">
import Earth from 'vue-material-design-icons/Earth.vue'

defineProps<{
  label: string
  modelValue: string
  placeholder?: string
}>()

defineEmits(['update:modelValue', 'save', 'cancel'])
</script>

<template>
  <div class="bg-gray-50 p-4 rounded-lg mb-4 animate-fade-in">
    <div class="mb-4">
      <div class="border-2 border-blue-500 rounded-lg px-3 py-2 bg-white">
        <label class="block text-xs text-blue-500 mb-0.5">{{ label }}</label>
        <input
          :value="modelValue"
          @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
          type="text"
          class="w-full outline-none text-gray-900 leading-tight"
          :placeholder="placeholder"
        />
      </div>
    </div>
    <div class="flex justify-between items-center border-t border-gray-300 pt-3">
      <button class="flex items-center space-x-2 bg-gray-200 hover:bg-gray-300 px-3 py-1.5 rounded-md font-semibold text-sm text-gray-700">
        <Earth class="text-lg" /> <span>Publiczne</span>
      </button>
      <div class="flex space-x-2">
        <button @click="$emit('cancel')" class="px-4 py-1.5 rounded-md font-semibold text-sm text-gray-700 hover:bg-gray-200">Anuluj</button>
        <button
          @click="$emit('save')"
          class="px-4 py-1.5 rounded-md font-semibold text-sm text-white transition"
          :class="modelValue ? 'bg-blue-600 hover:bg-blue-700' : 'bg-gray-300 cursor-not-allowed'"
        >
          Zapisz
        </button>
      </div>
    </div>
  </div>
</template>
