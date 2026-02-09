<template>
  <div class="relative">
    <input
      :id="id"
      type="text"
      :value="modelValue"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      class="peer block w-full rounded-xl border border-gray-300 bg-transparent px-4 pb-2.5 pt-6 text-base text-gray-900 focus:border-blue-600 focus:outline-none focus:ring-1 focus:ring-blue-600 transition-shadow"
      placeholder=" "
    />

    <label
      :for="id"
      class="absolute left-4 top-2 z-10 origin-[0] scale-75 transform text-gray-500 duration-300 cursor-text

      /* Stan 1: Input jest pusty (placeholder widoczny) -> Etykieta na środku */
      peer-placeholder-shown:top-1/2
      peer-placeholder-shown:-translate-y-1/2
      peer-placeholder-shown:scale-100

      /* Stan 2: Focus lub Input wypełniony -> Etykieta u góry (domyślne klasy + nadpisanie) */
      peer-focus:top-2
      peer-focus:-translate-y-0
      peer-focus:scale-75
      peer-focus:text-blue-600"
    >
      {{ label }}
    </label>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  id: string;
  label: string;
  modelValue: string;
}>();

defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();
</script>
