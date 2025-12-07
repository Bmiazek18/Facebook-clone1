<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import Earth from 'vue-material-design-icons/Earth.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import DotsVertical from 'vue-material-design-icons/DotsVertical.vue'
import Close from 'vue-material-design-icons/Close.vue'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import ShareIcon from 'vue-material-design-icons/ShareVariant.vue'
import ReactionButton from './ReactionButton.vue'
import { useTheme } from '@/composables/useTheme'
import ProfilePopper from './ProfilePopper.vue'
import Modal from './Modal.vue'
import PostModal from './PostModal.vue'
useI18n()

const isModalOpen = ref(false)
const toggleModal = () => {
    isModalOpen.value = !isModalOpen.value
}
const { isDark } = useTheme()
</script>

<template>
    <div  class="w-full bg-theme-bg-secondary rounded-lg my-4 shadow-md dark:shadow-lg">




            <div class="flex items-center p-1">
                <button class="mr-2">
                    <img class="rounded-full ml-1 min-w-[42px] max-h-[42px]" src="https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg?stp=cp0_dst-jpg_s40x40_tt6&amp;_nc_cat=104&amp;ccb=1-7&amp;_nc_sid=e99d92&amp;_nc_ohc=-o822DQWa_kQ7kNvwEBBrQN&amp;_nc_oc=Adk7CLzzn6vvAFCclTDzM32DkA0bnhHJCU8V-LZ-6Rgt046578D_zYBPKIpVqrH_jqSITUodiSom9HftYGfou-YR&amp;_nc_zt=24&amp;_nc_ht=scontent-waw2-1.xx&amp;_nc_gid=hWinwIkg4qpusDkFaBv_tg&amp;oh=00_AfhegpWXzJqTqkSqYk4lk-AflwjwvP0sVVYiWvBV-lyexg&amp;oe=6917A7AC" >
                </button>
                <div class="flex items-center justify-between p-2 rounded-full w-full">
                    <div>
                      <ProfilePopper />

                        <div class="flex items-center font-bold text-[13px] text-theme-text-secondary" >
                            11.11.2025 <Earth class="ml-1" :size="15" fillColor="#64676B" v-tooltip="'Publiczne'" />
                        </div>
                    </div>
                    <div class="flex items-center">
                      <button  class="rounded-full p-1.5 cursor-pointer hover:bg-theme-hover">
                            <DotsHorizontal :size="23" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
                        </button>
                        <button  class="rounded-full p-1.5 cursor-pointer hover:bg-theme-hover">
                            <Close :size="26" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
                        </button>
                    </div>
                </div>
            </div>
            <div class="px-5 pb-2 text-[15px] text-theme-text">This is a sample post text.</div>
            <router-link
            to="/photo"

          >
            <img class="mx-auto cursor-pointer"  src="https://picsum.photos/700/400" alt="Post image">
            </router-link>
            <div id="Likes" class="px-5">

                  <div class="flex items-center gap-2 py-0.5 text-theme-text-secondary">
    <ThumbUp fillColor="#1D72E2" :size="16"/> 1
                  </div>



            </div>

            <div class="flex items-center justify-around py-1 font-bold text-theme-text-secondary">

              <div
            class="flex items-center justify-center h-[38px] hover:bg-theme-hover w-full rounded-lg mx-1 cursor-pointer"
          >

           <ReactionButton/>
          </div>
           <button
@click="toggleModal"
            class="flex items-center justify-center h-[38px] hover:bg-theme-hover w-full rounded-lg mx-1 cursor-pointer"
          >
          <MessageOutline :size="18" fillColor="#65686C"  />
            {{ $t('home.comment') }}
          </button>
            <button
            class="flex items-center justify-center h-[38px] hover:bg-theme-hover w-full rounded-lg mx-1 cursor-pointer"
          >
          <ShareIcon :size="18" fillColor="#65686C" />
            {{ $t('actions.share') }}
          </button>
            </div>
        </div>

<Modal
            v-if="isModalOpen"
            @close="toggleModal"

        >
        <PostModal/>
      </Modal>
</template>
