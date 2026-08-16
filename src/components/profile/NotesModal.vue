<script setup lang="ts">
import { computed, ref } from 'vue'
import { useMutation } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const props = defineProps<{
  userId: string | number
  currentNote?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const noteText = ref(props.currentNote || '')
const maxLength = 60

const selectedMusic = ref<{
  title: string
  artist: string
} | null>(null)

const isMusicOpen = ref(false)

const charCount = computed(() => noteText.value.length)

const toggleMusic = () => {
  isMusicOpen.value = !isMusicOpen.value

  // Symulacja wyboru muzyki do testów (jeśli nic nie jest wybrane, wybierz utwór, w przeciwnym razie usuń)
  if (!selectedMusic.value) {
    selectedMusic.value = {
      title: 'Beautiful',
      artist: 'Giulio Cercato'
    }
  } else {
    selectedMusic.value = null
  }
}

const UPDATE_PROFILE_MUTATION = gql`
  mutation UpdateProfile($userId: ID!, $input: UpdateProfileInput!) {
    updateProfile(userId: $userId, input: $input) {
      id
      note
    }
  }
`

const { mutate: updateProfile } = useMutation(UPDATE_PROFILE_MUTATION)

const shareNote = async () => {
  if (charCount.value === 0) return

  try {
    const res = await updateProfile({
      userId: String(props.userId),
      input: {
        note: noteText.value
      }
    })
    if (res?.data?.updateProfile) {
      emit('close')
      window.location.reload()
    }
  } catch (err) {
    console.error('Failed to share note:', err)
  }
}

const deleteNote = async () => {
  try {
    const res = await updateProfile({
      userId: String(props.userId),
      input: {
        note: ""
      }
    })
    if (res?.data?.updateProfile) {
      emit('close')
      window.location.reload()
    }
  } catch (err) {
    console.error('Failed to delete note:', err)
  }
}
</script>

<template>
  <div class="w-[550px]">
    <div class="flex flex-col items-center mt-12">

      <!-- Dymek -->
      <div class="relative inline-flex flex-col items-center">
        <div
          class="absolute z-20 bottom-full cursor-pointer
                 -mb-2 left-1/2 -translate-x-1/2 ml-[-75px]"
        >
          <div
            class="relative bg-white
                   shadow-[0_2px_8px_rgba(0,0,0,0.15)]
                   rounded-[24px] px-4 py-3.5
                   border border-gray-100 min-w-[200px]"
          >
            <!-- Sekcja Muzyki (jeśli wybrano utwór) -->
            <div v-if="selectedMusic" class="mb-2 pb-2 flex flex-col items-center text-center">
              <div class="flex items-center gap-1.5 text-gray-900 font-semibold text-sm">
                <!-- Ikona fali dźwiękowej -->
                <svg class="w-4 h-4 text-gray-800" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 19V5l12 2v14M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-2" />
                </svg>
                <span>{{ selectedMusic.title }}</span>
              </div>
              <span class="text-xs text-gray-500 mt-0.5">{{ selectedMusic.artist }}</span>
            </div>

            <!-- Pole tekstowe z ikoną emoji -->
            <div class="flex items-center justify-between gap-2">
              <input
                v-model="noteText"
                :maxlength="maxLength"
                placeholder="Napisz, co myślisz..."
                class="text-[#65676B] text-[13px] w-full outline-none bg-transparent placeholder-gray-400"
              />
              <button type="button" class="text-xl flex-shrink-0 select-none hover:scale-110 transition-transform">
                😊
              </button>
            </div>

            <!-- Ogonek dymku -->
            <div
              class="absolute -bottom-1.5 right-[30px]
                     w-[18px] h-[18px]
                     bg-white rounded-full z-10"
            ></div>

            <div
              class="absolute -bottom-4 right-[25px]
                     w-2 h-2 bg-white rounded-full
                     shadow-[0_1px_3px_rgba(0,0,0,0.15)]"
            ></div>
          </div>
        </div>

        <!-- Awatar -->
        <div
          class="relative w-33 h-33 bg-gray-200
                 rounded-full flex items-center
                 justify-center overflow-hidden
                 mb-2 shadow-inner z-10"
        >
          <img
            :src="authStore.currentUser?.avatar || '/default-avatar.png'"
            alt="User Avatar"
            class="w-full h-full object-cover rounded-full"
          />
        </div>
      </div>

      <!-- Licznik znaków -->
      <span class="text-xs text-gray-400 mb-4">
        {{ charCount }}/{{ maxLength }}
      </span>

      <!-- Przycisk dodawania muzyki (w celach testowych klawisz przełącza stan muzyki) -->
      <button
        @click="toggleMusic"
        type="button"
        class="w-12 h-12 rounded-full bg-gray-100
               flex items-center justify-center
               text-gray-700 hover:bg-gray-200
               transition-colors shadow-sm"
      >
        <svg
          class="w-5 h-5"
          fill="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            d="M12 3v10.55c-.59-.34-1.27-.55-2-.55
            -2.21 0-4 1.79-4 4s1.79 4 4 4
            4-1.79 4-4V7h4V3h-6z"
          />
        </svg>
      </button>

    </div>

    <!-- Dolna część -->
    <div class="w-full flex flex-col items-center mb-4 mt-6">
      <p class="text-xs text-center text-gray-800 px-6 mb-6">
        Wszyscy będą mogli zobaczyć Twoją notatkę
        w Messengerze i na Facebooku przez 24 godziny.
        <a href="#" class="text-blue-500 font-medium hover:underline">
          Zmień
        </a>
      </p>

      <!-- Udostępnij -->
      <button
        @click="shareNote"
        :disabled="charCount === 0"
        :class="[
          'w-full py-3.5 rounded-full font-semibold text-base transition-all shadow-sm',
          charCount > 0
            ? 'bg-blue-600 text-white hover:bg-blue-700 cursor-pointer'
            : 'bg-gray-200 text-gray-400 cursor-not-allowed'
        ]"
      >
        Udostępnij
      </button>

      <!-- Usuń notatkę -->
      <button
        v-if="currentNote"
        @click="deleteNote"
        class="w-full mt-2 py-3 rounded-full font-semibold text-base transition-all border border-red-500 text-red-500 hover:bg-red-50 cursor-pointer text-center"
      >
        Usuń notatkę
      </button>
    </div>
  </div>
</template>
