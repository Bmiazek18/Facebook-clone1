<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import BriefcaseIcon from 'vue-material-design-icons/Briefcase.vue'
import SchoolIcon from 'vue-material-design-icons/School.vue'
import SendIcon from 'vue-material-design-icons/Send.vue'
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import { getUserById } from '@/data/users'

interface Props {
  name: string
  userId?: number
}

const props = withDefaults(defineProps<Props>(), {
  userId: undefined
})

const router = useRouter()

const user = computed(() => {
  return props.userId ? getUserById(props.userId) : null
})

const handleViewProfile = () => {
  if (props.userId) {
    router.push({ name: 'userProfile', params: { userId: props.userId } })
  }
}
</script>

<template>
  <VMenu placement="top-start">
    <div class="font-extrabold text-[15px] text-theme-text">{{ props.name }}</div>

    <template #popper>
      <div class="bg-theme-bg-secondary rounded-lg shadow-xl max-w-sm mx-auto overflow-hidden">

        <div class="flex justify-end p-2 pb-0">
          <button
            class="text-gray-500 hover:text-gray-700 p-1 transition focus:outline-none"
            aria-label="Zamknij kartę"
          >
            <CloseIcon :size="24" class="h-6 w-6" />
          </button>
        </div>

        <div class="px-4 pb-4 pt-0">
          <div class="flex items-start space-x-4">

            <div class="shrink-0">
              <img
                :src="user?.avatar || 'https://picsum.photos/id/400/80/80'"
                :alt="user?.name || props.name"
                class="h-20 w-20 rounded-full object-cover -mt-7"
              />
            </div>

            <div class="grow">
              <h2 class="text-xl font-bold text-theme-text">{{ user?.name || props.name }}</h2>

              <div v-if="user?.job" class="mt-3 flex items-start space-x-3">
                <span class="text-theme-text pt-0.5 shrink-0">
                  <BriefcaseIcon :size="20" class="h-5 w-5" />
                </span>
                <p class="text-theme-text text-sm leading-snug">
                  <span class="font-semibold">{{ user.job }}</span>
                  <span v-if="user.company"> w {{ user.company }}</span>
                </p>
              </div>

              <div v-if="user?.education" class="mt-4 flex items-start space-x-3">
                <span class="text-theme-text pt-0.5 shrink-0">
                  <SchoolIcon :size="20" class="h-5 w-5" />
                </span>
                <p class="text-theme-text text-sm leading-snug">
                  Studiuje {{ user.education }} na {{ user.school }}
                </p>
              </div>

              <div v-if="user?.mutualFriendsCount" class="mt-2 text-xs text-theme-text-secondary">
                {{ user.mutualFriendsCount }} wspólnych znajomych
              </div>
            </div>
          </div>

          <div v-if="user" class="mt-6 border-t border-gray-200 pt-4 flex justify-between space-x-2 font-[15px]">

            <button
              @click="handleViewProfile"
              class="flex-1 flex items-center justify-center space-x-2 px-4 py-2 text-sm font-semibold rounded-md bg-blue-600 text-white hover:bg-blue-700 transition"
            >
              <SendIcon :size="20" />
              <span>Wyświetl profil</span>
            </button>

            <button
              class="flex-1 flex items-center justify-center space-x-2 px-4 py-2 text-sm font-semibold rounded-md bg-gray-200 text-gray-800 hover:bg-gray-300 transition"
            >
              <AccountPlusIcon :size="20" />
              <span>Dodaj znajomego</span>
            </button>

            <button
              class="p-2 text-sm font-semibold rounded-md bg-gray-200 text-gray-800 hover:bg-gray-300 transition"
            >
              <DotsHorizontalIcon :size="20" />
            </button>
          </div>

        </div>
      </div>
    </template>
  </VMenu>
</template>
