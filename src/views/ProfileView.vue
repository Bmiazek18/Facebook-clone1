<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, provide } from 'vue';
import { useRoute, useRouter } from 'vue-router'; // Import useRouter
import { useI18n } from 'vue-i18n'; // Import useI18n

// --- IMPORTY KOMPONENTÓW ---
import ImageWithGradient from '@/components/media/ImageWithGradient.vue';
// Dodaj te importy w sekcji ikon
import MapMarker from 'vue-material-design-icons/MapMarker.vue';
import Domain from 'vue-material-design-icons/Domain.vue'; // lub School.vue
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
import BaseModal from '@/components/common/BaseModal.vue';
import EditProfileImgModal from '@/components/profile/EditProfileImgModal.vue';
import UserAvatar from '@/components/common/UserAvatar.vue';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const { t } = useI18n(); // Initialize useI18n and get the t function // Initialize useRouter

// --- KONFIGURACJA ZAKŁADEK ---
const tabs = [
    { key: 'posts', label: 'profile.tabs.posts' },
    { key: 'info', label: 'profile.tabs.info' },
    { key: 'friends', label: 'profile.tabs.friends' },
    { key: 'photos', label: 'profile.tabs.photos' },
    { key: 'videos', label: 'profile.tabs.videos' },

    { key: 'more', label: 'profile.tabs.more', hasDropdown: true },
];

const activeTab = computed(() => {
    const currentRouteName = route.name as string;
    if (currentRouteName.includes('profile-info') || currentRouteName.includes('userProfile-info')) {
        return 'info';
    }
    if (currentRouteName.includes('profile-')) {
        return currentRouteName.split('profile-')[1];
    } else if (currentRouteName.includes('userProfile-')) {
        return currentRouteName.split('userProfile-')[1];
    }
    return 'posts'; // Default to 'posts' if no specific tab route is matched
});

function setActiveTab(tabKey: string) {
    const userId = route.params.userId;
    let routeName;
    if (userId) {
        routeName = `userProfile-${tabKey}`;
    } else {
        routeName = `profile-${tabKey}`;
    }
    router.push({ name: routeName, params: { userId: userId as string } });
}

// --- DANE UŻYTKOWNIKA ---
const auth = useAuthStore();
const userIdParam = computed(() => {
    const id = route.params.userId;
    return id ? parseInt(id as string, 10) : null;
});

const profileUser = computed(() => {
    if (userIdParam.value) {
        return getUserById(userIdParam.value);
    }
    return auth.currentUser;
});

// Czy to profil zalogowanego użytkownika?
const isOwner = computed(() => {
    if (!profileUser.value || !auth.currentUser) {
        return false;
    }
    return profileUser.value.id === auth.currentUser.id;
});

provide('isOwner', isOwner);
provide('profileUser', profileUser);

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
    if (profileUser.value) {
        document.title = `${profileUser.value.name} | Facebook`;
    }
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
    <div v-if="profileUser" class="w-full min-h-screen pb-20 bg-theme-bg mt-[56px]">

        <div
            v-if="isTabsFixed"
            class="fixed top-[50px] left-0 right-0 h-[70px] bg-theme-bg-secondary  shadow-theme-shadow border-b border-theme-border z-30 animate-slide-down flex items-center"
        >
            <div class="max-w-[1200px] flex items-center justify-between w-full mx-auto   lg:px-0">
                <div  class="flex items-center space-x-3">
                    <UserAvatar :user="profileUser" :size="40" class="border border-theme-border" />
                    <div class="text-[17px] text-theme-text  leading-5">
                        {{ profileUser.name }}
                    </div>
                </div>
                <div class="flex items-center space-x-2">
                    <button class="w-9 h-9 flex items-center justify-center bg-theme-bg hover:bg-theme-bg-hover rounded-sm transition-colors">
                        <DotsHorizontal :size="20" fillColor="currentColor" class="text-theme-text"/>
                    </button>
                </div>
            </div>
        </div>

        <div class="w-full bg-theme-bg-secondary shadow-theme-shadow">
          <template v-if="profileUser.cover">
    <ImageWithGradient
        :image-url="profileUser.cover"
        class="rounded-b-xl "
    />
</template>

<div
    v-else
    class="w-full h-[200px] md:h-[350px] bg-gradient-to-b from-gray-200 to-gray-300 rounded-b-xl relative shadow-inner"
>
    <button
        v-if="isOwner"

        class="absolute bottom-4 right-4 bg-white hover:bg-gray-50 text-black px-4 py-2 rounded-md font-semibold text-[15px] shadow-sm flex items-center transition-colors cursor-pointer"
    >
        <Camera :size="20" class="mr-2"/>
        {{ $t('profile.addCoverPhoto') }}
    </button>
