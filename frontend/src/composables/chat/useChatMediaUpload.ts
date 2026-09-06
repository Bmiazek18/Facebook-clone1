const apiUrl = import.meta.env.VITE_BFF_API_URL || ''
const API_UPLOAD_URL = `${apiUrl}/api/chat/upload`

export function useChatMediaUpload() {
  const uploadFile = async (file: File): Promise<string> => {
    const formData = new FormData()
    formData.append('file', file)

    const data = await $fetch<{
      url?: string
      presignedUrl?: string
      stableUrl?: string
      objectKey?: string
    }>(API_UPLOAD_URL, {
      method: 'POST',
      body: formData,
    })

    const resolvedUrl = data?.stableUrl || data?.url || data?.presignedUrl
    if (!resolvedUrl) throw new Error('Błąd wgrywania pliku na serwer')
    return resolvedUrl
  }

  const uploadGifFromUrl = async (gifUrl: string): Promise<string> => {
    const response = await fetch(gifUrl)
    if (!response.ok) {
      throw new Error(`Nie udało się pobrać GIF-a (${response.status})`)
    }
    const blob = await response.blob()
    const contentType = blob.type || 'image/gif'
    const extension = contentType.includes('webp') ? 'webp' : 'gif'
    const file = new File([blob], `gif-${Date.now()}.${extension}`, { type: contentType })
    return uploadFile(file)
  }

  return {
    uploadFile,
    uploadGifFromUrl,
  }
}
