<script setup lang="ts">
import BaseModal from './BaseModal.vue';

const emit = defineEmits(['confirm', 'cancel']);


 defineProps<{
  title?: string;
  message?: string;
  confirmLabel?: string;
  cancelLabel?: string;

}>();


const handleConfirm = () => {
  emit('confirm');
  close();
};

const handleCancel = () => {
  emit('cancel');
  close();
};

defineExpose({
  open,
  close,
});
</script>

<template>
  <BaseModal :title="title" @close="handleCancel">
    <div class="p-6">
      <p class="mb-8 text-lg text-gray-900 leading-normal">
        Twój post nie jest jeszcze ukończony. Czy chcesz wyjść, nie kończąc?
      </p>

      <div class="flex justify-end items-center gap-4">
        <button
          @click="handleCancel"
          class="px-4 py-2 rounded-md text-blue-600 font-semibold hover:bg-blue-50 transition-colors duration-200"
        >
          Kontynuuj edycję
        </button>

        <button
          @click="handleConfirm"
          class="px-6 py-2 rounded-lg bg-blue-600 text-white font-semibold shadow-sm hover:bg-blue-700 transition-colors duration-200"
        >
          Opuść
        </button>
      </div>
    </div>
  </BaseModal>
</template>
