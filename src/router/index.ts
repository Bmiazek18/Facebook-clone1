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
      path: '/post/:id',
      name: 'post',
      component: HomeView,
      props: true
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
      path: '/groups',
      name: 'groups',
      component: () => import('@/views/GroupsView.vue'),
      meta: { showMainLayout: false },
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
      path: '/marketplace/you/dashboard',
      name: 'marketplaceLayout',
      component: () => import('@/views/MarketPlace/MarketplaceLayout.vue'),
      children: [
        {
          path: '',
          name: 'marketplaceDashboard',
          component: () => import('@/views/MarketPlace/DashboardView.vue'),
        },
      ]
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/views/SearchView.vue'),
      props: true,
    },
    {
      path: '/marketplace',
      name: 'mainMarketplaceLayout',
      component: () => import('@/views/MarketPlace/MainMarketplaceLayout.vue'),
      children: [
        {
          path: '',
          name: 'marketplace',
          component: () => import('@/views/MarketPlace/MarketPlaceView.vue'),
          props: true,
        },
        {
          path: 'notifications',
          name: 'marketplaceNotificationsFlat',
          component: () => import('@/views/MarketPlace/MarketplaceNotificationsView.vue'),
        }
      ]
    },
    {
      path: '/marketplace/item/:id',
      name: 'marketplaceItem',
      component: () => import('@/views/MarketPlace/MarketplaceItemView.vue'),
       meta: { showMainLayout: false },
      props: true,
    },
    {
      path: '/marketplace/create/item',
      name: 'createMarketplaceItem',
      component: () => import('@/views/MarketPlace/NewMarketplaceView.vue'),
          meta: { showMainLayout: false },
      props: true,
    },
    {
      path: '/reels',
      name: 'reels',
      component: () => import('@/components/ReelVideoEditor.vue'),
    },
    {
      path: '/reel/:id',
      name: 'reel',
      meta: { showMainLayout: false },
      component: () => import('@/views/ReelView.vue'),
      props: true
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
      meta: { hideMessageIcon: true },
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
       meta: { showMainLayout: false },
      component: () => import('@/views/StoryView.vue'),
    },
    {
      path: '/stories/:userId',
      name: 'userStories',
      meta: { showMainLayout: false },
      component: () => import('@/views/StoryView.vue'),
      props: true
    },
     {
      path: '/video',
      name: 'video',
      component: () => import('@/views/VideoCallView.vue'),
    }, {
      path: '/event',
      name: 'event',
      component: () => import('@/views/EventsPanelView.vue'),
    },
    {
      path: '/event/:id',
      name: 'event',
      component: () => import('@/views/EventView.vue'),
      props: true
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
      props: true,
      meta: { showMainLayout: false },
    },
     {
      path: '/comment/:postId/:commentId',
      name: 'comment',
      component: () => import('../views/GalleryView.vue'),
      props: true,
      meta: { showMainLayout: false },
    },
    {
      path: '/hashtag/:hashtag',
      name: 'hashtag',
      component: () => import('../views/HashtagView.vue'),
      props: true
    },
  ],
})
export default router
