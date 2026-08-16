<template>
  <div class="bg-white dark:bg-[#242526] rounded-xl p-4 shadow-sm border border-gray-200 dark:border-[#3e4042]">
    <div class="mb-4">
      <h2 class="text-[20px] font-semibold text-[#050505] dark:text-[#e4e6eb] leading-snug">Kryteria Asystenta administratora</h2>
      <p class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] mt-0.5 leading-snug">
        Dodaj kryteria automatyzacji działań asystenta w tej grupie.
      </p>
    </div>

    <!-- Akordeony Asystenta -->
    <div class="space-y-3">
      <div
        v-for="category in criteriaCategories"
        :key="category.id"
        class="border border-gray-200 dark:border-[#3e4042] rounded-xl overflow-hidden"
      >
        <!-- Nagłówek Akordeonu -->
        <button
          @click="toggleCategory(category.id)"
          class="w-full px-4 py-3 flex justify-between items-center transition-colors hover:bg-gray-100 dark:hover:bg-[#3a3b3c]/50 text-left cursor-pointer"
        >
          <div class="flex items-center gap-3 text-[#1877f2] dark:text-[#4599ff]">
            <component :is="category.icon" :size="20" />
            <span class="text-[15px] font-semibold">{{ category.title }} · {{ category.count }}</span>
          </div>
          <component
            :is="category.expanded ? ChevronUpIcon : ChevronDownIcon"
            :size="20"
            class="text-[#b0b3b8]"
          />
        </button>

        <!-- Opcje wewnątrz Akordeonu -->
        <div v-show="category.expanded" class="border-t border-gray-200 dark:border-[#3e4042] bg-gray-50/50 dark:bg-[#1c1e21]/30">
          <div
            v-for="(item, index) in category.items"
            :key="index"
            class="px-4 py-3 flex justify-between items-center border-b border-gray-200 dark:border-[#3e4042] last:border-0 hover:bg-gray-100/50 dark:hover:bg-[#3a3b3c]/20"
          >
            <span class="text-[14px] font-medium text-[#65676b] dark:text-[#b0b3b8] pr-4 leading-tight">
              {{ item.label }}
            </span>
            <button
              @click="handleAddCriteria(item)"
              class="px-3.5 py-1.5 rounded-lg bg-[#e7f3ff] hover:bg-[#d8eaff] dark:bg-[#252f3d] dark:hover:bg-[#2d3a4d] text-[#1877f2] dark:text-[#4599ff] font-semibold text-[13px] transition-colors cursor-pointer shrink-0"
            >
              Dodaj
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw } from 'vue'

// Importy ikon
import ChevronUpIcon from 'vue-material-design-icons/ChevronUp.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import TextBoxMultipleOutlineIcon from 'vue-material-design-icons/TextBoxMultipleOutline.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import ArchiveOutlineIcon from 'vue-material-design-icons/ArchiveOutline.vue'
import CommentMultipleOutlineIcon from 'vue-material-design-icons/CommentMultipleOutline.vue'

const emit = defineEmits<{
  (e: 'open-modal', key: string): void
}>()

const criteriaCategories = ref([
  {
    id: 'create_posts',
    title: 'Utwórz posty',
    count: 0,
    icon: markRaw(TextBoxMultipleOutlineIcon),
    expanded: false,
    items: [
      { label: 'Opublikuj post powitalny', modal: 'welcome' },
      { label: 'Opublikuj post niestandardowy', modal: 'prompt_setup' }
    ]
  },
  {
    id: 'manage_people',
    title: 'Zarządzaj osobami',
    count: 0,
    icon: markRaw(AccountGroupIcon),
    expanded: false,
    items: [
      { label: 'Zatwierdź prośbę o dołączenie, jeśli', modal: 'join_criteria' },
      { label: 'Odrzuć prośbę o dołączenie, jeśli', modal: 'join_criteria' },
      { label: 'Zawieś uprawnienia na 1 dzień, jeśli:', modal: 'prompt_setup' }
    ]
  },
  {
    id: 'manage_posts',
    title: 'Zarządzaj postami',
    count: 0,
    icon: markRaw(ArchiveOutlineIcon),
    expanded: false,
    items: [
      { label: 'Odrzuć nadchodzący post, jeśli', modal: 'decline_templates' },
      { label: 'Usuń opublikowany post, jeśli', modal: 'manage_posts' },
      { label: 'Wyłącz komentowanie, jeśli', modal: 'action_templates' },
      { label: 'Przenieś opublikowany post do weryfikacji, jeśli', modal: 'manage_posts' },
      { label: 'Przekaż nadchodzący post do weryfikacji, jeśli', modal: 'manage_posts' }
    ]
  },
  {
    id: 'manage_comments',
    title: 'Zarządzaj komentarzami',
    count: 0,
    icon: markRaw(CommentMultipleOutlineIcon),
    expanded: false,
    items: [
      { label: 'Odrzuć nadchodzący komentarz, jeśli', modal: 'comment_templates' },
      { label: 'Przekaż opublikowany komentarz do sprawdzenia, jeśli', modal: 'comment_templates' }
    ]
  }
])

const toggleCategory = (categoryId: string) => {
  const category = criteriaCategories.value.find(c => c.id === categoryId)
  if (category) {
    category.expanded = !category.expanded
  }
}

const handleAddCriteria = (item: { modal: string }) => {
  emit('open-modal', item.modal)
}
</script>
