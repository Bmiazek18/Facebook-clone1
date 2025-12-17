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
      path: '/createReel',
      name: 'createReel',
      component: () => import('@/views/CreateStoryView.vue'),
      meta: { showMainLayout: false },
    },
    {
      path: '/story',
      name: 'story',
      component: () => import('@/views/StoryView.vue'),
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
