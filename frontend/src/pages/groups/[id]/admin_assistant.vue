<script setup lang="ts">
import { ref, computed, markRaw } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import WelcomePostCriteriaModal from '@/components/groups/modals/WelcomePostCriteriaModal.vue'
import AdminAssistPromptSetup from '@/components/groups/modals/AdminAssistPromptSetup.vue'
import AdminAssistActionTemplates from '@/components/groups/modals/AdminAssistActionTemplates.vue'
import AdminAssistCommentTemplates from '@/components/groups/modals/AdminAssistCommentTemplates.vue'
import AdminAssistDeclineTemplates from '@/components/groups/modals/AdminAssistDeclineTemplates.vue'
import AdminAssistJoinCriteria from '@/components/groups/modals/AdminAssistJoinCriteria.vue'
import AdminAssistManagePosts from '@/components/groups/modals/AdminAssistManagePosts.vue'

// Importy ikon
import ClockOutlineIcon from 'vue-material-design-icons/ClockOutline.vue'
import StarIcon from 'vue-material-design-icons/Star.vue'
import MessageCheckOutlineIcon from 'vue-material-design-icons/MessageCheckOutline.vue'
import ChevronUpIcon from 'vue-material-design-icons/ChevronUp.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import TextBoxMultipleOutlineIcon from 'vue-material-design-icons/TextBoxMultipleOutline.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import ArchiveOutlineIcon from 'vue-material-design-icons/ArchiveOutline.vue'
import CommentMultipleOutlineIcon from 'vue-material-design-icons/CommentMultipleOutline.vue'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'

// --- Helper do niebieskich pigułek w tekstach reguł ---
const p = (text: string | number) =>
  `<span class="inline-flex items-center justify-center bg-[#e7f3ff] dark:bg-[#252f3d] text-[#1877f2] dark:text-[#4599ff] font-bold px-2 py-0.5 rounded mx-1">${text}</span>`

// Stan filtra czasu
const timeFilter = ref('dziś') // 'dziś' lub 'Wczoraj'

// Dane kryteriów (Rozbudowane o tablicę `rules` do wyświetlania aktywnych reguł)
const criteriaCategories = ref([
  {
    id: 'create_posts',
    title: 'Utwórz posty',
    count: 0,
    icon: markRaw(TextBoxMultipleOutlineIcon),
    expanded: false,
    items: [
      { label: 'Opublikuj post powitalny', modal: 'welcome', rules: [] as any[] },
      { label: 'Opublikuj post niestandardowy', modal: 'prompt_setup', rules: [] as any[] }
    ]
  },
  {
    id: 'manage_people',
    title: 'Zarządzaj osobami',
    count: 0,
    icon: markRaw(AccountGroupIcon),
    expanded: false,
    items: [
      { label: 'Zatwierdź prośbę o dołączenie, jeśli', modal: 'join_criteria', rules: [] as any[] },
      { label: 'Odrzuć prośbę o dołączenie, jeśli', modal: 'join_criteria', rules: [] as any[] },
      { label: 'Zawieś uprawnienia na 1 dzień, jeśli:', modal: 'prompt_setup', rules: [] as any[] }
    ]
  },
  {
    id: 'manage_posts',
    title: 'Zarządzaj postami',
    count: 0,
    icon: markRaw(ArchiveOutlineIcon),
    expanded: false,
    items: [
      { label: 'Odrzuć nadchodzący post, jeśli', modal: 'decline_templates', rules: [] as any[] },
      { label: 'Usuń opublikowany post, jeśli', modal: 'manage_posts', rules: [] as any[] },
      { label: 'Wyłącz komentowanie, jeśli', modal: 'action_templates', rules: [] as any[] },
      { label: 'Przenieś opublikowany post do weryfikacji, jeśli', modal: 'manage_posts', rules: [] as any[] },
      { label: 'Przekaż nadchodzący post do weryfikacji, jeśli', modal: 'manage_posts', rules: [] as any[] }
    ]
  },
  {
    id: 'manage_comments',
    title: 'Zarządzaj komentarzami',
    count: 2, // Zaktualizowany licznik
    icon: markRaw(CommentMultipleOutlineIcon),
    expanded: true, // Rozwinięty, aby pokazać dodane reguły
    items: [
      {
        label: 'Odrzuć nadchodzący komentarz, jeśli',
        modal: 'comment_templates',
        rules: [
          { text: 'Autor naruszył zasady grupy w ciągu ostatnich 28 dni.' },
          { text: `Autor został zgłoszony co najmniej ${p(3)} razy w ciągu ostatnich 28 dni.` }
        ]
      },
      {
        label: 'Przekaż opublikowany komentarz do sprawdzenia, jeśli',
        modal: 'comment_templates',
        rules: [] as any[]
      }
    ]
  }
])

