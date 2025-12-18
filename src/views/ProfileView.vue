<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute } from 'vue-router';

// Importy nowych komponentów zakładek
import ProfilePostsTab from '@/components/ProfilePostsTab.vue';
import ProfileInfoTab from '@/components/ProfileInfoTab.vue';
import FriendsSection from '@/components/FriendsSection.vue'; // Pełny widok znajomych

// Importy dla UI
import Camera from 'vue-material-design-icons/Camera.vue';
import Check from 'vue-material-design-icons/Check.vue';
import Message from 'vue-material-design-icons/Message.vue';
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'; // Dodany do górnego paska
import ImageWithGradient from '@/components/ImageWithGradient.vue';

// Importy do pobrania danych użytkownika
import { getUserById } from '@/data/users';
import type { User } from '@/data/users';

const route = useRoute();
const activeTab = ref('posts'); // Ustawiamy domyślnie na 'friends' jak w przykładzie

// Domyślny użytkownik (aktualnie zalogowany)
const currentUser: User = {
  id: 1,
  name: 'Bartosz Miazek',
  avatar: 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg',
  bio: 'Developer & Tech Enthusiast',
  location: 'Białystok, Polska',
  website: 'https://bartoszmiazek.com',
  joinDate: '2020-05-15',
  followersCount: 1250,
  followingCount: 342,
  friendsCount: 856,
  postsCount: 124,
  cover: 'https://picsum.photos/1200/300?random=101',
  status: 'online'
};

// Pobranie danych użytkownika na podstawie userId z URL lub użycie domyślnego
const userIdParam = computed(() => {
  const id = route.params.userId;
  return id ? parseInt(id as string, 10) : null;
});

const profileUser = computed(() => {
  if (userIdParam.value) {
    return getUserById(userIdParam.value) || currentUser;
  }
  return currentUser;
});

const userName = computed(() => profileUser.value.name);
const userImage = computed(() => profileUser.value.avatar);

function setActiveTab(tab: string) {
    activeTab.value = tab;
}

function updateTitle() {
        document.title = `${userName.value} | Facebook`;
}

// ----------------------------------------------------
// LOGIKA "PRZYKLEJANIA" (Sticky Tabs)
// ----------------------------------------------------
const tabsContainerRef = ref<HTMLElement | null>(null);
const isTabsFixed = ref(false);
const tabOffsetTop = ref(0);

const handleScroll = () => {
    if (window.scrollY > tabOffsetTop.value) {
        isTabsFixed.value = true;
    } else {
        isTabsFixed.value = false;
    }
};

onMounted(() => {
    updateTitle();
    if (tabsContainerRef.value) {
        tabOffsetTop.value = tabsContainerRef.value.getBoundingClientRect().top + window.scrollY;
    }
    window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
});
// ----------------------------------------------------
// KONIEC LOGIKI "PRZYKLEJANIA"
// ----------------------------------------------------


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

const miniPhotosList = [101, 102, 103, 104, 105, 106, 107, 108, 109];
</script>

