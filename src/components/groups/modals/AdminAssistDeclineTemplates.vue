<script setup lang="ts">
import { ref, markRaw } from 'vue'
import { useSlideTransition } from '@/composables/ui/useSlideTransition'
import '@/assets/animations/slideTransition.css'

// Importy ikon
import RocketLaunchOutlineIcon from 'vue-material-design-icons/RocketLaunchOutline.vue'
import TextBoxRemoveOutlineIcon from 'vue-material-design-icons/TextBoxRemoveOutline.vue'
import CheckDecagramIcon from 'vue-material-design-icons/CheckDecagram.vue'
import GavelIcon from 'vue-material-design-icons/Gavel.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import MessageOffOutlineIcon from 'vue-material-design-icons/MessageOffOutline.vue'
import AccountArrowRightOutlineIcon from 'vue-material-design-icons/AccountArrowRightOutline.vue'
import ImageMultipleOutlineIcon from 'vue-material-design-icons/ImageMultipleOutline.vue'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'

const emit = defineEmits<{
  (e: 'close'): void
}>()

// --- Helper do niebieskich pigułek w tekstach reguł ---
const p = (text: string | number) =>
  `<span class="inline-flex items-center justify-center bg-[#e7f3ff] dark:bg-[#252f3d] text-[#1877f2] dark:text-[#4599ff] font-bold px-2 py-0.5 rounded mx-1">${text}</span>`

// --- Definicje szablonów (zgodnie ze zrzutami ekranu) ---
const templates = [
  {
    id: 'spam',
    title: 'Posty uznane za spam',
    desc: 'Zapobiegaj pojawianiu się niechcianych postów i innych rodzajów spamu w grupie.',
    icon: markRaw(TextBoxRemoveOutlineIcon),
    iconBg: 'bg-[#ff5a5f]',
    criteriaCount: 4,
    rules: [
      'Post zawiera określone słowa kluczowe',
      'Autor nie ma zdjęcia profilowego',
      'Post zawiera mniej niż 10 znaków',
      'Post zawiera linki do określonych witryn'
    ]
  },
  {
    id: 'fake_profiles',
    title: 'Posty z fałszywych profili',
    desc: 'Zapewnij publikowanie w grupie wyłącznie postów z autentycznych profili.',
    icon: markRaw(CheckDecagramIcon),
    iconBg: 'bg-[#8a3ffc]',
    criteriaCount: 2,
    rules: [
      'Autor nie ma zdjęcia profilowego',
      `Autor ma konto na Facebooku od co najwyżej ${p('1 miesiąca')}`
    ]
  },
  {
    id: 'rules_violation',
    title: 'Posty, które naruszają zasady grupy',
    desc: 'Zapewnij zgodność postów z zasadami grupy.',
    icon: markRaw(GavelIcon),
    iconBg: 'bg-[#00a48d]',
    criteriaCount: 3,
    rules: [
      `Autor ma konto na Facebooku od co najwyżej ${p('1 miesiąca')}`,
      'Autor naruszył zasady grupy w ciągu ostatnich 28 dni',
      'Post zawiera ponownie udostępnione materiały spoza grupy'
    ]
  },
  {
    id: 'profanity',
    title: 'Posty z wulgaryzmami',
    desc: 'Zapobiegaj pojawianiu się wulgaryzmów i innych słów kluczowych w postach w grupie.',
    icon: markRaw(AccountGroupIcon),
    iconBg: 'bg-[#ff7a59]',
    criteriaCount: 2,
    rules: [
      `Autor został zgłoszony co najmniej ${p(3)} razy w ciągu ostatnich 28 dni`,
      'Post zawiera mniej niż 10 znaków'
    ]
  },
  {
    id: 'author_activity',
    title: 'Posty oparte na aktywności autora',
    desc: 'Ogranicz uczestnictwo osób, które wcześniej naruszały zasady lub których posty były odrzucane.',
    icon: markRaw(MessageOffOutlineIcon),
    iconBg: 'bg-[#5c4dff]',
    criteriaCount: 2,
    rules: [
      'Autor nie miał 100% zatwierdzonych lub opublikowanych postów w ciągu ostatnich 28 dni',
      'Autor naruszył zasady grupy w ciągu ostatnich 28 dni'
    ]
  },
  {
    id: 'reshared_content',
    title: 'Posty z ponownie udostępnionymi materiałami',
    desc: 'Ogranicz ponowne udostępnianie, aby zachęcić do publikowania oryginalnych postów w grupie.',
    icon: markRaw(AccountArrowRightOutlineIcon),
    iconBg: 'bg-[#00a48d]',
    criteriaCount: 2,
    rules: [
      'Post zawiera ponownie udostępnione materiały spoza grupy',
      'Post zawiera link'
    ]
  },
  {
    id: 'media',
    title: 'Posty z multimediami',
    desc: 'Ogranicz zdjęcia, filmy oraz inne multimedia i zezwól na udostępnianie tylko postów tekstowych.',
    icon: markRaw(ImageMultipleOutlineIcon),
    iconBg: 'bg-[#d81b60]',
    criteriaCount: 3,
    rules: [
      `Post zawiera co najmniej ${p(1)} zdjęcie`,
      'Post zawiera film',
      'Post zawiera link'
    ]
  }
]

