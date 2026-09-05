<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { getApolloClient } from '@/utils/apollo'
import { gql } from 'graphql-tag'
import type { Message } from '@/types/Message'

const props = defineProps<{
  message: Message
}>()

const router = useRouter()

const postData = ref<any>(null)
const loading = ref(false)

const GET_POST_BY_ID = gql`
  query GetPostById($postId: ID!) {
    getPostById(postId: $postId) {
      id
      authorId
      author {
        id
        firstName
        lastName
        avatar
      }
      content
      date
      timestamp
      media {
        src
        altText
      }
    }
  }
`

const extractedPostId = computed<string | null>(() => {
  const msg = props.message as any
  if (msg.sharedPostId) return String(msg.sharedPostId)

  const url = msg.url || msg.linkUrl || msg.content || ''
  const match = url.match(/\/posts?\/([a-zA-Z0-9-]+)/i)
  if (match && match[1]) return match[1]

  const uuidMatch = url.match(/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/)
  if (uuidMatch) return uuidMatch[0]

  return null
})

async function fetchPost(postId: string) {
  loading.value = true
  try {
    const client = getApolloClient()
    const { data } = await client.query({
      query: GET_POST_BY_ID,
      variables: { postId },
      fetchPolicy: 'cache-first',
    })
    if (data?.getPostById) {
      postData.value = data.getPostById
    }
  } catch (e) {
    console.error('Failed to load shared post in chat:', e)
  } finally {
    loading.value = false
  }
}

watch(
  () => extractedPostId.value,
  (newId) => {
    if (newId) {
      fetchPost(newId)
    }
  },
  { immediate: true }
)

const authorName = computed(() => {
  if (postData.value?.author) {
    const { firstName, lastName } = postData.value.author
    return `${firstName || ''} ${lastName || ''}`.trim() || 'Post użytkownika'
  }
  return 'Post'
})

const postImage = computed(() => {
  if (postData.value?.media && postData.value.media.length > 0) {
    return postData.value.media[0].src
  }
  return null
})

const postContent = computed(() => {
  return postData.value?.content || (props.message as any).content || ''
})

const postUrl = computed(() => {
  const msg = props.message as any
  if (extractedPostId.value) return `/posts/${extractedPostId.value}`
  return msg.url || msg.linkUrl || '#'
})

function handleClick(e: MouseEvent) {
  if (extractedPostId.value) {
    e.preventDefault()
    router.push(`/posts/${extractedPostId.value}`)
  }
}
</script>

<template>
  <a
    :href="postUrl"
    @click="handleClick"
    class="flex flex-col overflow-hidden rounded-[24px] shadow-sm min-w-[280px] max-w-full bg-[#f2f4f7] no-underline transition-opacity hover:opacity-95 cursor-pointer"
  >
    <!-- Nagłówek (Autor / Tytuł posta) -->
    <div class="px-5 py-4">
      <span class="font-bold text-gray-900 text-[20px] tracking-tight">
        {{ authorName }}
      </span>
    </div>

    <!-- Sekcja obrazka -->
    <div v-if="postImage" class="w-full bg-gray-800">
      <img
        :src="postImage"
        class="w-full h-auto object-cover max-h-[250px]"
        alt="Post media"
        loading="lazy"
      />
    </div>

    <!-- Dolna sekcja z opisem -->
    <div class="px-5 py-4 pb-5 flex flex-col">
      <h3 v-if="postContent" class="font-semibold text-gray-900 text-[18px] leading-snug mb-4 line-clamp-3">
        {{ postContent }}
      </h3>

      <div class="flex flex-col mt-auto gap-0.5">
        <span class="text-gray-500 text-[15px]">
          Facebook
        </span>
      </div>
    </div>
  </a>
</template>
