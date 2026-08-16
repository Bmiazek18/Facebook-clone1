export interface StoryHotspot {
  x: number
  y: number
  width: number
  height: number
}

export interface StorySharedPostInfo extends StoryHotspot {
  postId: string
}

export interface StorySharedLinkInfo extends StoryHotspot {
  url: string
}

export interface StoryUserTagInfo extends StoryHotspot {
  userId: string
}

export interface StoryMetadata {
  v: 1
  sharedPostInfo?: StorySharedPostInfo | null
  sharedLinkInfo?: StorySharedLinkInfo | null
  userTags?: StoryUserTagInfo[]
}

const META_PREFIX = 'STORY_META:'

export function encodeStoryMetadata(meta: StoryMetadata): string {
  return `${META_PREFIX}${JSON.stringify(meta)}`
}

export function parseStoryMetadata(text: string | null | undefined): StoryMetadata | null {
  if (!text) return null

  try {
    const raw = text.startsWith(META_PREFIX) ? text.slice(META_PREFIX.length) : text
    const parsed = JSON.parse(raw)
    if (!parsed || parsed.v !== 1) return null
    return parsed as StoryMetadata
  } catch {
    return null
  }
}

export function extractMentionUserIds(content: string): string[] {
  if (!content) return []
  const ids: string[] = []
  const re = /\[@([a-zA-Z0-9-]+)\]/g
  let match: RegExpExecArray | null
  while ((match = re.exec(content)) !== null) {
    if (match[1] && !ids.includes(match[1])) ids.push(match[1])
  }
  return ids
}
