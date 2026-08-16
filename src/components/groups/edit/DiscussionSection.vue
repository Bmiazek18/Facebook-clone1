<template>
  <div class="bg-white dark:bg-[#242526] rounded-xl p-4 shadow-sm border border-gray-200 dark:border-[#3e4042]">
    <h2 class="text-[20px] font-semibold text-[#050505] dark:text-[#e4e6eb] mb-2">Zarządzaj dyskusją</h2>

    <div class="flex flex-col">
      <template v-for="item in items" :key="item.id">
        <!-- REGULARNY PRZYCISK (Gdy element nie jest edytowany) -->
        <!-- REGULARNY PRZYCISK (Gdy element nie jest edytowany) -->
        <button
          v-if="activeEditId !== item.id"
          @click="toggleEdit(item.id)"
          :class="[
            'flex items-center justify-between py-3 border-b border-gray-200 dark:border-[#3e4042] last:border-0 hover:bg-gray-100 dark:hover:bg-[#3a3b3c]/50 transition-colors -mx-4 px-4 group text-left cursor-pointer',
            activeEditId && activeEditId !== item.id ? 'opacity-40 pointer-events-none' : ''
          ]"
        >
          <div class="flex flex-col pr-4">
            <span class="text-[15px] font-medium text-[#050505] dark:text-[#e4e6eb] leading-snug">
              {{ item.label }}
            </span>
            <span v-if="item.subLabel" class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] mt-0.5 leading-tight">
              {{ item.subLabel }}
            </span>
          </div>

          <div class="shrink-0 flex items-center justify-center text-[#65676b] dark:text-[#b0b3b8] group-hover:text-[#050505] dark:group-hover:text-[#e4e6eb] leading-snug">
            <PencilIcon v-if="item.action === 'edit'" :size="20" />
            <ChevronDownIcon v-else-if="item.action === 'dropdown'" :size="24" />
          </div>
        </button>

        <!-- 1. KTO MOŻE PUBLIKOWAĆ -->
        <div
          v-else-if="item.id === 'who_post' && activeEditId === 'who_post'"
          class="py-2 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-3">
            {{ item.label }}
          </h3>

          <div class="space-y-4">
            <label class="flex items-center justify-between cursor-pointer group">
              <span class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Każdy członek grupy</span>
              <input
                type="radio"
                name="who_post_radio"
                value="any_member"
                v-model="whoPostValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] cursor-pointer"
              />
            </label>

            <label class="flex items-center justify-between cursor-pointer group">
              <span class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Tylko administratorzy</span>
              <input
                type="radio"
                name="who_post_radio"
                value="admins_only"
                v-model="whoPostValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] cursor-pointer"
              />
            </label>
          </div>

          <!-- Przyciski: Anuluj / Zapisz -->
          <div class="flex items-center justify-end gap-3 mt-4 mb-2">
            <button
              type="button"
              @click="cancelEdit"
              class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors"
            >
              Anuluj
            </button>

            <button
              type="button"
              @click="saveWhoPost"
              :disabled="whoPostValue === initialWhoPostValue"
              :class="[
                'px-6 py-2 rounded-lg text-[15px] font-semibold transition-colors',
                whoPostValue !== initialWhoPostValue
                  ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
                  : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
              ]"
            >
              Zapisz
            </button>
          </div>
        </div>

        <!-- 2. ZATWIERDZANIE POSTÓW -->
        <div
          v-else-if="item.id === 'post_approval' && activeEditId === 'post_approval'"
          class="py-2 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-1">
            {{ item.label }}
          </h3>
          <p class="text-[14px] text-[#65676b] dark:text-[#b0b3b8] mb-4 leading-snug">
            Włącz tę funkcję, jeśli chcesz, aby administratorzy i moderatorzy zatwierdzali każdy post
          </p>

          <div class="space-y-4">
            <label class="flex items-center justify-between cursor-pointer group">
              <span class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Wszystkie posty</span>
              <input
                type="radio"
                name="post_approval_radio"
                value="all"
                v-model="postApprovalValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] cursor-pointer"
              />
            </label>

            <label class="flex items-center justify-between cursor-pointer group">
              <span class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Tylko posty anonimowe</span>
              <input
                type="radio"
                name="post_approval_radio"
                value="anonymous_only"
                v-model="postApprovalValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] cursor-pointer"
              />
            </label>

            <label class="flex items-center justify-between cursor-pointer group">
              <span class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Wył.</span>
              <input
                type="radio"
                name="post_approval_radio"
                value="off"
                v-model="postApprovalValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] cursor-pointer"
              />
            </label>
          </div>

          <div class="flex items-center justify-end gap-3 mt-4 mb-2">
            <button
              type="button"
              @click="cancelEdit"
              class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors"
            >
              Anuluj
            </button>

            <button
              type="button"
              @click="savePostApproval"
              :disabled="postApprovalValue === initialPostApprovalValue"
              :class="[
                'px-6 py-2 rounded-lg text-[15px] font-semibold transition-colors',
                postApprovalValue !== initialPostApprovalValue
                  ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
                  : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
              ]"
            >
              Zapisz
            </button>
          </div>
        </div>

        <!-- 3. ZATWIERDŹ ZMIANY -->
        <div
          v-else-if="item.id === 'approve_edits' && activeEditId === 'approve_edits'"
          class="py-2 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-3">
            {{ item.label }}
          </h3>

          <div class="space-y-4">
            <label class="flex items-start justify-between cursor-pointer group">
              <div class="flex-1 pr-4">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Włączone</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-tight mt-0.5">
                  Edytowane posty muszą być zatwierdzone przez administratora lub moderatora
                </div>
              </div>
              <input
                type="radio"
                name="approve_edits_radio"
                value="on"
                v-model="approveEditsValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-0.5 cursor-pointer"
              />
            </label>

            <label class="flex items-start justify-between cursor-pointer group">
              <div class="flex-1 pr-4">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Wyłączone</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-tight mt-0.5">
                  Członkowie mogą edytować swoje posty bezpośrednio
                </div>
              </div>
              <input
                type="radio"
                name="approve_edits_radio"
                value="off"
                v-model="approveEditsValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-0.5 cursor-pointer"
              />
            </label>
          </div>

          <div class="flex items-center justify-end gap-3 mt-4 mb-2">
            <button
              type="button"
              @click="cancelEdit"
              class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors"
            >
              Anuluj
            </button>

            <button
              type="button"
              @click="saveApproveEdits"
              :disabled="approveEditsValue === initialApproveEditsValue"
              :class="[
                'px-6 py-2 rounded-lg text-[15px] font-semibold transition-colors',
                approveEditsValue !== initialApproveEditsValue
                  ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
                  : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
              ]"
            >
              Zapisz
            </button>
          </div>
        </div>

        <!-- 4. SORTUJ KOMENTARZE -->
        <div
          v-else-if="item.id === 'sort_comments' && activeEditId === 'sort_comments'"
          class="py-2 border-b border-gray-200 dark:border-[#3e4042] last:border-0"
        >
          <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-1">
            {{ item.label }}
          </h3>
          <p class="text-[14px] text-[#65676b] dark:text-[#b0b3b8] mb-4 leading-snug">
            Będzie to ustawienie domyślne. Członkowie nadal będą mogli zmieniać kolejność wyświetlania komentarzy do poszczególnych postów.
          </p>

          <div class="space-y-4">
            <label class="flex items-start justify-between cursor-pointer group">
              <div class="flex-1 pr-4">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Proponowane ustawienie domyślne</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-tight mt-0.5">
                  Automatyczne wyświetlanie komentarzy w dowolnej kolejności, która zachęca do uczestnictwa w grupie.
                </div>
              </div>
              <input
                type="radio"
                name="sort_comments_radio"
                value="suggested"
                v-model="sortCommentsValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-0.5 cursor-pointer"
              />
            </label>

            <label class="flex items-start justify-between cursor-pointer group">
              <div class="flex-1 pr-4">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Najpopularniejsze komentarze</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-tight mt-0.5">
                  Wyświetl najpierw komentarze o największej aktywności.
                </div>
              </div>
              <input
                type="radio"
                name="sort_comments_radio"
                value="top"
                v-model="sortCommentsValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-0.5 cursor-pointer"
              />
            </label>

            <label class="flex items-start justify-between cursor-pointer group">
              <div class="flex-1 pr-4">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Od najnowszych</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-tight mt-0.5">
                  Wyświetlanie najnowszych komentarzy jako pierwszych.
                </div>
              </div>
              <input
                type="radio"
                name="sort_comments_radio"
                value="newest"
                v-model="sortCommentsValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-0.5 cursor-pointer"
              />
            </label>

            <label class="flex items-start justify-between cursor-pointer group">
              <div class="flex-1 pr-4">
                <div class="text-[15px] font-bold text-[#050505] dark:text-[#e4e6eb]">Wszystkie komentarze</div>
                <div class="text-[13px] text-[#65676b] dark:text-[#b0b3b8] leading-tight mt-0.5">
                  Wyświetlanie wszystkich komentarzy w kolejności chronologicznej, w tym potencjalny spam.
                </div>
              </div>
              <input
                type="radio"
                name="sort_comments_radio"
                value="all"
                v-model="sortCommentsValue"
                class="w-5 h-5 text-[#1877f2] focus:ring-[#1877f2] mt-0.5 cursor-pointer"
              />
            </label>
          </div>

          <div class="flex items-center justify-end gap-3 mt-4 mb-2">
            <button
              type="button"
              @click="cancelEdit"
              class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors"
            >
              Anuluj
            </button>

            <button
              type="button"
              @click="saveSortComments"
              :disabled="sortCommentsValue === initialSortCommentsValue"
              :class="[
                'px-6 py-2 rounded-lg text-[15px] font-semibold transition-colors',
                sortCommentsValue !== initialSortCommentsValue
                  ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
                  : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
              ]"
            >
              Zapisz
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// Importy ikon
import PencilIcon from 'vue-material-design-icons/Pencil.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'

