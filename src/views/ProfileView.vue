<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute } from 'vue-router';

// --- IMPORTY KOMPONENTÓW ---
import ProfilePostsTab from '@/components/ProfilePostsTab.vue';
import ProfileInfoTab from '@/components/ProfileInfoTab.vue';
import FriendsSection from '@/components/FriendsSection.vue';
import ImageWithGradient from '@/components/ImageWithGradient.vue';

// --- IMPORTY IKON (Vue Material Design Icons) ---
import Camera from 'vue-material-design-icons/Camera.vue';
import Pencil from 'vue-material-design-icons/Pencil.vue';
import Plus from 'vue-material-design-icons/Plus.vue';
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue';
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';
import Message from 'vue-material-design-icons/Message.vue';

// --- DANE (MOCK) ---
import { getUserById } from '@/data/users';
import type { User } from '@/data/users';
import BaseModal from '@/components/BaseModal.vue';
import EditProfileImgModal from '@/components/editProfileImgModal.vue';

const route = useRoute();

// --- KONFIGURACJA ZAKŁADEK ---
const activeTab = ref('posts');
const tabs = [
    { key: 'posts', label: 'Posty' },
    { key: 'info', label: 'Informacje' },
    { key: 'friends', label: 'Znajomi' },
    { key: 'photos', label: 'Zdjęcia' },
    { key: 'videos', label: 'Filmy' },
    { key: 'checkins', label: 'Miejsca' },
    { key: 'sport', label: 'Sport' },
    { key: 'more', label: 'Więcej', hasDropdown: true },
];

function setActiveTab(tabKey: string) {
    activeTab.value = tabKey;
}

// --- DANE UŻYTKOWNIKA ---
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
    friendsCount: 541,
    postsCount: 124,
    cover: 'https://picsum.photos/1200/300?random=101',
    status: 'online'
};

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

// Czy to profil zalogowanego użytkownika?
const isOwner = computed(() => profileUser.value.id === currentUser.id);

// --- STICKY HEADER LOGIC ---
const tabsContainerRef = ref<HTMLElement | null>(null);
const isTabsFixed = ref(false);

const handleScroll = () => {
    if (tabsContainerRef.value) {
        // Jeśli element zakładek dotyka górnego paska nawigacji (ok. 56px)
        const rect = tabsContainerRef.value.getBoundingClientRect();
        isTabsFixed.value = rect.top <= 56;
    }
};

onMounted(() => {
    document.title = `${profileUser.value.name} | Facebook`;
    window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
});
const isPickerOpen = ref(false);
const openImagePicker = () => {
    isPickerOpen.value = true;
};
// --- LISTA ZNAJOMYCH (MOCK) ---
const friendsList = ref([
    { name: 'Natalia Wójcik', mutual: 71, isFriend: true, imageId: 35 },
    { name: 'Kacper Szymański', mutual: 10, isFriend: false, imageId: 36 },
    { name: 'Monika Zawadzka', mutual: 211, isFriend: true, imageId: 37 },
    { name: 'Michał Kowalczyk', mutual: 15, isFriend: false, imageId: 38 },
    { name: 'Ewa Lipińska', mutual: 45, isFriend: true, imageId: 39 },
    { name: 'Marek Pająk', mutual: 8, isFriend: false, imageId: 40 },
    { name: 'Piotr Zieliński', mutual: 99, isFriend: true, imageId: 20 },
]);

const miniPhotosList = [101, 102, 103, 104, 105, 106, 107, 108, 109];
</script>

