<script setup lang="ts">
  import { ref, computed, inject } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import FriendsSection from '@/components/friends/FriendsSection.vue'

  const route = useRoute()
  const router = useRouter()
  const { t } = useI18n()
  const profileUser: any = inject('profileUser');

  const hasWorkOrEducation = computed(() => {
    if (!profileUser.value) return false;
    return profileUser.value.job || profileUser.value.company || profileUser.value.school || profileUser.value.education || profileUser.value.highSchool;
  });

  const hasPlaces = computed(() => {
    if (!profileUser.value) return false;
    return profileUser.value.location || profileUser.value.hometown;
  });

  const hasContactBasic = computed(() => {
    if (!profileUser.value) return false;
    return profileUser.value.phone || profileUser.value.website || profileUser.value.email;
  });

  const hasFamily = computed(() => {
    if (!profileUser.value) return false;
    return profileUser.value.relationshipStatus || (profileUser.value.familyMembers && profileUser.value.familyMembers.length > 0);
  });

  const hasDetails = computed(() => {
    if (!profileUser.value) return false;
    return profileUser.value.bioDetails || profileUser.value.namePronounciation || (profileUser.value.otherNames && profileUser.value.otherNames.length > 0) || (profileUser.value.favoriteQuotes && profileUser.value.favoriteQuotes.length > 0);
  });

  const hasEvents = computed(() => {
    if (!profileUser.value) return false;
    return profileUser.value.lifeEvents && profileUser.value.lifeEvents.length > 0;
  });

  const activeTab = computed(() => {
    const currentRouteName = route.name as string;
    if (currentRouteName.includes('profile-info-')) {
        return currentRouteName.split('profile-info-')[1];
    } else if (currentRouteName.includes('userProfile-info-')) {
        return currentRouteName.split('userProfile-info-')[1];
    }
    return 'overview';
  });

  const menuItems = computed(() => [
    { id: 'overview', label: 'profile.info.overview', visible: true },
    { id: 'work_edu', label: 'profile.info.work_edu', visible: hasWorkOrEducation.value },
    { id: 'places', label: 'profile.info.places', visible: hasPlaces.value },
    { id: 'contact_basic', label: 'profile.info.contact_basic', visible: hasContactBasic.value },
    { id: 'family', label: 'profile.info.family', visible: hasFamily.value },
    { id: 'details', label: 'profile.info.details', visible: hasDetails.value },
    { id: 'events', label: 'profile.info.events', visible: hasEvents.value },
  ].filter(item => item.visible));

  function setActiveTab(tabKey: string) {
    const userId = route.params.userId;
    let routeName;
    if (userId) {
        routeName = `userProfile-info-${tabKey}`;
    } else {
        routeName = `profile-info-${tabKey}`;
    }
    router.push({ name: routeName, params: { userId: userId as string } });
  }

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
  ])
</script>

<template>
    <div class="w-full mt-4 overflow-auto">
        <div v-if="profileUser" class="flex bg-theme-bg-secondary p-4 rounded-lg shadow-lg min-h-[400px]">

            <div class="w-1/3 md:w-1/4 border-r border-gray-200 pr-4">
                <h2 class="text-xl font-semibold mb-4 text-theme-text ml-2">Informacje</h2>
                <ul class="space-y-1 text-theme-text-secondary">
                    <li
                        v-for="item in menuItems"
                        :key="item.id"
                        @click="setActiveTab(item.id)"
                        class="p-2 rounded-lg cursor-pointer transition-colors"
                        :class="activeTab === item.id
                            ? 'bg-blue-50 text-blue-600 '
                            : 'hover:bg-gray-100'"
                    >
                        {{ item.id === 'details' ? t('profile.info.details_about', { name: profileUser.name.split(' ')[0] }) : t(item.label) }}
                    </li>
                </ul>
            </div>

            <div class="w-2/3 md:w-3/4 pl-6 text-theme-text-secondary">
                <router-view :profile-user="profileUser" />
            </div>
        </div>

        <div v-else class="p-8 text-center text-gray-500 bg-theme-bg-secondary rounded-lg shadow-lg">
            Nie znaleziono użytkownika lub brak ID w adresie URL.
        </div>

        <FriendsSection
            :friends-list="friendsList"
            :is-full-view="false"
            class="mt-4 border-none shadow-none p-0 bg-transparent"
        />
    </div>
</template>
