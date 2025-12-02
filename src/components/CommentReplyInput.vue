<script setup lang="ts">

const props = defineProps<{
    postAvatarSrc: string
    placeholder: string
}>()
</script>

<template>
    <div class="flex items-center flex-grow min-w-0">
        <!-- Current User Avatar -->
        <img class="rounded-full w-6 h-6 mr-2" :src="props.postAvatarSrc" alt="Twój Avatar">

        <!-- Input Field Container -->
        <div class="relative flex-grow min-w-0">
            <input
                type="text"
                :placeholder="props.placeholder"
                class="w-full border-none bg-gray-100 p-2 pr-12 rounded-full text-sm focus:ring-blue-500 focus:border-blue-500 dark:bg-[#333333]"
            />
            <!-- Action Buttons -->
            <div class="absolute inset-y-0 right-0 flex items-center pr-2 space-x-1">
                <button class="text-gray-500 hover:text-gray-700">😊</button>
                <button class="text-gray-500 hover:text-gray-700">📸</button>
            </div>
        </div>
    </div>
</template>
