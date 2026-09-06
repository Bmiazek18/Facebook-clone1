<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useGroupsStore } from '~/stores/groups'

// Importy ikon z vue-material-design-icons
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import DragVerticalIcon from 'vue-material-design-icons/DragVertical.vue'
import BaseModal from '~/components/common/BaseModal.vue'
import CreateRuleModal from '~/components/groups/modals/CreateRuleModal.vue'

interface Rule {
  id: string
  title: string
  description: string
  orderIndex: number
}

const route = useRoute()
const groupId = route.params.id as string
const groupsStore = useGroupsStore()

const rules = ref<Rule[]>([])
const isRuleModalOpen = ref(false)
const activeMenuId = ref<string | null>(null) // To manage active dropdown menu for a rule

const fetchRules = async () => {
  const result = await groupsStore.fetchGroupRules(groupId)
  rules.value = result.map((r: any) => ({
    id: r.id,
    title: r.title,
    description: r.description,
    orderIndex: r.orderIndex
  }))
}

onMounted(() => {
  fetchRules()
})

const addRule = () => {
  isRuleModalOpen.value = true
}

const handleCreateRule = async (newRule: { title: string; description: string }) => {
  isRuleModalOpen.value = false
  const created = await groupsStore.createGroupRule(groupId, newRule.title, newRule.description)
  if (created) {
    await fetchRules()
  }
}

const handleDeleteRule = async (ruleId: string) => {
  activeMenuId.value = null
  const success = await groupsStore.deleteGroupRule(ruleId)
  if (success) {
    await fetchRules()
  }
}

// Native HTML5 Drag and Drop for Reordering
const draggedIndex = ref<number | null>(null)

const dragStart = (index: number, event: DragEvent) => {
  draggedIndex.value = index
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
  }
}

const dragEnter = (index: number) => {
  if (draggedIndex.value !== null && draggedIndex.value !== index) {
    const list = [...rules.value]
    const item = list.splice(draggedIndex.value, 1)[0]
    list.splice(index, 0, item)
    rules.value = list
    draggedIndex.value = index
  }
}

const dragEnd = async () => {
  draggedIndex.value = null
  const ruleIds = rules.value.map(r => r.id)
  await groupsStore.updateGroupRulesOrder(groupId, ruleIds)
}

