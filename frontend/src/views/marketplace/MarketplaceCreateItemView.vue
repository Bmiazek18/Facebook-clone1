<script setup lang="ts">
import { reactive, ref, computed } from 'vue'

// Import ikon
import CloseIcon from 'vue-material-design-icons/Close.vue'
import PlusIcon from 'vue-material-design-icons/Plus.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import CellphoneIcon from 'vue-material-design-icons/Cellphone.vue'
import RocketLaunchIcon from 'vue-material-design-icons/RocketLaunch.vue'
import LockIcon from 'vue-material-design-icons/Lock.vue'
import EarthIcon from 'vue-material-design-icons/Earth.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import SellerModal from '@/components/marketplace/modals/SellerModal.vue'
import CustomInput from '@/components/common/CustomInput.vue'
import CustomTextarea from '@/components/common/CustomTextarea.vue'
import CustomDropdown from '@/components/common/CustomDropdown.vue'
import { useAuthStore } from '@/stores/auth'
import AppCloseHeader from '@/components/common/AppCloseHeader.vue'
import { marketplaceApi } from '@/api/marketplace'
import { useRouter } from 'nuxt/app'

// --- TYPY ---

interface DropdownOption {
  id: string
  title: string
  description: string
}

interface FormState {
  title: string
  price: string
  category: string
  condition: string
  description: string
}

// Typ pól, które mogą być "focused"
type FocusedFieldType =
  | 'title'
  | 'price'
  | 'category'
  | 'condition'
  | 'description'
  | 'photo'
  | null

// --- STAN ---
const form = reactive<FormState>({
  title: '',
  price: '',
  category: '',
  condition: '',
  description: '',
})

const focusedField = ref<FocusedFieldType>(null)

const showMoreInfo = ref(false)

// Funkcja Spotlight
const getHighlightClass = (fieldName: string): string => {
  const isAnyFocused = focusedField.value !== null
  const isThisFocused = focusedField.value === fieldName

  // Logika dla sekcji szczegółów (podświetla się, gdy edytujemy kategorię lub stan)
  if (fieldName === 'details') {
    if (focusedField.value === 'category' || focusedField.value === 'condition') {
      return 'bg-[#EAF3FF] ring-2 ring-transparent opacity-100 -mx-2 px-2'
    }
  }

  if (isThisFocused) {
    return 'bg-[#EAF3FF] ring-2 ring-transparent opacity-100 -mx-2 px-2'
  } else if (isAnyFocused) {
    return 'bg-transparent opacity-30 blur-[0.5px]'
  } else {
    return 'bg-transparent opacity-100'
  }
}

const categories: DropdownOption[] = [
  { id: 'tools', title: 'Narzędzia', description: 'Narzędzia i sprzęt do warsztatu lub ogrodu.' },
  { id: 'furniture', title: 'Meble', description: 'Meble do domu i biura.' },
  { id: 'garden', title: 'Ogród', description: 'Wyposażenie ogrodu i rośliny.' },
  { id: 'electronics', title: 'Elektronika', description: 'Urządzenia elektroniczne i akcesoria.' },
  { id: 'automotive', title: 'Motoryzacja', description: 'Części samochodowe i akcesoria.' },
]

const conditions: DropdownOption[] = [
  { id: 'new', title: 'Nowy', description: 'Przedmiot jest nowy, nigdy nie używany.' },
  {
    id: 'like_new',
    title: 'Używany - jak nowy',
    description: 'Przedmiot używany, w doskonałym stanie.',
  },
  { id: 'good', title: 'Używany - dobry', description: 'Przedmiot używany, w dobrym stanie.' },
  {
    id: 'acceptable',
    title: 'Używany - akceptowalny',
    description: 'Przedmiot używany, w akceptowalnym stanie.',
  },
]

// Obsługa zdjęć
const fileInput = ref<HTMLInputElement | null>(null)
const uploadedImages = ref<string[]>([])

const triggerUpload = () => {
  fileInput.value?.click()
}

