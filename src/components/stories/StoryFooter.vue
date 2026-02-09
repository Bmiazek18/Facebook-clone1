<script setup lang="ts">
import { defineProps, defineEmits, ref, type PropType } from 'vue';
import type { Story } from '@/types/Story';
import ChevronUp from 'vue-material-design-icons/ChevronUp.vue';

const props = defineProps({
  isOwner: {
    type: Boolean,
    required: true,
  },
  viewersWithReactions: {
    type: Array as PropType<any[]>,
    required: true,
  },
  currentItem: {
    type: Object as PropType<Story & { type: string, src: string, user: { name: string, avatar: string }, createAt: string }>,
    required: true,
  },
  reactions: {
    type: Array as PropType<any[]>,
    required: true,
  },
  t: {
    type: Function,
    required: true,
  },
});

const messageInput = ref('');

const emit = defineEmits(['update:isPaused', 'update:showViewers']);

const onFocus = () => {
    emit('update:isPaused', true);
}

const onBlur = () => {
    emit('update:isPaused', false);
}

const onShowViewers = () => {
    emit('update:showViewers', true);
    emit('update:isPaused', true);
}

</script>
<template>
    <div class="shrink-0 w-full flex flex-col items-center justify-end gap-3 pt-4 pb-2 z-30">
        <template v-if="isOwner">
            <div class="flex items-center gap-4">
                <div
                    @click="onShowViewers"
                    class="flex flex-col items-center justify-center cursor-pointer text-white hover:opacity-80 transition"
                >
                    <ChevronUp :size="32" />
                    <span class="text-sm font-medium">{{ viewersWithReactions.length }} {{ t('createLive.viewers') }}</span>
                </div>
            </div>
        </template>
        <template v-else>
            <div class="w-full flex items-end justify-between gap-3 max-w-[650px] px-4 md:px-0">
                <div class="relative flex-1 h-[44px]">
                    <input
                        v-model="messageInput"
                        type="text"
                        :placeholder="currentItem.type === 'birthday' ? 'Złóż życzenia' : t('story.sendMessage')"
                        class="w-full h-full bg-black border-[2px] border-white rounded-full px-6 text-white placeholder-gray-300 focus:outline-none focus:border-gray-200 transition text-[16px] font-normal tracking-wide"
                        @focus="onFocus"
                        @blur="onBlur"
                    />
                </div>
                <div v-if="currentItem.type !== 'birthday'" class="flex items-center gap-2 pb-0.5 hidden sm:flex">
                    <div v-for="(reaction, idx) in reactions" :key="idx" class="cursor-pointer hover:scale-125 active:scale-95 transition-transform duration-200 origin-bottom">
                            <div v-if="reaction.type === 'icon'" class="w-[38px] h-[38px] rounded-full flex items-center justify-center shadow-lg border-2 border-transparent" :class="reaction.class">
                                <component :is="reaction.component" :size="22" />
                            </div>
                            <div v-else class="text-[36px] leading-none drop-shadow-md filter hover:brightness-110 select-none">{{ reaction.content }}</div>
                    </div>
                </div>
            </div>
        </template>
    </div>
</template>
