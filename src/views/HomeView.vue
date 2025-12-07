<script setup lang="ts">
import VideoImage from 'vue-material-design-icons/VideoImage.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import CreatePostBox from '../components/CreatePostBox.vue'
import PostItem from '../components/PostItem.vue'
import HoverScrollbar from '../components/HoverScrollbar.vue'
import PeopleYouMayKnow from '../components/PeopleYouMayKnow.vue'
import StoriesList from '../components/StoriesList.vue'
import { ref } from 'vue'
import { useVirtualList } from '@vueuse/core'
import { useI18n } from 'vue-i18n'
import Magnify from 'vue-material-design-icons/Magnify.vue'
import TelevisionPlay from 'vue-material-design-icons/TelevisionPlay.vue'
import StorefrontOutline from 'vue-material-design-icons/StorefrontOutline.vue'
import AccountGroup from 'vue-material-design-icons/AccountGroup.vue'
import AccountMultiple from 'vue-material-design-icons/AccountMultiple.vue'
import Flag from 'vue-material-design-icons/Flag.vue'
import ClockTimeTwoOutline from 'vue-material-design-icons/ClockTimeTwoOutline.vue'
import Restore from 'vue-material-design-icons/Restore.vue'
import PostItemSceleton from '@/components/PostItemSceleton.vue'
import { useTheme } from '@/composables/useTheme'



const { isDark } = useTheme()


const isLoading = ref(true)
setTimeout(()=>{isLoading.value =false},2000)

const posts = ref(
  Array.from({ length: 10 }, (_, i) => ({
    id: i,
    content: `Post #${i + 1}`,
    image: 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg'
  }))
)


const peopleYouMayKnowIndex = Math.floor(Math.random() * 10) + 2

// Virtual list setup
const rowHeight = 10 // Adjust to match PostItem height
const { list: virtualPosts, containerProps } = useVirtualList(
  posts,
  { itemHeight: rowHeight }
)
const menuItems = [
  { icon: AccountMultiple, label: 'home.friends', bgColor: '#1B74E4' },
  { icon: Flag, label: 'home.pages', bgColor: '#EAB308' },
  { icon: ClockTimeTwoOutline, label: 'home.mostRecent', bgColor: '#10B981' },
  { icon: AccountGroup, label: 'home.groups', bgColor: '#9333EA' },
  { icon: StorefrontOutline, label: 'home.marketplace', bgColor: '#F97316' },
  { icon: TelevisionPlay, label: 'home.watch', bgColor: '#EF4444' },
  { icon: Restore, label: 'home.memories', bgColor: '#3B82F6' }
]
</script>

<template>
  <div class="w-full bg-theme-bg text-theme-text min-h-screen">
      <div
        class="grid grid-cols-[2fr_5fr_2fr] w-full 3xl:max-w-[1500px] max-w-full  mt-14 mx-auto px-4 "
      >
        <div id="LeftSection" class="hidden lg:block">
          <div class="max-w-[320px] pr-4 sticky top-[72px]">
            <a
              class="flex items-center justify-start w-full cursor-pointer hover:bg-theme-hover p-2 rounded-md"
            >
              <img
                class="rounded-full ml-1 min-w-[38px] max-h-[38px]"
                src="https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg?stp=cp0_dst-jpg_s40x40_tt6&amp;_nc_cat=104&amp;ccb=1-7&amp;_nc_sid=e99d92&amp;_nc_ohc=-o822DQWa_kQ7kNvwEBBrQN&amp;_nc_oc=Adk7CLzzn6vvAFCclTDzM32DkA0bnwHJCU8V-LZ-6Rgt046578D_zYBPKIpVqrH_jqSITUodiSom9HftYGfou-YR&amp;_nc_zt=24&amp;_nc_ht=scontent-waw2-1.xx&amp;_nc_gid=hWinwIkg4qpusDkFaBv_tg&amp;oh=00_AfhegpWXzJqTqkSqYk4lk-AflwjwvP0sVVYiWvBV-lyexg&amp;oe=6917A7AC"
              />
              <div class="text-[15px] text-theme-text font-medium pl-3">
                Bartosz Miazek
              </div>
            </a>

            <div class="mt-2">
              <button
                v-for="item in menuItems"
                :key="item.label"
                class="flex items-center w-full hover:bg-theme-hover rounded-md p-3 transition"
              >
                <div
                  class="flex items-center justify-center w-9 h-9 rounded-full"
                  :style="{ backgroundColor: item.bgColor }"
                >
                  <component :is="item.icon" :size="22" fillColor="white" />
                </div>
                <span class="text-[15px] text-theme-text font-medium pl-3">
                  {{ $t(item.label) }}
                </span>
              </button>
            </div>
          </div>
        </div>

        <div id="MiddleSection" class="flex justify-center w-full">
            <div
              id="PostsSection"
              class="max-w-[700px] w-full lg:mx-0 mx-auto"
              v-bind="containerProps"
            >

              <CreatePostBox
                :image="'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg'"
                :placeholder="$t('home.whatsOnYourMind')"
              />
          <StoriesList/>

              <div >
                <template v-if="isLoading">
          <PostItemSceleton />
          <PostItemSceleton />
                </template>

                <template v-else>
                  <template v-for="post in virtualPosts" :key="post.data.id">

                    <PostItem
                      :post="post.data"

                    />

                    <PeopleYouMayKnow v-if="post.data.id + 1 === peopleYouMayKnowIndex" />
                  </template>
                </template>


              </div>
            </div>
          </div>

       <div id="RightSection" class="hidden md:block pl-4">

          <div class="max-w-[340px] min-w-[250px] ml-auto sticky top-[72px]">
            <HoverScrollbar>
            <div class=" max-h-[calc(100vh-72px)] pr-2">
              <div class="font-semibold border-b border-b-theme-border text-theme-text">{{ $t('home.birthday') }}</div>

              <div class="flex items-center gap-2 p-2 hover:bg-theme-hover rounded-lg cursor-pointer">
                <span class="text-2xl">🎁</span>
                <p class="text-theme-text text-sm">
                  <span class="font-semibold">Bartosz Miazek</span> {{ $t('home.birthdayHas') }}.
                </p>
              </div>

              <div class="flex items-center justify-between border-b border-b-theme-border">
                <div class="font-semibold text-theme-text">{{ $t('home.contact') }}</div>
                <div class="flex items-center">
                  <div class="p-2 hover:bg-theme-hover rounded-full cursor-pointer">
                    <VideoImage :size="23" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
                  </div>
                  <div class="p-2 hover:bg-theme-hover rounded-full cursor-pointer">
                    <Magnify :size="23" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
                  </div>
                  <div class="p-2 hover:bg-theme-hover rounded-full cursor-pointer">
                    <DotsHorizontal :size="23" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
                  </div>
                </div>
              </div>

              <div v-for="n in 30" :key="n" class="flex items-center justify-start cursor-pointer hover:bg-theme-hover py-2 rounded-md">
                <img class="rounded-full ml-1 min-w-[38px] max-h-[38px]" :src="`https://picsum.photos/id/${140 + (n % 10)}/300/320`" />
                <div class="text-[15px] text-theme-text font-medium pl-3">Contact {{ n }}</div>
              </div>
            </div>
            </HoverScrollbar>
          </div>


        </div>


      </div>
    </div>

</template>
