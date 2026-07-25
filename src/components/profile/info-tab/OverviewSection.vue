<script setup lang="ts">
import { ref, reactive, inject } from 'vue'
import type { User } from '@/utils/users'
import { useMutation } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'

// Import ikon z Material Design
import Pencil from 'vue-material-design-icons/Pencil.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import MapMarker from 'vue-material-design-icons/MapMarker.vue'
import Domain from 'vue-material-design-icons/Domain.vue'
import HandWave from 'vue-material-design-icons/HandWave.vue' // <-- DODANA IKONA DŁONI

const props = defineProps<{ profileUser: User }>()
const isOwner = inject('isOwner')
const fetchUserProfile = inject<() => Promise<void>>('fetchUserProfile')

// --- STANY EDYCJI ---
const isEditingPinned = ref(false)
const isEditingBio = ref(false)

// --- DANE BIOGRAMU ---
const bioText = ref(props.profileUser.bio || '')
const maxBioLength = 101

// --- DANE PRZYPIĘTYCH SZCZEGÓŁÓW ---
const pinnedSettings = reactive({
  location: true,
  school: true,
})

const UPDATE_PROFILE_MUTATION = gql`
  mutation UpdateProfile($userId: ID!, $input: UpdateProfileInput!) {
    updateProfile(userId: $userId, input: $input) {
      id
      bio
    }
  }
`

const { mutate: updateProfile } = useMutation(UPDATE_PROFILE_MUTATION)

// --- FUNKCJE ZAPISU ---
const savePinned = () => {
  isEditingPinned.value = false
}
const saveBio = async () => {
  try {
    await updateProfile({
      userId: String(props.profileUser.id),
      input: {
        bio: bioText.value
      }
    })
    if (fetchUserProfile) {
      await fetchUserProfile()
    }
    isEditingBio.value = false
  } catch (err) {
    console.error('Failed to save bio:', err)
  }
}
</script>

