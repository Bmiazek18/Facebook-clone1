<script setup lang="ts">
import { ref, computed } from 'vue';
import Close from 'vue-material-design-icons/Close.vue';
import Link from 'vue-material-design-icons/Link.vue';
import OpenInNew from 'vue-material-design-icons/OpenInNew.vue';

const emit = defineEmits<{
  close: [];
  addLink: [data: { url: string; title: string; style: 'default' | 'minimal' | 'button' }];
}>();

defineProps<{
  isOpen: boolean;
}>();

const url = ref('');
const customTitle = ref('');
const selectedStyle = ref<'default' | 'minimal' | 'button'>('default');

const isValidUrl = computed(() => {
  if (!url.value) return false;
  try {
    new URL(url.value.startsWith('http') ? url.value : `https://${url.value}`);
    return true;
  } catch {
    return false;
  }
});

const displayTitle = computed(() => {
  if (customTitle.value) return customTitle.value;
  if (!url.value) return 'Twój link';
  try {
    const urlObj = new URL(url.value.startsWith('http') ? url.value : `https://${url.value}`);
    return urlObj.hostname.replace('www.', '');
  } catch {
    return url.value;
  }
});

const normalizedUrl = computed(() => {
  if (!url.value) return '';
  return url.value.startsWith('http') ? url.value : `https://${url.value}`;
});

const handleAddLink = () => {
  if (!isValidUrl.value) return;
  emit('addLink', {
    url: normalizedUrl.value,
    title: customTitle.value || displayTitle.value,
    style: selectedStyle.value
  });
  // Reset
  url.value = '';
  customTitle.value = '';
  selectedStyle.value = 'default';
};

const close = () => {
  emit('close');
  url.value = '';
  customTitle.value = '';
};
</script>

<template>
  <Teleport to="body">
    <div
      v-if="isOpen"
      class="fixed inset-0 z-9999 flex items-center justify-center bg-black/60 backdrop-blur-sm"
      @click.self="close"
    >
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 overflow-hidden">
        <!-- Header -->
        <div class="flex items-center justify-between p-4 border-b border-gray-200">
          <h2 class="text-xl font-bold text-gray-900">Dodaj naklejkę z linkiem</h2>
          <button
            @click="close"
            class="p-2 hover:bg-gray-100 rounded-full transition-colors"
          >
            <Close :size="24" class="text-gray-600" />
          </button>
        </div>

        <!-- Content -->
        <div class="p-5 space-y-5">
          <!-- URL Input -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Adres URL</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Link :size="20" class="text-gray-400" />
              </div>
              <input
                v-model="url"
                type="text"
                placeholder="np. instagram.com/user"
                class="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
              />
            </div>
          </div>

          <!-- Custom Title -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Własny tytuł (opcjonalnie)</label>
            <input
              v-model="customTitle"
              type="text"
              placeholder="np. Mój Instagram"
              class="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
            />
          </div>

          <!-- Style Selection -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-3">Styl naklejki</label>
            <div class="grid grid-cols-3 gap-3">
              <!-- Default Style -->
              <button
                @click="selectedStyle = 'default'"
                class="p-3 border-2 rounded-xl transition-all flex flex-col items-center gap-2"
                :class="selectedStyle === 'default' ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-gray-300'"
              >
                <div class="w-full bg-linear-to-r from-blue-500 to-purple-500 rounded-lg p-2 text-white text-xs font-medium flex items-center gap-1 justify-center">
                  <Link :size="12" />
                  <span class="truncate">Link</span>
                </div>
                <span class="text-xs text-gray-600">Domyślny</span>
              </button>

              <!-- Minimal Style -->
              <button
                @click="selectedStyle = 'minimal'"
                class="p-3 border-2 rounded-xl transition-all flex flex-col items-center gap-2"
                :class="selectedStyle === 'minimal' ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-gray-300'"
              >
                <div class="w-full bg-white/90 border border-gray-200 rounded-lg p-2 text-gray-800 text-xs font-medium flex items-center gap-1 justify-center">
                  <OpenInNew :size="12" />
                  <span class="truncate">Link</span>
                </div>
                <span class="text-xs text-gray-600">Minimalna</span>
              </button>

              <!-- Button Style -->
              <button
                @click="selectedStyle = 'button'"
                class="p-3 border-2 rounded-xl transition-all flex flex-col items-center gap-2"
                :class="selectedStyle === 'button' ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-gray-300'"
              >
                <div class="w-full bg-black rounded-full py-2 px-3 text-white text-xs font-bold text-center">
                  Zobacz więcej
                </div>
                <span class="text-xs text-gray-600">Przycisk</span>
              </button>
            </div>
          </div>

          <!-- Preview -->
          <div v-if="url" class="bg-gray-100 rounded-xl p-4">
            <p class="text-xs text-gray-500 mb-2">Podgląd:</p>
            
            <!-- Default Preview -->
            <div v-if="selectedStyle === 'default'" class="inline-flex items-center gap-2 bg-linear-to-r from-blue-500 to-purple-500 rounded-xl px-4 py-2.5 text-white font-medium shadow-lg">
              <Link :size="18" />
              <span>{{ displayTitle }}</span>
              <OpenInNew :size="16" class="opacity-70" />
            </div>

            <!-- Minimal Preview -->
            <div v-else-if="selectedStyle === 'minimal'" class="inline-flex items-center gap-2 bg-white/95 backdrop-blur border border-gray-200 rounded-xl px-4 py-2.5 text-gray-800 font-medium shadow-md">
              <Link :size="18" class="text-gray-500" />
              <span>{{ displayTitle }}</span>
              <OpenInNew :size="16" class="text-gray-400" />
            </div>

            <!-- Button Preview -->
            <div v-else class="inline-block bg-black rounded-full px-6 py-3 text-white font-bold shadow-lg">
              {{ displayTitle }}
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="p-4 border-t border-gray-200 flex justify-end gap-3">
          <button
            @click="close"
            class="px-5 py-2.5 text-gray-700 font-medium hover:bg-gray-100 rounded-lg transition-colors"
          >
            Anuluj
          </button>
          <button
            @click="handleAddLink"
            :disabled="!isValidUrl"
            class="px-5 py-2.5 bg-blue-500 text-white font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-blue-600"
          >
            Dodaj naklejkę
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