const toggleCategory = (categoryId: string) => {
  const category = criteriaCategories.value.find(c => c.id === categoryId)
  if (category) {
    category.expanded = !category.expanded
  }
}

// --- REJESTR DYNAMICZNYCH MODALI ---
const modalRegistry = {
  welcome: {
    component: WelcomePostCriteriaModal,
    title: 'Dostosuj post powitalny',
    noHeader: false
  },
  prompt_setup: {
    component: AdminAssistPromptSetup,
    title: '',
    noHeader: true
  },
  action_templates: {
    component: AdminAssistActionTemplates,
    title: '',
    noHeader: true
  },
  comment_templates: {
    component: AdminAssistCommentTemplates,
    title: '',
    noHeader: true
  },
  decline_templates: {
    component: AdminAssistDeclineTemplates,
    title: '',
    noHeader: true
  },
  join_criteria: {
    component: AdminAssistJoinCriteria,
    title: '',
    noHeader: true
  },
  manage_posts: {
    component: AdminAssistManagePosts,
    title: '',
    noHeader: true
  }
} as const

type ModalKey = keyof typeof modalRegistry
const activeModal = ref<ModalKey | null>(null)

const activeModalConfig = computed(() => {
  if (!activeModal.value) return null
  return modalRegistry[activeModal.value]
})

const openModal = (key: ModalKey) => {
  activeModal.value = key
}

const closeModal = () => {
  activeModal.value = null
}

const handleAddCriteria = (item: { modal: ModalKey }) => {
  openModal(item.modal)
}
</script>

