<template>
  <div class="relative w-full">
    <input
      ref="inputRef"
      :id="id"
      :type="type"
      :value="modelValue ?? ''"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      v-bind="$attrs"
      :class="inputClasses"
      placeholder=" "
    />

    <label :for="id" :class="labelClasses">
      {{ label }}
    </label>

    <div :class="['absolute top-1/2 -translate-y-1/2 flex items-center gap-2 text-[#606770]', variant === 'new' ? 'right-4' : 'right-3']">
      <slot name="icon"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

defineOptions({ inheritAttrs: false });

const props = withDefaults(defineProps<{
  id: string;
  label: string;
  modelValue: string | undefined;
  type?: string;
  error?: boolean;
  variant?: 'classic' | 'new';
  disableFocusColor?: boolean; // <--- NOWY PROP
}>(), {
  type: 'text',
  error: false,
  variant: 'new',
  disableFocusColor: false // <--- Domyślnie false (czyli standardowo robi się niebieski)
});

defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

// Eksponujemy referencję do inputa, aby główny komponent mógł wywołać na nim .focus()
const inputRef = ref<HTMLInputElement | null>(null);
defineExpose({ inputRef });

// Dynamiczne klasy dla Inputa
const inputClasses = computed(() => {
  const base = 'peer block w-full border text-[15px] text-[#1c1e21] focus:outline-none transition-all placeholder-transparent';

  if (props.variant === 'new') {
    const shape = 'bg-transparent rounded-xl px-4 pb-2.5 pt-6';

    // Logika koloru w zależności od nowego propa disableFocusColor
    const focusClass = props.disableFocusColor
      ? 'focus:border-[#ccd0d5] focus:ring-0'
      : 'focus:border-[#1877f2] focus:ring-1 focus:ring-[#1877f2]';

    const state = props.error
      ? 'border-[#d32f2f] focus:border-[#d32f2f] focus:ring-1 focus:ring-[#d32f2f]'
      : `border-[#ccd0d5] ${focusClass}`;

    return [base, shape, state];
  } else {
    // Wariant Classic
    const shape = 'bg-[#f5f6f7] rounded-[6px] px-3 pb-2 pt-6 focus:bg-white focus:ring-2';

    const focusClass = props.disableFocusColor
      ? 'focus:border-[#ccd0d5] focus:ring-0'
      : 'focus:border-[#1877f2] focus:ring-[#1877f2]/20';

    const state = props.error
      ? 'border-[#b0281c] focus:ring-[#b0281c]/20'
      : `border-[#ccd0d5] ${focusClass}`;

    return [base, shape, state];
  }
});

// Dynamiczne klasy dla Etykiety (Label)
const labelClasses = computed(() => {
  // Sprawdzamy, czy input ma jakąś wartość
  const hasValue = props.modelValue && props.modelValue.length > 0;

  // Bazowe klasy bez skomplikowanych reguł peer-placeholder-shown dla pozycji pionowej
  // Zostawiamy peer-focus, bo gdy klikamy pusty input, etykieta ma od razu uciekać do góry
  const base = 'absolute z-10 origin-[0] transform duration-300 cursor-text peer-focus:top-1 peer-focus:-translate-y-0 peer-focus:scale-75';

  // Jeśli ma wartość LUB ma focus -> trzymaj na górze. W przeciwnym wypadku -> wyśrodkuj.
  const position = hasValue
    ? 'top-1 -translate-y-0 scale-75'
    : 'top-1/2 -translate-y-1/2 scale-100 peer-focus:top-1 peer-focus:-translate-y-0 peer-focus:scale-75';

  // Kolor tekstu labela przy focusie
  const labelFocusColor = props.disableFocusColor ? 'peer-focus:text-[#606770]' : 'peer-focus:text-[#1877f2]';

  if (props.variant === 'new') {
    const pos = 'left-4';
    const colors = props.error ? 'text-[#d32f2f] peer-focus:text-[#d32f2f]' : `text-[#606770] ${labelFocusColor}`;
    return [base, position, pos, colors];
  } else {
    // Wariant Classic
    const pos = 'left-3';
    const colors = props.error ? 'text-[#b0281c] peer-focus:text-[#b0281c]' : `text-[#606770] ${labelFocusColor}`;
    return [base, position, pos, colors];
  }
});
</script>
