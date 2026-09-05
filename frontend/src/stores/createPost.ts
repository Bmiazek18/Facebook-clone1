import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/utils/users'
import type { LocationResult } from '@/types/Location'
import type { ImageTagType } from '@/types/Post'
import * as tus from 'tus-js-client'
import { useAuthStore } from '@/stores/auth'
import { usePostsStore } from '@/composables/feed/useAppState'
import { getApolloClient } from '@/utils/apollo'
import { gql } from 'graphql-tag'
import { useI18n } from 'vue-i18n'
import type { PostData } from '@/types/StoryElement'

export interface SelectedImage {
  url: string
  serverPath?: string
  altText: string
  tags?: ImageTagType[]
  progress?: number | null
  type?: 'image' | 'video'
}

export interface Feeling {
  emoji: string
  label: string
}

export interface Activity {
  parent: string | undefined
  item: { label: string; emoji: string }
}

export interface Poll {
  question: string
  options: { text: string }[]
}

const createInitialPostData = () => ({
  content: '',
  privacy: 'friends',
  taggedUsers: [] as User[],
  location: null as LocationResult | null,
  gif: null as string | null,
  images: [] as SelectedImage[],
  cardBgId: 0,
  feeling: null as Feeling | null,
  activity: null as Activity | null,
  targetId: null as string | null,
  targetType: null as 'User' | 'Group' | 'Event' | 'event' | null,
  isAnonymous: false,
  poll: null as Poll | null,
  postVideoUrl: null as string | null,
  sharedPost: null as PostData | null,
  scheduledPublishTime: null as number | null,
})

const createInitialUiState = () => ({
  initialView: null as string | null,
  imageToEdit: null as SelectedImage | null,
  imageIndexToEdit: null as number | null,
  videoToEdit: null as string | null,
  isSubmitting: false, // <-- DODANE: Flaga ładowania dodana do stanu początkowego
})

