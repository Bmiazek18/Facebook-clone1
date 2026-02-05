import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Heart from 'vue-material-design-icons/Heart.vue'
import { useI18n } from 'vue-i18n'

export const useReactionConfig = () => {
  const { t } = useI18n()

  const getReactionConfig = (type: string) => {
    switch (type) {
      case 'like':
        return {
          mode: 'icon',
          component: ThumbUp,
          wrapperClass: 'bg-[#1877F2]',
          color: '#FFFFFF',
          label: t('reaction.like')
        }
      case 'love':
        return {
          mode: 'icon',
          component: Heart,
          wrapperClass: 'bg-[#F3425F]',
          color: '#FFFFFF',
          label: t('reaction.love')
        }
      case 'haha':
        return { mode: 'emoji', char: '😆', wrapperClass: 'bg-white dark:bg-[#242526]', label: t('reaction.haha') }
      case 'wow':
        return { mode: 'emoji', char: '😮', wrapperClass: 'bg-white dark:bg-[#242526]', label: t('reaction.wow') }
      case 'sad':
        return { mode: 'emoji', char: '😢', wrapperClass: 'bg-white dark:bg-[#242526]', label: t('reaction.sad') }
      case 'angry':
        return { mode: 'emoji', char: '😡', wrapperClass: 'bg-white dark:bg-[#242526]', label: t('reaction.angry') }
      default:
        return {
          mode: 'icon',
          component: ThumbUp,
          wrapperClass: 'bg-[#1877F2]',
          color: '#FFFFFF',
          label: t('reaction.like')
        }
    }
  }

  return { getReactionConfig }
}
