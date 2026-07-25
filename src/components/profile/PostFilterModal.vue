<template>
  <div class="theme-bg shadow-lg w-full max-w-[540px] p-4   text-gray-900">
    <div class="mb-4">
      <h2 class="text-xl font-bold text-gray-900">
        {{ t('postFilter.title') }}
      </h2>
      <p class="text-gray-500 text-[15px] mt-1 leading-snug">
        {{ t('postFilter.description') }}
      </p>
    </div>

    <div class="space-y-3 mb-6">
      <div class="flex items-center justify-between">
        <label class="text-[17px] text-gray-900">{{ t('postFilter.yearLabel') }}</label>
        <div class="w-[180px]">
          <CustomDropdown
            v-model="filters.year"
            :options="yearOptions"
            :label="t('postFilter.yearLabel')"
          />
        </div>
      </div>

      <div class="flex items-center justify-between">
        <label class="text-[17px] text-gray-900">{{ t('postFilter.postedByLabel') }}</label>
        <div class="w-[180px]">
          <CustomDropdown
            v-model="filters.postedBy"
            :options="postedByOptions"
            :label="t('postFilter.postedByLabel')"
          />
        </div>
      </div>

      <div class="flex items-center justify-between">
        <label class="text-[17px] text-gray-900">{{ t('postFilter.privacyLabel') }}</label>
        <div class="w-[180px]">
          <CustomDropdown
            v-model="filters.privacy"
            :options="privacyOptions"
            :label="t('postFilter.privacyLabel')"
          />
        </div>
      </div>

      <div class="flex items-center justify-between">
        <label class="text-[17px] text-gray-900">{{ t('postFilter.taggedLabel') }}</label>
        <div class="w-[180px]">
          <CustomDropdown
            v-model="filters.tagged"
            :options="taggedOptions"
            :label="t('postFilter.taggedLabel')"
          />
        </div>
      </div>
    </div>

    <div class="flex justify-end space-x-3 pt-2">
      <button
        @click="clearFilters"
        class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-6 rounded-md transition-colors text-[15px]"
      >
        {{ t('postFilter.clear') }}
      </button>
      <button
        @click="applyFilters"
        class="bg-[#1877F2] hover:bg-blue-700 text-white font-semibold py-2 px-8 rounded-md transition-colors text-[15px]"
      >
        {{ t('postFilter.done') }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CustomDropdown from '@/components/common/CustomDropdown.vue'
import type { FunctionalComponent } from 'vue'

interface DropdownOption {
  id: string
  title: string
  description: string
  icon?: FunctionalComponent | string
}

const { t } = useI18n()

const emit = defineEmits(['apply', 'clear'])

const currentYear = new Date().getFullYear()
const years = Array.from({ length: 20 }, (_, i) => String(currentYear - i)) // Convert years to string

const yearOptions = computed<DropdownOption[]>(() => {
  const options: DropdownOption[] = [{ id: 'all', title: t('postFilter.yearAll'), description: '' }]
  years.forEach((year) => {
    options.push({ id: year, title: year, description: '' })
  })
  return options
})

const postedByOptions = computed<DropdownOption[]>(() => [
  { id: 'all', title: t('postFilter.postedByAll'), description: '' },
  { id: 'you', title: t('postFilter.postedByYou'), description: '' },
  { id: 'others', title: t('postFilter.postedByOthers'), description: '' },
])

const privacyOptions = computed<DropdownOption[]>(() => [
  { id: 'all', title: t('postFilter.privacyAll'), description: '' },
  { id: 'public', title: t('postFilter.privacyPublic'), description: '' },
  { id: 'friends', title: t('postFilter.privacyFriends'), description: '' },
  { id: 'only_me', title: t('postFilter.privacyOnlyMe'), description: '' },
])

const taggedOptions = computed<DropdownOption[]>(() => [
  { id: 'all', title: t('postFilter.taggedAll'), description: '' },
  { id: 'only_tagged', title: t('postFilter.taggedOnly'), description: '' },
])

const filters = reactive({
  year: 'all',
  postedBy: 'all',
  privacy: 'all',
  tagged: 'all',
})

const clearFilters = () => {
  filters.year = 'all'
  filters.postedBy = 'all'
  filters.privacy = 'all'
  filters.tagged = 'all'
  emit('clear')
}

const applyFilters = () => {
  emit('apply', { ...filters })
}
</script>
