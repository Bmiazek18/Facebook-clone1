<script setup lang="ts">
import { ref } from 'vue' // Dodajemy ref

import Earth from 'vue-material-design-icons/Earth.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Close from 'vue-material-design-icons/Close.vue'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import ShareIcon from 'vue-material-design-icons/ShareVariant.vue'
import ReactionButton from './ReactionButton.vue'

// --- Dodany stan ładowania ---
const isLoading = ref(false)
const emit = defineEmits(['openModal'])

// Funkcja do otwierania modala
const openPostModal = () => {
    emit('openModal')
}

// -----------------------------
</script>

<template>
    <div  class="w-full bg-white rounded-lg my-4 shadow-md">

        <div v-if="isLoading" class="animate-pulse p-4">
            <div class="flex items-center mb-4">
                <div class="rounded-full bg-gray-300 h-10 w-10 mr-3"></div>
                <div>
                    <div class="h-4 bg-gray-300 rounded w-40 mb-2"></div>
                    <div class="h-3 bg-gray-200 rounded w-24"></div>
                </div>
            </div>

            <div class="h-4 bg-gray-300 rounded mb-4 w-11/12"></div>

            <div class="bg-gray-200 w-full h-58 mb-4 rounded"></div>

            <div class="flex justify-around border-t border-gray-100 pt-3">
                <div class="h-6 bg-gray-200 rounded w-20"></div>
                <div class="h-6 bg-gray-200 rounded w-20"></div>
                <div class="h-6 bg-gray-200 rounded w-20"></div>
            </div>
        </div>


        <div v-else>
            <div class="flex items-center py-3 px-3">
                <button class="mr-2">
                    <img class="rounded-full ml-1 min-w-[42px] max-h-[42px]" src="https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg?stp=cp0_dst-jpg_s40x40_tt6&amp;_nc_cat=104&amp;ccb=1-7&amp;_nc_sid=e99d92&amp;_nc_ohc=-o822DQWa_kQ7kNvwEBBrQN&amp;_nc_oc=Adk7CLzzn6vvAFCclTDzM32DkA0bnhHJCU8V-LZ-6Rgt046578D_zYBPKIpVqrH_jqSITUodiSom9HftYGfou-YR&amp;_nc_zt=24&amp;_nc_ht=scontent-waw2-1.xx&amp;_nc_gid=hWinwIkg4qpusDkFaBv_tg&amp;oh=00_AfhegpWXzJqTqkSqYk4lk-AflwjwvP0sVVYiWvBV-lyexg&amp;oe=6917A7AC" >
                </button>
                <div class="flex items-center justify-between p-2 rounded-full w-full">
                    <div>
                        <div class="font-extrabold text-[15px]">Bartosz Miazek</div>
                        <div class="flex items-center font-bold text-[13px] text-gray-500">
                            11.11.2025 <Earth class="ml-1" :size="15" fillColor="#64676B"/>
                        </div>
                    </div>
                    <div class="flex items-center">
                      <button  class="rounded-full p-1.5 cursor-pointer hover:bg-[#F2F2F2]">
                            <DotsHorizontal :size="23" fillColor="#050505" />
                        </button>
                        <button  class="rounded-full p-1.5 cursor-pointer hover:bg-[#F2F2F2]">
                            <Close :size="26" fillColor="#555" />
                        </button>
                    </div>
                </div>
            </div>
            <div class="px-5 pb-2 text-[15px] ">This is a sample post text.</div>
            <router-link
            to="/photo"

          >
            <img class="mx-auto cursor-pointer"  src="https://picsum.photos/600/400" alt="Post image">
            </router-link>
            <div id="Likes" class="px-5">

                  <div class="flex items-center gap-2 py-0.5  ">
    <ThumbUp fillColor="#1D72E2" :size="16"/> 1
                  </div>



            </div>

            <div class="flex items-center justify-around py-1 font-bold text-[#65686C]">

              <div
            class="flex items-center justify-center h-[38px]  hover:bg-[#F2F2F2] w-full rounded-lg mx-1 cursor-pointer"
          >

           <ReactionButton/>
          </div>
           <button
@click="openPostModal"
            class="flex items-center justify-center h-[38px]  hover:bg-[#F2F2F2] w-full rounded-lg mx-1 cursor-pointer"
          >
          <MessageOutline :size="18" fillColor="#65686C"  />
            Kommentarz
          </button>
            <button
            class="flex items-center justify-center h-[38px]  hover:bg-[#F2F2F2] w-full rounded-lg mx-1 cursor-pointer"
          >
          <ShareIcon :size="18" fillColor="#65686C" />
            Udostepnij
          </button>
            </div>
        </div>

    </div>
</template>