const props = defineProps<{
  activeEditId: string | null
}>()

const emit = defineEmits<{
  (e: 'update:activeEditId', id: string | null): void
}>()

// 1. Stan dla 'Kto może publikować'
const whoPostValue = ref<'any_member' | 'admins_only'>('any_member')
const initialWhoPostValue = ref<'any_member' | 'admins_only'>('any_member')

// 2. Stan dla 'Zatwierdzanie postów'
const postApprovalValue = ref<'off' | 'all' | 'anonymous_only'>('off')
const initialPostApprovalValue = ref<'off' | 'all' | 'anonymous_only'>('off')

// 3. Stan dla 'Zatwierdź zmiany'
const approveEditsValue = ref<'off' | 'on'>('off')
const initialApproveEditsValue = ref<'off' | 'on'>('off')

// 4. Stan dla 'Sortuj komentarze'
const sortCommentsValue = ref<'suggested' | 'top' | 'newest' | 'all'>('suggested')
const initialSortCommentsValue = ref<'suggested' | 'top' | 'newest' | 'all'>('suggested')

const toggleEdit = (id: string) => {
  if (props.activeEditId === id) {
    cancelEdit()
  } else {
    emit('update:activeEditId', id)
  }
}

const cancelEdit = () => {
  whoPostValue.value = initialWhoPostValue.value
  postApprovalValue.value = initialPostApprovalValue.value
  approveEditsValue.value = initialApproveEditsValue.value
  sortCommentsValue.value = initialSortCommentsValue.value
  emit('update:activeEditId', null)
}

