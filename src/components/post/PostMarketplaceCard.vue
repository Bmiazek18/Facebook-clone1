<template>
  <div class="flex">

    <div class="flex-1 p-3 flex flex-col justify-between">
      <div>
        <h3 class="font-bold text-gray-900 text-[15px] line-clamp-2">{{ marketplaceData.title }}</h3>
        <p class="font-bold text-gray-900 text-[17px] mt-1">{{ marketplaceData.price }}</p>
        <p class="text-gray-600 text-[13px] mt-0.5">{{ marketplaceData.location }}</p>
      </div>
      <p class=" mt-0.5">{{ marketplaceData.description }}</p>
    </div>

    <!-- Button on the right -->
    <div class="flex items-center pr-3">
      <button
        @click.stop="openMessenger"
        class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-md transition-colors whitespace-nowrap"
      >
        Napisz do sprzedawcy
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
interface MarketplaceData {
  title: string
  price: string
  location: string
  description: string
  itemId: string
}

const props = defineProps<{
  marketplaceData: MarketplaceData
}>()

const emit = defineEmits<{
  openMessenger: [itemId: string]
}>()

const openMessenger = () => {
  emit('openMessenger', props.marketplaceData.itemId)
}
</script>
