<script setup lang="ts">
import { reactive, computed, ref, watch } from 'vue'
import CustomHoursModal from '@/components/pages/modal/CustomHoursModal.vue'
import BaseModal from '~/components/common/BaseModal.vue'
import CustomInput from '~/components/common/CustomInput.vue'
import CustomTextarea from '~/components/common/CustomTextarea.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

type ViewMode = 'desktop' | 'mobile'

interface PageForm {
  pageName: string
  category: string
  bio: string
  website: string
  phoneCode: string
  phone: string
  email: string
  address: string
  city: string
  zip: string
  hours: 'none' | 'always' | 'selected'
  profileImage: string | null
  coverImage: string | null
  pageNotifications: boolean
  promotionalEmails: boolean
}

const viewMode = ref<ViewMode>('desktop')
const step = ref(0)
const isHoursModalOpen = ref(false)
const isSubmitting = ref(false)

const form = reactive<PageForm>({
  pageName: '',
  category: '',
  bio: '',
  website: '',
  phoneCode: 'US+1',
  phone: '',
  email: '',
  address: '',
  city: '',
  zip: '',
  hours: 'none',
  profileImage: null,
  coverImage: null,
  pageNotifications: true,
  promotionalEmails: false
})

watch(() => form.hours, (newVal) => {
  if (newVal === 'selected') {
    isHoursModalOpen.value = true
  }
})

// --- COMPUTED PROPERTIES ---
const previewTitle = computed(() =>
  viewMode.value === 'mobile' ? 'Podgląd na urządzeniu mobilnym' : 'Podgląd na komputerze',
)

const isNameValid = computed(() => form.pageName.trim().length > 0)
const isCategoryValid = computed(() => form.category.trim().length > 0)

const removeCategory = () => {
  form.category = ''
}

const createPage = () => {
  if (isNameValid.value && isCategoryValid.value) {
    step.value = 1
  }
}

const closeCreation = () => {
  navigateTo('/pages')
}

// --- ZDJĘCIA OBSŁUGA W KROKU 2 ---
const handleProfileImage = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) {
    form.profileImage = URL.createObjectURL(file)
  }
}

const handleCoverImage = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) {
    form.coverImage = URL.createObjectURL(file)
  }
}

const removeProfileImage = () => {
  form.profileImage = null
}

const removeCoverImage = () => {
  form.coverImage = null
}

