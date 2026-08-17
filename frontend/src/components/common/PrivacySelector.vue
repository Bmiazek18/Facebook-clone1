<script setup lang="ts">
import { ref, computed } from 'vue'
import type { DefineComponent } from 'vue'
import { useCreatePostStore } from '@/stores/createPost'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import CloseFriendSelector from './CloseFriendSelector.vue'
import { useSlideTransition } from '@/composables/ui/useSlideTransition'
import '@/assets/animations/slideTransition.css'

// --- Import Ikony z vue-material-design-icons ---
import EarthIcon from 'vue-material-design-icons/Earth.vue'
import LockIcon from 'vue-material-design-icons/Lock.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue'
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue'
import AccountIcon from 'vue-material-design-icons/Account.vue' // <-- Dodano do obsługi pozycji "Wyświetlaj tylko..."
import CheckboxMarkedIcon from 'vue-material-design-icons/CheckboxMarked.vue'
import CheckboxBlankOutlineIcon from 'vue-material-design-icons/CheckboxBlankOutline.vue'

const props = defineProps<{
  initialPrivacy?: string
}>()

const emit = defineEmits<{
  (e: 'navigate', viewName: string): void
  (e: 'back'): void
  (e: 'confirm', payload: { id: string; setDefault: boolean }): void
  (e: 'update-title', title: string): void
}>()

// --- Definicje Opcji Prywatności ---
interface PrivacyOption {
  id: string
  label: string
  description?: string
  actionText?: string // Tekst niebieskiego linku (np. "Utwórz listę")
  iconComponent: DefineComponent<object, object, unknown>
}

const privacyOptions: PrivacyOption[] = [
  {
    id: 'public',
    label: 'Publiczne',
    description: 'Każdy na Facebooku i poza nim',
    iconComponent: EarthIcon,
  },
  {
    id: 'friends',
    label: 'Znajomi',
    description: 'Twoi znajomi na Facebooku',
    iconComponent: AccountGroupIcon,
  },
  {
    id: 'close_friends',
    label: 'Bliscy znajomi',
    actionText: 'Edytuj bliskich znajomych',
    iconComponent: AccountStarIcon,
  },
  {
    id: 'friends_except',
    label: 'Nie wyświetlaj...',
    actionText: 'Wybierz znajomych',
    iconComponent: AccountMultipleMinusIcon,
  },
  {
    id: 'specific_friends',
    label: 'Wyświetlaj tylko...',
    actionText: 'Wybierz znajomych',
    iconComponent: AccountIcon,
  },
  {
    id: 'only_me',
    label: 'Tylko ja',
    description: 'Widoczne tylko dla Ciebie',
    iconComponent: LockIcon,
  },
]

const createPostStore = useCreatePostStore()
const { postData } = storeToRefs(createPostStore)

const tempSelectedOption = ref(props.initialPrivacy ?? postData.value.privacy ?? 'friends')
const setDefault = ref(false)

const readDefaultFromCookie = (): string | null => {
  try {
    const m = document.cookie.match('(?:^|; )' + 'fc_default_privacy' + '=([^;]*)')
    return m && m[1] ? decodeURIComponent(m[1]) : null
  } catch {
    return null
  }
}

const defaultPrivacyId = computed(() => {
  try {
    return localStorage.getItem('fc_default_privacy') ?? readDefaultFromCookie()
  } catch {
    return null
  }
})

const selectOption = (id: string) => {
  tempSelectedOption.value = id
  if (defaultPrivacyId.value && tempSelectedOption.value === defaultPrivacyId.value) {
    setDefault.value = true
  }
}

const toggleSetDefault = () => {
  if (tempSelectedOption.value === defaultPrivacyId.value) return
  setDefault.value = !setDefault.value
}

const { t } = useI18n()

const {
  wrapperRef,
  currentView: currentSubView,
  transitionName,
  navigateTo,
  navigateBack,
  onEnter,
  onAfterEnter,
} = useSlideTransition('options')

const isTransitioningHeight = ref(false)

const onEnterWithTransition = (el: Element) => {
  isTransitioningHeight.value = true
  onEnter(el)
}

const onAfterEnterWithTransition = () => {
  isTransitioningHeight.value = false
  onAfterEnter()
}

const goToOptions = () => {
  navigateBack()
  emit('update-title', t('post.selectPrivacy'))
}

const goToCloseFriends = () => {
  navigateTo('close_friends')
  emit('update-title', t('post.closeFriendSelector'))
}

const goBack = () => {
  if (currentSubView.value === 'close_friends') {
    goToOptions()
    return true
  }
  return false
}

defineExpose({ goBack })

const handleActionClick = (id: string) => {
  if (id === 'close_friends') {
    goToCloseFriends()
  } else {
    console.log('Kliknięto akcję dla ID:', id)
  }
}

const handleDone = () => {
  emit('confirm', { id: tempSelectedOption.value, setDefault: setDefault.value })
}
</script>

