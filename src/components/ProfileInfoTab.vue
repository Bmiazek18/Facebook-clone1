<script setup lang="ts">
  import { ref } from 'vue';
  import FriendsSection from './FriendsSection.vue';
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
</script>

<template>
    <div class="w-full mt-4 overflow-auto">
        <div class="flex bg-theme-bg-secondary p-4 rounded-lg shadow-lg">
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
            </div>
        </div>
     <FriendsSection
    :friends-list="friendsList"
    :is-full-view="false" class="mt-4 border-none shadow-none p-0 bg-transparent"
/>
    </div>
</template>
