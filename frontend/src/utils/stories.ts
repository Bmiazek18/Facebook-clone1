import type { UserStories, StoryItem } from '@/types/Story'
import { parseStoryMetadata } from '@/utils/storyMetadata'

export function getMediaUrl(src: string) {
  if (!src) return ''
  const config = useRuntimeConfig()
  const API_URL = config.public.apiUrl
  if (src.startsWith('http://localhost/')) {
    src = src.replace('http://localhost/', `${API_URL}/`)
  }
  if (/^(https?|blob|data):/.test(src)) return src
  return src.startsWith('/') ? `${API_URL}${src}` : `${API_URL}/${src}`
}

export function getLocalViewedStories(): string[] {
  if (typeof window === 'undefined') return []
  try {
    return JSON.parse(localStorage.getItem('viewed_stories') || '[]')
  } catch (e) {
    return []
  }
}

export function processActiveStories(fetchedStories: any[], currentUserId: string): UserStories[] {
  const groupedStoriesMap = new Map<string, StoryItem[]>()

  fetchedStories.forEach((s: any) => {
    const authorId = String(s.authorId)
    const createdMs = Date.parse(s.createdAt) || Date.now()
    const expiresMs = Date.parse(s.expiresAt) || createdMs + 24 * 60 * 60 * 1000

    const meta = parseStoryMetadata(s.text)

    const storyItem: StoryItem = {
      id: s.id,
      userId: authorId,
      type: (s.mediaType || 'IMAGE').toLowerCase() as 'image' | 'video',
      imageUrl: getMediaUrl(s.mediaUrl),
      thumbnailUrl: s.thumbMediaUrl ? getMediaUrl(s.thumbMediaUrl) : undefined,
      createdAt: createdMs,
      expiresAt: expiresMs,
      viewCount: 0,
      interactions: s.interactions || [],
      elements: [],
      sharedPostInfo: meta?.sharedPostInfo || undefined,
      sharedLinkInfo: meta?.sharedLinkInfo || undefined,
      userTags: meta?.userTags || undefined,
    }

    if (!groupedStoriesMap.has(authorId)) groupedStoriesMap.set(authorId, [])
    groupedStoriesMap.get(authorId)!.push(storyItem)
  })

  const now = Date.now()
  const activeUserStories: UserStories[] = []
  
  groupedStoriesMap.forEach((stories, authorId) => {
    // Filter expired stories
    const validStories = stories.filter(story => story.expiresAt > now)
    if (validStories.length === 0) return

    const firstStory = fetchedStories.find(s => String(s.authorId) === authorId && s.author)
    const authorInfo = firstStory?.author

    const userName = authorInfo
      ? [authorInfo.firstName, authorInfo.lastName].filter(Boolean).join(' ')
      : `User ${authorId}`
    const userAvatar = authorInfo?.avatar || '/default-avatar.png'

    const localViewed = typeof window !== 'undefined' ? getLocalViewedStories() : []
    activeUserStories.push({
      userId: authorId,
      userName,
      userAvatar,
      stories: validStories.sort((a, b) => a.createdAt - b.createdAt),
      hasUnviewedStories: validStories.some(
        (st) => !st.interactions?.some((i: any) => i.userId === currentUserId) && !localViewed.includes(String(st.id)) && st.userId !== currentUserId
      ),
    })
  })

  // Separate birthday stories if any
  const birthdayStories: StoryItem[] = []
  const processedUserStories = JSON.parse(JSON.stringify(activeUserStories)) as UserStories[]

  processedUserStories.forEach((userStory) => {
    const nonBirthdayStories = userStory.stories.filter((story) => {
      if (story.type === 'birthday') {
        birthdayStories.push({
          ...story,
          originalUserName: userStory.userName,
          originalUserAvatar: userStory.userAvatar,
        })
        return false
      }
      return true
    })

    const localViewed = typeof window !== 'undefined' ? getLocalViewedStories() : []
    userStory.hasUnviewedStories = nonBirthdayStories.some(
      (s) => !s.interactions?.some((i: any) => i.userId === currentUserId) && !localViewed.includes(String(s.id)) && s.userId !== currentUserId
    )
    userStory.stories = nonBirthdayStories
  })

  let finalUserStories = processedUserStories.filter((us) => us.stories.length > 0)

  if (birthdayStories.length > 0) {
    const localViewed = typeof window !== 'undefined' ? getLocalViewedStories() : []
    finalUserStories.push({
      userId: 'birthdays',
      userName: 'Urodziny',
      userAvatar: 'https://emojicdn.elk.sh/🎂?style=twitter',
      stories: birthdayStories.sort((a, b) => b.createdAt - a.createdAt),
      hasUnviewedStories: birthdayStories.some(
        (story) => !story.interactions?.some((i: any) => i.userId === currentUserId) && !localViewed.includes(String(story.id)) && story.userId !== currentUserId
      ),
    })
  }

  // Sort
  finalUserStories.sort((a, b) => {
    if (a.userId === currentUserId) return -1
    if (b.userId === currentUserId) return 1
    if (a.userId === 'birthdays') return -1
    if (b.userId === 'birthdays') return 1
    if (a.hasUnviewedStories && !b.hasUnviewedStories) return -1
    if (!a.hasUnviewedStories && b.hasUnviewedStories) return 1

    const aLatest = Math.max(...a.stories.map((s) => s.createdAt))
    const bLatest = Math.max(...b.stories.map((s) => s.createdAt))
    return bLatest - aLatest
  })

  // Filter out stories that have already been viewed for non-current-user in feed view
  const localViewedFinal = typeof window !== 'undefined' ? getLocalViewedStories() : []
  return finalUserStories
    .map((userStory) => {
      if (userStory.userId !== currentUserId) {
        userStory.stories = userStory.stories.filter(
          (story) => !story.interactions?.some((i: any) => i.userId === currentUserId) && !localViewedFinal.includes(String(story.id))
        )
      }
      return userStory
    })
    .filter((userStory) => userStory.stories.length > 0)
}
