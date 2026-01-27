<script setup lang="ts">
import CreateBox from '../components/createPost/CreateBox.vue'
import PostItem from '../components/feed/post/PostItem.vue'
import PeopleYouMayKnow from '../components/friends/PeopleYouMayKnow.vue'
import StoriesList from '../components/stories/StoriesList.vue'
import LeftSidebar from '../components/home/LeftSidebar.vue'
import RightSidebar from '../components/home/RightSidebar.vue'
import { ref, watch } from 'vue'
import { useVirtualList } from '@vueuse/core'
import PostItemSkeleton from '@/components/feed/PostItemSkeleton.vue'
import { usePostsStore } from '@/stores/posts'
import { onBeforeRouteLeave, useRouter, type RouteLocation } from 'vue-router'
import { useCreatePostStore } from '@/stores/createPost'
import ConfirmationModal from '@/components/common/ConfirmationModal.vue'
import ReelsGallery from '@/components/ReelsGallery.vue'
import { getPostById } from '@/data/posts'
import PostModal from '@/components/feed/PostModal.vue'
import BaseModal from '@/components/common/BaseModal.vue'

import { getUserById } from '@/data/users'


const postsStore = usePostsStore()
const localPosts = ref([...postsStore.posts]);

watch(() => postsStore.posts, (newPosts) => {
  localPosts.value = [...newPosts];
}, { deep: true });



const isLoading = ref(true)
setTimeout(() => { isLoading.value = false }, 2000)

const peopleYouMayKnowIndex = Math.floor(Math.random() * 10) + 2
const router = useRouter()
const route = router.currentRoute

const post = getPostById(String(route.value.params.id))


const rowHeight = 500
const { list: virtualPosts, containerProps } = useVirtualList(
  localPosts,
  { itemHeight: rowHeight }
)



const createPostStore = useCreatePostStore()


const showConfirmModal = ref(false)
const pendingRoute = ref<RouteLocation | null>(null)

onBeforeRouteLeave((to, from, next) => {
  if (createPostStore.hasUnsavedChanges) {

    pendingRoute.value = to
    showConfirmModal.value = true
    next(false)
  } else {

    next()
  }
})

const handleConfirmLeave = () => {

  createPostStore.reset()
  showConfirmModal.value = false

  if (pendingRoute.value) {

    router.push(pendingRoute.value)
  }
}

const handleCancelLeave = () => {

  showConfirmModal.value = false
  pendingRoute.value = null
}
</script>

<template>
  <div class="w-full bg-theme-bg text-theme-text min-h-screen relative">

    <div
      class="flex flex-col md:grid md:grid-cols-[2fr_5fr_2fr] w-full 3xl:max-w-[1500px] max-w-full mt-14 mx-auto px-0 lg:px-4">
      <div id="LeftSection" class="hidden lg:block">
        <LeftSidebar  />
      </div>

      <div id="MiddleSection" class="flex justify-center w-full">
        <div id="PostsSection" class="w-full md:max-w-[700px] lg:mx-0 mx-0">
          <CreateBox  :placeholder="$t('home.whatsOnYourMind')"
             />
          <StoriesList />

          <div v-bind="containerProps">
              <template v-for="(post, i) in virtualPosts" :key="post.data.id">
                <PostItem :post="post.data" />
                <PeopleYouMayKnow v-if="i + 1 === peopleYouMayKnowIndex" />
                <!-- <ReelsGallery v-if="i + 2 === peopleYouMayKnowIndex" /> -->
              </template>
          </div>
        </div>
      </div>

      <div class="hidden md:block pl-4">
        <RightSidebar birthday-user="Bartosz Miazek" />
      </div>
    </div>
    <ConfirmationModal v-if="showConfirmModal" @confirm="handleConfirmLeave" @cancel="handleCancelLeave" />
    <BaseModal v-if="post" :title="getUserById(post.authorId)?.name" @close="post = undefined; router.push('/')">
      <PostModal :post="post" />
    </BaseModal>


  </div>
</template>