const toggleMenu = (ruleId: string) => {
  if (activeMenuId.value === ruleId) {
    activeMenuId.value = null
  } else {
    activeMenuId.value = ruleId
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#f0f2f5] dark:bg-[#18191a] text-[#050505] dark:text-[#e4e6eb] font-sans p-4 sm:p-8 flex justify-center selection:bg-blue-600">

    <div class="w-full max-w-2xl">

      <!-- ========================================== -->
      <!-- WIDOK 1: GDY SĄ ZASADY (rules.length > 0)  -->
      <!-- ========================================== -->
      <div v-if="rules.length > 0" class="space-y-4">

        <!-- Nagłówek karty z przyciskiem Utwórz -->
        <div class="bg-white dark:bg-[#242526] rounded-xl p-4 sm:p-5 shadow-sm border border-gray-200 dark:border-[#3e4042] flex items-center justify-between">
          <h1 class="text-[20px] font-bold">{{ $t('groups.regulyGrupy') }}</h1>
          <button
            @click="addRule"
            class="text-[#1877f2] dark:text-[#4599ff] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] font-semibold text-[15px] px-3 py-1.5 rounded-md transition-colors cursor-pointer"
          >{{ $t('feed.utworz') }}</button>
        </div>

        <!-- Lista kart z regułami -->
        <div
          v-for="(rule, index) in rules"
          :key="rule.id"
          draggable="true"
          @dragstart="dragStart(index, $event)"
          @dragover.prevent
          @dragenter="dragEnter(index)"
          @dragend="dragEnd"
          :class="[
            'bg-white dark:bg-[#242526] rounded-xl p-4 sm:p-5 shadow-sm border border-gray-200 dark:border-[#3e4042] flex items-start gap-3 group transition-opacity duration-150',
            draggedIndex === index ? 'opacity-40 border-dashed border-[#1877f2]' : ''
          ]"
        >
          <!-- Ikona uchwytu przeciągania (drag handle) -->
          <div class="text-[#8c939d] dark:text-[#b0b3b8] mt-0.5 cursor-grab active:cursor-grabbing shrink-0">
            <DragVerticalIcon :size="20" />
          </div>

          <!-- Numeracja i treść reguły -->
          <div class="flex-1 min-w-0 pr-2">
            <div class="flex items-baseline gap-3">
              <span class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] shrink-0">
                {{ index + 1 }}
              </span>
              <h2 class="text-[17px] font-bold text-[#050505] dark:text-[#e4e6eb] leading-tight">
                {{ rule.title }}
              </h2>
            </div>

            <p class="text-[15px] text-[#65676b] dark:text-[#b0b3b8] leading-relaxed mt-2 pl-6">
              {{ rule.description }}
            </p>
          </div>

          <!-- Menu Opcji (Trzy Kropki) z usuwaniem -->
          <div class="relative shrink-0">
            <button 
              @click="toggleMenu(rule.id)"
              class="text-[#8c939d] dark:text-[#b0b3b8] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] p-1.5 rounded-full transition-colors shrink-0 cursor-pointer"
            >
              <DotsHorizontalIcon :size="20" />
            </button>
            <div 
              v-if="activeMenuId === rule.id"
              class="absolute right-0 mt-1 w-32 bg-white dark:bg-[#242526] border border-gray-200 dark:border-[#3e4042] rounded-lg shadow-lg py-1 z-10"
            >
              <button 
                @click="handleDeleteRule(rule.id)"
                class="w-full text-left px-4 py-2 text-[14px] text-red-600 hover:bg-gray-100 dark:hover:bg-[#3a3b3c] font-semibold transition-colors cursor-pointer"
              >{{ $t('notifications_page.delete') }}</button>
            </div>
          </div>
        </div>

      </div>

      <!-- ========================================== -->
      <!-- WIDOK 2: BRAK ZASAD (rules.length === 0)   -->
      <!-- ========================================== -->
      <div v-else class="flex flex-col items-center justify-center text-center pt-12 pb-8 px-4">

        <!-- Dedykowany wektor ikony dokumentu/listy ze zrzutu ekranu -->
        <div class="w-24 h-24 mb-6 flex items-center justify-center">
          <svg width="80" height="90" viewBox="0 0 80 90" fill="none" xmlns="http://www.w3.org/2000/svg">
            <!-- Tło karty zasady (szara kartka ze zagiętym rogiem) -->
            <path d="M10 15C10 9.47715 14.4772 5 20 5H55L75 25V80C75 85.5228 70.5228 90 65 90H20C14.4772 90 10 85.5228 10 80V15Z" fill="#9a9c9f"/>
            <!-- Niebieskie zagięcie w lewym górnym rogu -->
            <path d="M10 15C10 9.47715 14.4772 5 20 5H35L10 15Z" fill="#1877f2"/>

            <!-- Punkty i linie listy -->
            <circle cx="28" cy="35" r="4" fill="#ffffff"/>
            <rect x="42" y="32" width="23" height="6" rx="3" fill="#55575a"/>

            <circle cx="28" cy="52" r="4" fill="#ffffff"/>
            <rect x="42" y="49" width="23" height="6" rx="3" fill="#55575a"/>

            <circle cx="28" cy="69" r="4" fill="#ffffff"/>
            <rect x="42" y="66" width="23" height="6" rx="3" fill="#55575a"/>
          </svg>
        </div>

        <!-- Tytuł stanu pustego -->
        <h2 class="text-[22px] font-bold text-[#050505] dark:text-[#e4e6eb] mb-2">{{ $t('groups.brakSkonfigurowanychZasad') }}</h2>

        <!-- Opis -->
        <p class="text-[16px] text-[#65676b] dark:text-[#b0b3b8] max-w-md leading-relaxed mb-6">{{ $t('groups.uzyjRegulAbyNadac') }}</p>

        <!-- Przycisk Rozpocznij -->
        <button
          @click="addRule"
          class="bg-[#1877f2] hover:bg-[#166fe5] text-white font-semibold text-[15px] px-8 py-2.5 rounded-lg shadow-sm transition-colors cursor-pointer"
        >{{ $t('groups.rozpocznij') }}</button>

      </div>

    </div>

  </div>
  <BaseModal
    v-if="isRuleModalOpen"
    @close="isRuleModalOpen = false"
    :title="$t('groups.utworzWlasnaRegule')"
  >
    <CreateRuleModal @close="isRuleModalOpen = false" @create="handleCreateRule" />
  </BaseModal>
</template>
