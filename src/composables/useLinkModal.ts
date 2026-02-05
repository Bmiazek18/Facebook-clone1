import { ref } from 'vue'

const isLinkModalVisible = ref(false)
const linkModalData = ref<string | null>(null)

export function useLinkModal() {
  const showLinkModal = (url: string) => {
    linkModalData.value = url
    isLinkModalVisible.value = true
  }

  const closeLinkModal = () => {
    isLinkModalVisible.value = false
    linkModalData.value = null
  }

  return {
    isLinkModalVisible,
    linkModalData,
    showLinkModal,
    closeLinkModal,
  }
}