<template>
  <div class="min-h-screen bg-[#f0f2f5] dark:bg-theme-bg text-[#050505] dark:text-theme-text pt-4 font-sans selection:bg-blue-600">
    <div class="max-w-2xl mx-auto space-y-4">

      <!-- Sekcja: Hero (Baner powitalny) -->
      <div class="bg-white dark:bg-theme-bg-secondary rounded-xl shadow-sm p-6 relative flex flex-col items-center text-center">
        <!-- Ikona historii/zegara (Prawy górny róg) -->
        <button class="absolute top-4 right-4 text-[#65676b] dark:text-[#e4e6eb] bg-[#e4e6eb] dark:bg-[#3a3b3c] hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50] rounded-full p-1.5 transition-colors cursor-pointer">
          <ClockOutlineIcon :size="20" />
        </button>

        <!-- Grafika Asystenta (Gwiazdka) -->
        <div class="relative w-14 h-14 mb-4 flex items-center justify-center">
          <div class="absolute inset-0 bg-[#e75349] rotate-12 rounded-lg opacity-80"></div>
          <div class="absolute inset-0 bg-[#1877f2] -rotate-6 rounded-lg opacity-90"></div>
          <div class="absolute inset-0 flex items-center justify-center text-white z-10">
            <StarIcon :size="28" />
          </div>
        </div>

        <h1 class="text-[24px] font-bold mb-2">{{ $t('groups.asystentAdministratora') }}</h1>
        <p class="text-[15px] text-[#65676b] dark:text-[#b0b3b8] mb-1">{{ $t('groups.oszczedzajCzasChronGrupe') }}</p>
        <a href="#" class="text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:underline">{{ $t('groups.wiecejInformacji') }}</a>
      </div>

      <!-- Sekcja: Dzień w skrócie -->
      <div class="bg-white dark:bg-theme-bg-secondary rounded-xl shadow-sm p-5">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-[20px] font-bold">{{ $t('groups.dzienWSkrocie') }}</h2>
          <button class="text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:underline cursor-pointer">{{ $t('notifications_page.viewAll') }}</button>
        </div>

        <!-- Przyciski filtrów czasu -->
        <div class="flex gap-2 mb-4">
          <button
            @click="timeFilter = 'dziś'"
            :class="[
              'px-4 py-1.5 rounded-full text-[15px] font-semibold transition-colors cursor-pointer',
              timeFilter === 'dziś' ? 'bg-[#e7f3ff] dark:bg-[#263951] text-[#1877f2] dark:text-[#4599ff]' : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#050505] dark:text-[#e4e6eb] hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50]'
            ]"
          >{{ $t('groups.dzis') }}</button>
          <button
            @click="timeFilter = 'Wczoraj'"
            :class="[
              'px-4 py-1.5 rounded-full text-[15px] font-semibold transition-colors cursor-pointer',
              timeFilter === 'Wczoraj' ? 'bg-[#e7f3ff] dark:bg-[#263951] text-[#1877f2] dark:text-[#4599ff]' : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#050505] dark:text-[#e4e6eb] hover:bg-[#d8dadf] dark:hover:bg-[#4e4f50]'
            ]"
          >{{ $t('groups.wczoraj') }}</button>
        </div>

        <p class="text-[15px] text-[#65676b] dark:text-[#b0b3b8] mb-4">{{ $t('groups.asystentAdministratoraPodjalDzisiaj') }}<span class="font-bold text-[#050505] dark:text-[#e4e6eb]">{{ $t('dashboard.test') }}</span>{{ $t('groups.0Razy') }}</p>

        <!-- Karta statystyk -->
        <div class="bg-[#f7f8fa] dark:bg-[#1c1e21] rounded-xl p-4 w-40 flex flex-col justify-between border border-gray-200 dark:border-transparent">
          <div class="flex justify-between items-start mb-3">
            <span class="text-3xl font-bold leading-none text-[#050505] dark:text-[#e4e6eb]">0</span>
            <div class="bg-[#31a24c] rounded-full p-1.5 text-white">
              <MessageCheckOutlineIcon :size="18" />
            </div>
          </div>
          <span class="text-[15px] text-[#050505] dark:text-[#e4e6eb] font-medium leading-snug">{{ $t('groups.zatwierdzone') }}<br />{{ $t('search.posts') }}</span>
        </div>
      </div>

      <!-- Nagłówek sekcji z kryteriami -->
      <div class="pt-2 pb-1">
        <h2 class="text-[20px] font-bold mb-1 text-[#050505] dark:text-[#e4e6eb]">{{ $t('groups.dodajLubEdytujKryteria') }}</h2>
        <p class="text-[15px] text-[#65676b] dark:text-[#b0b3b8]">{{ $t('groups.zachowujeszKontroleDostosujKryteria') }}</p>
      </div>

      <!-- Karty poszczególnych kategorii (Akordeony) -->
      <div
        v-for="category in criteriaCategories"
        :key="category.id"
        class="bg-white dark:bg-theme-bg-secondary rounded-xl shadow-sm overflow-hidden"
      >
        <!-- Nagłówek Akordeonu -->
        <button
          @click="toggleCategory(category.id)"
          class="w-full px-4 py-4 flex justify-between items-center transition-colors hover:bg-gray-50 dark:hover:bg-[#3a3b3c]/50 cursor-pointer"
        >
          <div class="flex items-center gap-3 text-[#1877f2] dark:text-[#4599ff]">
            <component :is="category.icon" :size="24" />
            <span class="text-[17px] font-semibold">{{ category.title }} <span v-if="category.count > 0">· {{ category.count }}</span></span>
          </div>
          <component
            :is="category.expanded ? ChevronUpIcon : ChevronDownIcon"
            :size="24"
            class="text-[#65676b] dark:text-[#b0b3b8]"
          />
        </button>

        <!-- Opcje wewnątrz Akordeonu -->
        <div v-show="category.expanded" class="pb-2">
          <div
            v-for="(item, index) in category.items"
            :key="index"
            class="px-4 py-3 border-t border-gray-100 dark:border-[#3e4042]/50"
          >
            <!-- Wiersz opcji (Label + Przycisk Dodaj) -->
            <div class="flex justify-between items-center">
              <span :class="[
                'text-[17px] leading-snug',
                item.rules.length > 0 ? 'font-bold text-[#050505] dark:text-[#e4e6eb]' : 'font-medium text-[#65676b] dark:text-[#b0b3b8]'
              ]">
                {{ item.label }}
              </span>
              <button
                @click="handleAddCriteria(item)"
                class="px-4 py-1.5 rounded-lg bg-[#e7f3ff] dark:bg-[#263951] text-[#1877f2] dark:text-[#4599ff] font-semibold text-[15px] hover:bg-[#d8eaff] dark:hover:bg-[#32455e] transition-colors cursor-pointer shrink-0 ml-4"
              >{{ $t('call.dodaj') }}</button>
            </div>

            <!-- Aktywne (dodane) reguły dla danej opcji -->
            <div v-if="item.rules.length > 0" class="flex flex-col gap-2.5 mt-4 mb-2">
              <div
                v-for="(rule, rIndex) in item.rules"
                :key="rIndex"
                class="bg-[#f7f8fa] dark:bg-[#3a3b3c]/50 border border-transparent dark:border-[#4e4f50] rounded-xl p-4 flex justify-between items-center gap-4"
              >
                <span
                  class="text-[15px] font-medium text-[#050505] dark:text-[#e4e6eb] leading-snug"
                  v-html="rule.text"
                ></span>
                <button class="w-8 h-8 rounded-full hover:bg-[#e4e6eb] dark:hover:bg-[#4e4f50] flex items-center justify-center text-[#65676b] dark:text-[#b0b3b8] transition-colors cursor-pointer shrink-0">
                  <PencilIcon :size="20" />
                </button>
              </div>
            </div>

          </div>
        </div>
      </div>

    </div>

    <!-- Dynamiczny modal registry -->
    <BaseModal
      v-if="activeModalConfig"
      :title="activeModalConfig.title"
      :noHeader="activeModalConfig.noHeader"
      @close="closeModal"
    >
      <component :is="activeModalConfig.component" @close="closeModal" />
    </BaseModal>
  </div>
</template>
