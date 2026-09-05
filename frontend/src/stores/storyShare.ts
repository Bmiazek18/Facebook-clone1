import { useStoriesStore } from '@/stores/stories'

/**
 * @deprecated Use useStoriesStore directly for story sharing state
 */
export const useStoryShareStore = () => {
  return useStoriesStore()
}

