<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';

import CreatePostBox from '@/components/CreatePostBox.vue';

import Camera from 'vue-material-design-icons/Camera.vue'
import ImageWithGradient from '@/components/ImageWithGradient.vue';

import Check from 'vue-material-design-icons/Check.vue'
import Message from 'vue-material-design-icons/Message.vue'
import Magnify from 'vue-material-design-icons/Magnify.vue'

const activeTab = ref('friends');
const userName = 'Wiktoria Królik';

function setActiveTab(tab: string) {
    activeTab.value = tab;
}

function updateTitle() {
        document.title = `${userName} | Facebook`;
}

onMounted(() => {
    updateTitle();
});



const friendsList = ref([
    { name: 'Natalia Wójcik', mutual: 71, isFriend: true, imageId: 35 },
    { name: 'Kacper Szymański', mutual: 10, isFriend: false, imageId: 36 },
    { name: 'Monika Zawadzka', mutual: 211, isFriend: true, imageId: 37 },
    { name: 'Michał Kowalczyk', mutual: 15, isFriend: false, imageId: 38 },
    { name: 'Ewa Lipińska', mutual: 45, isFriend: true, imageId: 39 },
    { name: 'Marek Pająk', mutual: 8, isFriend: false, imageId: 40 },
    { name: 'Piotr Zieliński', mutual: 99, isFriend: true, imageId: 20 },
    { name: 'Katarzyna Nowak', mutual: 56, isFriend: true, imageId: 21 },
    { name: 'Tomasz Dąbrowski', mutual: 139, isFriend: true, imageId: 22 },
    { name: 'Anna Kozłowska', mutual: 34, isFriend: false, imageId: 23 },
    { name: 'Rafał Woźniak', mutual: 157, isFriend: true, imageId: 24 },
    { name: 'Joanna Błaszczyk', mutual: 142, isFriend: true, imageId: 25 },
    { name: 'Łukasz Cichy', mutual: 144, isFriend: true, imageId: 26 },
    { name: 'Zuzanna Górska', mutual: 114, isFriend: true, imageId: 27 },
    { name: 'Maciej Kamiński', mutual: 52, isFriend: false, imageId: 28 },
    { name: 'Kinga Bartosiewicz', mutual: 38, isFriend: false, imageId: 29 },
    { name: 'Adam Wróbel', mutual: 46, isFriend: false, imageId: 30 },
    { name: 'Justyna Jurek', mutual: 128, isFriend: true, imageId: 31 },
    { name: 'Robert Kubiak', mutual: 89, isFriend: false, imageId: 32 },
    { name: 'Karolina Sęk', mutual: 80, isFriend: false, imageId: 33 },
]);
const miniFriendsList = friendsList.value.slice(0, 6);
const miniPhotosList = [101, 102, 103, 104, 105, 106, 107, 108, 109];

</script>

<template>

        <div class="w-full min-h-screen pb-20 bg-theme-bg">
            <div class="w-full bg-theme-bg-secondary">
              <ImageWithGradient class="rounded-b-xl"/>
                <div class="max-w-[1250px]  mx-auto pb-1">


                    <div id="ProfileInfo" class="flex md:flex-row flex-col items-center justify-between px-4">
                        <div class="flex md:flex-row flex-col gap-4 md:-mt-6 -mt-16 items-center">
                            <div class="relative">
                                <img
                                    class="rounded-full w-[165px] h-[165px] border-theme-border border-4"
