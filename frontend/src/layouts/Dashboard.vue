<template>
  <div class="flex h-[calc(100vh-56px)] w-full bg-[#F0F2F5] text-[#050505] font-sans antialiased overflow-hidden mt-[56px]">
    <!-- PASEK BOCZNY -->
    <aside class="w-[360px] bg-white flex flex-col shrink-0 border-r border-[#CED0D4] z-10 h-full">
      <div class="p-4 pb-2">
        <h1 class="text-[20px] font-bold text-[#050505]">{{ $t('common.pulpitProfesjonalny') }}</h1>
      </div>

      <nav class="flex-1 overflow-y-auto px-2 py-2 flex flex-col gap-1 custom-scrollbar">
        <!-- Strona główna -->
        <NuxtLink
          to="/professional_dashboard"
          class="flex items-center gap-3 px-2 py-2 rounded-md transition-colors"
          :class="isPathActive('/professional_dashboard') ? 'bg-[#F0F2F5]' : 'hover:bg-[#F0F2F5]'"
        >
          <div
            class="w-9 h-9 rounded-full flex items-center justify-center shrink-0 transition-colors"
            :class="isPathActive('/professional_dashboard') ? 'bg-[#1877F2] text-white' : 'bg-[#E4E6EB] text-[#050505]'"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" />
            </svg>
          </div>
          <span class="font-medium text-[15px]">{{ $t('friends.home') }}</span>
        </NuxtLink>

        <!-- Dynamiczne sekcje menu z podmenu -->
        <div v-for="item in menuItems" :key="item.id" class="flex flex-col">
          <!-- Kategoria główna (rozwijana) -->
          <button
            @click="toggleSection(item.id)"
            class="flex items-center justify-between px-2 py-2 rounded-md hover:bg-[#F0F2F5] transition-colors group w-full text-left cursor-pointer"
          >
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-full bg-[#E4E6EB] flex items-center justify-center text-[#050505] shrink-0">
                <component :is="item.icon" />
              </div>
              <span class="font-medium text-[15px]">{{ item.label }}</span>
            </div>

            <!-- Strzałka rozwijania -->
            <svg
              viewBox="0 0 24 24"
              width="20"
              height="20"
              fill="#65676B"
              class="group-hover:fill-[#050505] transition-transform duration-200"
              :class="expandedSections.includes(item.id) ? 'rotate-180' : ''"
            >
              <path d="M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z" />
            </svg>
          </button>

          <!-- Elementy podmenu -->
          <div
            v-show="expandedSections.includes(item.id)"
            class="flex flex-col mt-1 ml-[44px] mr-2 gap-1 overflow-hidden transition-all"
          >
            <NuxtLink
              v-for="child in item.children"
              :key="child.to"
              :to="child.to"
              class="flex items-center gap-3 px-3 py-2 rounded-md transition-colors"
              :class="isPathActive(child.to) ? 'bg-[#F0F2F5]' : 'hover:bg-[#F0F2F5]'"
            >
              <div
                class="w-7 h-7 rounded-full flex items-center justify-center shrink-0 transition-colors"
                :class="isPathActive(child.to) ? 'bg-[#1877F2] text-white' : 'bg-[#E4E6EB] text-[#050505]'"
              >
                <component :is="child.icon" />
              </div>
              <span class="font-medium text-[15px]">{{ child.label }}</span>
            </NuxtLink>
          </div>
        </div>


      </nav>

      <!-- Przycisk Utwórz post -->
      <div class="p-4 border-t border-[#CED0D4] bg-white shrink-0">
        <button class="w-full bg-[#1877F2] text-white font-semibold text-[15px] py-2 rounded-md flex items-center justify-center gap-1.5 hover:bg-[#166FE5] transition-colors cursor-pointer">{{ $t('post.createPost') }}<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M7 10l5 5 5-5z" />
          </svg>
        </button>
      </div>
    </aside>

    <!-- GŁÓWNA ZAWARTOŚĆ -->
    <div class="flex-1 overflow-y-auto custom-scrollbar">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, h, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// Sprawdzanie aktywnej ścieżki
const isPathActive = (path: string) => {
  return route.path === path
}

// Generowanie wykazu ikonek pomocniczych w Render-functions (SVG)
const createSvg = (pathD: string, size = 18) => () =>
  h('svg', { viewBox: '0 0 24 24', width: size, height: size, fill: 'currentColor' }, [
    h('path', { d: pathD })
  ])

// LEGENDA IKON PROSTO ZE ZRZUTU EKRANU
const Icons = {
  Stats: createSvg('M3.5 18.5l6-6 4 4L22 6.92 20.59 5.5l-7.09 8.09-4-4L2 17.09z'),
  Views: createSvg('M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z', 16),
  Income: createSvg('M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z', 16),
  Engagement: createSvg('M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.17L4 17.17V4h16v12zM7 9h10v2H7zm0-3h10v2H7z', 14),
  Audience: createSvg('M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z', 16),
  Content: createSvg('M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z'),
  Library: createSvg('M4 6h16v2H4zm0 5h16v2H4zm0 5h16v2H4z', 16),
  Calendar: createSvg('M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2z', 16),
  Monetization: createSvg('M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 14h-2v-2h2v2zm0-4h-2V7h2v5z'),
  Activity: createSvg('M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 9h12v2H6V9zm8 5H6v-2h8v2zm4-6H6V6h12v2z'),
  Comments: createSvg('M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18zM18 14H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z', 16),
  Moderation: createSvg('M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z', 16),
  Log: createSvg('M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z', 16)
}

// STRUKTURA DANYCH MENU - TUTAJ ŁATWO DODASZ NOWE LINKI
const menuItems = [
  {
    id: 'stats',
    label: 'Statystyki',
    icon: Icons.Stats,
    children: [
      { label: 'Wyświetlenia', to: '/professional_dashboard/profile_insights/views', icon: Icons.Views },
      { label: 'Dochody', to: '/professional_dashboard/profile_insights/income', icon: Icons.Income },
      { label: 'Zaangażowanie', to: '/professional_dashboard/insights/engagement', icon: Icons.Engagement },
      { label: 'Odbiorcy', to: '/professional_dashboard/profile_insights/audience', icon: Icons.Audience }
    ]
  },
  {
    id: 'content',
    label: 'Materiały',
    icon: Icons.Content,
    children: [
      { label: 'Biblioteka materiałów', to: '/professional_dashboard/content/content_library', icon: Icons.Library },
      { label: 'Terminarz', to: '/professional_dashboard/content_calendar/', icon: Icons.Calendar }
    ]
  },

  {
    id: 'activity',
    label: 'Aktywność',
    icon: Icons.Activity,
    children: [
      { label: 'Menedżer komentarzy', to: '/professional_dashboard/engagement/comments_manager/', icon: Icons.Comments },
      { label: 'Asystent moderacji', to: '/professional_dashboard/moderation_assist', icon: Icons.Moderation },
      { label: 'Dziennik aktywności', to: '/professional_dashboard/moderation_activity_log', icon: Icons.Log }
    ]
  }
]

// Stan otwartych sekcji
const expandedSections = ref<string[]>([])

const toggleSection = (sectionId: string) => {
  if (expandedSections.value.includes(sectionId)) {
    expandedSections.value = expandedSections.value.filter(id => id !== sectionId)
  } else {
    expandedSections.value.push(sectionId)
  }
}

// Auto-rozwijanie grupy, jeśli użytkownik przejdzie do pod-linka
const autoExpandActiveSections = () => {
  menuItems.forEach(item => {
    const hasActiveChild = item.children?.some(child => child.to === route.path)
    if (hasActiveChild && !expandedSections.value.includes(item.id)) {
      expandedSections.value.push(item.id)
    }
  })
}

watch(() => route.path, autoExpandActiveSections)
onMounted(autoExpandActiveSections)
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #CED0D4;
  border-radius: 20px;
}
</style>
