<script setup lang="ts">
import { computed } from 'vue';
import { Dropdown } from 'floating-vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue';
import type { FunctionalComponent } from 'vue';

interface DropdownOption {
  id: string;
  title: string;
  description: string;
  icon?: FunctionalComponent | string;
}

const props = defineProps<{
  modelValue: string;
  options: DropdownOption[];
  label: string;
  disabled?: boolean;
  iconClass?: string | object;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

const selectedOption = computed(() => {
  return props.options.find(option => option.id === props.modelValue) ?? props.options[0];
});

function selectOption(id: string) {
  if (props.disabled) return;
  emit('update:modelValue', id);
}
</script>

<template>
  <div>
    <label class="text-[15px] font-semibold mb-2 block">{{ label }}</label>
    <Dropdown class="w-full" :distance="6" :disabled="disabled" placement="bottom-start">
      <div
        class="w-full p-3 border border-theme-border rounded-lg flex items-center justify-between bg-theme-bg-secondary text-[15px] transition"
        :class="{'cursor-pointer hover:bg-theme-hover': !disabled, 'opacity-50 cursor-not-allowed': disabled}"
      >
        <div class="flex items-center gap-2">
          <component
            :is="selectedOption.icon"
            v-if="selectedOption.icon"
            :class="iconClass"
            class="text-theme-text"
            :size="20"
          />
          <span class="text-theme-text">{{ selectedOption.title }}</span>
        </div>
        <chevron-down-icon class="text-theme-text-secondary" :size="20" />
      </div>

      <template #popper>
        <div class="w-[500px] p-2 bg-theme-bg-secondary rounded-xl shadow-xl">
          <div
            v-for="option in options"
            :key="option.id"
            @click="selectOption(option.id)"
            :class="['flex items-center gap-3 p-2 rounded-lg cursor-pointer transition', modelValue === option.id ? 'bg-theme-primary-subtle text-theme-primary' : 'hover:bg-theme-hover text-theme-text']"
          >
            <div v-if="option.icon" :class="['p-2 rounded-full flex items-center justify-center', modelValue === option.id ? 'bg-theme-primary text-white' : 'bg-theme-bg-subtle text-theme-text']">
              <component :is="option.icon" :size="24" />
            </div>
            <div class="flex flex-col">
              <span class="text-[15px] font-bold">{{ option.title }}</span>
              <span :class="['text-[13px]', modelValue === option.id ? 'text-theme-primary/80' : 'text-theme-text-secondary']">{{ option.description }}</span>
            </div>
            <CheckCircleIcon v-if="modelValue === option.id" :size="20" class="text-theme-primary ml-auto" />
          </div>
        </div>
      </template>
    </Dropdown>
  </div>
</template>
