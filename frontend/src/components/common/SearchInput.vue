<template>
  <div
    class="flex items-center justify-center p-1 rounded-full h-10 transition-all bg-[#F1F2F5] dark:bg-[#333334]"
    :class="
      isFocused ? 'w-full bg-theme-hover' : 'lg:w-full lg:bg-theme-sec w-10  dark:border-none'
    "
    @click="!isFocused && emitFocus(true)"
  >
    <Magnify v-if="!isFocused" class="p-1 cursor-pointer" :size="22" fillColor="#64676B" />
    <input
      :class="isFocused ? 'block ml-3' : 'xl:block hidden'"
      class="bg-transparent p-0 text-[14px] font-normal placeholder-theme-text-secondary placeholder-[#64676B] w-full pr-3"
      :placeholder="placeholder"
      type="text"
      @focus="emitFocus(true)"
      @keyup.esc="emitFocus(false)"
      @keyup.enter="onEnter"
      v-model="innerValue"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Magnify from 'vue-material-design-icons/Magnify.vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  isFocused: { type: Boolean, default: false },
  placeholder: { type: String, default: 'Szukaj na Facebooku' },
})
const emit = defineEmits(['update:modelValue', 'update:isFocused', 'enter'])

const innerValue = computed({
  get: () => props.modelValue,
  set: (v: string) => emit('update:modelValue', v),
})

const isFocused = computed(() => props.isFocused)

function emitFocus(val: boolean) {
  emit('update:isFocused', val)
}

function onEnter() {
  emit('enter')
}
</script>

<style scoped>
/* keep styling minimal; layout controlled by parent classes */
</style>
