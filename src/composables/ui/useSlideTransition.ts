import { ref, type Ref, nextTick } from 'vue'

export const useSlideTransition = (initialView: string = 'main') => {
  const currentView: Ref<string> = ref(initialView)
  const wrapperRef = ref<HTMLElement | null>(null)

  const history = ref<string[]>(
    initialView === 'lifeEvent' ? ['creator', 'lifeEvent'] : [initialView],
  )
  const transitionName = ref('slide-left')

  const onEnter = (el: Element) => {
    const element = el as HTMLElement

    nextTick(() => {
      if (wrapperRef.value) {
        wrapperRef.value.style.height = `${element.offsetHeight}px`
      }
    })
  }

  const onAfterEnter = () => {
    if (wrapperRef.value) {
      wrapperRef.value.style.height = ''
    }
  }

  const navigateTo = (viewName: string) => {
    transitionName.value = 'slide-left'
    history.value.push(viewName)
    currentView.value = viewName
  }

  const navigateBack = () => {
    if (history.value.length > 1) {
      transitionName.value = 'slide-right'
      history.value.pop()
      currentView.value = history.value[history.value.length - 1]
    }
  }

  return {
    wrapperRef,
    currentView,
    transitionName,
    navigateTo,
    navigateBack,
    onEnter, // Eksportujemy hook
    onAfterEnter, // Eksportujemy hook
  }
}
