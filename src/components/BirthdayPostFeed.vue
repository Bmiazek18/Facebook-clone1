<template>
  <div class="flex justify-center bg-[#f0f2f5] min-h-screen pt-8">
    <div class="w-full ">

      <div class="bg-white rounded-lg shadow-sm border border-gray-300 overflow-hidden">

        <div class="p-4 flex items-start justify-between ">
             <div class="text-[15px] text-gray-500 leading-snug">
                17 znajomych opublikowało post na Twojej <span class="font-bold text-gray-900 cursor-pointer hover:underline">Profilu</span> z okazji Twoich urodzin.
             </div>
             <DotsHorizontalIcon class="text-gray-500 cursor-pointer -mt-1" />
        </div>

        <div class="px-4 py-2" v-for="(post) in posts" :key="post.id">
            <BirthdayPost :post="post" />


        </div>

        <div class="p-3 text-center border-t border-gray-200 hover:bg-gray-50 cursor-pointer transition-colors">
            <span class="text-[15px] font-semibold text-gray-600">Wyświetl jeszcze 14</span>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import BirthdayPost from './BirthdayPostItem.vue';
import type { Post } from '@/types/Post';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';

// Dane symulujące te ze zrzutu ekranu
const posts = ref<Post[]>([
  // POST 1: Prosty post bez komentarzy
  {
    id: '1',
    authorName: 'Andrzej Prokop',
    authorAvatar: 'https://i.pravatar.cc/150?u=andrzej',
    date: '24 lutego',
    content: 'Wszystkiego najlepszego z okazji urodzin! 🎁🥳 Niech Ci się darzy w życiu prywatnym i zawodowym!',
    isLiked: false,
    reactionCount: 2,
    commentCount: 0,
    images: [],
    authorId: 1,
    likesCount: 2,
    commentsCount: 0,
    sharesCount: 0,
    comments: [], // Brak komentarzy
    timestamp: Date.now(),
  },

  // POST 2: Post Trenera (Głębokie zagnieżdżenie - test rekurencji)
  {
    id: '2',
    authorName: 'Przemysław Wereszczyński',
    authorAvatar: 'https://i.pravatar.cc/150?u=przemek',
    date: '23 lutego',
    content: 'Wszystkiego najlepszego, sukcesów na macie i poza nią! 😊',
    isLiked: true,
    likedType: 'super',
    reactionCount: 15,
    commentCount: 4,
    images: [],
    authorId: 2,
    likesCount: 15,
    commentsCount: 4,
    sharesCount: 0,
    comments: [
        {
            id: 101,
            authorName: 'Bartosz Miazek', // Główny komentarz
            authorAvatar: 'https://i.pravatar.cc/150?u=me',
            content: 'Dziękuję trenerze 😊',
            date: '41 tyg.',
            likesCount: 3,
            replies: [
                {
                    id: 201,
                    authorName: 'Przemysław Wereszczyński', // Odpowiedź trenera (Poziom 1)
                    authorAvatar: 'https://i.pravatar.cc/150?u=przemek',
                    content: 'Nie ma za co, widzimy się na treningu!',
                    date: '41 tyg.',
                    likesCount: 1,
                    replies: [
                        {
                            id: 301,
                            authorName: 'Bartosz Miazek', // Odpowiedź Bartosza (Poziom 2 - test linii bocznej)
                            authorAvatar: 'https://i.pravatar.cc/150?u=me',
                            content: 'Będę na pewno! 💪',
                            date: '41 tyg.',
                            likesCount: 0,
                            replies: []
                        }
                    ]
                }
            ]
        },
        {
            id: 102,
            authorName: 'Klub Sportowy "Olimp"',
            authorAvatar: 'https://i.pravatar.cc/150?u=olimp',
            content: 'Dołączamy się do życzeń! 🥇',
            date: '40 tyg.',
            likesCount: 5,
            replies: []
        }
    ],
    timestamp: Date.now(),
  },

  // POST 3: Post z wieloma krótkimi komentarzami (Test listy)
  {
    id: '3',
    authorName: 'Mateusz Sak',
    authorAvatar: 'https://i.pravatar.cc/150?u=mateusz',
    date: '23 lutego',
    content: 'Wszystkiego najlepszego zdrówka 💪 Pamiętaj o regeneracji!',
    isLiked: true,
    likedType: 'like',
    reactionCount: 8,
    commentCount: 3,
    images: [],
    authorId: 3,
    likesCount: 8,
    commentsCount: 3,
    sharesCount: 0,
    comments: [
        {
            id: 103,
            authorName: 'Bartosz Miazek',
            authorAvatar: 'https://i.pravatar.cc/150?u=me',
            content: 'Dzięki wielkie byku 💪',
            date: '41 tyg.',
            likesCount: 1,
            replies: []
        },
        {
            id: 104,
            authorName: 'Kamil Nowak',
            authorAvatar: 'https://i.pravatar.cc/150?u=kamil',
            content: 'Sto lat Bartek!',
            date: '41 tyg.',
            likesCount: 0,
            replies: [
                 {
                    id: 205,
                    authorName: 'Bartosz Miazek',
                    authorAvatar: 'https://i.pravatar.cc/150?u=me',
                    content: 'Dzięki Kamil!',
                    date: '41 tyg.',
                    likesCount: 0,
                    replies: []
                }
            ]
        }
    ],
    timestamp: Date.now(),
  },

  // POST 4: Długi tekst i dużo reakcji (Test layoutu)
  {
    id: '4',
    authorName: 'Anna Kowalska',
    authorAvatar: 'https://i.pravatar.cc/150?u=anna',
    date: '22 lutego',
    content: 'Bartosz! Życzę Ci, aby każdy dzień przynosił nowe wyzwania, które będziesz pokonywał z uśmiechem. Dużo zdrowia, szczęścia, miłości i spełnienia marzeń, nawet tych najskrytszych! 🎂🥂 Nie zmieniaj się!',
    isLiked: false,
    reactionCount: 42,
    commentCount: 1,
    images: [],
    authorId: 4,
    likesCount: 42,
    commentsCount: 1,
    sharesCount: 0,
    comments: [
         {
            id: 105,
            authorName: 'Bartosz Miazek',
            authorAvatar: 'https://i.pravatar.cc/150?u=me',
            content: 'Ania, dziękuję za piękne słowa! 🥰',
            date: '41 tyg.',
            likesCount: 2,
            replies: []
        }
    ],
    timestamp: Date.now(),
  }
]);
</script>