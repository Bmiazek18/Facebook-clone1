import { defineStore } from 'pinia'
import duneBackgroundImage from '@/assets/backgroundImages/unnamed-4.jpg'
import loveBackgroundImage from '@/assets/backgroundImages/Gemini_Generated_Image_5xflz55xflz55xfl.png'
import winterBackgroundImage from '@/assets/backgroundImages/348974998_2339033139614106_2348824258185195153_n.jpg.png'
import winter2BackgroundImage from '@/assets/backgroundImages/unnamed.jpg'
import type { Theme } from '@/types/Theme'

export const useChatThemeStore = defineStore('chatTheme', {
  state: () => ({
    themes: [
      {
        id: 'winter',
        title: 'Zimowe Królestwo',
        subtitle: 'Lód i śnieg',
        image: 'https://ui-avatars.com/api/?name=ZK&background=06B6D4&color=fff&rounded=true',
        backgroundImage: winterBackgroundImage,
        gradientClass: '#083344',
        sentBubbleColor: '#06b6d4',
        iconColor: '#06b6d4',
        headerColor: '#fff',
        footerColor: '#e5f2ff',
        headerTextColor: 'black',
        timestampColor: '#6B7280',
        textInputColor: '#d3e5f5',
      },
      {
        id: 'dune',
        title: 'Piaszczysta Planeta',
        subtitle: 'Gorący wiatr pustyni',
        image: 'https://ui-avatars.com/api/?name=PP&background=D97706&color=fff&rounded=true',
        backgroundImage: duneBackgroundImage,
        gradientClass: '#451a03',
        sentBubbleColor: '#ea580c',
        iconColor: '#ea580c',
        headerColor: '#2d2d2d',
        footerColor: '#2d2d2d',
        headerTextColor: '#F3F4F6',
        timestampColor: '#F3F4F6',
        textInputColor: '#4a4a4a',
      },
      {
        id: 'cyberpunk',
        title: 'Neon City 2077',
        subtitle: 'Przyszłość jest teraz',
        image: 'https://ui-avatars.com/api/?name=NC&background=D946EF&color=fff&rounded=true',
        backgroundImage: loveBackgroundImage,
        sentBubbleColor: '#b68ad0',
        iconColor: '#b68ad0',
        headerColor: '#fff',
        footerColor: '#e1d1f0',
        headerTextColor: 'black',
        timestampColor: '#6B7280',
        textInputColor: '#fff',
      },
      {
        id: 'matrix',
        title: 'Kod Źródłowy',
        subtitle: 'Cyfrowy deszcz',
        image: 'https://ui-avatars.com/api/?name=KŹ&background=15803D&color=fff&rounded=true',
        backgroundImage: winter2BackgroundImage,
        sentBubbleColor: '#013f97',
        iconColor: '#013f97',
        timestampColor: '#000',
        textInputColor: '#409fff',
      },
      {
        id: 'space',
        title: 'Gwiezdna Odyseja',
        subtitle: 'Nieskończona przestrzeń',
        image: 'https://ui-avatars.com/api/?name=GO&background=1E3A8A&color=fff&rounded=true',
        backgroundImage:
          'https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=2072&auto=format&fit=crop',
        gradientClass: '#1e3a8a',
        sentBubbleColor: '#2563eb',
        iconColor: '#2563eb',
        timestampColor: '#fff',
      },
      {
        id: 'magic',
        title: 'Szkoła Magii',
        subtitle: 'Zaklęcia i tajemnice',
        image: 'https://ui-avatars.com/api/?name=SM&background=7F1D1D&color=fff&rounded=true',
        backgroundImage:
          'https://images.unsplash.com/photo-1507842217121-9e93a5868658?q=80&w=1974&auto=format&fit=crop',
        gradientClass: '#451a03',
        sentBubbleColor: '#991b1b',
        iconColor: '#991b1b',
        timestampColor: '#6B7280',
      },
      {
        id: 'candy',
        title: 'Różowy Świat',
        subtitle: 'Słodko i stylowo',
        image: 'https://ui-avatars.com/api/?name=RŚ&background=EC4899&color=fff&rounded=true',
        backgroundImage:
          'https://images.unsplash.com/photo-1525268771113-32d9e9021a97?q=80&w=2080&auto=format&fit=crop',
        gradientClass: '#fbcfe8',
        sentBubbleColor: '#ec4899',
        iconColor: '#ec4899',
        timestampColor: '#6B7280',
      },
      {
        id: 'ocean',
        title: 'Głębiny Oceanu',
        subtitle: 'Podwodna kraina',
        image: 'https://ui-avatars.com/api/?name=GO&background=0D9488&color=fff&rounded=true',
        backgroundImage:
          'https://images.unsplash.com/photo-1582967788606-a171f1080ca8?q=80&w=2070&auto=format&fit=crop',
        gradientClass: '#134e4a',
        sentBubbleColor: '#0d9488',
        iconColor: '#0d9488',
        timestampColor: '#6B7280',
      },
      {
        id: 'jungle',
        title: 'Dzika Dżungla',
        subtitle: 'Przygodowa wyprawa',
        image: 'https://ui-avatars.com/api/?name=DD&background=166534&color=fff&rounded=true',
        backgroundImage:
          'https://images.unsplash.com/photo-1569336415962-a4bd9f69cd83?q=80&w=2069&auto=format&fit=crop',
        gradientClass: '#14532d',
        sentBubbleColor: '#16a34a',
        iconColor: '#16a34a',
        timestampColor: '#6B7280',
      },
      {
        id: 'gotham',
        title: 'Mroczny Rycerz',
        subtitle: 'Cienie wielkiego miasta',
        image: 'https://ui-avatars.com/api/?name=MR&background=111827&color=fff&rounded=true',
        backgroundImage:
          'https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?q=80&w=2074&auto=format&fit=crop',
        gradientClass: '#1f2937',
        sentBubbleColor: '#374151',
        iconColor: '#374151',
        timestampColor: '#6B7280',
      },
      {
        id: 'retro',
        title: 'Retro Lata 80.',
        subtitle: 'Syntezatory i neony',
        image: 'https://ui-avatars.com/api/?name=R8&background=991B1B&color=fff&rounded=true',
        backgroundImage:
          'https://images.unsplash.com/photo-1626128665085-483747621778?q=80&w=2024&auto=format&fit=crop',
        gradientClass: '#7f1d1d',
        sentBubbleColor: '#dc2626',
        iconColor: '#dc2626',
        timestampColor: '#6B7280',
      },
      {
        id: 'gold',
        title: 'Wielki Gatsby',
        subtitle: 'Luksus i złoto',
        image: 'https://ui-avatars.com/api/?name=WG&background=B45309&color=fff&rounded=true',
        backgroundImage:
          'https://images.unsplash.com/photo-1505691938895-1cd102b93149?q=80&w=2070&auto=format&fit=crop',
        gradientClass: '#b45309',
        sentBubbleColor: '#d97706',
        iconColor: '#d97706',
        timestampColor: '#6B7280',
      },
    ] as Theme[],
    selectedThemeId: 'candy' as string,
    selectedEmoji: '👍' as string,
  }),
  getters: {
    selectedTheme: (state) =>
      state.themes.find((t) => t.id === state.selectedThemeId) || state.themes[0],
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

export default useChatThemeStore
