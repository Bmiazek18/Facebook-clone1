<script setup lang="ts">
import { computed, type DefineComponent } from 'vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Close from 'vue-material-design-icons/Close.vue'
import ProfilePopper from '@/components/profile/ProfilePopper.vue'
import type { Post } from '@/types/Post'

import LockIcon from 'vue-material-design-icons/Lock.vue'
import EarthIcon from 'vue-material-design-icons/Earth.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import AccountMultipleMinusIcon from 'vue-material-design-icons/AccountMultipleMinus.vue'
import AccountStarIcon from 'vue-material-design-icons/AccountStar.vue'
import Play from 'vue-material-design-icons/Play.vue'
import { Dropdown as VDropdown } from 'floating-vue'
import 'floating-vue/dist/style.css'
import PostSettingPopper from './PostSettingPopper.vue'
import FormattedDate from '@/components/common/FormattedDate.vue'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { useGroupsStore } from '@/stores/groups'
import type { Group } from '@/types/Group'

const groupsStore = useGroupsStore()
const getGroupById = (id: string): Group | undefined => {
  return groupsStore.getGroupById(id)
}

import { useEventsStore } from '@/stores/events'
import type { Event } from '@/types/Event'
import AdminBadge from './AdminBadge.vue'
import { getUserById } from '@/utils/users'

const props = withDefaults(
  defineProps<{
    post?: Post
    isShared?: boolean
    isAnonymous?: boolean
    hideCloseButton?: boolean
    isGroup?: boolean
  }>(),
  {
    post: () => ({} as any),
    isShared: false,
    isAnonymous: false,
    hideCloseButton: false,
    isGroup: false,
  }
)

// Placeholder for anonymous user data
const anonymousUser = {
  name: 'Użytkownik anonimowy',
  avatar: 'https://via.placeholder.com/150/000000/FFFFFF?text=Anon', // Generic placeholder
  id: 0, // A dummy ID for anonymous user
}

const emit = defineEmits<{
  (e: 'menu'): void
  (e: 'close'): void
  (e: 'editPost', postId: number): void
  (e: 'deletePost', postId: number): void
  (e: 'hidePost', postId: number): void
}>()

// Dane statyczne mapy poza computed dla wydajności
const PRIVACY_MAP = {
  only_me: { label: 'Tylko ja', icon: LockIcon },
  public: { label: 'Publiczne', icon: EarthIcon },
  friends: { label: 'Znajomi', icon: AccountGroupIcon },
  friends_except: { label: 'Znajomi z wyjątkiem...', icon: AccountMultipleMinusIcon },
  specific_friends: { label: 'Konkretni znajomi', icon: AccountStarIcon },
} as const

// Dane autora są zwracane w zapytaniu posta — nie pobieramy ich z lokalnej mapy users.ts.
const author = computed(() => props.post?.author)
const authorName = computed(
  () => [author.value?.firstName, author.value?.lastName].filter(Boolean).join(' ') || 'Użytkownik',
)
const authorForAvatar = computed(
  () =>
    author.value && {
      id: author.value.id,
      name: authorName.value,
      avatar: author.value.avatar || undefined,
    },
)

const targetUser = computed(() => {
  if (props.post?.targetId && (props.post?.targetType === 'User' || props.post?.targetType === 'user')) {
    const user = getUserById(props.post.targetId)
    if (user) {
      return {
        id: user.id,
        name: user.name,
        avatar: user.avatar
      }
    }
  }
  return null
})

const targetGroup = computed(() => {
  if (props.isGroup) {
    return null
  }
  if (props.post?.targetId && props.post?.targetType === 'Group') {
    return getGroupById(props.post.targetId)
  }
  return null
})

const targetEvent = computed(() => {
  if (props.post?.targetId && props.post?.targetType === 'Event') {
    const eventsStore = useEventsStore()
    return eventsStore.getEventById(props.post.targetId)
  }
  return null
})

const taggedUsers = computed<{ id: string | number; name: string }[]>(() => {
  if (!props.post?.taggedUsers) return []
  return props.post.taggedUsers.map((u: any) => ({
    id: u.id,
    name: [u.firstName, u.lastName].filter(Boolean).join(' ') || 'Użytkownik'
  }))
})

const privacyInfo = computed(() => {
  const privacy = props.post?.visibility?.toLowerCase() || props.post?.context?.privacy || 'public'
  return PRIVACY_MAP[privacy as keyof typeof PRIVACY_MAP] || PRIVACY_MAP.public
})

const authorGroupRole = ref<string | null>(null)

