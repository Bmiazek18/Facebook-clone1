<script setup lang="ts">
type ImageTag = {
  id: string;
  x: number;
  y: number;
  name: string;
};

defineProps<{
  tag: ImageTag;
}>();
</script>

<template>
  <div
    class="flex flex-col items-center transform -translate-x-1/2 -translate-y-full mt-[-1px] filter drop-shadow-md"
  >
    <div
      class="bg-white text-black px-3 py-1.5 rounded text-xs font-semibold whitespace-nowrap"
    >
      {{ tag.name }}
    </div>

    <div
      class="w-0 h-0 border-l-[6px] border-l-transparent border-r-[6px] border-r-transparent border-t-[6px] border-t-white"
    ></div>
  </div>
</template>