<template>
    <div class="w-full min-h-screen pb-20 bg-[#F0F2F5]">

        <div
            v-if="isTabsFixed"
            class="fixed top-[50px] left-0 right-0 h-[60px] bg-white shadow-sm border-b border-gray-300 z-30 animate-slide-down flex items-center"
        >
            <div class="max-w-[1095px] flex items-center justify-between w-full mx-auto px-4 lg:px-0">
                <div class="flex items-center space-x-3">
                    <img class="rounded-full w-[40px] h-[40px] border border-gray-200 object-cover" :src="profileUser.avatar" alt="Avatar">
                    <div class="text-[17px] text-[#050505] font-bold leading-5">
                        {{ profileUser.name }}
                    </div>
                </div>
                <div class="flex items-center space-x-2">
                    <button class="w-9 h-9 flex items-center justify-center bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-full transition-colors">
                        <DotsHorizontal :size="20" fillColor="#050505"/>
                    </button>
                </div>
            </div>
        </div>

        <div class="w-full bg-white shadow-sm">  <ImageWithGradient :image-url="profileUser.cover" class="rounded-b-xl" />
            <div class="max-w-[1250px] mx-auto relative">



                <div id="ProfileInfo" class="px-4 lg:px-[32px]  relative">

                    <div class="flex flex-col lg:flex-row items-center lg:items-end justify-between relative z-10">

                        <div class="relative -mt-[86px] lg:mr-4">
                            <div class="relative group">
                                <img
                                    class="rounded-full w-[168px] h-[168px] border-[4px] border-white bg-white object-cover shadow-sm"
                                    :src="profileUser.avatar"
                                    alt="Avatar"
                                >
                                <button @click="openImagePicker" class="absolute bottom-2 right-2 bg-[#E4E6EB] hover:bg-[#D8DADF] p-2 rounded-full cursor-pointer transition-colors border-2 border-white">
                                    <Camera :size="20" fillColor="#050505"/>
                                </button>
                            </div>
                        </div>

                        <div class="flex flex-col items-center lg:items-start -mt-3 lg:mt-0 lg:mb-4 flex-1">
                            <h1 class="text-[32px] text-[#050505] font-bold leading-tight text-center lg:text-left">
                                {{ profileUser.name }}
                            </h1>
                            <div class="text-[15px] font-semibold text-gray-500 mt-1">
                                {{ profileUser.friendsCount }} znajomi
                            </div>

                            <div class="flex items-center justify-center lg:justify-start mt-2 cursor-pointer">
                                <img v-for="i in 7" :key="i"
                                    class="rounded-full -ml-2 first:ml-0 w-[32px] h-[32px] border-2 border-white object-cover relative z-0"
                                    :src="`https://picsum.photos/id/${140 + i}/100/100`"
                                >
                            </div>
                        </div>

                        <div class="flex flex-col sm:flex-row items-center gap-3 mt-4 lg:mt-0 lg:mb-6 lg:self-end">
                            <template v-if="isOwner">
                                <button class="flex items-center px-3 py-[7px] bg-[#1877F2] hover:bg-[#166fe5] text-white rounded-md font-semibold text-[15px] transition-colors">
                                    <Plus :size="20" class="mr-1.5" fillColor="#FFFFFF"/>
                                    Dodaj do relacji
                                </button>
                                <button class="flex items-center px-3 py-[7px] bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] rounded-md font-semibold text-[15px] transition-colors">
                                    <Pencil :size="18" class="mr-1.5" fillColor="#050505"/>
                                    Edytuj profil
                                </button>
                                <button class="flex items-center justify-center w-[36px] h-[36px] bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md transition-colors">
                                    <ChevronDown :size="24" fillColor="#050505"/>
                                </button>
                            </template>

                            <template v-else>
                                <button class="flex items-center px-3 py-[7px] bg-[#1877F2] text-white rounded-md font-semibold">
                                    <Message :size="20" class="mr-1.5" /> Wyślij wiadomość
                                </button>
                            </template>
                        </div>

                    </div>

                    <div class="h-[1px] bg-gray-300 mt-6 lg:mt-4 mb-1 mx-auto opacity-70"></div>

                    <div ref="tabsContainerRef" class="flex flex-wrap items-center justify-start lg:gap-1">
                        <button
                            v-for="tab in tabs"
                            :key="tab.key"
                            @click="setActiveTab(tab.key)"
                            class="relative h-[60px] px-4 flex items-center justify-center cursor-pointer rounded-lg hover:bg-gray-100 transition-colors group"
                        >
                            <span
                                class="text-[15px] font-semibold"
                                :class="activeTab === tab.key ? 'text-[#1877F2]' : 'text-gray-600'"
                            >
                                {{ tab.label }}
                                <ChevronDown v-if="tab.hasDropdown" :size="16" class="inline-block ml-1 opacity-70"/>
                            </span>

                            <div
                                v-if="activeTab === tab.key"
                                class="absolute bottom-0 left-0 w-full h-[3px] bg-[#1877F2] rounded-t-sm"
                            ></div>
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="max-w-[1250px] mx-auto md:px-0 px-2 ">
            <div class="grid grid-cols-1 gap-4">

                <ProfilePostsTab
                    v-if="activeTab === 'posts'"
                    :friends-list="friendsList"
                    :mini-photos-list="miniPhotosList"
                    :user-name="profileUser.name"
                    :user-image="profileUser.avatar"
                />

                <ProfileInfoTab v-if="activeTab === 'info'" />

                <div v-if="activeTab === 'friends'" class="bg-white rounded-lg shadow-sm p-4">
                    <FriendsSection :friends-list="friendsList" :is-full-view="true" />
                </div>

                <div v-if="!['posts', 'info', 'friends'].includes(activeTab)" class="bg-white p-8 rounded-lg shadow-sm text-center text-gray-500">
                    <h3 class="font-bold text-xl mb-2">Sekcja: {{ activeTab }}</h3>
                    <p>Ta część interfejsu jest jeszcze w budowie.</p>
                </div>

            </div>
        </div>
    </div>
    <BaseModal v-if="true" @close="()=>!isPickerOpen" :title="'Wybierz zdjecie profileowe'"> <EditProfileImgModal /></BaseModal>
</template>

<style scoped>
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
