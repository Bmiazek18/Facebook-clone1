<script setup lang="ts">
import { ref } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'

const emit = defineEmits(['discard', 'saveDraft', 'cancel'])

const isOpen = ref(false)

const open = () => {
  isOpen.value = true
}

const close = () => {
  isOpen.value = false
}

const handleDiscard = () => {
  emit('discard')
  close()
}

const handleSaveDraft = () => {
  emit('saveDraft')
  close()
}

const handleCancel = () => {
  emit('cancel')
  close()
}

defineExpose({
  open,
  close,
})
</script>

<template>
  <BaseModal v-if="isOpen" :title="$t('common.odrzucicPost')" @close="handleCancel">
    <div class="p-6 max-w-md">
      <p class="mb-6 text-[15px] text-gray-500 leading-normal">{{ $t('common.jesliTerazOdrzuciszTen') }}</p>

      <div class="flex justify-end items-center gap-3">
        <button
          @click="handleDiscard"
          class="px-4 py-2.5 rounded-lg text-red-600 font-semibold hover:bg-red-50 transition-colors duration-200 text-[15px]"
        >{{ $t('common.usunWersjeRobocza') }}</button>

        <button
          @click="handleSaveDraft"
          class="px-5 py-2.5 rounded-lg bg-blue-600 text-white font-semibold shadow-sm hover:bg-blue-700 transition-colors duration-200 text-[15px]"
        >{{ $t('common.zapiszJakoWersjeRobocza') }}</button>
      </div>
    </div>
  </BaseModal>
</template>
