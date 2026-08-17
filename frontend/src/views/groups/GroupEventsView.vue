<script setup>
import { ref } from 'vue'

// Przykładowe dane dla minionych wydarzeń (odwzorowane ze zrzutu ekranu)
const pastEvents = ref([
  {
    id: 1,
    date: 'Sob, 26 paź 2019',
    title: '4 Nocny Bieg Pamięci Bohaterskich Lotników Podl...',
    author: 'Artur Krasowski',
    authorAvatar: 'https://i.pravatar.cc/150?img=11',
    // Tymczasowy obrazek zastępczy przypominający ten ze screena
    image: 'https://images.unsplash.com/photo-1552674605-15c37042ce88?q=80&w=200&auto=format&fit=crop'
  },
  {
    id: 2,
    date: 'Sob, 3 sie 2019',
    title: 'Puchar Polski: RED Sielczyk - OKS BOZPN Biała P...',
    author: 'Jarosław Śledź',
    authorAvatar: 'https://i.pravatar.cc/150?img=12',
    image: 'https://images.unsplash.com/photo-1518605368461-1e1e38dd1ba7?q=80&w=200&auto=format&fit=crop'
  },
  {
    id: 3,
    date: 'Sob, 28 paź 2017',
    title: 'II Nocny Bieg Pamięci Bohaterskich Lotników Podl...',
    author: 'Piotr Dziubak',
    authorAvatar: 'https://i.pravatar.cc/150?img=13',
    image: 'https://images.unsplash.com/photo-1530549387789-4c1017266635?q=80&w=200&auto=format&fit=crop'
  }
])
</script>

<template>
  <div class="min-h-screen bg-[#F0F2F5] dark:bg-[#18191A] p-4 flex flex-col items-center gap-4 font-sans">

    <!-- KARTA 1: Nadchodzące wydarzenia (Pusty stan) -->
    <div class="w-full max-w-[680px] bg-white dark:bg-[#242526] rounded-lg border border-gray-200 dark:border-zinc-700 shadow-sm p-4">

      <!-- Nagłówek i przyciski -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-8">
        <h2 class="text-[20px] font-bold text-[#050505] dark:text-[#E4E6EB]">
          Nadchodzące wydarzenia
        </h2>

        <div class="flex items-center gap-2">
          <button class="bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] text-[#050505] dark:text-[#E4E6EB] px-3.5 py-1.5 rounded-md font-semibold text-[15px] transition-colors">
            Znajdź wydarzenia
          </button>
          <button class="bg-[#0866FF] hover:bg-[#0052CC] text-white px-3.5 py-1.5 rounded-md font-semibold text-[15px] transition-colors">
            Utwórz wydarzenie
          </button>
        </div>
      </div>

      <!-- Ikona i tekst pustego stanu -->
      <div class="flex flex-col items-center justify-center py-6 pb-10">
        <!-- Ikona Kalendarza SVG -->
        <svg viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="w-24 h-24 text-[#65676B] dark:text-[#B0B3B8] mb-4">
          <rect x="14" y="12" width="36" height="40" rx="6" />
          <line x1="26" y1="20" x2="38" y2="20" stroke-width="4" />
          <line x1="14" y1="28" x2="50" y2="28" />
          <line x1="14" y1="40" x2="50" y2="40" />
          <line x1="26" y1="28" x2="26" y2="52" />
          <line x1="38" y1="28" x2="38" y2="52" />
        </svg>
        <span class="text-[17px] text-[#050505] dark:text-[#E4E6EB]">
          Brak nadchodzących wydarzeń.
        </span>
      </div>

    </div>

    <!-- KARTA 2: Minione wydarzenia (Lista) -->
    <div class="w-full max-w-[680px] bg-white dark:bg-[#242526] rounded-lg border border-gray-200 dark:border-zinc-700 shadow-sm pt-4">

      <!-- Nagłówek -->
      <h2 class="text-[20px] font-bold text-[#050505] dark:text-[#E4E6EB] px-4 mb-2">
        Minione wydarzenia
      </h2>

      <!-- Lista Wydarzeń -->
      <div class="flex flex-col">
        <div v-for="(event, index) in pastEvents" :key="event.id">

          <div class="flex items-start gap-4 p-4 hover:bg-gray-50 dark:hover:bg-[#3A3B3C]/50 transition-colors cursor-pointer">

            <!-- Miniaturka wydarzenia -->
            <img
              :src="event.image"
              alt="Plakat wydarzenia"
              class="w-[100px] h-[100px] rounded-lg object-cover border border-gray-200 dark:border-zinc-700 flex-shrink-0"
            />

            <!-- Informacje o wydarzeniu -->
            <div class="flex flex-col justify-start flex-1">

              <!-- Data -->
              <span class="text-[#D5232F] dark:text-[#FC7F7F] text-[13px] font-semibold tracking-wide uppercase">
                {{ event.date }}
              </span>

              <!-- Tytuł -->
              <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#E4E6EB] leading-snug mt-0.5 mb-1.5 hover:underline">
                {{ event.title }}
              </h3>

              <!-- Autor (Udostępnione przez) -->
              <div class="flex items-center text-[13px] text-[#65676B] dark:text-[#B0B3B8] mb-2.5">
                <img :src="event.authorAvatar" class="w-[20px] h-[20px] rounded-full object-cover mr-1.5 border border-gray-100 dark:border-zinc-700" />
                <span>
                  Udostępnione przez <span class="font-bold text-[#050505] dark:text-[#E4E6EB]">{{ event.author }}</span>
                </span>
              </div>

              <!-- Przycisk opcji (...) -->
              <button class="w-[36px] h-[32px] bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] rounded-md flex items-center justify-center text-[#050505] dark:text-[#E4E6EB] transition-colors" @click.stop>
                <svg class="w-5 h-5 fill-currentColor" viewBox="0 0 24 24">
                  <path d="M6 12c0-1.1.9-2 2-2s2 .9 2 2-.9 2-2 2-2-.9-2-2zm6 0c0-1.1.9-2 2-2s2 .9 2 2-.9 2-2 2-2-.9-2-2zm6 0c0-1.1.9-2 2-2s2 .9 2 2-.9 2-2 2-2-.9-2-2z" />
                </svg>
              </button>

            </div>
          </div>

          <!-- Linia oddzielająca (z wyjątkiem ostatniego elementu przed stopką) -->
          <hr v-if="index !== pastEvents.length - 1" class="border-[#CED0D4] dark:border-[#3E4042] mx-4" />
        </div>
      </div>

      <!-- Stopka: Zobacz więcej -->
      <hr class="border-[#CED0D4] dark:border-[#3E4042]" />
      <div class="p-2">
        <button class="w-full py-2 hover:bg-gray-50 dark:hover:bg-[#3A3B3C] rounded-md text-[#0866FF] dark:text-[#2D88FF] font-semibold text-[15px] transition-colors">
          Zobacz więcej
        </button>
      </div>

    </div>

  </div>
</template>