export const useCreatePostStore = defineStore('createPost', () => {
  const { t } = useI18n()
  const postData = ref(createInitialPostData())
  const uiState = ref(createInitialUiState())

  const closeFriends = ref<User[]>([])
  const tusUploads = new Map<string, tus.Upload>()
  const triggerImageSelector = ref<(() => void) | null>(null)

  const currentView = ref<string>('creator')
  const history = ref<string[]>(['creator'])
  const transitionName = ref('slide-left')

  function setInitialView(viewName: string) {
    uiState.value.initialView = viewName
    currentView.value = viewName
    history.value = (viewName === 'lifeEvent' || viewName === 'poll') ? ['creator', viewName] : [viewName]
  }

  function navigateTo(viewName: string) {
    transitionName.value = 'slide-left'
    history.value.push(viewName)
    currentView.value = viewName
  }

  function navigateBack() {
    if (history.value.length > 1) {
      transitionName.value = 'slide-right'
      history.value.pop()
      currentView.value = history.value[history.value.length - 1] || 'creator'
    }
  }

  function saveEditedMedia(url: string) {
    const editIndex = uiState.value.imageIndexToEdit
    if (currentView.value === 'imageEditor' && editIndex !== null) {
      const targetImg = postData.value.images[editIndex]
      if (targetImg) {
        targetImg.url = url
      }
      setImageToEdit(null, null)
    } else if (currentView.value === 'videoEditor') {
      if (editIndex !== null && editIndex >= 0) {
        const targetImg = postData.value.images[editIndex]
        if (targetImg) {
          targetImg.url = url
        }
      } else {
        postData.value.postVideoUrl = url
      }
      uiState.value.videoToEdit = null
      uiState.value.imageIndexToEdit = null
    }
    navigateBack()
  }

  try {
    const saved = localStorage.getItem('fc_close_friends')
    if (saved) closeFriends.value = JSON.parse(saved)
  } catch (e) {
    console.error('Failed to parse close friends from localStorage:', e)
  }

  if (typeof window !== 'undefined' && window.localStorage) {
    try {
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i)
        if (key && key.startsWith('tus::')) {
          const value = localStorage.getItem(key)
          if (value && value.includes('http://localhost/files/')) {
            localStorage.removeItem(key)
            i--
          }
        }
      }
    } catch (e) {
      console.error('Failed to clean up old tus localStorage entries:', e)
    }
  }

  function setTarget(id: string | null, type: 'User' | 'Group' | 'Event' | null) {
    postData.value.targetId = id
    postData.value.targetType = type
  }

  function setCloseFriends(users: User[]) {
    closeFriends.value = users
    try {
      localStorage.setItem('fc_close_friends', JSON.stringify(users))
    } catch (e) {
      console.error(e)
    }
  }

  function addTaggedUser(user: User) {
    postData.value.taggedUsers.push(user)
  }

  function addSelectedImage(image: SelectedImage) {
    postData.value.images.push(image)
  }

  function removeSelectedImage(index: number) {
    const itemToRemove = postData.value.images[index]
    if (itemToRemove) {
      const upload = tusUploads.get(itemToRemove.url)
      if (upload) {
        upload.abort()
        tusUploads.delete(itemToRemove.url)
      }

      if (itemToRemove.url.startsWith('blob:')) {
        URL.revokeObjectURL(itemToRemove.url)
      }
    }
    postData.value.images.splice(index, 1)
  }

  function updateImageAltText(index: number, altText: string) {
    if (postData.value.images[index]) {
      postData.value.images[index].altText = altText
    }
  }

  function setImageToEdit(image: SelectedImage | null, index: number | null = null) {
    uiState.value.imageToEdit = image
    uiState.value.imageIndexToEdit = index
  }

  function uploadVideoInChunks(file: File) {
    const localBlobUrl = URL.createObjectURL(file)
    const isImg = file.type.startsWith('image/')
    const isVid = file.type.startsWith('video/')
    const type = isImg ? 'image' : (isVid ? 'video' : 'file')

    postData.value.images.push({
      url: localBlobUrl,
      altText: type === 'file' ? `file:${file.name}|size:${file.size}` : '',
      progress: 0,
      type: type,
    })

    const upload = new tus.Upload(file, {
      endpoint: '/files/',
      retryDelays: [0, 3000, 5000, 10000],
      chunkSize: 2 * 1024 * 1024,
      metadata: {
        filename: file.name,
        filetype: file.type,
      },
      onProgress: (bytesUploaded, bytesTotal) => {
        const targetItem = postData.value.images.find((img) => img.url === localBlobUrl)
        if (targetItem) {
          targetItem.progress = Math.round((bytesUploaded / bytesTotal) * 100)
        }
      },
      onError: (error) => {
        console.error(`Błąd Tus dla pliku ${file.name}:`, error)
      },
      onSuccess: () => {
        const targetItem = postData.value.images.find((img) => img.url === localBlobUrl)
        if (targetItem) {
          let serverPath = upload.url || undefined
          if (serverPath && serverPath.includes('/files/')) {
            serverPath = '/files/' + serverPath.split('/files/').pop()
          }
          targetItem.serverPath = serverPath
          targetItem.progress = null
        }
        tusUploads.delete(localBlobUrl)
      },
    })

    tusUploads.set(localBlobUrl, upload)

    upload.findPreviousUploads().then((previousUploads) => {
      if (previousUploads.length > 0 && previousUploads[0]) {
        upload.resumeFromPreviousUpload(previousUploads[0])
        console.log(`🚀 Znaleziono przerwany upload pliku ${file.name}. Wznawianie sesji...`)
      } else {
        console.log(`🆕 Pierwsze przesyłanie pliku ${file.name}.`)
      }
      upload.start()
    })
  }

  function reset() {
    tusUploads.forEach((upload) => upload.abort())
    tusUploads.clear()

    postData.value.images.forEach((img) => {
      if (img.url.startsWith('blob:')) {
        URL.revokeObjectURL(img.url)
      }
    })

    postData.value = createInitialPostData()
    uiState.value = createInitialUiState()
    currentView.value = 'creator'
    history.value = ['creator']
    transitionName.value = 'slide-left'
  }

  const hasUnsavedChanges = computed(() => {
    const isPostDataChanged = JSON.stringify(postData.value) !== JSON.stringify(createInitialPostData())

    const ui = uiState.value
    const isUiChanged = ui.imageToEdit !== null || ui.videoToEdit !== null

    return isPostDataChanged || isUiChanged
  })

  const CREATE_POST_MUTATION = gql`
    mutation CreatePost($input: CreatePostInput!) {
      createPost(input: $input) {
        id
        content
        authorId
        date
        timestamp
        isAnonymous
        media {
          src
          altText
          backgroundColor
          tags {
            id
            x
            y
            userId
            user {
              id
              firstName
              lastName
            }
          }
        }
        taggedUsers {
          id
          firstName
          lastName
        }
        commentCount
        shareCount
        status
        scheduledPublishTime
        reactions {
          reactionType
          userIds
        }
        context {
          feeling {
            emoji
            label
          }
          location {
            title
            subtitle
            type
            lat
            lon
          }
          poll {
            question
            options {
              id
              text
              votes
            }
          }
        }
      }
    }
  `

  async function publishPost() {
    uiState.value.isSubmitting = true // <-- DODANE: Włączenie loadera

    const authStore = useAuthStore()
    const postsStore = usePostsStore()

    const mediaList: any[] = postData.value.images.map((img) => {
      const tags = Array.isArray(img.tags)
        ? img.tags
            .filter((t: any) => t.userId || t.user?.id)
            .map((t: any) => ({
              id: String(t.id),
              x: Number(t.x),
              y: Number(t.y),
              userId: String(t.userId || t.user?.id || ''),
            }))
        : []
      return {
        src: img.serverPath || img.url,
        altText: img.altText || '',
        tags,
      }
    })

    if (postData.value.postVideoUrl) {
      mediaList.push({ src: postData.value.postVideoUrl, altText: '', tags: [] })
    }
    if (postData.value.gif) {
      mediaList.push({ src: postData.value.gif, altText: '', tags: [] })
    }

    const currentUser = authStore.currentUser
    const authorId = String(currentUser?.id || authStore.currentUserId)
    const isSharing = !!postData.value.sharedPost
    const originalPost = postData.value.sharedPost

    const taggedUsersIds = Array.isArray(postData.value.taggedUsers)
      ? postData.value.taggedUsers.map((u: any) => String(u.id))
      : []

    try {
      const client = getApolloClient()
      const result = await client.mutate({
        mutation: CREATE_POST_MUTATION,
        variables: {
          input: {
            content: postData.value.content,
            authorId,
            media: isSharing ? [] : mediaList,
            isAnonymous: !!postData.value.isAnonymous,
            targetId: isSharing ? originalPost!.id : (postData.value.targetId || undefined),
            targetType: isSharing ? 'post' : (postData.value.targetType || undefined),
            visibility: postData.value.privacy || 'PUBLIC',
            allowedUserIds: [],
            taggedUsersIds,
            context: postData.value.feeling || postData.value.location || postData.value.poll ? {
              feeling: postData.value.feeling ? {
                emoji: postData.value.feeling.emoji,
                label: postData.value.feeling.label,
              } : null,
              location: postData.value.location ? {
                title: postData.value.location.title,
                subtitle: postData.value.location.subtitle || '',
                type: postData.value.location.type || '',
                lat: postData.value.location.lat || '',
                lon: postData.value.location.lon || '',
              } : null,
              poll: postData.value.poll ? {
                question: postData.value.poll.question || postData.value.content,
                options: postData.value.poll.options
                  .filter(opt => opt.text.trim().length > 0)
                  .map((opt, idx) => ({
                    id: `opt_${Date.now()}_${idx}`,
                    text: opt.text,
                    votes: []
                  }))
              } : null,
            } : null,
            scheduledPublishTime: postData.value.scheduledPublishTime || undefined,
          }
        },
        update: (cache, { data }) => {
          const createdPost = data?.createPost
          if (!createdPost) return

          const firstName = postData.value.isAnonymous
            ? (t('post.anonymousUser') || 'Anonim')
            : ((currentUser as any)?.firstName || currentUser?.name?.split(' ')[0] || '')

          const lastName = postData.value.isAnonymous
            ? ''
            : ((currentUser as any)?.lastName || currentUser?.name?.split(' ').slice(1).join(' ') || '')

          const authorData = {
            __typename: 'User',
            id: String(currentUser?.id || authStore.currentUserId),
            firstName,
            lastName,
            avatarId: postData.value.isAnonymous
              ? '/img/anonymous-avatar.png'
              : (currentUser?.avatar || (currentUser as any)?.avatarId || '/default-avatar.png'),
          }

          const newPostWithAuthor = {
            ...createdPost,
            author: authorData,
            targetId: postData.value.targetId || null,
            targetType: postData.value.targetType || null,
            visibility: postData.value.privacy || 'PUBLIC',
            allowedUserIds: [],
            reactions: (createdPost.reactions || []).map((r: any) => ({
              ...r,
              users: []
            }))
          }

          cache.modify({
            fields: {
              getFeed(existingFeedRefs = [], { readField }) {
                const newPostRef = cache.writeFragment({
                  data: newPostWithAuthor,
                  fragment: gql`
                    fragment NewFeedPost on Post {
                      id
                      authorId
                      author {
                        id
                        firstName
                        lastName
                        avatarId
                      }
                      content
                      date
                      timestamp
                      isAnonymous
                      targetId
                      targetType
                      commentCount
                      shareCount
                      visibility
                      allowedUserIds
                      reactions {
                        reactionType
                        userIds
                        users {
                          id
                          firstName
                          lastName
                        }
                      }
                    }
                  `
                })

                if (existingFeedRefs.some((ref: any) => readField('id', ref) === createdPost.id)) {
                  return existingFeedRefs
                }

                return [newPostRef, ...existingFeedRefs]
              }
            }
          })
        }
      })

      const created = result?.data?.createPost
      if (created) {
        if (created.status === 'SCHEDULED') {
          console.log('Post został pomyślnie zaplanowany w kolejce Redis na czas:', created.scheduledPublishTime)
          return
        }

        const formattedReactions: Record<string, number[]> = {}
        if (Array.isArray(created.reactions)) {
          created.reactions.forEach((r: any) => {
            formattedReactions[r.reactionType.toLowerCase()] = r.userIds.map(String)
          })
        }

        const firstName = postData.value.isAnonymous
          ? (t('post.anonymousUser') || 'Anonim')
          : ((currentUser as any)?.firstName || currentUser?.name?.split(' ')[0] || '')

        const lastName = postData.value.isAnonymous
          ? ''
          : ((currentUser as any)?.lastName || currentUser?.name?.split(' ').slice(1).join(' ') || '')

        const displayName = postData.value.isAnonymous
          ? t('post.anonymousUser') || 'Anonim'
          : currentUser?.name || `${(currentUser as any)?.firstName || ''} ${(currentUser as any)?.lastName || ''}`.trim()

        const displayAvatar = postData.value.isAnonymous
          ? '/img/anonymous-avatar.png'
          : (currentUser?.avatar || (currentUser as any)?.avatarId || '/default-avatar.png')

        const postWithStats = {
          ...created,
          author: {
            id: authorId,
            name: displayName,
            firstName,
            lastName,
            avatar: displayAvatar,
            avatarId: displayAvatar,
            username: postData.value.isAnonymous
              ? 'anonymous'
              : ((currentUser as any)?.username || displayName)
          },
          targetId: isSharing ? originalPost!.id : (postData.value.targetId || null),
          targetType: isSharing ? 'post' : (postData.value.targetType || null),
          visibility: postData.value.privacy || 'PUBLIC',
          allowedUserIds: [],
          reactions: formattedReactions,
          rawReactions: created.reactions || [],
          reactionUserNames: {},
          stats: {
            reactions: 0,
            comments: created.commentCount ?? 0,
            shares: created.shareCount ?? 0
          },
          ...(isSharing && originalPost ? {
            sharedContent: {
              type: 'post',
              originalId: originalPost.id,
              media: originalPost.media,
            }
          } : {})
        }

        if (isSharing && originalPost) {
          const origInStore = postsStore.getPostById(originalPost.id)
          if (origInStore && origInStore.stats) {
            origInStore.stats.shares = (origInStore.stats.shares || 0) + 1
          }
        }

        postsStore.addPost(postWithStats)
      }
    } catch (err) {
      console.error('Failed to create post in DB:', err)
      throw err
    } finally {
      uiState.value.isSubmitting = false
    }
  }

  function editImage(index: number) {
    const img = postData.value.images[index]
    const url = img?.url || ''
    const existingAlt = img?.altText || ''
    setImageToEdit({ url, altText: existingAlt }, index)
    navigateTo('imageEditor')
  }

  function editVideo(index: number) {
    const url = index === -1 ? postData.value.postVideoUrl || '' : postData.value.images[index]?.url || ''
    uiState.value.videoToEdit = url
    uiState.value.imageIndexToEdit = index
    navigateTo('videoEditor')
  }

  return {
    postData,
    uiState,
    closeFriends,
    hasUnsavedChanges,
    currentView,
    history,
    transitionName,

    setTarget,
    setCloseFriends,
    addTaggedUser,
    addSelectedImage,
    removeSelectedImage,
    updateImageAltText,
    setImageToEdit,
    uploadVideoInChunks,
    reset,
    setInitialView,
    navigateTo,
    navigateBack,
    saveEditedMedia,
    publishPost,
    editImage,
    editVideo,
    triggerImageSelector,
  }
})