// Seller modal
const isSellerModalOpen = ref<boolean>(false)
const openSellerModal = () => {
  isSellerModalOpen.value = true
}
const closeSellerModal = () => {
  isSellerModalOpen.value = false
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement

  if (target.files) {
    const files = Array.from(target.files)
    files.forEach((file) => {
      if (file && uploadedImages.value.length < 10) {
        uploadedImages.value.push(URL.createObjectURL(file))
      }
    })
  }

  // Wyczyść input żeby można było dodać ten sam plik ponownie
  target.value = ''
}

const removeImage = (index: number) => {
  uploadedImages.value.splice(index, 1)
}
const auth = useAuthStore()
const router = useRouter()
const isPublishing = ref(false)

const publishListing = async () => {
  if (!form.title || isPublishing.value) return
  isPublishing.value = true

  try {
    let mappedCondition = 'USED'
    if (form.condition === 'new') {
      mappedCondition = 'NEW'
    }

    let mappedCategory = form.category || 'tools'

    await marketplaceApi.createListing({
      title: form.title,
      price: parseFloat(form.price) || 0.0,
      category: mappedCategory.toUpperCase(),
      condition: mappedCondition,
      description: form.description || '',
      latitude: 52.0689,
      longitude: 19.3824,
    })

    router.push('/marketplace')
  } catch (err) {
    console.error('Failed to publish listing via GraphQL:', err)
  } finally {
    isPublishing.value = false
  }
}

// Lightbox
const isLightboxOpen = ref(true)
const lightboxMedia = computed(() =>
  uploadedImages.value.map((img, index) => ({
    id: index,
    type: 'image' as const,
    imageUrl: img,
  })),
)
</script>

