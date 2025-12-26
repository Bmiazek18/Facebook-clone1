<script setup lang="ts">
import { ref, computed } from 'vue';


export interface PollOption {
  id: string | number;
  text: string;
  votes: number;
  votedByMe: boolean;
  avatars?: string[];
}


interface Props {
  question: string;
  initialOptions?: PollOption[];
  allowMultiple?: boolean;
  allowAddOption?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  initialOptions: () => [],
  allowMultiple: true,
  allowAddOption: true,
});


const emit = defineEmits<{
  (e: 'update:options', options: PollOption[]): void;
  (e: 'vote', optionIds: (string | number)[]): void;
}>();

const options = ref<PollOption[]>(JSON.parse(JSON.stringify(props.initialOptions)));
const newOptionText = ref('');
const isAddingOption = ref(false);
const selectedIds = ref<Set<string | number>>(new Set());


options.value.forEach(opt => {
  if (opt.votedByMe) selectedIds.value.add(opt.id);
});



const totalVotes = computed(() => {
  return options.value.reduce((acc, opt) => acc + opt.votes, 0);
});

const toggleSelection = (id: string | number) => {
  if (selectedIds.value.has(id)) {
    selectedIds.value.delete(id);
  } else {
    if (!props.allowMultiple) {
      selectedIds.value.clear();
    }
    selectedIds.value.add(id);
  }
};

const submitVote = () => {
  options.value.forEach(opt => {
    const wasVoted = opt.votedByMe;
    const isSelected = selectedIds.value.has(opt.id);

    if (isSelected && !wasVoted) {
      opt.votes++;
      opt.votedByMe = true;
    }

    else if (!isSelected && wasVoted) {
      opt.votes--;
      opt.votedByMe = false;
    }
  });

  emit('vote', Array.from(selectedIds.value));
  emit('update:options', options.value);
};


const addNewOption = () => {
  if (!newOptionText.value.trim()) return;

  const newOption: PollOption = {
    id: Date.now(),
    text: newOptionText.value,
    votes: 0,
    votedByMe: false,
  };

  options.value.push(newOption);

  selectedIds.value.add(newOption.id);

  newOptionText.value = '';
  isAddingOption.value = false;

  emit('update:options', options.value);
};

const getPercentage = (votes: number): string => {
  if (totalVotes.value === 0) return '0%';
  return `${Math.round((votes / totalVotes.value) * 100)}%`;
};
</script>

<template>
  <div class="w-64 bg-[#F2F2F7] rounded-3xl p-4 shadow-sm font-sans select-none">

    <h2 class="text-center text-base font-bold text-black mb-3 leading-tight">
      {{ question }}
    </h2>

    <div class="flex flex-col gap-3">
      <div
        v-for="option in options"
        :key="option.id"
        @click="toggleSelection(option.id)"
        class="group cursor-pointer"
      >
        <div class="flex justify-between items-center mb-1 px-1">
          <span class="text-sm font-bold text-black tracking-wide leading-none">
            {{ option.text }}
          </span>

          <div
            class="w-5 h-5 rounded-full border-2 transition-all duration-200 flex items-center justify-center"
            :class="[
              selectedIds.has(option.id)
                ? 'border-black bg-black'
                : 'border-[#D1D1D6] bg-transparent group-hover:border-gray-400'
            ]"
          >
            <svg
              v-if="selectedIds.has(option.id)"
              xmlns="http://www.w3.org/2000/svg"
              class="h-3 w-3 text-white font-bold"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
            </svg>
          </div>
        </div>

        <div class="h-2 w-full bg-[#E5E5EA] rounded-full overflow-hidden relative">
          <div
            v-if="totalVotes > 0"
            class="h-full bg-black/20 transition-all duration-500 ease-out"
            :style="{ width: getPercentage(option.votes) }"
          ></div>
        </div>
      </div>
    </div>

    <div v-if="allowAddOption" class="mt-3 px-1">
      <div v-if="!isAddingOption" @click="isAddingOption = true" class="cursor-pointer text-gray-400 font-medium text-xs hover:text-gray-500">
        {{ $t('ui.oneMoreOption') }}
      </div>
      <div v-else class="flex items-center gap-2">
        <input
          v-model="newOptionText"
          @keyup.enter="addNewOption"
          type="text"
          :placeholder="$t('ui.option')"
          class="w-full bg-transparent border-b border-gray-300 py-0.5 text-sm font-medium focus:outline-none focus:border-black placeholder-gray-400"
          autofocus
        />
        <button
          @click="addNewOption"
          :disabled="!newOptionText"
          class="text-xs font-bold text-black disabled:opacity-30"
        >
          {{ $t('ui.ok') }}
        </button>
      </div>
    </div>

    <button
      @click="submitVote"
      class="w-full mt-4 bg-white py-2 rounded-xl shadow-sm text-black text-sm font-bold hover:bg-gray-50 active:scale-[0.98] transition-all"
    >
      {{ $t('ui.vote') }}
    </button>

  </div>
</template>
