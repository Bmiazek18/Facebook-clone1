<script setup lang="ts">
import { ref, computed } from 'vue'
import exifr from 'exifr'

// Import nowych, lepszych ikon componentowych
import AccountPlus from 'vue-material-design-icons/AccountPlus.vue'
import MapMarker from 'vue-material-design-icons/MapMarker.vue'
import ClockOutline from 'vue-material-design-icons/ClockOutline.vue'

import CustomInput from '@/components/common/CustomInput.vue'
import CustomDropdown from '@/components/common/CustomDropdown.vue'
import CustomTextarea from '@/components/common/CustomTextarea.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import PrivacySelector from '@/components/common/PrivacySelector.vue'

const sortByOptions = [
  { id: 'drag', title: 'Przeciągnij i upuść', description: 'Ułóż zdjęcia w dowolnej kolejności' },
  { id: 'date_asc', title: 'Data: od najstarszych', description: 'Najstarsze zdjęcia na początku' },
  { id: 'date_desc', title: 'Data: od najnowszych', description: 'Najnowsze zdjęcia na początku' },
]

const albumName = ref('')
const sortBy = ref('drag')
const files = ref([])
const draggedItem = ref(null)

const showPrivacyModal = ref(false)
const selectedPrivacy = ref('friends')

const privacyLabel = computed(() => {
  switch (selectedPrivacy.value) {
    case 'public':
      return 'Publiczne'
    case 'friends':
      return 'Znajomi'
    case 'close_friends':
      return 'Bliscy znajomi'
    case 'friends_except':
      return 'Nie wyświetlaj...'
    case 'specific_friends':
      return 'Wyświetlaj tylko...'
    case 'only_me':
      return 'Tylko ja'
    default:
      return 'Grupa prywatna'
  }
})

const privacyIcon = computed(() => {
  switch (selectedPrivacy.value) {
    case 'public':
      return '🌎'
    case 'friends':
      return '👥'
    case 'close_friends':
      return '⭐'
    case 'friends_except':
      return '👤🚫'
    case 'specific_friends':
      return '👤✔️'
    case 'only_me':
      return '🔒'
    default:
      return '👤'
  }
})

const handlePrivacyConfirm = (payload: { id: string; setDefault: boolean }) => {
  selectedPrivacy.value = payload.id
  showPrivacyModal.value = false
}

const handleFileUpload = (event) => {
  const uploadedFiles = Array.from(event.target.files)

  const newFiles = uploadedFiles.map((file) => ({
    id: Math.random().toString(36).substr(2, 9),
    file: file,
    url: URL.createObjectURL(file),
    description: '',
    createdDate: null,
    location: null,
  }))

  files.value = [...files.value, ...newFiles]
  event.target.value = ''
}

const removeFile = (id) => {
  files.value = files.value.filter((f) => f.id !== id)
}

const useFileMetadata = async () => {
  try {
    const updatedFiles = await Promise.all(
      files.value.map(async (item) => {
        let date = null
        try {
          const data = await exifr.parse(item.file, ['DateTimeOriginal'])
          if (data && data.DateTimeOriginal) {
            date = new Date(data.DateTimeOriginal)
          }
        } catch (exifError) {
          console.warn(`Brak tagów EXIF w pliku ${item.file.name}`)
        }

        if (!date && item.file.lastModified) {
          date = new Date(item.file.lastModified)
        }

        return { ...item, createdDate: date }
      }),
    )

    files.value = updatedFiles
    sortFiles()
  } catch (error) {
    console.error('Błąd przy przetwarzaniu metadanych:', error)
  }
}

const sortFiles = () => {
  if (sortBy.value === 'date_asc') {
    files.value.sort((a, b) => {
      if (!a.createdDate) return 1
      if (!b.createdDate) return -1
      return a.createdDate.getTime() - b.createdDate.getTime()
    })
  } else if (sortBy.value === 'date_desc') {
    files.value.sort((a, b) => {
      if (!a.createdDate) return 1
      if (!b.createdDate) return -1
      return b.createdDate.getTime() - a.createdDate.getTime()
    })
  }
}