const effectiveGroupRole = computed(() => {
  if (props.post?.authorGroupRole) {
    return props.post.authorGroupRole.toUpperCase()
  }
  return authorGroupRole.value
})

const computeGroupId = () => {
  if (props.post?.targetType === 'Group' || props.post?.targetType === 'group') {
    return String(props.post.targetId)
  }
  if (props.post?.groupId) {
    return String(props.post.groupId)
  }
  return null
}

const loadAuthorGroupRole = async () => {
  if (props.post?.authorGroupRole) {
    authorGroupRole.value = props.post.authorGroupRole.toUpperCase()
    return
  }
  const gId = computeGroupId()
  const authorId = String(props.post?.authorId || props.post?.author?.id || '')
  if (gId && authorId && !props.post?.isAnonymous && !props.isAnonymous) {
    try {
      const role = await groupsStore.getGroupMembership(gId, authorId)
      authorGroupRole.value = (role || '').toUpperCase()
    } catch {
      authorGroupRole.value = null
    }
  } else {
    authorGroupRole.value = null
  }
}

watch(
  [
    () => props.post?.authorGroupRole,
    () => props.post?.targetId,
    () => props.post?.targetType,
    () => props.post?.authorId,
    () => props.post?.author?.id,
    () => props.group?.id,
  ],
  () => {
    loadAuthorGroupRole()
  },
  { immediate: true },
)
</script>