<template>
  <div class="max-w-[850px] text-[#050505] antialiased">
    <!-- ============================================== -->
    <!-- SEKCJA 1: BIOGRAM                              -->
    <!-- ============================================== -->
    <h2 class="font-semibold text-[17px] mb-5">Biogram</h2>

    <!-- TRYB EDYCJI BIOGRAMU -->
    <div v-if="isEditingBio" class="mb-8">
      <div
        class="inline-flex items-center gap-1.5 bg-[#E4E6EB] px-3 py-1.5 rounded-md font-semibold text-[15px] text-[#050505] mb-4"
      >
        <Earth :size="16" class="text-[#65676B]" />
        Publiczne
      </div>

      <h3 class="font-semibold text-[17px] mb-3">Edytuj biogram</h3>

      <div
        class="relative border-[2px] border-[#1877F2] rounded-lg p-3 pt-6 pb-[8px] bg-white focus-within:ring-0"
      >
        <span class="absolute top-2 left-3 text-[13px] text-[#1877F2] font-normal"
          >Przedstaw się</span
        >
        <textarea
          v-model="bioText"
          :maxlength="maxBioLength"
          rows="3"
          class="w-full resize-none outline-none bg-transparent text-[15px] text-[#050505]"
          autofocus
        ></textarea>
      </div>

      <div class="text-[13px] text-[#65676B] text-left mt-1.5">
        {{ bioText.length }}/{{ maxBioLength }}
      </div>

      <div class="flex justify-end space-x-2 pt-4 border-t border-gray-200 mt-4">
        <button
          @click="isEditingBio = false"
          class="px-4 py-1.5 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold rounded-md text-[15px] transition-colors"
        >
          Anuluj
        </button>
        <button
          @click="saveBio"
          :disabled="bioText.length === 0"
          class="px-4 py-1.5 font-semibold rounded-md text-[15px] transition-colors"
          :class="
            bioText.length > 0
              ? 'bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF]'
              : 'bg-[#E4E6EB] text-[#BCC0C4] cursor-not-allowed'
          "
        >
          Zapisz
        </button>
      </div>
    </div>

    <!-- TRYB PODGLĄDU BIOGRAMU (KLIKALNY TEKST Z IKONĄ) -->
    <div class="mb-[8px]" v-else>
      <!-- Element aktywujący edycję -->
      <div
        @click="isOwner ? (isEditingBio = true) : null"
        class="inline-flex items-center text-[15px] text-[#65676B] font-medium transition-colors"
        :class="isOwner ? 'cursor-pointer hover:bg-gray-100 p-2 -ml-2 rounded-md' : ''"
      >
        <HandWave :size="20" class="mr-3 text-black" />

        <span>{{ bioText ? bioText : 'Informacje o Tobie' }}</span>
      </div>
    </div>

    <!-- ============================================== -->
    <!-- SEKCJA 2: PRZYPIĘTE SZCZEGÓŁY                  -->
    <!-- ============================================== -->
    <div class="flex justify-between items-center mb-5">
      <h3 class="font-semibold text-[17px]">Przypięte szczegóły</h3>
    </div>

    <!-- TRYB EDYCJI PRZYPIĘTYCH -->
    <div v-if="isEditingPinned" class="mt-1 pb-[8px]">
      <p class="text-[15px] text-[#65676B] mb-5 leading-5">
        Wybierz maksymalnie 5 szczegółów do wyświetlenia u góry Twojego profilu. Informacje
        szczegółowe będą wyświetlane na podstawie ustawień grupy odbiorców.
      </p>

      <div class="space-y-5">
        <label v-if="profileUser.location" class="flex items-start space-x-3 cursor-pointer">
          <input
            type="checkbox"
            v-model="pinnedSettings.location"
            class="w-5 h-5 mt-0.5 rounded text-[#1877F2] border-gray-300 focus:ring-[#1877F2]"
          />
          <MapMarker :size="24" class="text-[#65676B] shrink-0" />
          <div class="flex flex-col">
            <span class="font-medium text-[15px] leading-5">{{ profileUser.location }}</span>
            <div class="flex items-center text-[13px] text-[#65676B] mt-0.5 gap-1">
              <Earth :size="12" /> Publiczne
            </div>
          </div>
        </label>

        <label v-if="profileUser.school" class="flex items-start space-x-3 cursor-pointer">
          <input
            type="checkbox"
            v-model="pinnedSettings.school"
            class="w-5 h-5 mt-0.5 rounded text-[#1877F2] border-gray-300 focus:ring-[#1877F2]"
          />
          <Domain :size="24" class="text-[#65676B] shrink-0" />
          <div class="flex flex-col">
            <span class="font-medium text-[15px] leading-5">{{ profileUser.school }}</span>
            <div class="flex items-center text-[13px] text-[#65676B] mt-0.5 gap-1">
              <Earth :size="12" /> Publiczne
            </div>
          </div>
        </label>
      </div>

      <div class="flex justify-end space-x-2 pt-4 border-t border-gray-200 mt-6">
        <button
          @click="isEditingPinned = false"
          class="px-4 py-1.5 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold rounded-md text-[15px] transition-colors"
        >
          Anuluj
        </button>
        <button
          @click="savePinned"
          class="px-4 py-1.5 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold rounded-md text-[15px] transition-colors"
        >
          Zapisz
        </button>
      </div>
    </div>

    <!-- TRYB PODGLĄDU PRZYPIĘTYCH -->
    <div
      v-else
      class="mt-4 flex flex-wrap items-center gap-x-2 gap-y-2 justify-between text-[15px] text-[#050505] font-normal"
    >
      <div class="flex flex-row">
        <div v-if="profileUser.location && pinnedSettings.location" class="flex items-center gap-2">
          <MapMarker :size="20" class="text-[#050505]" />
          <span>{{ profileUser.location }}</span>
        </div>

        <span
          v-if="
            profileUser.location &&
            pinnedSettings.location &&
            profileUser.school &&
            pinnedSettings.school
          "
          class="text-[#65676B] font-bold"
          >·</span
        >

        <div v-if="profileUser.school && pinnedSettings.school" class="flex items-center gap-2">
          <Domain :size="20" class="text-[#050505]" />
          <span>{{ profileUser.school }}</span>
        </div>
      </div>
      <button
        v-if="isOwner && !isEditingPinned"
        @click="isEditingPinned = true"
        class="p-2 hover:bg-[#F2F2F2] rounded-full transition-colors"
      >
        <Pencil :size="20" class="text-[#65676B]" />
      </button>
    </div>
  </div>
</template>
