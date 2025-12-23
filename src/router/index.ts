import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ProfileView from '../views/ProfileView.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
    },
    {
      path: '/profile/:userId',
      name: 'userProfile',
      component: ProfileView,
      props: true
    },
     {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
     {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
    },
     {
      path: '/live/produce/create-live',
      name: 'createLive',
      component: () => import('@/views/CreateLiveView.vue'),
    },
     {
      path: '/live/produce/create-event',
      name: 'createEvent',
      component: () => import('@/views/CreateEventView.vue'),
    },
     {
      path: '/friends',
      name: 'friends',
      component: () => import('@/views/FriendsView.vue'),
    },
    {
      path: '/reel',
      name: 'reel',
      component: () => import('@/views/ReelView.vue'),
    },
     {
      path: '/chat',
      name: 'chat',
      component: () => import('@/views/ChatView.vue'),
      meta: { showMainLayout: false },
    },
    {
      path: '/chat/:chatId',
      name: 'chatMessages',
      component: () => import('@/views/ChatView.vue'),
      props: true,
      meta: { showMainLayout: false },
    },
     {
      path: '/stories/create',
      name: 'createReel',
      component: () => import('@/views/CreateStoryView.vue'),
      meta: { showMainLayout: false },
    },
     {
      path: '/live/produce',
      name: 'liveProduce',
      component: () => import('@/views/LiveProducer.vue'),
      meta: { showMainLayout: false },
    },
    {
      path: '/story',
      name: 'story',
      component: () => import('@/views/StoryView.vue'),
    },
     {
      path: '/video',
      name: 'video',
      component: () => import('@/views/VideoCallView.vue'),
    },
 {
      path: '/login/2',
      name: 'loginAs',
      component: () => import('@/views/LoginAsView.vue'),
    },
{
      path: '/photo/:postId/:imageIndex',
      name: 'photo',
      component: () => import('../views/GalleryView.vue'),
      props: true
    },
  ],
})

export default router
