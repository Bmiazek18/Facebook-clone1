<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { renderGrid } from '@giphy/js-components'
import { GiphyFetch } from '@giphy/js-fetch-api'

useI18n()

/** * Utils & Logic
 */
type DebounceFunction = (...args: unknown[]) => void

const debounce = (func: DebounceFunction, wait: number): DebounceFunction => {
  let timeout: ReturnType<typeof setTimeout> | null = null
  return (...args: unknown[]) => {
    const later = () => {
      timeout = null
      func(...args)
    }
    if (timeout !== null) clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

interface Emits {
  (e: 'handleGifSelection', url: string): void
}

const emit = defineEmits<Emits>()

const gifs = ref<HTMLElement | null>(null)
const searchTerm = ref<string>('')
const grid = ref<{ remove: () => void } | null>(null)
const previewGifUrl = ref<string | null>(null)

const gf = new GiphyFetch(import.meta.env.VITE_GIPHY_KEY as string)

onMounted(() => {
  if (gifs.value) {
    grid.value = makeGrid(gifs.value)
  }
})

const fetchGifs = (offset: number) => {
  if (searchTerm.value) {
    return gf.search(searchTerm.value, { offset, limit: 25 })
  }
  return gf.trending({ offset, limit: 25 })
}

const makeGrid = (targetEl: HTMLElement): { remove: () => void } => {
  const render = () => {
    return renderGrid(
      {
        width: 280, // Dopasowane do szerokości w-72 (288px) minus lekki padding
        fetchGifs,
        columns: 2,
        gutter: 2, // Minimalny odstęp dla efektu "kafelków" ze zdjęcia
        noLink: true,
        hideAttribution: true,
        onGifClick,
      },
      targetEl,
    )
  }

  const remove = render()
  return { remove: () => remove() }
}

interface IGif {
  images: {
    fixed_height: {
      url: string
    }
  }
}

const onGifClick = (gif: IGif, e: MouseEvent) => {
  e.preventDefault()
  const url = gif.images.fixed_height.url

  emit('handleGifSelection', url)
}

const handleGifSearch = debounce(() => {
  previewGifUrl.value = null
  clearGridAndFetchGifs()
}, 400)

const clearGridAndFetchGifs = (): void => {
  if (grid.value) grid.value.remove()
  if (gifs.value) grid.value = makeGrid(gifs.value)
}
</script>

<template>
  <div
    class="relative flex flex-col w-72 h-[420px] bg-white shadow-2xl rounded-2xl border border-gray-100 overflow-hidden"
  >
    <div class="p-3 bg-white z-10">
      <div class="relative group">
        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </span>
        <input
          type="text"
          v-model="searchTerm"
          @input="handleGifSearch"
          class="w-full bg-gray-100 border-none py-2 pl-10 pr-4 rounded-full text-[15px] focus:ring-2 focus:ring-gray-200 placeholder-gray-500 transition-all outline-none"
          placeholder="Szukaj"
        />
      </div>
    </div>

    <div class="flex-1 overflow-y-auto custom-scrollbar px-1 pb-1">
      <div ref="gifs" class="flex justify-center" />
    </div>

    <div
      class="absolute -bottom-1.5 left-1/2 -translate-x-1/2 w-4 h-4 bg-white rotate-45 border-r border-b border-gray-100 shadow-[2px_2px_2px_rgba(0,0,0,0.02)]"
    ></div>
  </div>
</template>

<style scoped>
/* Stylizacja scrollbara - cienki i subtelny */
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background-color: transparent;
}

/* Giphy Grid Reset - usuwa niechciane marginesy z biblioteki */
:deep(.giphy-grid) {
  margin: 0 auto;
}

/* Animacja pojawiania się */
.animate-in {
  animation: fadeIn 0.2s ease-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
