<script setup lang="ts">
import TagList from './TagList.vue';
import AltTextEditor from './AltTextEditor.vue';
import type { ImageTagType } from '@/types/ImageTag';
import type { DefineComponent } from 'vue';

interface Tool {
  id: number;
  label: string;
  icon: DefineComponent;
  action?: string;
}

// --- PROPS ---
defineProps<{
  tools: Tool[];
  tags: ImageTagType[];
  isCroppingMode: boolean;
  showAltTextInput: boolean;
  altText: string;
}>();

// --- EMITS ---
const emit = defineEmits<{
  (e: 'tool-action', action: string | undefined): void;
  (e: 'remove-tag', id: string): void;
  (e: 'confirm-crop'): void;
  (e: 'cancel-crop'): void;
  (e: 'cancel-edit'): void;
  (e: 'update:altText', value: string): void;
  (e: 'done'): void;
}>();
</script>

<template>
  <aside class="w-[360px] bg-white h-full flex flex-col relative z-20 shadow-xl shrink-0">
    <div class="flex-1 flex flex-col gap-1 p-2 pt-4 overflow-y-auto">
      <template v-for="tool in tools" :key="tool.id">
        <button
          class="flex items-center gap-3 p-2 rounded-lg hover:bg-[#f0f2f5] transition-colors group text-left w-full cursor-pointer active:scale-[0.98] transition-transform duration-100"
          @click="emit('tool-action', tool.action)"
        >
          <div class="w-9 h-9 rounded-full bg-[#e4e6eb] flex items-center justify-center text-gray-900 group-hover:bg-[#d8dadf] transition-colors shrink-0">
            <component :is="tool.icon" :size="20" />
          </div>
          <span class="text-[15px] font-semibold text-[#050505]">
            {{ tool.label }}
          </span>
        </button>

                          <div v-if="tool.action === 'addTag' && tags.length > 0">
                            <TagList :tags="tags" @remove-tag="(id) => emit('remove-tag', id)" />
                          </div>
                                          <AltTextEditor
                                            v-if="tool.action === 'toggleAltText' && showAltTextInput"
                                            :alt-text="altText"
                                            @update:altText="(value) => emit('update:altText', value)"
                                            @close="emit('tool-action', 'toggleAltText')"
                                          />                        </template>
                      </div>        
              <div class="p-4 flex items-center gap-3 border-t border-gray-200 bg-white shadow-[0_-2px_4px_rgba(0,0,0,0.05)]">      <template v-if="!isCroppingMode">
        <button @click="emit('done')" class="flex-1 py-2 px-4 bg-blue-500 hover:bg-blue-600 text-white font-semibold rounded-md text-[15px] transition-colors">
          Gotowe
        </button>
        <button @click="emit('cancel-edit')" class="flex-1 py-2 px-4 bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505] font-semibold rounded-md text-[15px] transition-colors">
          Anuluj
        </button>
      </template>
      <template v-else>
        <button @click="emit('confirm-crop')" class="flex-1 py-2 px-4 bg-green-500 hover:bg-green-600 text-white font-semibold rounded-md text-[15px] transition-colors">
          Przytnij
        </button>
        <button @click="emit('cancel-crop')" class="flex-1 py-2 px-4 bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505] font-semibold rounded-md text-[15px] transition-colors">
          Anuluj
        </button>
      </template>
    </div>
  </aside>
</template>
