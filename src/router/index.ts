import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ProfileView from '../views/ProfileView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import LoginAsView from '@/views/LoginAsView.vue'
import FriendsView from '@/views/FriendsView.vue'
import ReelView from '@/views/ReelView.vue'
import StoryView from '@/views/StoryView.vue'
import CreateStory from '@/views/CreateStory.vue'
import CreateStory2 from '@/views/CreateStory2.vue'
import CreateStory3 from '@/views/CreateStory3.vue'
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
      component: LoginView,
    },
     {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
     {
      path: '/friends',
      name: 'friends',
      component: FriendsView,
    },
    {
      path: '/reel',
      name: 'reel',
      component: ReelView,
    },
     {
      path: '/createReel',
      name: 'createReel',
      component: CreateStory,
      meta: { showMainLayout: false },
    },
     {
      path: '/createReel2',
      name: 'reel2',
      component: CreateStory2,
      meta: { showMainLayout: false },
    },
       {
      path: '/createReel3',
      name: 'reel3',
      component: CreateStory3,
      meta: { showMainLayout: false },
    },
    {
      path: '/story',
      name: 'story',
      component: StoryView,
    },
 {
      path: '/login/2',
      name: 'loginAs',
      component: LoginAsView,
    },
{
      path: '/photo',
      name: 'photo',
      component: () => import('../views/GalleryView.vue')

    },
  ],
})

export default router