src="https://picsum.photos/id/142/2000/320"
                                >
                                <button

                                    class="absolute right-0 top-[100px] bg-gray-200 hover:bg-gray-300 p-1.5 rounded-full cursor-pointer"
                                >
                                    <Camera :size="25"/>
                                </button>
                            </div>
                            <div class="md:mt-4 text-center md:text-left -mt-3">
                                <div class="text-[28px] text-theme-text font-extrabold pt-1">
                                   Wiktoria Królik
                                </div>
                                <div class="text-[17px] font-bold text-theme-text-secondary mb-1.5 text-center md:text-left">525 znajomi · 79 wspólnych znajomych</div>
                                <div class="flex md:justify-start justify-center md:-ml-1">
                                    <img
                                        class="rounded-full -ml-1 z-[10] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/141/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[9] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/142/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[8] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/143/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[7] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/144/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[6] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/145/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[5] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/146/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[4] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/147/2000/320"
                                    >
                                </div>
                            </div>
                        </div>

                        <div class="flex items-center md:my-0 my-4 gap-2">
                             <a
                                class="
                                    flex
                                    justify-center
                                    items-baseline
                                    bg-gray-200
                                    hover:bg-gray-300
                                    rounded-lg
                                    cursor-pointer
                                "
                            >
                                <button class="flex items-center px-4 py-2 font-bold">
                                    <Check class="-ml-2 mr-1" :size="22"/> Znajomi
                                </button>
                            </a>
                             <a
                                class="
                                    flex
                                    justify-center
                                    items-baseline
                                    bg-blue-500
                                    hover:bg-blue-600
                                    rounded-lg
                                    cursor-pointer
                                    text-white
                                "
                            >
                                <button class="flex items-center px-4 py-2 font-bold">
                                    <Message class="-ml-2 mr-1" :size="22"/> {{ $t('profile.message') }}
                                </button>
                            </a>
                        </div>
                    </div>

                    <div class="flex items-center w-full border-t h-[50px] text-theme-text-secondary py-[4px]">
                        <button class="w-[85px]" @click="setActiveTab('posts')">
                            <div
                                :class="[
                                    'flex items-center text-[15px] justify-center h-[45px] hover:bg-[#F2F2F2] w-full font-bold rounded-lg cursor-pointer',
                                    activeTab === 'posts' ? 'text-blue-500' : ''
                                ]"
                            >
                                {{ $t('profile.tabs.posts') }}
                            </div>
                            <div v-if="activeTab === 'posts'" class="border-b-4 border-b-blue-400 rounded-md"></div>
                        </button>

                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[100px] font-bold rounded-lg mx-1 cursor-pointer" @click="setActiveTab('info')">
                            <div
                                :class="[
                                    'flex items-center text-[15px] justify-center h-[45px] w-full font-bold rounded-lg cursor-pointer',
                                    activeTab === 'info' ? 'text-blue-500' : ''
                                ]"
                            >
                                {{ $t('profile.tabs.info') }}
                            </div>
                            <div v-if="activeTab === 'info'" class="border-b-4 border-b-blue-400 rounded-md -mt-[4px]"></div>
                        </button>

                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[85px] font-bold rounded-lg mx-1 cursor-pointer" @click="setActiveTab('friends')">
                            <div
                                :class="[
                                    'flex items-center text-[15px] justify-center h-[45px] w-full font-bold rounded-lg cursor-pointer',
                                    activeTab === 'friends' ? 'text-blue-500' : ''
                                ]"
                            >
                                {{ $t('profile.tabs.friends') }}
                            </div>
                            <div v-if="activeTab === 'friends'" class="border-b-4 border-b-blue-400 rounded-md -mt-[4px]"></div>
                        </button>

                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[85px] font-bold rounded-lg mx-1 cursor-pointer">
                            {{ $t('profile.tabs.photos') }}
                        </button>
                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[110px] font-bold rounded-lg mx-1 cursor-pointer">
                            {{ $t('profile.tabs.checkins') }}
                        </button>
                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[70px] font-bold rounded-lg mx-1 cursor-pointer">
                            {{ $t('profile.tabs.sports') }}
                        </button>
                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[90px] font-bold rounded-lg mx-1 cursor-pointer">
                            {{ $t('common.more') }}
                        </button>
                    </div>
                </div>
            </div>

            <div class="flex-cols md:flex w-full max-w-[1250px] justify-between h-[calc(100%-56px)] md:px-0 px-2 mx-auto">

                <div v-if="activeTab === 'posts'" id="LeftSection" class="w-full md:w-5/12 mt-4 mr-4">
                    <div class="bg-theme-bg-secondary p-3 rounded-lg shadow-lg">
                        <div class="font-extrabold pb-2 text-theme-text text-xl">Intro</div>
                        <div class="pb-5">
                            <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                                Add bio
                            </button>
                        </div>
                        <div class="pb-5">
                            <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                                Edit details
                            </button>
                        </div>
                        <div class="pb-5">
                            <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                                Add hobbies
                            </button>
                        </div>
                        <div>
                            <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                                Add feature
                            </button>
                        </div>
                    </div>

                      <div class="bg-theme-bg-secondary p-3 mt-4 rounded-lg shadow-lg">
                            <div class="flex justify-between items-center mb-2">
                                <div class="font-extrabold text-theme-text text-xl">Zdjęcia</div>
                                <a class="text-blue-500 font-semibold text-[15px] hover:underline cursor-pointer">Zobacz wszystkie zdjęcia</a>
                            </div>

                            <div class="grid grid-cols-3 gap-1">
                                <img v-for="id in miniPhotosList" :key="id"
                                    :src="`https://picsum.photos/id/${id}/150/150`"
                                    class="w-full h-24 md:h-28 object-cover rounded-lg aspect-square cursor-pointer"
                                    alt="Zdjęcie"
                                >
                            </div>
                        </div>


                        <div class="bg-theme-bg-secondary p-3 mt-4 rounded-lg shadow-lg">
                            <div class="flex justify-between items-center mb-2">
                                <div class="font-extrabold text-theme-text text-xl">Znajomi</div>
                                <a class="text-blue-500 font-semibold text-[15px] hover:underline cursor-pointer">Pokaż wszystkich znajomych</a>
                            </div>
                            <div class="text-sm text-theme-text-secondary mb-3">525 (wspólnych: 79)</div>

                            <div class="grid grid-cols-3 gap-4">
                                <div v-for="(friend, index) in miniFriendsList" :key="index" class="cursor-pointer">
                                    <img
                                        :src="`https://picsum.photos/id/${friend.imageId}/120/120`"
                                        class="w-full h-auto rounded-lg object-cover aspect-square"
                                        :alt="friend.name"
                                    >
                                    <div class="text-sm font-semibold mt-1 text-theme-text">{{ friend.name.split(' ')[0] }}</div>
                                    <div class="text-xs text-theme-text-secondary">{{ friend.mutual }} wsp. znajomych</div>
                                </div>
                            </div>
                        </div>
                </div>

                <div
                    id="ContentSection"
                    :class="[
                        'w-full mt-4 overflow-auto',
                        activeTab === 'posts' ? 'md:w-7/12' : 'md:w-full'
                    ]"
                >

                    <div v-if="activeTab === 'posts'">
                        <CreatePostBox
                            :image="'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg'"
                            :placeholder="'What\'s on your mind Wiktoria?'"
                        />
                        </div>

                    <div v-if="activeTab === 'info'" class="bg-theme-bg-secondary p-4 rounded-lg shadow-lg">
                        <div class="flex">
                            <div class="w-1/4 border-r pr-4">
                                <ul class="space-y-1 text-theme-text-secondary">
                                    <li class="p-2 rounded-lg bg-blue-100 text-blue-600 font-bold cursor-pointer">Przegląd</li>
                                    <li class="p-2 rounded-lg hover:bg-gray-100 cursor-pointer">Praca i wykształcenie</li>
                                    <li class="p-2 rounded-lg hover:bg-gray-100 cursor-pointer">Wcześniejsze miejsca zamieszkania</li>
                                    <li class="p-2 rounded-lg hover:bg-gray-100 cursor-pointer">Dane kontaktowe i podstawowe informacje</li>
                                    <li class="p-2 rounded-lg hover:bg-gray-100 cursor-pointer">Rodzina i związki</li>
                                    <li class="p-2 rounded-lg hover:bg-gray-100 cursor-pointer">Informacje szczegółowe o użytkowniku Wiktoria</li>
                                    <li class="p-2 rounded-lg hover:bg-gray-100 cursor-pointer">Wydarzenia z życia</li>
                                </ul>
                            </div>

                            <div class="w-3/4 pl-6 text-theme-text-secondary">
                                <h2 class="text-xl font-bold mb-4 text-theme-text">Informacje</h2>
                                <div class="space-y-4">
                                    <div class="flex items-center">
                                        <span class="material-icons mr-3">💼</span>
                                        Brak miejsc pracy do wyświetlenia
                                    </div>
                                    <div class="flex items-center">
                                        <span class="material-icons mr-3">🎓</span>
                                        Uczęszczała do: **IV LO im. Jana Pawła II w Łukowie**
                                    </div>
                                    <div class="flex items-center">
                                        <span class="material-icons mr-3">🏠</span>
                                        Mieszka w: **Łuków**
                                    </div>
                                    <div class="flex items-center">
                                        <span class="material-icons mr-3">📍</span>
                                        Z: **Łuków**
                                    </div>
                                    <div class="flex items-center">
                                        <span class="material-icons mr-3">❤️</span>
                                        Brak informacji o związku do wyświetlenia
                                    </div>
                                </div>

                                <h3 class="text-xl font-bold mt-8 mb-4 text-theme-text">Znajomi</h3>
                                <div class="flex space-x-4 mb-4 text-theme-text-secondary">
                                    <span class="font-bold border-b-2 border-b-blue-500 pb-1 text-blue-600 cursor-pointer">Wszyscy znajomi</span>
                                    <span class="hover:border-b-2 hover:border-b-gray-300 pb-1 cursor-pointer">Wspólni znajomi</span>
                                    <span class="hover:border-b-2 hover:border-b-gray-300 pb-1 cursor-pointer">Aktualne miejsce zamieszkania</span>
                                    <span class="hover:border-b-2 hover:border-b-gray-300 pb-1 cursor-pointer">Miejscowość rodzinna</span>
                                    <span class="hover:border-b-2 hover:border-b-gray-300 pb-1 cursor-pointer">Obserwowani</span>
                                </div>

                                <div class="grid grid-cols-2 gap-4">
                                    </div>
                            </div>
                        </div>
                    </div>

                    <div v-if="activeTab === 'friends'" class="bg-theme-bg-secondary p-4 rounded-lg shadow-lg border border-gray-200">
                        <div class="flex justify-between items-center mb-4 pb-3">
                            <h2 class="text-2xl font-extrabold text-theme-text">Znajomi</h2>
                            <div class="relative w-48">
                                <input
                                    type="text"
                                    placeholder="Szukaj"
                                    class="bg-gray-100 rounded-full py-2 pl-10 pr-4 w-full text-sm text-theme-text-secondary focus:outline-none focus:ring-2 focus:ring-blue-500 hover:bg-gray-200"
                                >
                                <Magnify :size="18" class="absolute left-3 top-2.5 text-gray-500"/>
                            </div>
                        </div>

                        <div class="flex space-x-6 mb-4 text-theme-text-secondary border-b">
                            <span class="font-bold border-b-4 border-b-blue-500 pb-3 text-blue-600 cursor-pointer text-[15px]">Wszyscy znajomi</span>
                            <span class="hover:border-b-4 hover:border-b-gray-300 pb-3 cursor-pointer text-[15px]">Wspólni znajomi</span>
                            <span class="hover:border-b-4 hover:border-b-gray-300 pb-3 cursor-pointer text-[15px]">Aktualne miejsce zamieszkania</span>
                            <span class="hover:border-b-4 hover:border-b-gray-300 pb-3 cursor-pointer">Miejscowość rodzinna</span>
                            <span class="hover:border-b-4 hover:border-b-gray-300 pb-3 cursor-pointer">Obserwowani</span>
                        </div>

                        <div class="flex flex-wrap -mx-2 mt-4">

                            <template v-for="(friend, index) in friendsList" :key="index">
                                <div class="w-full md:w-1/2 px-2 mb-2">
                                    <div class="flex items-center justify-between p-2 hover:bg-gray-100 rounded-lg">

                                        <div class="flex items-center">
                                            <img :src="`https://picsum.photos/id/${friend.imageId}/60/60`" class="w-14 h-14 rounded-lg mr-3 border border-gray-200" :alt="`Zdjęcie ${friend.name}`">
                                            <div>
                                                <div class="font-semibold text-theme-text">{{ friend.name }}</div>
                                                <div class="text-xs text-theme-text-secondary">{{ friend.mutual }} wspólnych znajomych</div>
                                            </div>
                                        </div>

                                        <div class="flex items-center space-x-2">

                                            <button v-if="!friend.isFriend" class="bg-blue-500 hover:bg-blue-600 text-white text-sm px-3 py-1.5 rounded-lg font-bold whitespace-nowrap">Dodaj znajomego</button>

                                            <button
                                                v-if="friend.isFriend"
                                                class="w-8 h-8 flex items-center justify-center hover:bg-gray-200 rounded-full text-theme-text-secondary"
                                            >
                                                <span class="font-bold text-xl leading-none -mt-1">...</span>
                                            </button>


                                        </div>
                                    </div>
                                </div>
                            </template>

                        </div>
                    </div>

                </div>
            </div>
        </div>

</template>
