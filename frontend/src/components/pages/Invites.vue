<template>
  <div class="max-w-[700px]">
    <h1 class="text-[24px] font-bold text-gray-900 mb-1 leading-tight">Zaproszenia</h1>
    <h2 class="text-[17px] font-bold text-gray-900 mb-4 leading-tight">Zaproszenia do stron i profilów</h2>

    <div class="flex flex-col gap-4">
      <div
        v-for="invite in pageInvites"
        :key="invite.id"
        class="bg-white rounded-[12px] shadow-sm border border-gray-200 p-4"
      >
        <div class="flex gap-4">
          <!-- Duży awatar strony -->
          <img
            :src="invite.pageAvatar"
            :alt="invite.pageName"
            class="w-[72px] h-[72px] rounded-full border border-gray-200 object-cover shrink-0"
          />

          <!-- Informacje -->
          <div class="flex flex-col flex-1">
            <span class="text-[17px] font-bold text-gray-900">{{ invite.pageName }}</span>
            <span class="text-[14px] text-gray-600 mb-2">{{ invite.category }}</span>

            <!-- Informacja o osobie zapraszającej -->
            <div class="flex items-center gap-2 mb-3">
              <img :src="invite.inviterAvatar" alt="Inviter" class="w-6 h-6 rounded-full object-cover shrink-0" />
              <span class="text-[14px] text-gray-800">
                <b class="font-bold">{{ invite.inviterName }}</b> zaprasza Cię do obserwowania strony.
              </span>
            </div>

            <!-- Przyciski -->
            <div class="flex gap-2">
              <button class="flex-1 bg-[#0866FF] hover:bg-blue-700 text-white font-semibold text-[15px] py-1.5 rounded-md transition-colors flex justify-center items-center gap-2">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6zM16 7a1 1 0 10-2 0v1h-1a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1V7z" />
                </svg>
                Akceptuj
              </button>
              <!-- Przycisk z kropkami -->
              <button class="w-[44px] bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md flex items-center justify-center transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-700" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M6 10a2 2 0 11-4 0 2 2 0 014 0zM12 10a2 2 0 11-4 0 2 2 0 014 0zM16 12a2 2 0 100-4 2 2 0 000 4z" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const pageInvites = [
  {
    id: 1,
    pageName: 'tasza Beauty centrum',
    category: 'Uroda, kosmetyka i higiena osobista',
    pageAvatar: 'https://picsum.photos/seed/tasza/150/150',
    inviterName: 'Ola Adkonis',
    inviterAvatar: 'https://picsum.photos/seed/ola/50/50'
  },
  {
    id: 2,
    pageName: 'Cantores Lukovienses',
    category: 'Organizacja non profit',
    pageAvatar: 'https://picsum.photos/seed/cantores/150/150',
    inviterName: 'Wojciech Smyk',
    inviterAvatar: 'https://picsum.photos/seed/wojciech/50/50'
  },
  {
    id: 3,
    pageName: 'Chór Ziemi Łukowskiej',
    category: 'Zespół',
    pageAvatar: 'https://picsum.photos/seed/chor/150/150',
    inviterName: 'Wojciech Smyk',
    inviterAvatar: 'https://picsum.photos/seed/wojciech/50/50'
  },
  {
    id: 4,
    pageName: 'MKS Pogoń Siedlce Piłka Nożna Dziewcząt',
    category: 'Drużyna sportowa',
    pageAvatar: 'https://picsum.photos/seed/pogon/150/150',
    inviterName: 'Tomasz Kowalski',
    inviterAvatar: 'https://picsum.photos/seed/tomek/50/50'
  }
]
</script>
