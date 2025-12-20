import { defineStore } from 'pinia'

interface Theme {
  id: string;
  title: string;
  subtitle?: string;
  image: string;
  backgroundImage: string;
  gradientClass: string;
  sentBubbleColor: string;
}

export const useMessengerThemeStore = defineStore('messengerTheme', {
  state: () => ({
    themes: [
      {
        id: 'winter',
        title: 'Zimowe Królestwo',
        subtitle: 'Lód i śnieg',
        image: 'https://ui-avatars.com/api/?name=ZK&background=06B6D4&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1491002052546-bf38f186af56?q=80&w=2108&auto=format&fit=crop',
        gradientClass: 'bg-cyan-900/20 mix-blend-overlay',
        sentBubbleColor: 'bg-cyan-600',
      },
      {
        id: 'dune',
        title: 'Piaszczysta Planeta',
        subtitle: 'Gorący wiatr pustyni',
        image: 'https://ui-avatars.com/api/?name=PP&background=D97706&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1547234935-80c7142ee969?q=80&w=1974&auto=format&fit=crop',
        gradientClass: 'bg-orange-900/30 mix-blend-multiply',
        sentBubbleColor: 'bg-orange-700',
      },
      {
        id: 'cyberpunk',
        title: 'Neon City 2077',
        subtitle: 'Przyszłość jest teraz',
        image: 'https://ui-avatars.com/api/?name=NC&background=D946EF&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1555680202-c86f0e12f086?q=80&w=2070&auto=format&fit=crop',
        gradientClass: 'bg-purple-900/40 mix-blend-overlay',
        sentBubbleColor: 'bg-fuchsia-600',
      },
      {
        id: 'matrix',
        title: 'Kod Źródłowy',
        subtitle: 'Cyfrowy deszcz',
        image: 'https://ui-avatars.com/api/?name=KŹ&background=15803D&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?q=80&w=2070&auto=format&fit=crop',
        gradientClass: 'bg-black/60 mix-blend-darken',
        sentBubbleColor: 'bg-green-700',
      },
      {
        id: 'space',
        title: 'Gwiezdna Odyseja',
        subtitle: 'Nieskończona przestrzeń',
        image: 'https://ui-avatars.com/api/?name=GO&background=1E3A8A&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=2072&auto=format&fit=crop',
        gradientClass: 'bg-blue-900/30 mix-blend-overlay',
        sentBubbleColor: 'bg-blue-600',
      },
      {
        id: 'magic',
        title: 'Szkoła Magii',
        subtitle: 'Zaklęcia i tajemnice',
        image: 'https://ui-avatars.com/api/?name=SM&background=7F1D1D&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1507842217121-9e93a5868658?q=80&w=1974&auto=format&fit=crop',
        gradientClass: 'bg-yellow-900/30 mix-blend-multiply',
        sentBubbleColor: 'bg-red-800',
      },
      {
        id: 'candy',
        title: 'Różowy Świat',
        subtitle: 'Słodko i stylowo',
        image: 'https://ui-avatars.com/api/?name=RŚ&background=EC4899&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1525268771113-32d9e9021a97?q=80&w=2080&auto=format&fit=crop',
        gradientClass: 'bg-pink-200/20 mix-blend-overlay',
        sentBubbleColor: 'bg-pink-500',
      },
      {
        id: 'ocean',
        title: 'Głębiny Oceanu',
        subtitle: 'Podwodna kraina',
        image: 'https://ui-avatars.com/api/?name=GO&background=0D9488&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1582967788606-a171f1080ca8?q=80&w=2070&auto=format&fit=crop',
        gradientClass: 'bg-teal-900/40 mix-blend-overlay',
        sentBubbleColor: 'bg-teal-600',
      },
      {
        id: 'jungle',
        title: 'Dzika Dżungla',
        subtitle: 'Przygodowa wyprawa',
        image: 'https://ui-avatars.com/api/?name=DD&background=166534&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1569336415962-a4bd9f69cd83?q=80&w=2069&auto=format&fit=crop',
        gradientClass: 'bg-green-900/30 mix-blend-darken',
        sentBubbleColor: 'bg-emerald-700',
      },
      {
        id: 'gotham',
        title: 'Mroczny Rycerz',
        subtitle: 'Cienie wielkiego miasta',
        image: 'https://ui-avatars.com/api/?name=MR&background=111827&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?q=80&w=2074&auto=format&fit=crop',
        gradientClass: 'bg-gray-900/60 mix-blend-multiply',
        sentBubbleColor: 'bg-slate-700',
      },
      {
        id: 'retro',
        title: 'Retro Lata 80.',
        subtitle: 'Syntezatory i neony',
        image: 'https://ui-avatars.com/api/?name=R8&background=991B1B&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1626128665085-483747621778?q=80&w=2024&auto=format&fit=crop',
        gradientClass: 'bg-red-900/40 mix-blend-overlay',
        sentBubbleColor: 'bg-red-600',
      },
      {
        id: 'gold',
        title: 'Wielki Gatsby',
        subtitle: 'Luksus i złoto',
        image: 'https://ui-avatars.com/api/?name=WG&background=B45309&color=fff&rounded=true',
        backgroundImage: 'https://images.unsplash.com/photo-1505691938895-1cd102b93149?q=80&w=2070&auto=format&fit=crop',
        gradientClass: 'bg-yellow-900/40 mix-blend-multiply',
        sentBubbleColor: 'bg-amber-600',
      },
    ] as Theme[],
    selectedThemeId: 'candy' as string,
    selectedEmoji: '👍' as string,
  }),
  getters: {
    selectedTheme: (state) => state.themes.find(t => t.id === state.selectedThemeId) || state.themes[0],
  },
  actions: {
    setSelectedTheme(id: string) {
      this.selectedThemeId = id
    },
    setSelectedEmoji(emoji: string) {
      this.selectedEmoji = emoji
    },
  },
})

export default useMessengerThemeStore