const onDragStart = (event, item) => {
  if (sortBy.value !== 'drag') {
    event.preventDefault()
    return
  }
  draggedItem.value = item
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.dropEffect = 'move'
}

const onDragEnter = (targetItem) => {
  if (sortBy.value !== 'drag' || !draggedItem.value || draggedItem.value.id === targetItem.id) {
    return
  }

  const oldIndex = files.value.findIndex((f) => f.id === draggedItem.value.id)
  const newIndex = files.value.findIndex((f) => f.id === targetItem.id)

  if (oldIndex !== -1 && newIndex !== -1) {
    const itemToMove = files.value.splice(oldIndex, 1)[0]
    files.value.splice(newIndex, 0, itemToMove)
  }
}

const onDragEnd = () => {
  draggedItem.value = null
}

const openLocationSelector = (item) => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        item.location = {
          lat: latitude,
          lng: longitude,
          name: `${latitude.toFixed(4)}, ${longitude.toFixed(4)}`,
        }
      },
      (error) => {
        console.error('Błąd przy pobieraniu lokalizacji:', error)
        alert('Nie udało się pobrać lokalizacji. Sprawdź uprawnienia.')
      },
    )
  } else {
    alert('Geolokalizacja nie jest obsługiwana przez tę przeglądarkę.')
  }
}
</script>

