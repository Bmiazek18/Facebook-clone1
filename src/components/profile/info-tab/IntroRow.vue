<script setup lang="ts">
import PlusCircleOutline from 'vue-material-design-icons/PlusCircleOutline.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import ItemMenu from './ItemMenu.vue' // Zakładam, że masz ten komponent z poprzednich kroków

defineProps<{
  hasData: any
  isEditing: boolean
  addLabel: string
  editText?: string
  removeText?: string
}>()

defineEmits(['open', 'remove'])
</script>

<template>
  <div>
    <div v-if="isEditing" class="mt-2">
      <slot name="form"></slot>
    </div>

    <div v-else-if="hasData" class="flex justify-between items-start">
      <div class="flex items-center text-gray-900">
        <div class="text-gray-500 mr-3 text-2xl flex items-center">
          <slot name="icon"></slot>
        </div>
        <span>
          <slot name="content"></slot>
        </span>
      </div>

      <div class="flex items-center space-x-2 text-gray-500">
        <Earth class="text-lg" />
        <ItemMenu
          v-if="editText && removeText"
          :editText="editText"
          :removeText="removeText"
          @edit="$emit('open')"
          @remove="$emit('remove')"
        />
        <button v-else @click="$emit('open')" class="hover:bg-gray-100 p-1 rounded-full">
           <DotsHorizontal class="text-xl" />
        </button>
      </div>
    </div>

    <button
      v-else
      @click="$emit('open')"
      class="flex items-center text-blue-600 hover:underline font-medium"
    >
      <PlusCircleOutline class="mr-3 text-2xl" />
      {{ addLabel }}
    </button>
  </div>
</template>
