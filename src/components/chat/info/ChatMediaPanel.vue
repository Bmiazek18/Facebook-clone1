<template>
  <div class="h-full flex flex-col bg-white">
    <div class="flex items-center px-4 py-4">
      <button
        @click="$emit('close')"
        class="mr-4 hover:bg-gray-100 rounded-full p-1 transition"
        aria-label="Powrót"
      >
        <ArrowLeftIcon :size="24" class="text-black" />
      </button>
      <h2 class="text-[17px] font-bold text-gray-900">Multimedia i pliki</h2>
    </div>

    <div class="flex border-b border-gray-200 px-4  ">
      <button
        @click="activeTab = 'media'"
        class="px-4 py-3 font-semibold text-[14px] cursor-pointer transition relative"
        :class="activeTab === 'media' ? 'text-blue-600 border-b-[3px] border-blue-600' : 'text-gray-500 hover:bg-gray-50 rounded-t-md ml-2'"
      >
        Multimedia
      </button>
      <button
        @click="activeTab = 'files'"
        class="px-4 py-3 font-semibold text-[14px] cursor-pointer transition relative ml-2"
        :class="activeTab === 'files' ? 'text-blue-600 border-b-[3px] border-blue-600' : 'text-gray-500 hover:bg-gray-50 rounded-t-md'"
      >
        Pliki
      </button>
    </div>

    <!-- Zawartość zakładki: MULTIMEDIA -->
    <div v-if="activeTab === 'media'" class="flex-1 overflow-y-auto custom-scrollbar p-1">
      <div v-for="(group, index) in groupedPhotos" :key="index" class="mb-4">
        <div class="p-2 pt-3">
          <h3 class="font-bold text-[15px] text-gray-900 capitalize">{{ group.title }}</h3>
        </div>

        <div class="grid grid-cols-3 gap-1 px-1">
          <div
            v-for="photo in group.items"
            :key="photo.id"
            class="relative aspect-square bg-gray-100 cursor-pointer overflow-hidden group"
          >
            <img :src="photo.url" loading="lazy" class="w-full h-full object-cover" />
            <div
              class="absolute inset-0 bg-black/10 opacity-0 group-hover:opacity-100 transition"
            ></div>
          </div>
        </div>
      </div>

      <div v-if="groupedPhotos.length === 0" class="text-center py-10 text-gray-500 text-sm">
        Brak multimediów
      </div>
    </div>

    <!-- Zawartość zakładki: PLIKI (stylizowana pod zrzut ekranu) -->
    <div v-else-if="activeTab === 'files'" class="flex-1 overflow-y-auto custom-scrollbar">
      <div v-for="file in mockFiles" :key="file.id" class="flex items-center px-4 py-3 hover:bg-gray-50 transition border-b border-gray-100 cursor-pointer">
        <!-- Ikona pliku w zaokrąglonym boksie -->
        <div class="w-12 h-12 rounded-xl bg-gray-50 flex items-center justify-center shrink-0 mr-3 text-gray-800">
          <FileDocumentOutlineIcon :size="26" />
        </div>

        <!-- Nazwa i rozmiar -->
        <div class="flex-1 min-w-0 pr-2">
          <h4 class="text-[15px] font-bold text-gray-900 truncate leading-snug">
            {{ file.name }}
          </h4>
          <p class="text-[13px] text-gray-500 mt-0.5">
            {{ formatFileSize(file.size) }}
          </p>
        </div>
      </div>

      <div v-if="mockFiles.length === 0" class="text-center py-10 text-gray-500 text-sm">
        Brak plików
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import FileDocumentOutlineIcon from 'vue-material-design-icons/FileDocumentOutline.vue'

interface Photo {
  id: number
  url: string
  uploadDate: string
}

interface FileItem {
  id: number
  name: string
  size: number // w bajtach
  uploadDate: string
}

const activeTab = ref<'media' | 'files'>('media')

// Dane testowe zdjęć
const mockDatabase = ref<Photo[]>([
  { id: 1, url: 'https://picsum.photos/300?random=1', uploadDate: '2025-12-20T10:00:00' },
  { id: 2, url: 'https://picsum.photos/300?random=2', uploadDate: '2025-12-15T14:30:00' },
  { id: 3, url: 'https://picsum.photos/300?random=3', uploadDate: '2025-11-28T11:20:00' },
  { id: 4, url: 'https://picsum.photos/300?random=4', uploadDate: '2024-12-10T16:45:00' },
  { id: 5, url: 'https://picsum.photos/300?random=5', uploadDate: '2024-05-30T12:00:00' },
  { id: 6, url: 'https://picsum.photos/300?random=6', uploadDate: '2023-01-05T18:00:00' },
])

// Dane testowe plików (wzorowane na Twoim zrzucie ekranu)
const mockFiles = ref<FileItem[]>([
  { id: 1, name: '2024_certyfikat_klubowy (1).pdf', size: 396000, uploadDate: '2025-01-10T10:00:00' },
  { id: 2, name: 'IC-bilet-WN55711702.pdf', size: 114000, uploadDate: '2025-01-09T10:00:00' },
  { id: 3, name: 'IC-bilet-WN55711845.pdf', size: 114000, uploadDate: '2025-01-08T10:00:00' },
  { id: 4, name: 'What_you_need_to_achieve_in_a_con s....pdf', size: 211000, uploadDate: '2025-01-07T10:00:00' },
  { id: 5, name: 'IC-bilet-WN53334122.pdf', size: 114000, uploadDate: '2025-01-06T10:00:00' },
  { id: 6, name: 'IC-bilet-WN53333240.pdf', size: 105000, uploadDate: '2025-01-05T10:00:00' },
  { id: 7, name: 'IC-bilet-WN52299719.pdf', size: 114000, uploadDate: '2025-01-04T10:00:00' },
  { id: 8, name: 'IC-bilet-WN52677022.pdf', size: 116000, uploadDate: '2025-01-03T10:00:00' },
])

// Helper do formatowania rozmiaru pliku (np. bajty na KB)
const formatFileSize = (bytes: number) => {
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(2)} KB`
  }
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`
}

const groupedPhotos = computed(() => {
  const sorted = [...mockDatabase.value].sort((a, b) => {
    return new Date(b.uploadDate).getTime() - new Date(a.uploadDate).getTime()
  })

  const groups: { title: string; items: Photo[] }[] = []
  const currentYear = new Date().getFullYear()

  sorted.forEach((photo) => {
    const date = new Date(photo.uploadDate)
    const photoYear = date.getFullYear()
    let monthName = ''

    if (photoYear === currentYear) {
      monthName = date.toLocaleString('pl-PL', { month: 'long' })
    } else {
      monthName = date.toLocaleString('pl-PL', { month: 'long', year: 'numeric' })
    }

    let lastGroup = groups[groups.length - 1]

    if (!lastGroup || lastGroup.title !== monthName) {
      lastGroup = { title: monthName, items: [] }
      groups.push(lastGroup)
    }

    lastGroup.items.push(photo)
  })

  return groups
})
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 20px;
}
</style>
