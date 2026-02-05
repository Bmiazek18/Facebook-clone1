import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ProfileView from '../views/ProfileView.vue'
import GroupsLayout from '@/Layouts/GroupsLayout.vue'
import GroupDiscussionView from '@/views/groups/GroupDiscussionView.vue'
import GroupInfoView from '@/views/groups/GroupInfoView.vue'
import GroupMembersView from '@/views/groups/GroupMembersView.vue'
import GroupEventsView from '@/views/groups/GroupEventsView.vue'
import GroupMediaView from '@/views/groups/GroupMediaView.vue'
import GroupFilesView from '@/views/groups/GroupFilesView.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      children:[
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
      ]
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
      redirect: { name: 'profile-posts' },
      children: [
        {
          path: 'posts',
          name: 'profile-posts',
          component: () => import('@/components/profile/ProfilePostsTab.vue')
        },
        {
          path: 'info',
          name: 'profile-info',
          component: () => import('@/components/profile/ProfileInfoTab.vue'),
          redirect: { name: 'profile-info-overview' },
          children: [
            {
              path: 'overview',
              name: 'profile-info-overview',
              component: () => import('@/components/profile/info-tab/OverviewSection.vue')
            },
            {
              path: 'work_edu',
              name: 'profile-info-work_edu',
              component: () => import('@/components/profile/info-tab/WorkEducationSection.vue')
            },
            {
              path: 'places',
              name: 'profile-info-places',
              component: () => import('@/components/profile/info-tab/PlacesSection.vue')
            },
            {
              path: 'contact_basic',
              name: 'profile-info-contact_basic',
              component: () => import('@/components/profile/info-tab/OverviewSection.vue')
            },
            {
              path: 'family',
              name: 'profile-info-family',
              component: () => import('@/components/profile/info-tab/FamilySection.vue')
            },
            {
              path: 'details',
              name: 'profile-info-details',
              component: () => import('@/components/profile/info-tab/DetailsSection.vue')
            },
            {
              path: 'events',
              name: 'profile-info-events',
              component: () => import('@/components/profile/info-tab/EventsSection.vue')
            }
          ]
        },
        {
          path: 'friends',
          name: 'profile-friends',
          component: () => import('@/components/friends/FriendsSection.vue'),
          props: { isFullView: true }
        },
        {
          path: 'photos',
          name: 'profile-photos',
          component: () => import('@/components/profile/PlaceholderTab.vue'),
          props: { tabName: 'Photos' }
        },
        {
          path: 'videos',
          name: 'profile-videos',
          component: () => import('@/components/profile/PlaceholderTab.vue'),
          props: { tabName: 'Videos' }
        },
        {
          path: 'checkins',
          name: 'profile-checkins',
          component: () => import('@/components/profile/PlaceholderTab.vue'),
          props: { tabName: 'Check-ins' }
        },
      ]
    },
    {
      path: '/profile/:userId',
      name: 'userProfile',
      component: ProfileView,
      props: true,
      redirect: to => {
        return { name: 'userProfile-posts', params: { userId: to.params.userId } };
      },
      children: [
        {
          path: 'posts',
          name: 'userProfile-posts',
          component: () => import('@/components/profile/ProfilePostsTab.vue')
        },
        {
          path: 'info',
          name: 'userProfile-info',
          component: () => import('@/components/profile/ProfileInfoTab.vue'),
          redirect: to => {
            return { name: 'userProfile-info-overview', params: { userId: to.params.userId } };
          },
          children: [
            {
              path: 'overview',
              name: 'userProfile-info-overview',
              component: () => import('@/components/profile/info-tab/OverviewSection.vue')
            },
            {
              path: 'work_edu',
              name: 'userProfile-info-work_edu',
              component: () => import('@/components/profile/info-tab/WorkEducationSection.vue')
            },
            {
              path: 'places',
              name: 'userProfile-info-places',
              component: () => import('@/components/profile/info-tab/PlacesSection.vue')
            },
            {
              path: 'contact_basic',
              name: 'userProfile-info-contact_basic',
              component: () => import('@/components/profile/info-tab/OverviewSection.vue')
            },
            {
              path: 'family',
              name: 'userProfile-info-family',
              component: () => import('@/components/profile/info-tab/FamilySection.vue')
            },
            {
              path: 'details',
              name: 'userProfile-info-details',
              component: () => import('@/components/profile/info-tab/DetailsSection.vue')
            },
            {
              path: 'events',
              name: 'userProfile-info-events',
              component: () => import('@/components/profile/info-tab/EventsSection.vue')
            }
          ]
        },
        {
          path: 'friends',
          name: 'userProfile-friends',
          component: () => import('@/components/friends/FriendsSection.vue'),
          props: { isFullView: true }
        },
        {
          path: 'photos',
          name: 'userProfile-photos',
          component: () => import('@/components/profile/PlaceholderTab.vue'),
          props: { tabName: 'Photos' }
        },
        {
          path: 'videos',
          name: 'userProfile-videos',
          component: () => import('@/components/profile/PlaceholderTab.vue'),
          props: { tabName: 'Videos' }
        },
        {
          path: 'checkins',
          name: 'userProfile-checkins',
          component: () => import('@/components/profile/PlaceholderTab.vue'),
          props: { tabName: 'Check-ins' }
        },
      ]
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
      path: '/live/produce',
      name: 'createLiveLayout',
      component: () => import('@/layouts/CreateLiveLayout.vue'),
      children: [
         {
          path: '',
          name: 'picker',
          component: () => import('@/views/LiveProducer.vue'),
        },
        {
          path: 'create-live',
          name: 'createLive',
          component: () => import('@/views/CreateLiveView.vue'),
        },
        {
          path: 'dashboard',
          name: 'liveDashboard',
          component: () => import('@/views/LiveDashboard.vue'),
        }
      ]
    },
    {
      path: '/groups',
      name: 'groups',
      component: () => import('@/views/GroupsView.vue'),

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
      path: '/friends/birthday',
      name: 'friends-birthday',
      component: () => import('@/views/BirthdayView.vue'),
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
      path: '/marketplace/create',
      name: 'MarketplaceItem',
      component: () => import('@/views/MarketPlace/NewMarketplaceView.vue'),
          meta: { showMainLayout: false },

    },
    {
      path: '/marketplace/create/item',
      name: 'createMarketplaceItem',
      component: () => import('@/views/MarketPlace/MarketplaceCreateItemView.vue'),
          meta: { showMainLayout: false },
      props: true,
    },
    {
      path: '/create/reel',
      name: 'reels',
      component: () => import('@/views/CreateReelView.vue'),
    },
    {
      path: '/reel/:id?',
      name: 'reel',
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
      path: '/addAlbum',
      name: 'addAlbum',
      component: () => import('@/views/addAlbum.vue'),
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
      name: 'events',
      component: () => import('@/views/EventsPanelView.vue'),
    },
    {
      path: '/event/:id',
      name: 'event',
      component: () => import('@/views/EventView.vue'),
      props: true,
      redirect: to => {
        return { name: 'event-about', params: { id: to.params.id } };
      },
      children: [
        {
          path: '',
          name: 'event-about',
          component: () => import('@/views/events/EventAboutView.vue'),
          props: true
        },
        {
          path: 'discussion',
          name: 'event-discussion',
          component: () => import('@/views/events/EventDiscussionView.vue'),
          props: true
        }
      ]
    },
    {
      path: '/groups/:id',
      component: GroupsLayout,
      props: true,
      children: [
        {
          path: '',
          name: 'group-discussion',
          component: GroupDiscussionView,
        },
        {
          path: 'info',
          name: 'group-info',
          component: GroupInfoView,
        },
        {
          path: 'members',
          name: 'group-members',
          component: GroupMembersView,
        },
        {
          path: 'events',
          name: 'group-events',
          component: GroupEventsView,
        },
        {
          path: 'media',
          name: 'group-media',
          component: GroupMediaView,
        },
        {
          path: 'files',
          name: 'group-files',
          component: GroupFilesView,
        }
      ]
    },
 {
      path: '/login/2',
      name: 'loginAs',
      component: () => import('@/views/LoginAsView.vue'),
    },



    {
      path: '/hashtag/:hashtag',
      name: 'hashtag',
      component: () => import('../views/HashtagView.vue'),
      props: true
    },
    {
      path: '/add-group',
      name: 'addGroup',
      component: () => import('@/views/AddGroupsView.vue'),
    },
  ],
})
export default router