<template>
  <div class="flex h-screen w-full mt-[53px] bg-theme text-theme-text   antialiased">
    <aside
      class="w-[360px] bg-theme-bg-secondary border-r border-theme-border flex flex-col shadow-2xl z-10 shrink-0"
    >
      <div class="p-4">
        <h1 class="text-[24px] font-bold">Utwórz album</h1>
      </div>

      <div class="p-4 flex-1 overflow-y-auto space-y-4">
        <div
          @click="showPrivacyModal = true"
          class="bg-theme-bg-tertiary hover:bg-theme-hover-strong transition-colors cursor-pointer p-1.5 px-3 rounded-md inline-flex items-center gap-2 text-[13px] font-semibold select-none"
        >
          <span class="text-theme-text-secondary">{{ privacyIcon }}</span> {{ privacyLabel }}
        </div>

        <CustomInput id="album-input" label="Nazwa albumu" v-model="albumName" />

        <label
          class="flex items-center justify-center gap-2 w-full bg-theme-bg-tertiary hover:bg-theme-hover-strong transition-colors cursor-pointer rounded-lg p-2.5 font-semibold text-theme-primary"
        >
          <span class="text-xl">+</span> Prześlij zdjęcia lub filmy
          <input
            type="file"
            multiple
            class="hidden"
            @change="handleFileUpload"
            accept="image/*,video/*"
          />
        </label>

        <CustomDropdown
          label="Sortuj według"
          v-model="sortBy"
          :options="sortByOptions"
          @update:modelValue="sortFiles"
        />

        <button
          @click="useFileMetadata"
          :disabled="files.length === 0"
          v-tooltip="
            'Odczyta datę utworzenia ze zdjęć. Jeśli niedostępna, użyje daty modyfikacji pliku'
          "
          class="flex items-center gap-2 w-full bg-theme-bg-tertiary hover:bg-theme-hover-strong transition-colors disabled:opacity-50 disabled:cursor-not-allowed rounded-lg p-2.5 font-semibold text-sm"
        >
          <ClockOutline :size="16" /> Użyj daty ze zdjęć
        </button>
      </div>

      <div class="p-4 border-t border-theme-border">
        <button
          :disabled="!albumName || files.length === 0"
          class="w-full py-2 rounded-lg font-semibold transition-all bg-theme-primary text-white hover:bg-theme-primary-hover disabled:bg-theme-bg-tertiary disabled:text-theme-text-secondary disabled:cursor-not-allowed"
        >
          Opublikuj
        </button>
      </div>
    </aside>

    <main class="flex-1 overflow-y-auto p-6 scrollbar-hide">
      <div
        v-if="files.length === 0"
        class="h-full flex flex-col items-center justify-center text-center"
      >
        <div
          class="w-20 h-20 mb-4 bg-theme-bg-secondary rounded-xl flex items-center justify-center border border-theme-border relative"
        >
          <div class="w-10 h-10 border-2 border-theme-border rounded bg-theme-bg-tertiary"></div>
          <div
            class="absolute -top-1 -right-1 w-5 h-5 bg-theme-primary rounded-full border-4 border-theme-bg"
          ></div>
        </div>
        <h2 class="text-xl font-bold text-theme-text-secondary">Może coś dodasz?</h2>
        <p class="text-theme-text-secondary">Przeciągnij zdjęcia i filmy tutaj, aby rozpocząć.</p>
      </div>

      <div v-else>
        <TransitionGroup
          name="list"
          tag="div"
          class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 2xl:grid-cols-7 gap-4 relative"
        >
          <div
            v-for="item in files"
            :key="item.id"
            :draggable="sortBy === 'drag'"
            @dragstart="onDragStart($event, item)"
            @dragenter.prevent="onDragEnter(item)"
            @dragover.prevent
            @dragend="onDragEnd"
            class="bg-theme-bg-secondary h-[450px] rounded-lg border border-theme-border flex flex-col relative overflow-hidden shadow-md group transition-all duration-300"
            :class="{
              'cursor-move hover:shadow-xl': sortBy === 'drag',
              'opacity-40 border-2 border-dashed border-theme-primary scale-95 bg-theme-bg':
                draggedItem && draggedItem.id === item.id,
            }"
          >
            <button
              @click="removeFile(item.id)"
              class="absolute top-2 right-2 z-20 bg-theme-bg/60 hover:bg-theme-bg/90 text-theme-text p-1.5 rounded-full transition-colors backdrop-blur-sm"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>

            <div
              class="w-full aspect-square bg-black flex items-center justify-center overflow-hidden relative select-none"
            >
              <img
                :src="item.url"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                draggable="false"
              />
            </div>

            <div class="p-3 flex flex-col gap-2 justify-between flex-1">
              <div class="relative">
                <CustomTextarea
                  :id="`description-${item.id}`"
                  label="Opis (opcjonalnie)"
                  v-model="item.description"
                />
              </div>

              <div class="flex items-center justify-between pt-1">
                <div class="flex gap-1">
                  <button
                    title="Oznacz osoby"
                    class="p-2 text-theme-text-secondary hover:text-theme-text hover:bg-theme-bg-tertiary rounded-full transition-all"
                  >
                    <AccountPlus :size="20" />
                  </button>

                  <button
                    @click="openLocationSelector(item)"
                    v-tooltip="`Lokalizacja: ${item.location ? item.location.name : 'Brak'}`"
                    class="p-2 hover:bg-theme-bg-tertiary rounded-full transition-all"
                    :class="
                      item.location
                        ? 'text-theme-primary'
                        : 'text-theme-text-secondary hover:text-theme-text'
                    "
                  >
                    <MapMarker :size="20" />
                  </button>

                  <button
                    v-tooltip="
                      `${item.createdDate ? item.createdDate.toLocaleString('pl-PL') : 'Brak daty'}`
                    "
                    class="p-2 rounded-full transition-all"
                    :class="
                      item.createdDate
                        ? 'text-theme-primary bg-theme-primary/10 font-bold'
                        : 'text-theme-text-secondary hover:text-theme-text hover:bg-theme-bg-tertiary'
                    "
                  >
                    <ClockOutline :size="20" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </TransitionGroup>
      </div>
    </main>

    <BaseModal
      v-if="showPrivacyModal"
      title="Wybierz grupę odbiorców"
      @close="showPrivacyModal = false"
    >
      <PrivacySelector
        :initial-privacy="selectedPrivacy"
        @confirm="handlePrivacyConfirm"
        @back="showPrivacyModal = false"
      />
    </BaseModal>
  </div>
</template>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.list-move {
  transition: transform 0.3s ease;
}

.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

.list-leave-active {
  position: absolute;
}
</style>
