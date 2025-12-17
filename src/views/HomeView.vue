<script setup lang="ts">
import CreatePostBox from '../components/CreatePostBox.vue'
import PostItem from '../components/PostItem.vue'
import PeopleYouMayKnow from '../components/PeopleYouMayKnow.vue'
import StoriesList from '../components/StoriesList.vue'
import LeftSidebar from '../components/home/LeftSidebar.vue'
import RightSidebar from '../components/home/RightSidebar.vue'
import { ref } from 'vue'
import { useVirtualList } from '@vueuse/core'
import PostItemSceleton from '@/components/PostItemSceleton.vue'

const isLoading = ref(true)
setTimeout(() => { isLoading.value = false }, 2000)

const posts = ref(
  Array.from({ length: 10 }, (_, i) => ({
    id: i,
    content: `Post #${i + 1}`,
    imageUrl: `https://picsum.photos/700/400?random=${i}`
  }))
)

const peopleYouMayKnowIndex = Math.floor(Math.random() * 10) + 2

// Virtual list setup
const rowHeight = 10
const { list: virtualPosts, containerProps } = useVirtualList(
  posts,
  { itemHeight: rowHeight }
)

const userAvatar = 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg?stp=cp0_dst-jpg_s40x40_tt6&_nc_cat=104&ccb=1-7&_nc_sid=e99d92&_nc_ohc=-o822DQWa_kQ7kNvwEBBrQN&_nc_oc=Adk7CLzzn6vvAFCclTDzM32DkA0bnwHJCU8V-LZ-6Rgt046578D_zYBPKIpVqrH_jqSITUodiSom9HftYGfou-YR&_nc_zt=24&_nc_ht=scontent-waw2-1.xx&_nc_gid=hWinwIkg4qpusDkFaBv_tg&oh=00_AfhegpWXzJqTqkSqYk4lk-AflwjwvP0sVVYiWvBV-lyexg&oe=6917A7AC'
</script>

<template>
  <div class="w-full bg-theme-bg text-theme-text min-h-screen">
      <div
        class="grid grid-cols-[2fr_5fr_2fr] w-full 3xl:max-w-[1500px] max-w-full  mt-14 mx-auto px-4 "
      >
        <div id="LeftSection" class="hidden lg:block">
          <LeftSidebar
            user-name="Bartosz Miazek"
            :user-avatar="userAvatar"
          />
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
          <RightSidebar birthday-user="Bartosz Miazek" />
        </div>


      </div>
    </div>

</template>
