  <script setup lang="ts">
import Close from 'vue-material-design-icons/Close.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import ShareIcon from 'vue-material-design-icons/ShareVariant.vue'
import HoverScrollbar from './HoverScrollbar.vue'
import ReactionButton from './ReactionButton.vue'

const emit = defineEmits(['close'])

const closeModal = () => {
    emit('close')
}

// Przykładowe dane dla postu (commentsCount: 3)
const postData = {
    userName: "Kolegium Sędziów Opolskiego Związku Piłki Nożnej",
    date: "10 min",
    text: "✅ Niedziela siatkarska z dużą kontrolą sędziów z 2 stref! (...) Wspieramy! Będziemy kontrolować! Będziemy dbać o rzetelność i będziemy dbać o dobrą komunikację w zespole we współpracy. 😉",
    likesCount: 9,
    commentsCount: 3, // Zmieniono na 3
    imageSrc: "https://picsum.photos/600/350?random=1",
    avatarSrc: "https://picsum.photos/40/40?random=2"
}

// Przykładowe komentarze, w tym zagnieżdżone odpowiedzi
const comments = [
    {
        id: 1,
        userName: "Marek Kowalski",
        text: "Super inicjatywa! Wsparcie dla sędziów jest kluczowe. Oby tak dalej!",
        date: "5 min",
        likesCount: 2,
        avatarSrc: "https://picsum.photos/40/40?random=3",
        replies: []
    },
    {
        id: 2,
        userName: "Anna Zając",
        text: "Świetnie! Zgadzam się z komunikacją. Ostatnio było z tym kiepsko. 💪",
        date: "3 min",
        likesCount: 5,
        avatarSrc: "https://picsum.photos/40/40?random=4",
        replies: [
            {
                id: 21,
                userName: "Jan Nowak",
                text: "Dokładnie, transparentność jest najważniejsza!",
                date: "2 min",
                likesCount: 1,
                avatarSrc: "https://picsum.photos/40/40?random=6"
            },
            {
                id: 22,
                userName: "Tester",
                text: "Popieram, jest nadzieja na poprawę.",
                date: "1 min",
                likesCount: 0,
                avatarSrc: "https://picsum.photos/40/40?random=7"
            }
        ]
    },
    {
        id: 3,
        userName: "Sportowiec",
        text: "Kto będzie sędziował mecz ligowy w ten weekend?",
        date: "1 min",
        likesCount: 0,
        avatarSrc: "https://picsum.photos/40/40?random=5",
        replies: []
    }
]

</script>