<template>

    <div class="w-full min-h-screen pb-20 bg-theme-bg">
        <div
            v-if="isTabsFixed"
            class="fixed top-[50px] left-0 right-0 h-[60px] bg-theme-bg-secondary shadow-md z-30 animate-slide-down"
        >
            <div class="max-w-[1250px] mx-auto flex items-center justify-between h-full px-4 md:px-0">

                <div class="flex items-center space-x-3">
                    <img
                        class="rounded-full w-9 h-9 border-theme-border border-2 object-cover"
                        :src="userImage"
                        alt="Avatar"
                    >
                    <div class="text-[17px] text-theme-text font-bold">
                       {{ userName }}
                    </div>
                </div>

                <div class="flex items-center space-x-2">
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
                        <button class="flex items-center px-3 py-1.5 font-bold text-sm">
                            <Message class="mr-1" :size="20"/> Wiadomość
                        </button>
                    </a>

                    <button
                        class="w-8 h-8 flex items-center justify-center bg-gray-200 hover:bg-gray-300 rounded-full cursor-pointer"
                    >
                        <DotsHorizontal :size="20"/>
                    </button>
                </div>

            </div>
        </div>

        <div class="w-full bg-theme-bg-secondary">
            <ImageWithGradient :image-url="profileUser.cover" class="rounded-b-xl"/>
            <div class="max-w-[1250px]  mx-auto pb-1">

                <div id="ProfileInfo" class="flex md:flex-row flex-col items-center justify-between px-4">
                    <div class="flex md:flex-row flex-col gap-4 md:-mt-6 -mt-16 items-center">
                        <div class="relative">
                            <img
                                class="rounded-full w-[165px] h-[165px] border-theme-border border-4"
                                :src="userImage"
                            >
                            <button
                                class="absolute right-0 top-[100px] bg-gray-200 hover:bg-gray-300 p-1.5 rounded-full cursor-pointer"
                            >
                                <Camera :size="25"/>
                            </button>
                        </div>
                        <div class="md:mt-4 text-center md:text-left -mt-3">
                            <div class="text-[28px] text-theme-text font-extrabold pt-1">
                                {{ userName }}
                            </div>
                            <div class="text-[17px] font-bold text-theme-text-secondary mb-1.5 text-center md:text-left">{{ profileUser.friendsCount }} znajomi · {{ profileUser.mutualFriendsCount || 0 }} wspólnych znajomych</div>
                            <div class="flex md:justify-start justify-center md:-ml-1">
                                <img v-for="i in 7" :key="i"
                                    class="rounded-full -ml-3 z-[10 - i] w-[40px] h-[40px] border-white border-2"
                                    :src="`https://picsum.photos/id/${140 + i}/2000/320`"
                                >
                            </div>
                        </div>
                    </div>

                    <div class="flex items-center md:my-0 my-4 gap-2">
                         <a
                            class="flex justify-center items-baseline bg-gray-200 hover:bg-gray-300 rounded-lg cursor-pointer"
                        >
                            <button class="flex items-center px-4 py-2 font-bold">
                                <Check class="-ml-2 mr-1" :size="22"/> {{ $t('ui.friend') }}
                            </button>
                        </a>
                         <a
                            class="flex justify-center items-baseline bg-blue-500 hover:bg-blue-600 rounded-lg cursor-pointer text-white"
                        >
                            <button class="flex items-center px-4 py-2 font-bold">
                                <Message class="-ml-2 mr-1" :size="22"/> {{ $t('ui.message') }}
                            </button>
                        </a>
                    </div>
                </div>

                <div
                    ref="tabsContainerRef"
                    class="w-full"
                    :class="{'h-[50px]': isTabsFixed}"
                >
                    <div
                        class="flex items-center w-full border-t h-[50px] text-theme-text-secondary py-[4px]"
                    >
                        <div class="max-w-[1250px] mx-auto flex w-full h-full px-4 md:px-0">
                            <button class="w-[85px]" @click="setActiveTab('posts')">
                                <div
                                    :class="[
                                        'flex items-center text-[15px] justify-center h-[45px] hover:bg-[#F2F2F2] w-full font-bold rounded-lg cursor-pointer',
                                        activeTab === 'posts' ? 'text-blue-500' : ''
                                    ]"
                                >
                                    Posty
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
                                    Informacje
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
                                    Znajomi
                                </div>
                                <div v-if="activeTab === 'friends'" class="border-b-4 border-b-blue-400 rounded-md -mt-[4px]"></div>
                            </button>

                            <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[85px] font-bold rounded-lg mx-1 cursor-pointer">
                                Zdjęcia
                            </button>
                            <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[110px] font-bold rounded-lg mx-1 cursor-pointer">
                                Miejsca
                            </button>
                            <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[70px] font-bold rounded-lg mx-1 cursor-pointer">
                                Sport
                            </button>
                            <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[90px] font-bold rounded-lg mx-1 cursor-pointer">
                                Więcej
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="max-w-[1250px] mx-auto md:px-0 px-2">

            <ProfilePostsTab
                v-if="activeTab === 'posts'"
                :friends-list="friendsList"
                :mini-photos-list="miniPhotosList"
                :user-name="userName"
                :user-image="userImage"
            />

            <ProfileInfoTab
                v-if="activeTab === 'info'"
            />

            <div v-if="activeTab === 'friends'" class="mt-4">
                <FriendsSection
                    :friends-list="friendsList"
                    :is-full-view="true"
                />
            </div>

            </div>
    </div>
    </template>
    <style>
    @keyframes slide-down {
        from {
            opacity: 0;
            transform: translateY(-100%);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    .animate-slide-down {
        animation: slide-down 0.3s ease-out;
    }
    </style>