// --- ZAKOŃCZENIE KREACJI STRONY ---
const finishPage = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true
  try {
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const ownerId = String(authStore.originalUserId || authStore.currentUserId)
    const payload = {
      ownerId,
      pageName: form.pageName,
      name: form.pageName,
      category: form.category,
      bio: form.bio,
      website: form.website,
      phoneCode: form.phoneCode,
      phone: form.phone,
      email: form.email,
      address: form.address,
      city: form.city,
      zip: form.zip,
      hours: form.hours,
      profileImage: form.profileImage,
      coverImage: form.coverImage,
      pageNotifications: form.pageNotifications,
      promotionalEmails: form.promotionalEmails
    }

    let savedPage: any = null
    try {
      const res = await fetch(`${apiUrl}/api/pages`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': ownerId,
        },
        body: JSON.stringify(payload)
      })
      if (res.ok) {
        savedPage = await res.json()
      }
    } catch (networkErr) {
      console.warn('Network error while saving page to backend:', networkErr)
    }

    if (!savedPage) {
      const newId = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `page_${Date.now()}`
      savedPage = {
        id: newId,
        ownerId,
        name: form.pageName,
        category: form.category,
        bio: form.bio,
        website: form.website,
        phoneCode: form.phoneCode,
        phone: form.phone,
        email: form.email,
        address: form.address,
        city: form.city,
        zip: form.zip,
        hours: form.hours,
        avatar: form.profileImage || `https://i.pravatar.cc/150?u=${newId}`,
        cover: form.coverImage || `https://picsum.photos/seed/${newId}/1200/400`,
        pageNotifications: form.pageNotifications,
        promotionalEmails: form.promotionalEmails,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
    }

    authStore.addPage(savedPage)
    authStore.switchToPage(savedPage)
    await navigateTo(`/profile/${savedPage.id}`)
  } catch (err) {
    console.error('Error during finishPage:', err)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] text-[#050505] overflow-hidden antialiased font-sans">
    <!-- SIDEBAR -->
    <aside class="w-[360px] shrink-0 flex flex-col bg-white shadow-[2px_0_5px_rgba(0,0,0,0.05)] h-full z-20 relative border-r border-[#E5E5E5]">
      <!-- Górny pasek z ikoną X i logo FB -->
      <div class="h-[56px] flex items-center px-4 shrink-0 gap-3 border-b border-transparent">
        <div
          @click="closeCreation"
          class="w-10 h-10 flex items-center justify-center rounded-full bg-[#E4E6EB] hover:bg-[#D8DADF] cursor-pointer text-[#050505] transition-colors"
        >
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"></path>
          </svg>
        </div>
        <div class="w-10 h-10 text-[#1877F2]">
          <svg viewBox="0 0 36 36" width="40" height="40" fill="currentColor">
            <path d="M15 35.8C6.5 34.3 0 26.9 0 18 0 8.1 8.1 0 18 0s18 8.1 18 18c0 8.9-6.5 16.3-15 17.8l-1-.8h-4l-1 .8z" fill="#1877F2"></path>
            <path d="M21.1 18h-3.4v17.8c-1 .2-2 .3-3.1.3s-2.1-.1-3.1-.3V18h-2.5v-3.8h2.5v-2.3c0-2.8 1.4-4.5 4.7-4.5 1.3 0 2.8.2 2.8.2v3h-1.6c-1.5 0-2.1.8-2.1 1.9v1.7h3.6l-.5 3.8z" fill="#FFF"></path>
          </svg>
        </div>
      </div>

      <!-- KROK 0: TWORZENIE STRONY -->
      <template v-if="step === 0">
        <div class="flex-1 overflow-y-auto px-4 py-2 custom-scrollbar">
          <div class="text-[13px] text-[#65676B] mb-2 font-medium">
            <span class="hover:underline cursor-pointer">Strony</span>
            <span class="mx-1.5">›</span>
            <span class="text-[#65676B]">Utwórz stronę</span>
          </div>

          <h1 class="text-[24px] font-bold leading-tight mb-3 text-[#050505]">
            Utwórz stronę
          </h1>

          <p class="text-[15px] text-[#65676B] leading-snug mb-6">
            Twoja strona to miejsce, w którym inne osoby chcą dowiedzieć się więcej na Twój temat. Upewnij się, że zawiera ona wszystkie informacje, których mogą potrzebować.
          </p>

          <div class="mb-4">
            <CustomInput
              v-model="form.pageName"
              label="Nazwa strony (wymagana)"
            />
            <p class="text-[12px] text-[#65676B] mt-1.5 leading-snug">
              Użyj nazwy swojej firmy, marki lub organizacji albo nazwy objaśniającej tematykę Twojej strony.
              <a href="#" class="text-[#1877F2] hover:underline">Dowiedz się więcej o nadawaniu nazwy stronie</a>
            </p>
          </div>

          <div class="mb-4">
            <div class="relative border border-[#CED0D4] focus-within:border-[#1877F2] focus-within:shadow-[0_0_0_1px_#1877F2] rounded-md px-3 pt-5 pb-1.5 bg-white transition-shadow">
              <label class="absolute left-3 top-2 text-[12px] text-[#65676B] focus-within:text-[#1877F2]">
                Kategoria (wymagana)
              </label>
              <div class="flex items-center">
                <div class="flex-1 flex flex-wrap gap-1 items-center min-h-[24px]">
                  <div v-if="form.category" class="bg-[#E7F3FF] text-[#1877F2] flex items-center gap-1.5 px-2.5 py-1 rounded-md text-[15px] font-medium mr-1">
                    {{ form.category }}
                    <button @click="removeCategory" class="hover:bg-[#DBE7F2] rounded-full p-0.5 transition-colors">
                      <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"></path>
                      </svg>
                    </button>
                  </div>
                  <input
                    v-if="!form.category"
                    v-model="form.category"
                    type="text"
                    placeholder="Wprowadź kategorię"
                    class="w-full focus:outline-none text-[15px] text-[#050505] bg-transparent"
                  />
                </div>
                <div v-if="isCategoryValid" class="w-5 h-5 rounded-full bg-[#31A24C] flex items-center justify-center shrink-0 ml-2">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="white">
                    <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"></path>
                  </svg>
                </div>
              </div>
            </div>
            <p class="text-[12px] text-[#65676B] mt-1.5">
              Wprowadź kategorię, która opisuje Cię najlepiej.
            </p>
          </div>

          <div class="mb-5">
           <CustomTextarea
              v-model="form.bio"
              label="Opis (opcjonalny)"
              placeholder="Opowiedz użytkownikom, czym się zajmujesz."
              />
            <p class="text-[12px] text-[#65676B] mt-1.5">
              Opowiedz użytkownikom, czym się zajmujesz.
            </p>
          </div>
        </div>

        <div class="p-4 bg-white shrink-0 mt-auto border-t border-[#E5E5E5]">
          <button
            @click="createPage"
            class="w-full py-[9px] rounded-md font-semibold text-[15px] transition-colors"
            :class="isNameValid && isCategoryValid ? 'bg-[#1877F2] text-white hover:bg-[#166FE5]' : 'bg-[#E4E6EB] text-[#8A8D91] cursor-not-allowed'"
            :disabled="!isNameValid || !isCategoryValid"
          >
            Utwórz stronę
          </button>
          <p class="text-[12px] text-[#65676B] text-center mt-3 leading-snug">
            Tworząc stronę, akceptujesz
            <a href="#" class="text-[#1877F2] hover:underline font-semibold">Zasady dotyczące stron,<br>grup i wydarzeń</a>
          </p>
        </div>
      </template>

      <!-- KROK 1: SZCZEGÓŁOWA KONFIGURACJA -->
      <template v-else-if="step === 1">
        <div class="flex-1 overflow-y-auto px-4 py-2 custom-scrollbar">
          <div class="text-[13px] text-[#65676B] mb-1 font-semibold">
            Krok 1 z 5
          </div>

          <h1 class="text-[24px] font-bold leading-tight mb-2 text-[#050505]">
            Ukończ konfigurowanie strony
          </h1>

          <p class="text-[15px] text-[#65676B] leading-snug mb-4">
            Gotowe! Utworzono stronę <span class="font-bold text-[#050505]">{{ form.pageName }}</span>. Teraz dodaj więcej szczegółowych informacji, aby użytkownicy mogli łatwiej się z Tobą skontaktować.
          </p>

          <div class="inline-flex items-center gap-1.5 bg-[#25823B] text-white px-2 py-1 rounded-[4px] text-[13px] font-semibold mb-6">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
              <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm1 14.5h-2v-6h2zm0-8h-2v-2h2z"></path>
            </svg>
            Pomaga polepszyć wyniki reklam
          </div>

          <!-- KONTAKT -->
          <h2 class="text-[17px] font-bold text-[#050505] mb-3">Kontakt</h2>

          <div class="mb-4">
            <CustomInput
              v-model="form.website"
              label="Witryna internetowa"
            />
          </div>

          <div class="flex gap-2 mb-4">
            <div class="w-[90px] border border-[#CED0D4] rounded-md flex items-center justify-between px-3 h-[56px] bg-[#F0F2F5] cursor-pointer hover:bg-[#E4E6EB] transition-colors shrink-0">
              <span class="text-[15px] font-medium">{{ form.phoneCode }}</span>
              <svg class="w-4 h-4 text-[#050505] fill-current" viewBox="0 0 24 24"><path d="M7 10l5 5 5-5H7z"/></svg>
            </div>
            <div class="flex-1">
              <CustomInput
                v-model="form.phone"
                label="Numer telefonu"
                type="tel"
              />
            </div>
          </div>

          <div class="mb-6">
            <CustomInput
              v-model="form.email"
              label="Adres e-mail (opcjonalny)"
              type="email"
            />
          </div>

          <!-- LOKALIZACJA -->
          <h2 class="text-[17px] font-bold text-[#050505] mb-3">Lokalizacja</h2>

          <div class="mb-4">
            <CustomInput
              v-model="form.address"
              label="Adres"
            />
          </div>

          <div class="mb-4">
            <CustomInput
              v-model="form.city"
              label="Miejscowość"
            />
          </div>

          <div class="mb-6">
            <CustomInput
              v-model="form.zip"
              label="Kod pocztowy"
            />
          </div>

          <!-- GODZINY OTWARCIA -->
          <h2 class="text-[17px] font-bold text-[#050505] mb-1">Godziny otwarcia</h2>
          <p class="text-[15px] text-[#65676B] leading-snug mb-4">
            Poinformuj użytkowników o godzinach otwarcia lokalizacji.
          </p>

          <div class="flex flex-col gap-4 mb-4">
            <label class="flex items-start gap-3 cursor-pointer group">
              <div class="w-5 h-5 rounded-full border-[2px] mt-0.5 flex items-center justify-center transition-colors" :class="form.hours === 'none' ? 'border-[#1877F2]' : 'border-[#8A8D91] group-hover:border-[#65676B]'">
                <div v-if="form.hours === 'none'" class="w-2.5 h-2.5 bg-[#1877F2] rounded-full"></div>
              </div>
              <div class="flex-1">
                <div class="text-[15px] font-medium text-[#050505] leading-tight mb-0.5">Nie podano godzin otwarcia</div>
                <div class="text-[13px] text-[#65676B] leading-snug">Nie wyświetlaj żadnych godzin.</div>
              </div>
              <input type="radio" value="none" v-model="form.hours" class="hidden" />
            </label>

            <label class="flex items-start gap-3 cursor-pointer group">
              <div class="w-5 h-5 rounded-full border-[2px] mt-0.5 flex items-center justify-center transition-colors" :class="form.hours === 'always' ? 'border-[#1877F2]' : 'border-[#8A8D91] group-hover:border-[#65676B]'">
                <div v-if="form.hours === 'always'" class="w-2.5 h-2.5 bg-[#1877F2] rounded-full"></div>
              </div>
              <div class="flex-1">
                <div class="text-[15px] font-medium text-[#050505] leading-tight mb-0.5">Czynne całą dobę</div>
                <div class="text-[13px] text-[#65676B] leading-snug">Otwarte całodobowo przez 7 dni w tygodniu.</div>
              </div>
              <input type="radio" value="always" v-model="form.hours" class="hidden" />
            </label>

            <label class="flex items-start gap-3 cursor-pointer group">
              <div class="w-5 h-5 rounded-full border-[2px] mt-0.5 flex items-center justify-center transition-colors" :class="form.hours === 'selected' ? 'border-[#1877F2]' : 'border-[#8A8D91] group-hover:border-[#65676B]'">
                <div v-if="form.hours === 'selected'" class="w-2.5 h-2.5 bg-[#1877F2] rounded-full"></div>
              </div>
              <div class="flex-1">
                <div class="text-[15px] font-medium text-[#050505] leading-tight mb-0.5">Otwarte w wybranych godzinach</div>
                <div class="text-[13px] text-[#65676B] leading-snug">Podaj określone godziny.</div>
              </div>
              <input type="radio" value="selected" v-model="form.hours" class="hidden" />
            </label>
          </div>
        </div>

        <div class="px-4 py-3 bg-white shrink-0 mt-auto border-t border-[#E5E5E5] shadow-[0_-2px_4px_rgba(0,0,0,0.05)]">
          <div class="text-[15px] text-[#050505] mb-1">
            Kondycja strony: <span class="font-bold">Wymaga dopracowania</span>
          </div>
          <div class="text-[15px] text-[#050505] leading-snug mb-3">
            W porównaniu z podobnymi stronami o dużej aktywności.
          </div>
          <div class="h-1.5 w-full bg-[#E5E5E5] rounded-full mb-4"></div>

          <div class="flex gap-2">
            <button
              @click="step = 0"
              class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF] transition-colors"
            >
              Wstecz
            </button>
            <button
              @click="step = 2"
              class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E7F3FF] text-[#1877F2] hover:bg-[#DBE7F2] transition-colors"
            >
              Dalej
            </button>
          </div>
        </div>
      </template>

      <!-- KROK 2: ZDJĘCIA -->
      <template v-else-if="step === 2">
        <div class="flex-1 overflow-y-auto px-4 py-2 custom-scrollbar">
          <div class="text-[13px] text-[#65676B] mb-1 font-semibold">
            Krok 2 z 5
          </div>

          <h1 class="text-[24px] font-bold leading-tight mb-2 text-[#050505]">
            Dostosuj swoją stronę
          </h1>

          <p class="text-[15px] text-[#65676B] leading-snug mb-4">
            Twoje zdjęcie profilowe to jedna z pierwszych rzeczy, które widzą inne osoby. Spróbuj użyć logo lub obrazu, który można łatwo skojarzyć z Tobą.
          </p>

          <div class="inline-flex items-center gap-1.5 bg-[#25823B] text-white px-2 py-1 rounded-[4px] text-[13px] font-semibold mb-6">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
              <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm1 14.5h-2v-6h2zm0-8h-2v-2h2z"></path>
            </svg>
            Pomaga polepszyć wyniki reklam
          </div>

          <!-- Dodaj zdjęcie profilowe -->
          <div class="mb-4">
            <label v-if="!form.profileImage" class="block border border-[#CED0D4] rounded-[8px] p-6 text-center cursor-pointer hover:bg-[#F0F2F5] transition-colors">
              <input type="file" accept="image/*" class="hidden" @change="handleProfileImage" />
              <div class="w-[36px] h-[36px] bg-[#E4E6EB] rounded-full flex items-center justify-center mx-auto mb-3 text-[#050505]">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M19 11h-6V5h-2v6H5v2h6v6h2v-6h6v-2z"></path>
                </svg>
              </div>
              <div class="font-bold text-[17px] text-[#050505] mb-1">Dodaj zdjęcie profilowe</div>
              <div class="text-[15px] text-[#65676B]">lub przeciągnij i upuść</div>
            </label>
            <div v-else class="relative border border-[#CED0D4] rounded-[8px] h-[160px] overflow-hidden group">
              <img :src="form.profileImage" class="w-full h-full object-cover" />
              <button @click="removeProfileImage" class="absolute top-3 right-3 w-9 h-9 bg-white rounded-full flex items-center justify-center shadow-md hover:bg-gray-100 transition-colors z-10 text-[#050505]">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Dodaj zdjęcie w tle -->
          <div class="mb-6">
            <label v-if="!form.coverImage" class="block border border-[#CED0D4] rounded-[8px] p-6 text-center cursor-pointer hover:bg-[#F0F2F5] transition-colors">
              <input type="file" accept="image/*" class="hidden" @change="handleCoverImage" />
              <div class="w-[36px] h-[36px] bg-[#E4E6EB] rounded-full flex items-center justify-center mx-auto mb-3 text-[#050505]">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M19 11h-6V5h-2v6H5v2h6v6h2v-6h6v-2z"></path>
                </svg>
              </div>
              <div class="font-bold text-[17px] text-[#050505] mb-1">Dodaj zdjęcie w tle</div>
              <div class="text-[15px] text-[#65676B]">lub przeciągnij i upuść</div>
            </label>
            <div v-else class="relative border border-[#CED0D4] rounded-[8px] h-[160px] overflow-hidden group">
              <img :src="form.coverImage" class="w-full h-full object-cover" />
              <button @click="removeCoverImage" class="absolute top-3 right-3 w-9 h-9 bg-white rounded-full flex items-center justify-center shadow-md hover:bg-gray-100 transition-colors z-10 text-[#050505]">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Przycisk działania -->
          <button class="w-full bg-[#1877F2] text-white font-semibold text-[15px] py-2 rounded-md flex items-center justify-center gap-2 hover:bg-[#166FE5] transition-colors">
            <div class="w-5 h-5 bg-white rounded flex items-center justify-center">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="#1877F2">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14.5v-5H8.5v5H7v-5H5.5v5H4v-7h7v7h-2zM19 16.5h-5v-7h5v7z"></path>
              </svg>
            </div>
            Przycisk działania
          </button>
        </div>

        <div class="px-4 py-3 bg-white shrink-0 mt-auto border-t border-[#E5E5E5] shadow-[0_-2px_4px_rgba(0,0,0,0.05)]">
          <div class="text-[15px] text-[#050505] mb-1">
            Kondycja strony: <span class="font-bold">Przeciętna</span>
          </div>
          <div class="text-[15px] text-[#050505] leading-snug mb-3">
            W porównaniu z podobnymi stronami o dużej aktywności.
          </div>
          <div class="h-1.5 w-full bg-[#E5E5E5] rounded-full mb-4 flex overflow-hidden">
             <!-- Pasek postępu -->
            <div class="bg-[#B58A14] w-[35%] h-full rounded-full"></div>
          </div>

          <div class="flex gap-2">
            <button
              @click="step = 1"
              class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF] transition-colors"
            >
              Wstecz
            </button>
            <button
              @click="step = 3"
              class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E7F3FF] text-[#1877F2] hover:bg-[#DBE7F2] transition-colors"
            >
              Dalej
            </button>
          </div>
        </div>
      </template>

      <!-- KROK 3 (w UI "Krok 4 z 5"): POSZERZ GRUPĘ ODBIORCÓW -->
      <template v-else-if="step === 3">
        <div class="flex-1 overflow-y-auto px-4 py-2 custom-scrollbar flex flex-col">
          <div class="text-[13px] text-[#65676B] mb-1 font-semibold">
            Krok 4 z 5
          </div>

          <h1 class="text-[24px] font-bold leading-tight mb-2 text-[#050505]">
            Poszerz grupę odbiorców strony
          </h1>

          <p class="text-[15px] text-[#65676B] leading-snug mb-4">
            Rozwijaj stronę <span class="font-bold text-[#050505]">{{ form.pageName || 'Nazwa strony' }}</span>, zapraszając znajomych do nawiązania kontaktu z nią.
          </p>

          <div class="inline-flex items-center gap-1.5 bg-[#25823B] text-white px-2 py-1 rounded-[4px] text-[13px] font-semibold mb-10 w-fit">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
              <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm1 14.5h-2v-6h2zm0-8h-2v-2h2z"></path>
            </svg>
            Pomaga polepszyć wyniki reklam
          </div>

          <div class="flex flex-col items-center text-center mt-2 px-2">
            <!-- Ikona Grupy Ludzi -->
            <svg width="110" height="90" viewBox="0 0 100 90" fill="none" xmlns="http://www.w3.org/2000/svg" class="mb-6 mx-auto">
              <!-- Lewa postać -->
              <rect x="20" y="35" width="40" height="45" rx="8" fill="#A4A7AB" />
              <circle cx="40" cy="20" r="12" fill="#75A6FF" />
              <!-- Włosy lewa postać -->
              <path d="M28 20 C 28 8, 52 8, 52 20 L 52 14 C 52 5, 28 5, 28 14 Z" fill="#050505" />

              <!-- Prawa postać (nadpisująca lewą) -->
              <path d="M45 80 C 45 60, 95 60, 95 80 Z" fill="#5E6165" />
              <circle cx="70" cy="40" r="11" fill="#1877F2" />
              <!-- Włosy prawa postać (kok) -->
              <circle cx="60" cy="32" r="6" fill="#A4A7AB" />
            </svg>

            <h3 class="text-[19px] font-bold text-[#050505] mb-2 leading-tight">
              Dodaj znajomych później, aby rozwijać swoją stronę.
            </h3>
            <p class="text-[15px] text-[#65676B] leading-snug">
              Aby zaprosić znajomych do połączenia ze stroną <span class="font-bold">{{ form.pageName || 'Nazwa strony' }}</span>, najpierw dodaj ich do swojego profilu <span class="font-bold">{{ authStore.originalUser?.name || 'Profil osobisty' }}</span>. Wypróbuj tę funkcję po skonfigurowaniu swojej strony.
              <a href="#" class="text-[#1877F2] font-semibold hover:underline">Dowiedz się więcej</a>
            </p>
          </div>
        </div>

        <div class="px-4 py-3 bg-white shrink-0 mt-auto border-t border-[#E5E5E5] shadow-[0_-2px_4px_rgba(0,0,0,0.05)]">
          <div class="text-[15px] text-[#050505] mb-1">
            Kondycja strony: <span class="font-bold">Przeciętna</span>
          </div>
          <div class="text-[15px] text-[#050505] leading-snug mb-3">
            W porównaniu z podobnymi stronami o dużej aktywności.
          </div>
          <div class="h-1.5 w-full bg-[#E5E5E5] rounded-full mb-4 flex overflow-hidden">
             <!-- Pasek postępu (Krok 4 z 5) - nieco większy progres -->
            <div class="bg-[#B58A14] w-[45%] h-full rounded-full"></div>
          </div>

          <div class="flex gap-2">
            <button
              @click="step = 2"
              class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF] transition-colors"
            >
              Wstecz
            </button>
            <button
              @click="step = 4"
              class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E7F3FF] text-[#1877F2] hover:bg-[#DBE7F2] transition-colors"
            >
              Dalej
            </button>
          </div>
        </div>
      </template>

      <!-- KROK 4 (w UI "Krok 5 z 5"): OTRZYMUJ INFORMACJE -->
      <template v-else-if="step === 4">
        <div class="flex-1 overflow-y-auto px-4 py-2 custom-scrollbar">
          <div class="text-[13px] text-[#65676B] mb-1 font-semibold">
            Krok 5 z 5
          </div>

          <h1 class="text-[24px] font-bold leading-tight mb-2 text-[#050505]">
            Otrzymuj informacje dotyczące Twojej strony
          </h1>

          <p class="text-[15px] text-[#65676B] leading-snug mb-6">
            Włącz te funkcje, aby w pełni wykorzystać możliwości strony <span class="font-bold text-[#050505]">{{ form.pageName || 'Nazwa strony' }}</span>. W dowolnym momencie możesz je zmienić w ustawieniach.
          </p>

          <!-- Przełącznik 1 (Powiadomienia) -->
          <div class="flex items-start gap-3 mb-6">
            <div class="mt-1 text-[#050505]">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"></path>
              </svg>
            </div>
            <div class="flex-1 pr-2">
              <h3 class="text-[15px] font-bold text-[#050505] leading-snug mb-1">Powiadomienia w profilu dotyczące strony</h3>
              <p class="text-[13px] text-[#65676B] leading-snug">
                Nie przeocz żadnych aktualizacji dotyczących Twojej strony <span class="font-bold text-[#050505]">{{ form.pageName || 'Nazwa strony' }}</span> po przełączeniu na profil <span class="font-bold text-[#050505]">{{ authStore.originalUser?.name || 'Profil osobisty' }}</span>.
                <a href="#" class="text-[#1877F2] font-semibold hover:underline">Dowiedz się, jak przełączyć</a>
              </p>
            </div>
            <div class="shrink-0 pt-1">
              <label class="relative inline-flex items-center cursor-pointer">
                <input type="checkbox" v-model="form.pageNotifications" class="sr-only peer">
                <div class="w-11 h-6 bg-[#8A8D91] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#1877F2]"></div>
              </label>
            </div>
          </div>

          <!-- Przełącznik 2 (Marketingowe e-maile) -->
          <div class="flex items-start gap-3">
            <div class="mt-1 text-[#050505]">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"></path>
              </svg>
            </div>
            <div class="flex-1 pr-2">
              <h3 class="text-[15px] font-bold text-[#050505] leading-snug mb-1">Marketingowe i promocyjne e-maile dotyczące Twojej strony</h3>
              <p class="text-[13px] text-[#65676B] leading-snug">
                Dowiedz się więcej o produktach i usługach Meta, dzięki którym strona <span class="font-bold text-[#050505]">{{ form.pageName || 'Nazwa strony' }}</span> może łatwiej odnieść sukces.
              </p>
            </div>
            <div class="shrink-0 pt-1">
              <label class="relative inline-flex items-center cursor-pointer">
                <input type="checkbox" v-model="form.promotionalEmails" class="sr-only peer">
                <div class="w-11 h-6 bg-[#8A8D91] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#1877F2]"></div>
              </label>
            </div>
          </div>
        </div>

        <div class="px-4 py-3 bg-white shrink-0 mt-auto border-t border-[#E5E5E5] shadow-[0_-2px_4px_rgba(0,0,0,0.05)]">
          <div class="text-[15px] text-[#050505] mb-1">
            Kondycja strony: <span class="font-bold">Przeciętna</span>
          </div>
          <div class="text-[15px] text-[#050505] leading-snug mb-3">
            W porównaniu z podobnymi stronami o dużej aktywności.
          </div>
          <div class="h-1.5 w-full bg-[#E5E5E5] rounded-full mb-4 flex overflow-hidden">
             <!-- Pasek postępu (Krok 5 z 5) - jeszcze większy progres -->
            <div class="bg-[#B58A14] w-[55%] h-full rounded-full"></div>
          </div>

          <div class="flex gap-2">
            <button
              @click="step = 3"
              class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF] transition-colors"
            >
              Wstecz
            </button>
            <button
              @click="finishPage"
              :disabled="isSubmitting"
              class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#1877F2] text-white hover:bg-[#166FE5] transition-colors flex items-center justify-center gap-2"
              :class="{ 'opacity-70 cursor-not-allowed': isSubmitting }"
            >
              <svg v-if="isSubmitting" class="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
              </svg>
              <span>{{ isSubmitting ? 'Tworzenie...' : 'Gotowe' }}</span>
            </button>
          </div>
        </div>
      </template>
    </aside>

    <!-- GŁÓWNY WIDOK PODGLĄDU -->
    <main class="flex-1 flex flex-col bg-[#F0F2F5] items-center overflow-hidden p-4 mt-[70px]">
      <div
        :class="[
          'bg-white rounded-lg shadow-[0_1px_2px_rgba(0,0,0,0.1)] flex flex-col overflow-hidden transition-all duration-300 ease-in-out w-full h-full border border-[#CED0D4]',
          viewMode === 'mobile' ? 'max-w-[460px]' : 'max-w-[965px]',
        ]"
      >
        <!-- Nagłówek podglądu -->
        <div class="flex items-center justify-between px-5 py-3 bg-white shrink-0 ">
          <span class="font-bold text-[#050505] text-[15px]">{{ previewTitle }}</span>

          <div class="flex gap-2 text-[#65676B]">
            <button
              @click="viewMode = 'desktop'"
              :class="[
                'w-8 h-8 rounded-full flex items-center justify-center transition-colors',
                viewMode === 'desktop' ? 'text-[#1877F2] bg-[#E7F3FF]' : 'hover:bg-[#F0F2F5]',
              ]"
            >
              <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
              </svg>
            </button>

            <button
              @click="viewMode = 'mobile'"
              :class="[
                'w-8 h-8 rounded-full flex items-center justify-center transition-colors',
                viewMode === 'mobile' ? 'text-[#1877F2] bg-[#E7F3FF]' : 'hover:bg-[#F0F2F5]',
              ]"
            >
              <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round">
                <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
                <line x1="12" y1="18" x2="12.01" y2="18"></line>
              </svg>
            </button>
          </div>
        </div>

        <!-- Obszar przewijany podglądu -->
        <div class="flex-1 overflow-y-auto custom-scrollbar  p-4 flex justify-center">

          <!-- PODGLĄD DESKTOP -->
          <template v-if="viewMode === 'desktop'">
            <div class="border border-[#CED0D4] rounded-[8px] w-full flex flex-col overflow-hidden bg-white shadow-sm h-fit">
              <!-- Zdjęcie w tle i awatar (kontener relatywny) -->
              <div class="relative w-full rounded-t-[8px]">
                <!-- Zdjęcie w tle -->
                <div class="h-[350px] bg-gradient-to-b from-[#F5F6F8] to-[#EBEDF0] w-full rounded-t-[8px] overflow-hidden">
                  <img v-if="form.coverImage" :src="form.coverImage" class="w-full h-full object-cover" />
                </div>
                <!-- Awatar -->
                <div class="absolute left-1/2 -bottom-[14px] transform -translate-x-1/2 z-10">
                  <div class="w-[168px] h-[168px] rounded-full border-[4px] border-white bg-[#D8DADF] flex items-end justify-center overflow-hidden">
                    <img v-if="form.profileImage" :src="form.profileImage" class="w-full h-full object-cover" />
                    <svg v-else viewBox="0 0 24 24" class="w-[140px] h-[140px] fill-white translate-y-3">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                  </div>
                </div>
              </div>

              <!-- Tytuł strony i Biogram -->
              <div class="pt-[16px] px-4 relative bg-white flex flex-col items-center text-center">
                <h1
                  class="text-[32px] font-bold "
                  :class="form.pageName ? 'text-[#050505]' : 'text-[#BCC0C4]'"
                >
                  {{ form.pageName || 'Nazwa strony' }}
                </h1>
                <p v-if="form.bio" class="text-[17px] pb-2 text-[#050505] max-w-[500px] break-words">
                  {{ form.bio }}
                </p>
              </div>

              <div class="px-8 bg-white">
                <div class="h-px bg-[#CED0D4] w-full mb-1"></div>
              </div>

              <!-- Menu nawigacyjne -->
              <div class="px-4 flex items-center justify-between py-1 bg-white mb-2">
                <div class="flex items-center text-[15px] font-semibold text-[#65676B]">
                  <div class="py-3 px-4  cursor-pointer rounded-md ">Posty</div>
                  <div class="py-3 px-4 cursor-pointer rounded-md ">Informacje</div>
                  <div class="py-3 px-4 cursor-pointer rounded-md ">Obserwujący</div>
                  <div class="py-3 px-4 flex items-center gap-1.5 cursor-pointer rounded-md ">
                    Więcej
                    <svg viewBox="0 0 20 20" width="16" height="16" fill="currentColor">
                      <path d="M10 14a1 1 0 01-.755-.34l-5-5.5a1 1 0 011.51-1.32L10 11.528l4.245-4.668a1 1 0 011.51 1.32l-5 5.5A1 1 0 0110 14z"></path>
                    </svg>
                  </div>
                </div>
                <div class="flex gap-2">
                  <button class="bg-[#E4E6EB] text-[#BCC0C4] px-3.5 py-[7px] rounded-md font-semibold text-[15px] flex items-center gap-1.5 cursor-not-allowed">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
                    Obserwuj
                  </button>
                  <button class="bg-[#E4E6EB] text-[#BCC0C4] px-3.5 py-[7px] rounded-md font-semibold text-[15px] flex items-center gap-1.5 cursor-not-allowed">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/></svg>
                    Wyślij wiadomość
                  </button>
                  <button class="bg-[#E4E6EB] text-[#BCC0C4] w-[44px] py-[7px] rounded-md font-semibold text-[15px] flex items-center justify-center cursor-not-allowed tracking-widest">
                    •••
                  </button>
                </div>
              </div>

              <!-- Sekcja dolna -->
              <div class="bg-[#F0F2F5] p-4 flex gap-4 w-full border-t border-[#CED0D4]">
                <div class="w-[40%] bg-white rounded-[8px] shadow-sm border border-[#CED0D4] p-4 h-max">
                  <h2 class="font-bold text-[20px] text-[#050505] mb-4">Prezentacja</h2>
                  <div class="flex items-center gap-3 mb-4">
                    <div class="w-[20px] h-[20px] flex justify-center items-center shrink-0">
                      <svg viewBox="0 0 24 24" width="20" height="20" fill="#65676B">
                        <path d="M19 6h-3V5c0-1.65-1.35-3-3-3h-2C9.35 2 8 3.35 8 5v1H5c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-9-1c0-.55.45-1 1-1h2c.55 0 1 .45 1 1v1h-4V5zm11 13H3V8h18v10z"></path>
                      </svg>
                    </div>
                    <span class="text-[#050505] font-semibold text-[15px] leading-tight">0 obserwujący</span>
                  </div>
                  <div class="flex items-start gap-3">
                    <div class="w-[20px] h-[20px] flex justify-center items-center shrink-0 mt-0.5">
                      <svg viewBox="0 0 24 24" width="20" height="20" fill="#65676B">
                        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"></path>
                      </svg>
                    </div>
                    <span class="text-[#050505] font-semibold text-[15px] leading-tight">
                      <span class="font-bold">Strona</span> · <span class="font-normal">{{ form.category || 'Kategoria' }}</span>
                    </span>
                  </div>
                </div>

                <div class="w-[60%] flex gap-4 flex-col">
                  <div class="bg-white rounded-[8px] shadow-sm border border-[#CED0D4] p-4 flex items-center justify-between">
                    <h2 class="font-bold text-[20px] text-[#050505]">Posty</h2>
                    <button class="bg-[#E4E6EB] text-[#050505] px-3.5 py-1.5 rounded-md font-semibold text-[15px] flex items-center gap-2 hover:bg-[#D8DADF] transition-colors">
                      <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                        <path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z"></path>
                      </svg>
                      Filtry
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <!-- PODGLĄD MOBILE -->
          <template v-else>
            <div class="border border-[#CED0D4] rounded-[8px] w-full max-w-[420px] flex flex-col overflow-hidden bg-white shadow-sm h-fit">
              <!-- Zdjęcie w tle i awatar (kontener relatywny) -->
              <div class="relative w-full">
                <!-- Zdjęcie w tle -->
                <div class="h-[200px] bg-gradient-to-b from-[#F5F6F8] to-[#EBEDF0] w-full overflow-hidden">
                  <img v-if="form.coverImage" :src="form.coverImage" class="w-full h-full object-cover" />
                </div>
                <!-- Awatar -->
                <div class="absolute left-1/2 -bottom-[66px] transform -translate-x-1/2 z-10">
                  <div class="w-[132px] h-[132px] rounded-full border-[4px] border-white bg-[#D8DADF] flex items-end justify-center overflow-hidden shadow-sm">
                    <img v-if="form.profileImage" :src="form.profileImage" class="w-full h-full object-cover" />
                    <svg v-else viewBox="0 0 24 24" class="w-[110px] h-[110px] fill-white translate-y-2">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                  </div>
                </div>
              </div>

              <!-- Tytuł strony i Biogram -->
              <div class="pt-[80px] pb-4 px-4 bg-white text-center flex flex-col items-center">
                <h1
                  class="text-[28px] font-bold leading-tight"
                  :class="form.pageName ? 'text-[#050505]' : 'text-[#BCC0C4]'"
                >
                  {{ form.pageName || 'Nazwa strony' }}
                </h1>
                <p v-if="form.bio" class="text-[15px] text-[#050505] mt-1 text-center break-words w-full">
                  {{ form.bio }}
                </p>
              </div>

              <div class="px-4 bg-white">
                <div class="h-px bg-[#CED0D4] w-full mb-3"></div>
              </div>

              <!-- Przyciski akcji (Mobile) -->
              <div class="px-4 flex items-center justify-between pb-4 bg-white gap-2">
                <div class="text-[15px] font-semibold text-[#65676B] flex items-center gap-1 cursor-pointer hover:bg-[#F0F2F5] px-2 py-1.5 rounded-md">
                  Więcej
                  <svg viewBox="0 0 20 20" width="16" height="16" fill="currentColor">
                    <path d="M10 14a1 1 0 01-.755-.34l-5-5.5a1 1 0 011.51-1.32L10 11.528l4.245-4.668a1 1 0 011.51 1.32l-5 5.5A1 1 0 0110 14z"></path>
                  </svg>
                </div>
                <div class="flex gap-1.5">
                  <button class="bg-[#E4E6EB] text-[#BCC0C4] px-3 py-1.5 rounded-md font-semibold text-[14px] flex items-center gap-1.5 cursor-not-allowed">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
                    Obserwuj
                  </button>
                  <button class="bg-[#E4E6EB] text-[#BCC0C4] px-3 py-1.5 rounded-md font-semibold text-[14px] flex items-center gap-1.5 cursor-not-allowed">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/></svg>
                    Wyślij wiadomość
                  </button>
                  <button class="bg-[#E4E6EB] text-[#BCC0C4] px-3 py-1.5 rounded-md font-semibold text-[14px] flex items-center justify-center cursor-not-allowed">
                    •••
                  </button>
                </div>
              </div>

              <!-- Sekcja zawartości -->
              <div class="bg-[#F0F2F5] p-4 flex flex-col gap-4 border-t border-[#CED0D4]">
                <!-- Prezentacja -->
                <div class="bg-white rounded-[8px] p-4 border border-[#CED0D4] shadow-sm">
                  <h3 class="font-bold text-[20px] mb-4 text-[#050505]">Prezentacja</h3>
                  <div class="flex flex-col gap-4">
                    <div class="flex items-center gap-3">
                      <div class="w-5 h-5 flex justify-center items-center shrink-0 text-[#65676B]">
                        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19 6h-3V5c0-1.65-1.35-3-3-3h-2C9.35 2 8 3.35 8 5v1H5c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-9-1c0-.55.45-1 1-1h2c.55 0 1 .45 1 1v1h-4V5zm11 13H3V8h18v10z"></path></svg>
                      </div>
                      <span class="text-[#050505] font-semibold text-[15px]">0 obserwujący</span>
                    </div>
                    <div class="flex items-start gap-3">
                      <div class="w-5 h-5 flex justify-center items-center shrink-0 text-[#65676B] mt-0.5">
                        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"></path></svg>
                      </div>
                      <span class="text-[#050505] font-semibold text-[15px]">
                        <span class="font-bold">Strona</span> <span class="font-normal text-[#65676B]">· {{ form.category || 'Kategoria' }}</span>
                      </span>
                    </div>
                  </div>
                </div>

                <!-- Posty -->
                <div class="bg-white rounded-[8px] p-4 border border-[#CED0D4] shadow-sm flex items-center justify-between">
                  <h3 class="font-bold text-[20px] text-[#050505]">Posty</h3>
                  <button class="bg-[#E4E6EB] text-[#050505] px-3.5 py-1.5 rounded-md font-semibold text-[15px] flex items-center gap-2 hover:bg-[#D8DADF] transition-colors">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z"></path></svg>
                    Filtry
                  </button>
                </div>
              </div>
            </div>
          </template>

        </div>
      </div>
    </main>
  </div>

  <!-- Modal -->
  <BaseModal
    v-if="isHoursModalOpen"
    @close="isHoursModalOpen = false"
  >
    <CustomHoursModal
      @close="isHoursModalOpen = false"
      @save="isHoursModalOpen = false"
    />
  </BaseModal>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #bcc0c4;
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #8d949e;
}
</style>