</div>
            <div class="max-w-[1250px] mx-auto relative">



                <div id="ProfileInfo" class="px-4 lg:px-[32px] ">

                    <div class="flex flex-col lg:flex-row items-center lg:items-end relative ">

                        <div class="relative z-10 flex-shrink-0">
                            <div class="relative group p-1 bg-theme-bg-secondary rounded-full">
                                <UserAvatar
                                    :user="profileUser"
                                    :size="168"
                                    class="border-[4px] border-theme-bg-secondary bg-theme-bg-secondary shadow-sm relative block"
                                />


                                <button v-if="isOwner" @click="openImagePicker" class="absolute bottom-4 right-4 bg-gray-200 hover:bg-gray-300 text-black p-2 rounded-full cursor-pointer transition-colors ">
                                    <Camera :size="22" fillColor="currentColor" class="text-theme-text"/>
                                </button>
                            </div>
                        </div>

                        <div class="flex-1 flex flex-col items-center lg:items-start mt-2 lg:mt-0 lg:ml-6 lg:mb-4 min-w-0">
                            <h1 class="text-[32px] font-bold text-theme-text leading-tight text-center lg:text-left mb-1">
                                {{ profileUser.name }}
                            </h1>

                            <div class="flex items-center text-[15px] font-semibold text-theme-text mb-2">
                                <span class="hover:underline cursor-pointer">{{ $t('profile.friendsCount', { count: profileUser.friendsCount }) }}</span>
                                <template v-if="profileUser.mutualFriendsCount && !isOwner">
                                  <span class="mx-1.5">•</span>
                                  <span class="hover:underline cursor-pointer">{{ $t('profile.mutualFriendsCount', { count: profileUser.mutualFriendsCount }) }}</span>
                                </template>
                            </div>

                            <div class="flex flex-wrap items-center justify-center lg:justify-start gap-4 text-[15px] text-theme-text font-medium mb-3" v-if="profileUser.location || profileUser.school">
                                <div class="flex items-center gap-1.5" v-if="profileUser.location">
                                    <MapMarker :size="18" class="text-theme-text-secondary opacity-70" />
                                    <span>{{ profileUser.location.split(',')[0] }}</span> </div>
                                <div class="flex items-center gap-1.5" v-if="profileUser.school">
                                    <Domain :size="18" class="text-theme-text-secondary opacity-70" />
                                    <span>{{ profileUser.school }}</span> </div>
                            </div>

                            <div v-if="!isOwner" class="flex items-center -space-x-2 mt-1">
                                <img v-for="i in 8" :key="i"
                                    class="w-[32px] h-[32px] rounded-full  object-cover cursor-pointer  relative"
                                    :src="`https://picsum.photos/id/${150 + i}/100/100`"
                                    :alt="`Friend ${i}`"
                                >
                            </div>
                        </div>

                        <div class="flex flex-col sm:flex-row items-center gap-3 mt-6 lg:mt-0 lg:mb-8 lg:self-end flex-shrink-0">
                            <button v-if="isOwner" @click="router.push('/stories/create')" class="flex items-center px-4 py-[8px] bg-theme-primary hover:bg-theme-primary-hover text-white rounded-[6px] font-semibold text-[15px] transition-colors">
                                <Plus :size="20" class="mr-1.5" fillColor="#FFFFFF"/>
                                {{ $t('profile.addToStory') }}
                            </button>
                            <button v-if="isOwner" @click="router.push({ name: 'profile-info' })" class="flex items-center px-4 py-[8px] bg-theme-bg hover:bg-theme-bg-hover text-theme-text rounded-[6px] font-semibold text-[15px] transition-colors">
                                <Pencil :size="18" class="mr-1.5 text-theme-text" fillColor="currentColor" />
                                {{ $t('profile.editProfile') }}
                            </button>

                            <template v-else>
                                <button class="flex items-center px-3 py-[7px] bg-theme-primary text-white rounded-md font-semibold">
                                    <Message :size="20" class="mr-1.5" /> {{ $t('profile.sendMessage') }}
                                </button>
                            </template>

                             <button class="flex items-center justify-center w-[36px] h-[36px] bg-theme-bg hover:bg-theme-bg-hover rounded-[6px] transition-colors">
                                <ChevronDown :size="24" fillColor="currentColor" class="text-theme-text"/>
                            </button>
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
                                {{ t(tab.label) }}
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
            <router-view :friends-list="friendsList" :mini-photos-list="miniPhotosList" :user-name="profileUser.name" :user-image="profileUser.avatar" />
        </div>
    </div>
    <BaseModal v-if="isPickerOpen" @close="()=>!isPickerOpen" :title="$t('profile.editProfileImage')"> <EditProfileImgModal /></BaseModal>
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