<template>
  <div class="flex h-screen w-full bg-theme-bg   text-theme-text overflow-hidden">
    <aside
      class="w-[360px] flex-shrink-0 bg-theme-bg-secondary shadow-sm flex flex-col border-r border-theme-border z-20 h-full"
    >
      <AppCloseHeader class="shadow-sm pb-2" />
      <div class="px-4 pt-4 pb-2 flex-shrink-0">
        <div class="flex justify-between items-start mb-1">
          <span class="text-[13px] text-theme-text-secondary font-normal">{{ $t('login.marketplace') }}</span>
          <button class="text-[15px] text-theme-primary font-semibold hover:underline">{{ $t('marketplace.zapiszWersjeRobocza') }}</button>
        </div>
        <h1 class="text-2xl font-bold leading-tight mb-4">{{ $t('marketplace.przedmiotNaSprzedaz') }}</h1>

        <div class="flex items-center gap-3 mb-4">
          <img
            :src="auth.currentUser?.avatar"
            :alt="$t('chat.avatar')"
            class="w-10 h-10 rounded-full border border-gray-100"
          />
          <div>
            <p class="font-bold text-[15px] text-theme-text">{{ auth.currentUser?.name }}</p>
            <div class="flex items-center gap-1 text-[13px] text-theme-text-secondary">
              <span>{{ $t('marketplace.oglaszanieWMarketplace') }}</span>
              <span>·</span>
              <EarthIcon :size="12" />
              <span>{{ $t('postFilter.privacyPublic') }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto px-4 pb-4 space-y-6 scrollbar-visible">
        <div
          @mouseenter="focusedField = 'photo'"
          @mouseleave="focusedField = null"
          class="group transition duration-200"
        >
          <div class="flex items-center gap-1 mb-3">
            <span class="font-medium text-gray-700 text-[12px]">{{ $t('marketplace.zdjecia') }}<span class="text-gray-900">{{ $t('marketplace.uploadedimagesLength10') }}</span>
            </span>
            <span class="text-[12px] text-gray-500 font-normal">{{ $t('marketplace.mozeszDodacMaksymalnie10') }}</span>
          </div>

          <div
            class="flex flex-col gap-4 p-4 border-2 border-gray-200 rounded-xl transition-all duration-300"
            :class="{ 'border-blue-500 bg-blue-50/10': focusedField === 'photo' }"
          >
            <div v-if="uploadedImages.length > 0" class="flex gap-3 overflow-x-auto pb-2">
              <div
                v-for="(img, index) in uploadedImages"
                :key="index"
                class="relative w-24 h-24 flex-shrink-0 bg-gray-100 rounded-lg border border-gray-200 overflow-hidden group/item"
              >
                <img :src="img" class="w-full h-full object-cover" />
                <button
                  @click.stop="removeImage(index)"
                  class="absolute top-1 right-1 bg-black/50 text-white rounded-full p-1 opacity-0 group-hover/item:opacity-100 transition-opacity"
                >
                  <CloseIcon :size="14" />
                </button>
              </div>
            </div>

            <div
              v-if="uploadedImages.length < 10"
              @click="triggerUpload"
              class="w-full py-10 flex flex-col items-center justify-center rounded-lg cursor-pointer transition-colors bg-gray-50/50"
            >
              <div
                class="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center mb-4 text-gray-600"
              >
                <PlusIcon :size="24" />
              </div>

              <div class="text-center">
                <h3 class="text-[18px] font-bold text-gray-900 leading-tight">{{ $t('marketplace.dodajZdjecia') }}</h3>
                <p class="text-[16px] text-gray-500 font-normal">{{ $t('pages.lubPrzeciagnijIUpusc') }}</p>
              </div>

              <input
                type="file"
                ref="fileInput"
                @change="handleFileChange"
                class="hidden"
                accept="image/*"
                multiple
              />
            </div>
          </div>
        </div>

        <div class="bg-theme-bg rounded-lg p-3 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <CellphoneIcon :size="24" class="text-gray-800" />
            <div>
              <p class="text-[13px] font-semibold leading-tight mb-0.5">{{ $t('marketplace.dodajZdjeciaBezposrednioZ') }}</p>
              <a href="#" class="text-[13px] text-theme-primary hover:underline"
                >{{ $t('auth.register.learnMore') }}</a
              >
            </div>
          </div>
          <button
            class="bg-theme-hover-strong hover:bg-[#cdd0d5] text-theme-text text-[13px] font-semibold px-3 py-1.5 rounded-md transition"
          >{{ $t('marketplace.wyprobuj') }}</button>
        </div>

        <div class="space-y-3">
          <div class="mb-1">
            <h3 class="font-bold text-[17px]">{{ $t('profile.wymagane') }}</h3>
            <p class="text-[13px] text-theme-text-secondary">{{ $t('marketplace.opisPowinienBycJak') }}</p>
          </div>

          <CustomInput
            id="title"
            :label="$t('groups.tytul')"
            v-model="form.title"
            @focus="focusedField = 'title'"
            @blur="focusedField = null"
          />

          <CustomInput
            id="price"
            :label="$t('marketplace.cena')"
            v-model="form.price"
            @focus="focusedField = 'price'"
            @blur="focusedField = null"
          />

          <CustomDropdown
            v-model="form.category"
            :options="categories"
            :label="$t('marketplace.kategoria')"
            @focus="focusedField = 'category'"
            @blur="focusedField = null"
          />

          <CustomDropdown
            v-model="form.condition"
            :options="conditions"
            :label="$t('marketplace.stan')"
            @focus="focusedField = 'condition'"
            @blur="focusedField = null"
          />

          <div class="border-t border-theme-border pt-4 mt-2">
            <div
              @click="showMoreInfo = !showMoreInfo"
              class="flex justify-between items-center cursor-pointer"
            >
              <div>
                <h3 class="font-bold text-[17px]">{{ $t('marketplace.wiecejInformacji') }}</h3>
                <p class="text-[13px] text-theme-text-secondary mt-0.5 max-w-[280px]">{{ $t('marketplace.wzbudzWiekszeZainteresowaniePodajac') }}</p>
              </div>
              <div
                class="p-2 bg-theme-bg-subtle rounded-full transition-transform"
                :class="{ 'rotate-180': showMoreInfo }"
              >
                <ChevronDownIcon :size="20" />
              </div>
            </div>

            <CustomTextarea
              v-if="showMoreInfo"
              id="description"
              :label="$t('createLive.description')"
              v-model="form.description"
              @focus="focusedField = 'description'"
              @blur="focusedField = null"
            />
          </div>

          <hr class="border-theme-border" />

          <div class="space-y-5 pt-1">
            <div class="flex items-center justify-between">
              <div class="flex gap-3">
                <div class="mt-0.5"><RocketLaunchIcon :size="24" class="text-gray-800" /></div>
                <div class="max-w-[220px]">
                  <h4 class="font-semibold text-[15px]">{{ $t('marketplace.promujOgloszeniePoOpublikowaniu') }}</h4>
                  <p class="text-[12px] text-theme-text-secondary mt-0.5 leading-snug">{{ $t('marketplace.dodajKrokWCelu') }}</p>
                </div>
              </div>
              <div class="w-12 h-7 bg-theme-border rounded-full relative cursor-pointer">
                <div class="w-5 h-5 bg-white rounded-full shadow absolute top-1 left-1"></div>
              </div>
            </div>

            <div class="flex items-center justify-between pb-4">
              <div class="flex gap-3 items-center">
                <div><LockIcon :size="24" class="text-gray-800" /></div>
                <div>
                  <h4 class="font-semibold text-[15px]">{{ $t('marketplace.ukryjPrzedZnajomymi') }}</h4>
                  <p class="text-[12px] text-theme-text-secondary mt-0.5">{{ $t('marketplace.toOgloszenieJestNadal') }}</p>
                </div>
              </div>
              <div class="w-12 h-7 bg-theme-border rounded-full relative cursor-pointer">
                <div class="w-5 h-5 bg-white rounded-full shadow absolute top-1 left-1"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        class="px-4 pb-4 pt-2 border-t border-theme-border bg-theme-bg-secondary shadow-[0_-2px_5px_rgba(0,0,0,0.05)] z-30"
      >
        <div class="flex gap-1 mb-3">
          <div class="h-1.5 flex-1 bg-theme-primary rounded-full"></div>
          <div class="h-1.5 flex-1 bg-theme-bg-subtle rounded-full"></div>
        </div>

        <button
          @click="publishListing"
          class="w-full py-2 rounded-md font-semibold text-[15px] transition select-none flex items-center justify-center gap-2"
          :class="
            form.title && !isPublishing
              ? 'bg-theme-primary text-white hover:bg-theme-primary-hover'
              : 'bg-theme-bg-subtle text-theme-text-secondary cursor-not-allowed'
          "
          :disabled="!form.title || isPublishing"
        >
          <span v-if="isPublishing">{{ $t('post.publishing') }}</span>
          <span v-else>{{ $t('post.publish') }}</span>
        </button>
      </div>
    </aside>

    <main
      class="flex-1 flex flex-col items-center justify-center p-8 relative overflow-hidden bg-theme-bg"
    >
      <div class="w-full h-[90vh] max-w-[980px] z-10 flex flex-col items-center">
        <h2 class="text-[17px] font-semibold mb-3 text-theme-text w-full text-left">{{ $t('chat.podglad') }}</h2>

        <div
          class="bg-theme-bg-secondary rounded-lg shadow-sm border border-theme-border w-full h-full flex overflow-hidden"
        >
          <div
            class="w-[60%] bg-theme-bg flex flex-col items-center justify-center text-center border-r border-theme-border relative transition-all duration-300 ease-in-out"
            :class="
              focusedField === 'photo'
                ? 'opacity-100'
                : focusedField
                  ? 'opacity-40 blur-[1px]'
                  : 'opacity-100'
            "
          >
            <MediaLightbox
              v-if="uploadedImages.length > 0"
              :media="lightboxMedia"
              :start-index="0"
              :fullscreen="false"
              v-model="isLightboxOpen"
              class="h-full rounded-xl"
            />

            <div v-else class="max-w-[260px] select-none">
              <h3 class="text-[17px] font-bold text-theme-text-secondary mb-2">{{ $t('marketplace.podgladTwojegoOgloszenia') }}</h3>
              <p class="text-[13px] text-theme-text-secondary leading-[1.3]">{{ $t('marketplace.tworzacOgloszenieMozeszWyswietlic') }}</p>
            </div>
          </div>

          <div class="w-[40%] flex flex-col h-full bg-theme-bg-secondary">
            <div class="p-5 flex-1 overflow-y-auto space-y-4 scrollbar-visible">
              <div class="space-y-1">
                <div
                  class="transition-all duration-300 rounded-lg py-1"
                  :class="getHighlightClass('title')"
                >
                  <h1 class="text-2xl font-bold leading-tight break-words text-theme-text">
                    {{ form.title || 'Tytuł' }}
                  </h1>
                </div>

                <div
                  class="transition-all duration-300 rounded-lg py-1"
                  :class="getHighlightClass('price')"
                >
                  <p class="text-[17px] text-theme-text">
                    {{ form.price ? form.price + ' zł' : 'Cena' }}
                  </p>
                  <p class="text-xs text-theme-text-secondary mt-1">{{ $t('marketplace.opublikowanoKilkaSekundTemu') }}<span class="font-semibold">{{ $t('marketplace.leczycaGmina') }}</span>
                  </p>
                </div>
              </div>

              <div
                class="transition-all duration-300 rounded-lg py-2"
                :class="getHighlightClass('details')"
              >
                <h3 class="text-[17px] font-bold mb-3 text-theme-text">{{ $t('marketplace.szczegoly') }}</h3>
                <div class="flex justify-between text-[15px] py-1">
                  <span class="text-theme-text-secondary">{{ $t('marketplace.stan') }}</span>
                  <span class="text-theme-text">{{ form.condition || '–' }}</span>
                </div>
                <div class="flex justify-between text-[15px] py-1">
                  <span class="text-theme-text-secondary">{{ $t('marketplace.kategoria') }}</span>
                  <span class="text-theme-text">{{ form.category || '–' }}</span>
                </div>
              </div>

              <div
                class="transition-all duration-300 rounded-lg py-2"
                :class="getHighlightClass('description')"
              >
                <p
                  class="text-[15px] text-theme-text leading-relaxed break-words whitespace-pre-line"
                >
                  {{ form.description || 'W tym miejscu pojawi się opis.' }}
                </p>
              </div>

              <hr class="border-theme-border my-2" />

              <div
                class="transition-all duration-300 py-1 rounded-lg"
                :class="focusedField ? 'opacity-30 blur-[1px]' : 'opacity-100'"
              >
                <div class="flex justify-between items-center mb-4">
                  <h3 class="text-[17px] font-bold text-theme-text">{{ $t('marketplace.informacjeOSprzedawcy') }}</h3>
                  <a href="#" class="text-[15px] text-theme-primary font-semibold hover:underline"
                    >{{ $t('marketplace.szczegoly') }}</a
                  >
                </div>
                <div @click="openSellerModal" class="flex items-center gap-3 cursor-pointer">
                  <img
                    src="https://i.pravatar.cc/150?img=12"
                    :alt="$t('chat.avatar')"
                    class="w-10 h-10 rounded-full border border-gray-200"
                  />
                  <div>
                    <p class="font-semibold text-[15px] text-theme-text">{{ $t('marketplace.bartoszMiazek') }}</p>
                    <p class="text-[13px] text-theme-text-secondary">{{ $t('marketplace.dolaczylADoFacebooka') }}</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="p-4 border-t border-theme-border bg-theme-bg-secondary z-20">
              <button
                disabled
                class="w-full bg-theme-bg-subtle text-theme-text-secondary font-bold py-2 rounded-md mb-3 cursor-not-allowed select-none transition-colors"
              >{{ $t('profile.sendMessage') }}</button>
              <p class="text-[11px] text-theme-text-secondary text-center leading-[1.2] px-1">
                <a href="#" class="text-theme-primary hover:underline">{{ $t('auth.register.learnMore') }}</a>{{ $t('marketplace.aboutPurchasingFromConsumers2') }}</p>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
  <BaseModal v-if="isSellerModalOpen" @close="closeSellerModal" :title="'Profil sprzedawcy'">
    <SellerModal
      :profile="{
        name: 'Bartosz Miazek',
        joinedText: 'Na Facebooku od 2015',
        location: '',
        avatarUrl: 'https://i.pravatar.cc/150?img=12',
      }"
      @close="closeSellerModal"
    />
  </BaseModal>
</template>

<style scoped></style>