<template>
  <div
    class="flex flex-col bg-white max-w-[520px] w-full h-[480px] rounded-xl text-[#050505] select-none relative overflow-hidden"
  >
    <div
      class="relative h-full w-full"
      :class="{ 'transition-[height] duration-300 ease-in-out': isTransitioningHeight }"
      ref="wrapperRef"
    >
      <Transition
        :name="transitionName"
        @enter="onEnterWithTransition"
        @after-enter="onAfterEnterWithTransition"
      >
        <!-- SUBVIEW: OPTIONS -->
        <div
          v-if="currentSubView === 'options'"
          key="options"
          class="view-container flex flex-col h-full w-full bg-white"
        >
          <!-- GŁÓWNY KONTENER PRZEWIJANY (Nagłówek + Lista opcji razem) -->
          <div class="grow overflow-y-auto pb-2 fb-custom-scrollbar">
            <!-- Nagłówek (teraz wewnątrz scrolla) -->
            <div class="px-2 pb-4">
              <h2 class="text-[17px] font-medium leading-tight">Kto może zobaczyć Twój post?</h2>
              <p class="text-[#65676b] text-[15px] mt-1.5 leading-snug t">
                Post pojawi się w Aktualnościach, w Twoim profilu oraz w wynikach wyszukiwania.
              </p>
            </div>

            <!-- Lista opcji -->
            <div
              v-for="option in privacyOptions"
              :key="option.id"
              class="flex items-center px-2 py-2.5 my-0.5 rounded-lg cursor-pointer transition-all duration-150"
              :class="{
                'bg-[#ebf5ff] hover:bg-[#dfe9f2]': tempSelectedOption === option.id,
                'hover:bg-[#f2f2f2]': tempSelectedOption !== option.id,
              }"
              @click="selectOption(option.id)"
            >
              <component
                :is="option.iconComponent"
                :size="26"
                class="mr-3.5 text-[#050505] shrink-0"
              />

              <div class="grow min-w-0 pr-2">
                <p class="text-[15px] font-medium leading-tight text-[#050505]">
                  {{ option.label }}
                </p>

                <p v-if="option.description" class="text-[#65676b] text-[13px] mt-0.5 truncate t">
                  {{ option.description }}
                </p>

                <button
                  v-else-if="option.actionText"
                  type="button"
                  class="text-[#1877f2] font-medium text-[14px] mt-0.5 hover:underline text-left block"
                  @click.stop="handleActionClick(option.id)"
                >
                  {{ option.actionText }}
                </button>
              </div>

              <div class="ml-2 shrink-0 flex items-center justify-center">
                <div
                  class="w-6 h-6 rounded-full flex items-center justify-center transition-colors"
                  :class="
                    tempSelectedOption === option.id
                      ? 'bg-[#1877f2]'
                      : 'border-[2px] border-[#8a8d91] bg-white'
                  "
                >
                  <div
                    v-if="tempSelectedOption === option.id"
                    class="w-2.5 h-2.5 rounded-full bg-white"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <!-- STOPKA (jedyna stała, przyklejona część na dole) -->
          <div class="px-2 border-t border-gray-200 shrink-0 bg-white">
            <!-- Checkbox przeniesiony na prawą stronę, dokładnie jak na screenie -->
            <div
              class="flex items-center justify-between mb-4 mt-1 cursor-pointer"
              :class="{ 'opacity-40 pointer-events-none': tempSelectedOption === defaultPrivacyId }"
              @click="toggleSetDefault"
            >
              <span class="text-[#050505] text-[14px]">Ustaw jako domyślną grupę odbiorców.</span>

              <component
                :is="
                  tempSelectedOption === defaultPrivacyId || setDefault
                    ? CheckboxMarkedIcon
                    : CheckboxBlankOutlineIcon
                "
                :size="24"
                class="ml-3 shrink-0"
                :class="
                  tempSelectedOption === defaultPrivacyId || setDefault
                    ? 'text-[#1877f2]'
                    : 'text-[#8a8d91]'
                "
              />
            </div>

            <div class="flex justify-end space-x-2.5">
              <button
                type="button"
                class="py-2 bg-[#1a69e0] text-white font-semibold text-[15px] rounded-lg transition-colors shadow-sm w-full"
                @click="handleDone"
              >
                Dalej
              </button>
            </div>
          </div>
        </div>

        <!-- SUBVIEW: CLOSE FRIENDS -->
        <CloseFriendSelector
          v-else-if="currentSubView === 'close_friends'"
          key="close_friends"
          class="view-container"
          @back="goToOptions"
        />
      </Transition>
    </div>
  </div>
</template>

<style scoped>
/* Pasek scrolla stylizowany na natywny Facebooka */
.fb-custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.fb-custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.fb-custom-scrollbar::-webkit-scrollbar-thumb {
  background: #bcc0c4;
  border-radius: 4px;
}
.fb-custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #8a8d91;
}

.view-container {
  width: 100%;
  top: 0;
  left: 0;
}
</style>
