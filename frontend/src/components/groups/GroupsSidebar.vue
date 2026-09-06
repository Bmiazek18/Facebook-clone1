<template>
  <Sidebar
    :title="$t('login.groups')"
    search-placeholder="Szukaj grup"
    :show-settings="true"
    :show-search="true"
    :items="sidebarItems"
    :create-button="createGroupButton"
  >
    <template #list-header>
      <h2 class="text-[17px] font-semibold text-theme-text leading-snug">{{ $t('groups.grupyDoKtorychNalezysz') }}</h2>
      <a href="#" class="text-[13px] text-blue-600 hover:underline shrink-0">{{ $t('groups.wyswietlWszystkie') }}</a>
    </template>

    <template #list-items>
      <NuxtLink
        v-for="(group, index) in groups"
        :key="index"
        :to="'/groups/' + group.id"
        class="flex items-center space-x-3 p-2 rounded-lg hover:bg-theme-hover transition-colors group"
      >
        <img
          :src="group.image"
          :alt="$t('groups.groupIcon')"
          class="w-12 h-12 rounded-xl object-cover border border-theme-border shrink-0"
        />
        <div class="flex-1 min-w-0">
          <h3
            class="text-[15px] font-semibold text-theme-text truncate group-hover:text-theme-text"
          >
            {{ group.name }}
          </h3>
          <p class="text-[13px] text-gray-500 truncate">{{ $t('groups.ostatniaAktywnoscGroupLastactive') }}</p>
        </div>
      </NuxtLink>
    </template>
  </Sidebar>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Sidebar from '@/components/common/Sidebar.vue'
import PlusIcon from 'vue-material-design-icons/Plus.vue'
import NewspaperVariantOutlineIcon from 'vue-material-design-icons/NewspaperVariantOutline.vue'
import InformationOutlineIcon from 'vue-material-design-icons/InformationOutline.vue'
import AccountGroupOutlineIcon from 'vue-material-design-icons/AccountGroupOutline.vue'
import { useGroupsStore } from '@/stores/groups'
import { onMounted } from 'vue'

const groupsStore = useGroupsStore()

onMounted(() => {
  groupsStore.fetchGroups()
})

const sidebarItems = ref([
  {
    icon: NewspaperVariantOutlineIcon,
    text: 'Twoje Aktualności',
    route: '#',
  },
  {
    icon: InformationOutlineIcon,
    text: 'Odkryj',
    route: '#',
  },
  {
    icon: AccountGroupOutlineIcon,
    text: 'Twoje grupy',
    route: '#',
  },
])

const createGroupButton = ref({
  icon: PlusIcon,
  text: 'Utwórz nową grupę',
  route: '/add-group',
})

const groups = computed(() => groupsStore.groups)
</script>