const saveWhoPost = () => {
  initialWhoPostValue.value = whoPostValue.value
  const item = items.find(i => i.id === 'who_post')
  if (item) {
    item.subLabel = whoPostValue.value === 'any_member' ? 'Każdy członek grupy' : 'Tylko administratorzy'
  }
  emit('update:activeEditId', null)
}

const savePostApproval = () => {
  initialPostApprovalValue.value = postApprovalValue.value
  const item = items.find(i => i.id === 'post_approval')
  if (item) {
    const labels = {
      off: 'Wył.',
      all: 'Wszystkie posty',
      anonymous_only: 'Tylko posty anonimowe'
    }
    item.subLabel = labels[postApprovalValue.value]
  }
  emit('update:activeEditId', null)
}

const saveApproveEdits = () => {
  initialApproveEditsValue.value = approveEditsValue.value
  const item = items.find(i => i.id === 'approve_edits')
  if (item) {
    item.subLabel = approveEditsValue.value === 'on' ? 'Włączone' : 'Wyłączone'
  }
  emit('update:activeEditId', null)
}

const saveSortComments = () => {
  initialSortCommentsValue.value = sortCommentsValue.value
  const item = items.find(i => i.id === 'sort_comments')
  if (item) {
    const labels = {
      suggested: 'Proponowane ustawienie domyślne',
      top: 'Najpopularniejsze komentarze',
      newest: 'Od najnowszych',
      all: 'Wszystkie komentarze'
    }
    item.subLabel = labels[sortCommentsValue.value]
  }
  emit('update:activeEditId', null)
}

const items = [
  { id: 'anonymous', label: 'Udział anonimowy', subLabel: 'Wł.', action: 'edit' },
  { id: 'who_post', label: 'Kto może publikować', subLabel: 'Każdy członek grupy', action: 'edit' },
  { id: 'post_approval', label: 'Zatwierdzanie postów', subLabel: 'Wył.', action: 'edit' },
  { id: 'approve_edits', label: 'Zatwierdź zmiany', subLabel: 'Wyłączone', action: 'edit' },
  { id: 'post_formats', label: 'Edytuj formaty posta', subLabel: '', action: 'edit' },
  { id: 'sort_posts', label: 'Sortuj posty', subLabel: 'Najtrafniejsze', action: 'edit' },
  { id: 'sort_comments', label: 'Sortuj komentarze', subLabel: 'Proponowane ustawienie domyślne', action: 'edit' },
  { id: 'default_tab', label: 'Karta Domyślne', subLabel: 'Dyskusja', action: 'edit' },
]
</script>
