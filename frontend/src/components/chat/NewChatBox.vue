<template>
  <div class="w-[328px] h-[455px] bg-white dark:bg-[#242526] rounded-t-xl shadow-2xl flex flex-col border border-black/5 dark:border-white/10 overflow-hidden font-sans">

    <div class="px-4 pt-4 pb-3 flex flex-col border-b border-gray-200 dark:border-gray-700 shrink-0">

      <div class="flex justify-between items-center mb-4">
        <h2 class="text-[17px] font-semibold text-black dark:text-gray-100 tracking-wide">
          Nowa wiadomość
        </h2>
        <button
          @click="$emit('close')"
          class="text-[#8c34ff] dark:text-[#a666ff] hover:bg-gray-100 dark:hover:bg-gray-700 p-1.5 rounded-full transition-colors outline-none flex items-center justify-center -mr-1"
        >
          <CloseIcon class="w-5 h-5" />
        </button>
      </div>

      <div class="flex items-center gap-2">
        <span class="text-[15px] text-black dark:text-gray-200">Do:</span>
        <input
          type="text"
          v-model="searchQuery"
          class="flex-1 bg-transparent border-none outline-none focus:ring-0 text-[15px] text-black dark:text-white p-0 m-0 caret-black dark:caret-white"
          autofocus
        />
      </div>
    </div>

    <div class="flex-1 overflow-y-auto max-h-[380px] custom-scrollbar py-2">
      <ul>
        <li
          v-for="user in filteredUsers"
          :key="user.id"
          @click="startChat(user)"
          class="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer transition-colors"
        >
          <div class="relative shrink-0 flex items-center justify-center w-10 h-10">
            <template v-if="user.isAi">
              <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-cyan-400 via-blue-500 to-purple-500 p-[2px]">
                <div class="w-full h-full bg-white dark:bg-[#242526] rounded-full border border-transparent flex items-center justify-center">
                  <div class="w-[22px] h-[22px] rounded-full border-[3px] border-cyan-400"></div>
                </div>
              </div>
            </template>

            <template v-else-if="user.avatar">
              <img
                :src="user.avatar"
                :alt="user.name"
                class="w-10 h-10 rounded-full object-cover"
              />
            </template>

            <template v-else>
              <div class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center text-gray-500 dark:text-gray-400">
                <AccountIcon class="w-7 h-7" />
              </div>
            </template>
          </div>

          <div class="flex items-center gap-1.5 min-w-0 pr-2">
            <span class="text-[15px] font-medium text-black dark:text-gray-200 truncate">
              {{ user.name }}
            </span>
            <CheckDecagramIcon
              v-if="user.verified"
              class="w-[18px] h-[18px] text-[#1877F2] shrink-0"
            />
          </div>
        </li>
      </ul>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// Importy Ikon (upewnij się, że używasz odpowiedniej biblioteki np. vue-material-design-icons)
import CloseIcon from 'vue-material-design-icons/Close.vue'
import CheckDecagramIcon from 'vue-material-design-icons/CheckDecagram.vue' // do niebieskiego znaczka weryfikacji
import AccountIcon from 'vue-material-design-icons/Account.vue' // placeholder usera

const emit = defineEmits(['close', 'select-user'])

const searchQuery = ref('')

const users = ref([
  {
    id: 1,
    name: 'Meta AI',
    isAi: true,
    verified: true,
    avatar: null
  },
  {
    id: 2,
    name: 'Wiktoria Szerszeń',
    isAi: false,
    verified: false,
    avatar: 'https://i.pravatar.cc/150?u=wiktoria'
  },
  {
    id: 3,
    name: 'Wojciech Szczęśniak',
    isAi: false,
    verified: false,
    avatar: 'https://i.pravatar.cc/150?u=wojciech'
  },
  {
    id: 4,
    name: 'Krystian Wojda',
    isAi: false,
    verified: false,
    avatar: 'https://i.pravatar.cc/150?u=krystian'
  },
  {
    id: 5,
    name: 'Antoni Ciężki',
    isAi: false,
    verified: false,
    avatar: 'https://i.pravatar.cc/150?u=antoni'
  },
  {
    id: 6,
    name: 'Marzena Miazek-Banach',
    isAi: false,
    verified: false,
    avatar: 'https://i.pravatar.cc/150?u=marzena'
  },
  {
    id: 7,
    name: 'Bartosz Paszkiewicz',
    isAi: false,
    verified: false,
    avatar: null // Wymusi pokazanie szarego placeholdera
  }
])

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const query = searchQuery.value.toLowerCase()
  return users.value.filter(user => user.name.toLowerCase().includes(query))
})

const startChat = (user: any) => {
  emit('select-user', user)
  searchQuery.value = ''
}
</script>

<style scoped>
/* Stylizacja scrollbara z zachowaniem odstępu (pływający pasek) tak jak na zrzucie */
.custom-scrollbar::-webkit-scrollbar {
  width: 14px; /* Całkowita szerokość razem z przezroczystym obramowaniem */
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #8c8c8c;
  border-radius: 10px;
  /* Ramka z kolorem tła daje efekt odstępu (floating scrollbar) */
  border: 4px solid white;
}

/* Dostosowanie ramki scrollbara do trybu ciemnego (jeśli u Ciebie występuje) */
:deep(.dark) .custom-scrollbar::-webkit-scrollbar-thumb {
  border: 4px solid #242526;
  background-color: #666;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: #737373;
}
</style>