<template>

    <div
        @click.self="closeModal"
        class="fixed inset-0 z-50 flex items-center justify-center bg-gray-200/80 "
    >
      <HoverScrollbar class="bg-white rounded-lg shadow-2xl w-full max-w-2xl mx-4 my-8 relative flex flex-col max-h-[98vh]">


          <div class="sticky top-0 bg-white z-10 p-4 border-b flex items-center justify-center relative">
              <h2 class="text-xl font-bold">Post</h2>
              <button
                  @click="closeModal"
                  class="absolute right-4 text-gray-500 hover:bg-gray-200 rounded-full p-2"
              >
                  <Close :size="24" fillColor="#65686C" />
              </button>
          </div>
            <div class="flex-grow">

                <div class="p-4 border-b">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center">
                            <img class="rounded-full w-10 h-10 mr-3" :src="postData.avatarSrc" alt="Avatar">
                            <div>
                                <div class="font-extrabold text-[15px]">{{ postData.userName }}</div>
                                <div class="flex items-center font-semibold text-[13px] text-gray-500">
                                    {{ postData.date }} <Earth class="ml-1" :size="15" fillColor="#64676B"/>
                                </div>
                            </div>
                        </div>
                        <button class="text-gray-500 hover:bg-gray-100 rounded-full p-2">
                           <DotsHorizontal :size="23" fillColor="#050505" />
                        </button>
                    </div>
                    <p class="mt-2 text-[15px]">{{ postData.text }}</p>
                </div>

                <div v-if="postData.imageSrc" class="py-2">
                    <img class="w-full h-auto object-cover" :src="postData.imageSrc" alt="Post content image">
                </div>

                <div class="px-4 pt-2">
                    <div id="Likes" class="flex items-center justify-between pb-2 text-gray-500 text-sm">
                         <div class="flex items-center gap-1">
                            <ThumbUp fillColor="#1D72E2" :size="16" class="mt-[-1px]"/> {{ postData.likesCount }}
                         </div>
                         <span class="cursor-pointer hover:underline">{{ postData.commentsCount }} Komentarze</span>
                    </div>

                    <div class="flex items-center justify-around py-1 font-bold text-[#65686C] border-y border-gray-200">
                        <button class="flex items-center justify-center h-[38px] hover:bg-[#F2F2F2] w-full rounded-lg mx-1 cursor-pointer">
                            <ReactionButton/>
                        </button>
                        <button class="flex items-center justify-center h-[38px] hover:bg-[#F2F2F2] w-full rounded-lg mx-1 cursor-pointer">
                            <MessageOutline :size="18" fillColor="#65686C" class="mr-1" /> Komentarz
                        </button>
                        <button class="flex items-center justify-center h-[38px] hover:bg-[#F2F2F2] w-full rounded-lg mx-1 cursor-pointer">
                            <ShareIcon :size="18" fillColor="#65686C" class="mr-1" /> Udostępnij
                        </button>
                    </div>
                </div>

                <div class="px-4 py-4">
                    <div v-for="comment in comments" :key="comment.id" class="flex mt-4">
                        <img class="rounded-full w-8 h-8 mr-2 mt-1" :src="comment.avatarSrc" :alt="comment.userName + ' Avatar'">

                        <div class="flex-grow">
                            <div class="bg-gray-100 p-2 rounded-xl">
                                <span class="font-extrabold text-[13px] hover:underline cursor-pointer">{{ comment.userName }}</span>
                                <p class="text-[15px]">{{ comment.text }}</p>
                            </div>

                            <div class="flex items-center ml-2 space-x-2 text-xs font-semibold text-gray-500 mt-1">
                                <span class="cursor-pointer hover:underline">Lubię to!</span>
                                <span class="cursor-pointer hover:underline">Odpowiedz</span>
                                <span>{{ comment.date }}</span>
                                <div v-if="comment.likesCount > 0" class="flex items-center ml-1">
                                    <ThumbUp fillColor="#1D72E2" :size="12" class="mt-[-2px]"/>
                                    <span>{{ comment.likesCount }}</span>
                                </div>
                            </div>

                            <div v-if="comment.replies.length > 0" class="mt-3">

                                <div class="text-blue-500 font-bold text-sm cursor-pointer mb-3">
                                    Wyświetl wszystkie {{ comment.replies.length }} odpowiedzi
                                </div>

                                <div v-for="reply in comment.replies.slice(0, 1)" :key="reply.id" class="flex mt-2">
                                    <img class="rounded-full w-8 h-8 mr-2 mt-1" :src="reply.avatarSrc" :alt="reply.userName + ' Reply Avatar'">

                                    <div class="flex-grow">
                                        <div class="bg-gray-100 p-2 rounded-xl">
                                            <span class="font-extrabold text-[13px] hover:underline cursor-pointer">{{ reply.userName }}</span>
                                            <p class="text-[15px]">{{ reply.text }}</p>
                                        </div>

                                        <div class="flex items-center ml-2 space-x-2 text-xs font-semibold text-gray-500 mt-1">
                                            <span class="cursor-pointer hover:underline">Lubię to!</span>
                                            <span class="cursor-pointer hover:underline">Odpowiedz</span>
                                            <span>{{ reply.date }}</span>
                                            <div v-if="reply.likesCount > 0" class="flex items-center ml-1">
                                                <ThumbUp fillColor="#1D72E2" :size="12" class="mt-[-2px]"/>
                                                <span>{{ reply.likesCount }}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            <div class="p-4 border-t">
                <div class="flex items-start">

                    <div class="relative w-8 flex justify-center mr-2 mt-[-4px]">
                        <div class="h-full w-0.5 bg-gray-300"></div>

                        <div class="absolute top-4 w-4 h-0.5 bg-gray-300 left-1/2 -ml-2"></div>
                    </div>

                    <div class="flex items-center flex-grow">
                        <img class="rounded-full w-8 h-8 mr-2" :src="postData.avatarSrc" alt="Twój Avatar">
                        <div class="relative flex-grow">
                            <input
                                type="text"
                                placeholder="Odpowiedz Sportowiec..."
                                class="w-full border-none bg-gray-100 p-2 pr-12 rounded-full text-sm focus:ring-blue-500 focus:border-blue-500"
                            >
                            <div class="absolute inset-y-0 right-0 flex items-center pr-2 space-x-1">
                                <button class="text-gray-500 hover:text-gray-700">😊</button>
                                <button class="text-gray-500 hover:text-gray-700">📸</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>


 </HoverScrollbar>
  </div>

</template>