<template>
  <div
    class="px-3 pt-3 pb-1"
    v-memo="[post?.id, post?.visibility, post?.context?.privacy, authorName, isShared, effectiveGroupRole]"
  >
    <div class="flex items-start">
      <template v-if="targetGroup">
        <!-- Zmieniono w-10 h-10 na w-[34px] h-[34px] -->
        <div class="relative w-[34px] h-[34px] mr-3 shrink-0">
          <NuxtLink :to="`/groups/${targetGroup.id}`">
            <img
              :src="targetGroup.image"
              alt="Group"
              class="w-full h-full object-cover rounded-[8px] border border-black/10 dark:border-white/10"
            />
          </NuxtLink>
          <div
            class="absolute -bottom-[8px] -right-[4px] z-10 rounded-full ring-2 ring-white dark:ring-[#242526]"
          >
            <UserAvatar v-if="post?.isAnonymous || isAnonymous" :user="anonymousUser" :size="20" />
            <UserAvatar v-else-if="authorForAvatar" :user="authorForAvatar" :size="20" />
          </div>
        </div>
      </template>

      <!-- Zmieniono size na 34 -->
      <UserAvatar
        v-else-if="post?.isAnonymous || isAnonymous"
        :user="anonymousUser"
        :size="40"
        class="mr-2.5 shrink-0"
      />
      <UserAvatar
        v-else-if="authorForAvatar"
        :user="authorForAvatar"
        :size="40"
        class="mr-2.5 shrink-0"
      />

      <div class=" ml-2 flex-1 min-w-0 mt-0.5">
        <div v-if="targetGroup">
          <div
            class="text-theme-text text-[15px] font-bold leading-tight hover:underline cursor-pointer"
          >
            <NuxtLink :to="`/groups/${targetGroup.id}`">{{ targetGroup.name }}</NuxtLink>
          </div>
          <div class="text-[13px] flex items-center mt-0.5 text-meta">
            <span class="hover:underline cursor-pointer font-medium">
              <ProfilePopper
                v-if="post?.isAnonymous || isAnonymous"
                :name="anonymousUser.name"
                :user-id="anonymousUser.id"
                mention
              />
              <ProfilePopper v-else :name="authorName" :user-id="post?.authorId || post?.author?.id" mention />
            </span>
            <AdminBadge v-if="effectiveGroupRole === 'ADMIN' " />

            <span
              v-else-if="effectiveGroupRole === 'MODERATOR'"
              class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded text-[11px] font-semibold bg-emerald-100 text-emerald-700 dark:bg-emerald-950/80 dark:text-emerald-400 ml-1.5 leading-none"
              title="Moderator grupy"
            >
              Moderator
            </span>
            <span class="mx-1">·</span>
            <FormattedDate :date="post?.date" class="hover:underline" />
            <span class="mx-1">·</span>
            <component
              :is="privacyInfo.icon"
              :size="14"
              class="fill-meta"
              v-tooltip="privacyInfo.label"
            />
          </div>
        </div>

        <div v-else>
          <div class="flex flex-wrap items-baseline gap-1 text-theme-text text-[15px] leading-snug">
            <span class="font-bold hover:underline cursor-pointer">
              <ProfilePopper
                v-if="post?.isAnonymous || isAnonymous"
                :name="anonymousUser.name"
                :user-id="anonymousUser.id"
              />
              <ProfilePopper v-else :name="authorName" :user-id="post?.authorId || post?.author?.id" comment />
            </span>

            <template v-if="post?.targetType === 'GroupCreated'">
              <span class="text-meta">utworzył grupę</span>
              <span class="font-bold hover:underline cursor-pointer text-theme-text">
                <NuxtLink :to="`/groups/${post.targetId}`">{{ post.content }}</NuxtLink>
              </span>
            </template>

            <span
              v-if="effectiveGroupRole === 'ADMIN'"
              class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded text-[11px] font-semibold bg-blue-100 text-[#1877F2] dark:bg-blue-950/80 dark:text-blue-400 ml-1 leading-none"
              title="Administrator grupy"
            >
              <svg class="w-3 h-3 fill-current" viewBox="0 0 24 24">
                <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/>
              </svg>
              Administrator
            </span>
            <span
              v-else-if="effectiveGroupRole === 'MODERATOR'"
              class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded text-[11px] font-semibold bg-emerald-100 text-emerald-700 dark:bg-emerald-950/80 dark:text-emerald-400 ml-1 leading-none"
              title="Moderator grupy"
            >
              Moderator
            </span>

            <template v-if="targetUser">
              <Play :size="15" class="fill-meta self-center" />
              <span class="font-bold hover:underline cursor-pointer">
                <ProfilePopper :name="targetUser.name" :user-id="targetUser.id" comment />
              </span>
            </template>

            <template v-if="targetEvent">
              <Play :size="15" class="fill-meta self-center" />
              <NuxtLink
                :to="`/event/${targetEvent.id}`"
                class="font-bold hover:underline cursor-pointer"
              >
                {{ targetEvent.name }}
              </NuxtLink>
            </template>

            <template v-if="taggedUsers.length">
              <span class="text-meta">z</span>
              <span class="font-bold hover:underline cursor-pointer">
                <ProfilePopper :name="taggedUsers[0]!.name" :user-id="taggedUsers[0]!.id" />
              </span>
              <span v-if="taggedUsers.length > 1" class="text-meta">
                i {{ taggedUsers.length - 1 }} innymi</span
              >
            </template>

            <template v-if="post?.context?.feeling">
              <span class="text-meta">czuje się</span>
              <span class="font-bold">{{ post.context.feeling.label }}</span>
              <span v-if="post.context.feeling.emoji">{{ post.context.feeling.emoji }}</span>
            </template>

            <template v-if="post?.context?.location">
              <span class="text-meta">— jest w:</span>
              <span
                class="font-bold hover:underline cursor-pointer text-blue-600 dark:text-blue-400"
                >{{ post.context.location.title }}</span
              >
            </template>
          </div>

          <div class="flex items-center text-[13px] text-meta  font-semibold">
            <FormattedDate :date="post?.date" class="hover:underline cursor-pointer" />
            <span class="mx-1">·</span>
            <component
              :is="privacyInfo.icon"
              :size="14"
              class="fill-meta"
              v-tooltip="privacyInfo.label"
            />
          </div>
        </div>
      </div>

      <div v-if="!isShared" class="flex items-center -mr-2 ml-2 gap-1">
        <VDropdown placement="bottom-end" :triggers="['click']">
          <button @click="emit('menu')" class="post-header-btn">
            <DotsHorizontal :size="20" />
          </button>
          <template #popper>
            <PostSettingPopper v-if="post?.id" :post-id="post.id" :author-id="post.authorId || post.author?.id" />
          </template>
        </VDropdown>
        <button v-if="!isShared && !hideCloseButton" @click="emit('close')" class="post-header-btn">
          <Close :size="20" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Kolor tekstu dla daty i separatorów */
.text-meta {
  color: #65676b;
}
.dark .text-meta {
  color: #b0b3b8;
}

/* Kolor dla ikon (zastępuje fillColor w JS) */
.fill-meta {
  fill: #65676b;
}
.dark .fill-meta {
  fill: #b0b3b8;
}

/* Przyciski (Dots/Close) */
.post-header-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
  width: 34px; /* Ustawiono na sztywno 34px */
  height: 34px; /* Ustawiono na sztywno 34px */
  transition: background-color 0.2s;
  color: #65676b;
}
.dark .post-header-btn {
  color: #b0b3b8;
}
.post-header-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
}
.dark .post-header-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* Wymuszenie koloru dla ikon Material Design */
:deep(svg) {
  fill: currentColor !important;
}
</style>