// --- Stan formularza ---
const selectedTemplate = ref<typeof templates[0] | null>(null)

// --- Integracja useSlideTransition ---
const {
  wrapperRef,
  currentView,
  transitionName,
  navigateTo,
  navigateBack,
  onEnter,
  onAfterEnter,
} = useSlideTransition('list')

const isTransitioningHeight = ref(false)

const onEnterWithTransition = (el: Element) => {
  isTransitioningHeight.value = true
  onEnter(el)
}

const onAfterEnterWithTransition = () => {
  isTransitioningHeight.value = false
  onAfterEnter()
}

// --- Obsługa nawigacji ---
const handleSelectTemplate = (template: typeof templates[0]) => {
  selectedTemplate.value = template
  navigateTo('detail')
}

const handleBack = () => {
  navigateBack()
  setTimeout(() => {
    selectedTemplate.value = null
  }, 300)
}

const handleSave = () => {
  console.log('Dodano szablon do Asystenta administratora:', selectedTemplate.value?.id)
  emit('close')
}
</script>

<template>
  <div class="flex flex-col w-[550px] max-w-full mx-auto bg-white dark:bg-[#242526] font-sans text-[#050505] dark:text-[#e4e6eb] shadow-lg rounded-xl overflow-hidden border border-gray-200 dark:border-[#3e4042]">

    <div
      class="relative w-full overflow-hidden"
      :class="{ 'transition-[height] duration-300 ease-in-out': isTransitioningHeight }"
      ref="wrapperRef"
    >
      <Transition
        :name="transitionName"
        @enter="onEnterWithTransition"
        @after-enter="onAfterEnterWithTransition"
      >
        <!-- ========================================== -->
        <!-- WIDOK 1: LISTA SZABLONÓW                   -->
        <!-- ========================================== -->
        <div v-if="currentView === 'list'" key="list" class="view-container">
          <div class="p-4 sm:p-6 pb-8">

            <div class="mb-8">
              <h1 class="text-[24px] font-bold leading-tight mb-1">
                Rozpocznij
              </h1>
              <p class="text-[15px] text-[#65676b] dark:text-[#b0b3b8]">
                Utwórz własne kryterium lub wybierz kryterium domyślne w celu dostosowania.
              </p>
            </div>

            <!-- Utwórz własne -->
            <button class="w-full flex items-center gap-4 bg-[#f7f8fa] dark:bg-[#3a3b3c]/50 hover:bg-[#e4e6eb] dark:hover:bg-[#4e4f50] p-5 rounded-2xl transition-colors cursor-pointer text-left mb-8">
              <div class="w-[46px] h-[46px] rounded-full flex items-center justify-center shrink-0 bg-[#1877f2] text-white">
                <RocketLaunchOutlineIcon :size="24" />
              </div>
              <div class="flex flex-col">
                <span class="text-[17px] font-bold leading-tight">Utwórz własne</span>
                <span class="text-[14px] text-[#65676b] dark:text-[#b0b3b8] mt-0.5">Dodaj niestandardowe kryterium do swojej grupy.</span>
              </div>
            </button>

            <!-- Ustawienia domyślne -->
            <div>
              <h2 class="text-[13px] font-bold text-[#65676b] dark:text-[#b0b3b8] uppercase tracking-wide mb-3">
                Ustawienia domyślne
              </h2>

              <div class="flex flex-col gap-4">
                <button
                  v-for="tpl in templates"
                  :key="tpl.id"
                  @click="handleSelectTemplate(tpl)"
                  class="w-full flex items-start gap-4 bg-[#f7f8fa] dark:bg-[#3a3b3c]/50 hover:bg-[#e4e6eb] dark:hover:bg-[#4e4f50] p-5 rounded-2xl transition-colors cursor-pointer text-left"
                >
                  <div :class="['w-[46px] h-[46px] rounded-full flex items-center justify-center shrink-0 text-white', tpl.iconBg]">
                    <component :is="tpl.icon" :size="24" />
                  </div>
                  <div class="flex flex-col flex-1">
                    <span class="text-[17px] font-bold leading-tight">{{ tpl.title }}</span>
                    <span class="text-[14px] text-[#65676b] dark:text-[#b0b3b8] mt-1 leading-snug">{{ tpl.desc }}</span>
                    <!-- Pigułka z ilością kryteriów -->
                    <div class="mt-2.5 bg-[#e4e6eb] dark:bg-[#4e4f50] text-[#050505] dark:text-[#e4e6eb] text-[13px] font-semibold px-2.5 py-1 rounded-full w-fit">
                      {{ tpl.criteriaCount }} kryteriów
                    </div>
                  </div>
                </button>
              </div>
            </div>

          </div>
        </div>

        <!-- ========================================== -->
        <!-- WIDOK 2: SZCZEGÓŁY SZABLONU                -->
        <!-- ========================================== -->
        <div v-else-if="currentView === 'detail' && selectedTemplate" key="detail" class="view-container flex flex-col max-h-[85vh]">

          <!-- Nagłówek -->
          <div class="px-4 py-3 flex items-center justify-between border-b border-gray-200 dark:border-[#3e4042]">
            <button
              @click="handleBack"
              class="w-9 h-9 rounded-full bg-[#e4e6eb] dark:bg-[#3a3b3c] hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50] flex items-center justify-center text-[#65676b] dark:text-[#b0b3b8] transition-colors cursor-pointer shrink-0"
            >
              <ArrowLeftIcon :size="20" />
            </button>
            <h1 class="text-[17px] font-bold leading-tight text-center truncate px-4">
              {{ selectedTemplate.title }}
            </h1>
            <button
              @click="emit('close')"
              class="w-9 h-9 rounded-full bg-[#e4e6eb] dark:bg-[#3a3b3c] hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50] flex items-center justify-center text-[#65676b] dark:text-[#b0b3b8] transition-colors cursor-pointer shrink-0"
            >
              <CloseIcon :size="20" />
            </button>
          </div>

          <!-- Przewijana zawartość -->
          <div class="p-4 sm:p-6 pt-5 overflow-y-auto flex-1">

            <h2 class="text-[24px] font-bold leading-tight mb-6">
              Świetnie! Sprawdź je i dostosuj przed dodaniem
            </h2>

            <!-- Wizytówka wybranego szablonu -->
            <div class="flex items-start gap-4 mb-8">
              <div :class="['w-[46px] h-[46px] rounded-full flex items-center justify-center shrink-0 text-white', selectedTemplate.iconBg]">
                <component :is="selectedTemplate.icon" :size="24" />
              </div>
              <div class="flex flex-col flex-1">
                <span class="text-[17px] font-bold leading-tight">{{ selectedTemplate.title }}</span>
                <span class="text-[15px] text-[#65676b] dark:text-[#b0b3b8] mt-1 leading-snug">{{ selectedTemplate.desc }}</span>
                <div class="mt-2.5 bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#050505] dark:text-[#e4e6eb] text-[13px] font-semibold px-2.5 py-1 rounded-full w-fit">
                  {{ selectedTemplate.criteriaCount }} kryteriów
                </div>
              </div>
            </div>

            <!-- Lista Reguł -->
            <div>
              <h3 class="text-[20px] font-bold mb-4">Odrzuć nadchodzący post, jeśli</h3>

              <div class="flex flex-col gap-3">
                <div
                  v-for="(rule, index) in selectedTemplate.rules"
                  :key="index"
                  class="bg-[#f7f8fa] dark:bg-[#3a3b3c]/50 border border-transparent dark:border-[#4e4f50] rounded-xl p-4 flex items-center justify-between gap-4"
                >
                  <span
                    class="text-[15px] font-medium text-[#050505] dark:text-[#e4e6eb] leading-snug"
                    v-html="rule"
                  ></span>

                  <button class="w-8 h-8 rounded-full hover:bg-[#e4e6eb] dark:hover:bg-[#4e4f50] flex items-center justify-center text-[#65676b] dark:text-[#b0b3b8] transition-colors cursor-pointer shrink-0">
                    <PencilIcon :size="20" />
                  </button>
                </div>
              </div>
            </div>

          </div>

          <!-- Stopka z akcjami -->
          <div class="border-t border-gray-200 dark:border-[#3e4042] p-4 bg-white dark:bg-[#242526] shrink-0 flex flex-col items-center">
            <button
              @click="handleSave"
              class="w-full bg-[#1877f2] hover:bg-[#166fe5] text-white font-semibold text-[15px] py-2.5 rounded-lg transition-colors cursor-pointer shadow-sm"
            >
              Dodaj do Asystenta administratora
            </button>

            <p class="text-[13px] text-[#bcc0c4] dark:text-[#65676b] font-semibold mt-3 text-center">
              Przekaż nadchodzący post do weryfikacji, jeśli
            </p>
          </div>
        </div>

      </Transition>
    </div>
  </div>
</template>

<style scoped>
.view-container {
  width: 100%;
}
</style>
