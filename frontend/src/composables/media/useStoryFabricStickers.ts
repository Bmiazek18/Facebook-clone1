import * as fabric from 'fabric'
import type { Ref } from 'vue'
import { getMediaUrl } from '@/utils/stories'
import type { PostData, ReelData, BackgroundSettings } from '@/types/StoryElement'
import type { MusicTrack } from '@/components/create/createStory/MusicModal.vue'
import type { CustomFabricObject } from './useStoryFabricControls'

interface StoryDimensions {
  width: number
  height: number
}

interface StoryOffset {
  x: number
  y: number
}

export function useStoryFabricStickers(
  applyCustomControls: (obj: CustomFabricObject) => void,
) {
  const getHotspotFromObject = (
    obj: CustomFabricObject,
    storyDimensions: StoryDimensions,
    storyOffset: StoryOffset,
  ) => {
    const br = obj.getBoundingRect()
    return {
      x: ((br.left - storyOffset.x) / storyDimensions.width) * 100,
      y: ((br.top - storyOffset.y) / storyDimensions.height) * 100,
      width: (br.width / storyDimensions.width) * 100,
      height: (br.height / storyDimensions.height) * 100,
    }
  }

  const addMusicPoster = async (
    fCanvas: fabric.Canvas,
    track: MusicTrack,
    storyDimensions: StoryDimensions,
    center: { x: number; y: number },
    audioPlayer: HTMLAudioElement,
    selectedMusicUrl: Ref<string | null>,
    isPlaying: Ref<boolean>,
  ) => {
    const id = `el_music_${Date.now()}`

    try {
      const img = await fabric.FabricImage.fromURL(track.coverUrlLarge || '', {
        crossOrigin: 'anonymous',
      })
      const targetWidth = storyDimensions.width * 0.4
      img.scaleToWidth(targetWidth)
      img.set({ originX: 'center', originY: 'center', top: -30 })

      const bg = new fabric.Rect({
        width: targetWidth + 20,
        height: targetWidth + 100,
        fill: 'white',
        rx: 10,
        ry: 10,
        originX: 'center',
        originY: 'center',
        shadow: new fabric.Shadow({ color: 'rgba(0,0,0,0.2)', blur: 10 }),
      })

      const titleText = new fabric.Text(track.title, {
        top: targetWidth / 2 - 10,
        originX: 'center',
        originY: 'center',
        fontSize: storyDimensions.width * 0.035,
        fontWeight: 'bold',
        fill: '#000',
      })

      const artistText = new fabric.Text(track.artist, {
        top: targetWidth / 2 + 15,
        originX: 'center',
        originY: 'center',
        fontSize: storyDimensions.width * 0.025,
        fill: '#666',
      })

      const group = new fabric.Group([bg, img, titleText, artistText], {
        id: id,
        elementType: 'image',
        musicTitle: track.title,
        musicArtist: track.artist,
        musicStyle: 'large',
        left: center.x,
        top: center.y,
        originX: 'center',
        originY: 'center',
      } as CustomFabricObject)

      applyCustomControls(group)

      fCanvas.add(group)
      fCanvas.setActiveObject(group)

      selectedMusicUrl.value = track.previewUrl
      if (track.previewUrl) {
        audioPlayer.src = track.previewUrl
        audioPlayer.play().then(() => (isPlaying.value = true))
      }
    } catch (err) {
      console.error('Błąd ładowania plakatów muzycznych:', err)
    }
  }

  const addLinkSticker = (
    fCanvas: fabric.Canvas,
    data: { url: string; title: string; style: string },
    storyDimensions: StoryDimensions,
    center: { x: number; y: number },
  ) => {
    const id = `el_link_${Date.now()}`

    const textVal = data.title || data.url
    const text = new fabric.Text('🔗 ' + textVal, {
      fontSize: storyDimensions.width * 0.04,
      fill: data.style === 'button' ? 'white' : '#2563eb',
      fontWeight: 'bold',
      originX: 'center',
      originY: 'center',
    })

    const bg = new fabric.Rect({
      width: (text.width || 100) + 40,
      height: storyDimensions.height * 0.06,
      fill: data.style === 'button' ? '#3b82f6' : 'white',
      rx: 25,
      ry: 25,
      originX: 'center',
      originY: 'center',
      shadow: new fabric.Shadow({ color: 'rgba(0,0,0,0.2)', blur: 5 }),
    })

    const group = new fabric.Group([bg, text], {
      id: id,
      elementType: 'link',
      linkUrl: data.url,
      left: center.x,
      top: center.y + 100,
      originX: 'center',
      originY: 'center',
    } as CustomFabricObject)

    applyCustomControls(group)

    fCanvas.add(group)
    fCanvas.setActiveObject(group)
  }

  const addPostSticker = async (
    fCanvas: fabric.Canvas,
    post: PostData,
    storyDimensions: StoryDimensions,
    center: { x: number; y: number },
    background: BackgroundSettings,
    applyFabricBackground: () => void,
  ) => {
    const cardWidth = storyDimensions.width * 0.78
    const objects: fabric.FabricObject[] = []

    const headerHeight = 52
    const content = (post.content || '').slice(0, 120)
    const hasMedia = !!post.media?.[0]?.src

    let mediaImg: fabric.FabricImage | null = null
    let mediaHeight = 0
    if (hasMedia) {
      try {
        mediaImg = await fabric.FabricImage.fromURL(getMediaUrl(post.media[0]!.src), {
          crossOrigin: 'anonymous',
        })
        const maxH = storyDimensions.height * 0.35
        mediaImg.scaleToWidth(cardWidth)
        if ((mediaImg.getScaledHeight() || 0) > maxH) {
          mediaImg.scaleToHeight(maxH)
        }
        mediaHeight = mediaImg.getScaledHeight() || 0
      } catch (err) {
        console.warn('Nie udało się wczytać mediów posta do story:', err)
        mediaImg = null
        mediaHeight = 0
      }
    }

    const contentText = new fabric.Textbox(content || 'Udostępniony post', {
      width: cardWidth - 28,
      fontSize: Math.max(12, storyDimensions.width * 0.028),
      fill: '#1c1e21',
      fontFamily: 'Arial, sans-serif',
      originX: 'left',
      originY: 'top',
    })
    const textHeight = contentText.calcTextHeight() || 20
    const footerHeight = 34
    const cardHeight = headerHeight + 12 + textHeight + 12 + mediaHeight + footerHeight

    const bg = new fabric.Rect({
      width: cardWidth,
      height: cardHeight,
      fill: '#ffffff',
      rx: 14,
      ry: 14,
      originX: 'left',
      originY: 'top',
      shadow: new fabric.Shadow({ color: 'rgba(0,0,0,0.25)', blur: 12, offsetY: 4 }),
    })
    objects.push(bg)

    try {
      const avatar = await fabric.FabricImage.fromURL(
        post.author.avatar || '/default-avatar.png',
        { crossOrigin: 'anonymous' },
      )
      avatar.scaleToWidth(32)
      avatar.set({ left: 14, top: 12, originX: 'left', originY: 'top' })
      const clip = new fabric.Circle({
        radius: 16,
        originX: 'center',
        originY: 'center',
        left: 14 + 16,
        top: 12 + 16,
      })
      avatar.set({ clipPath: clip as any, left: 14, top: 12 })
      objects.push(avatar)
    } catch {
      const avatarFallback = new fabric.Circle({
        radius: 16,
        fill: '#e4e6eb',
        left: 14,
        top: 12,
        originX: 'left',
        originY: 'top',
      })
      objects.push(avatarFallback)
    }

    const authorName = new fabric.Text(post.author.name || 'Użytkownik', {
      left: 56,
      top: 14,
      fontSize: Math.max(12, storyDimensions.width * 0.03),
      fontWeight: 'bold',
      fill: '#050505',
      fontFamily: 'Arial, sans-serif',
    })
    objects.push(authorName)

    const publicLabel = new fabric.Text('Publiczny', {
      left: 56,
      top: 34,
      fontSize: Math.max(10, storyDimensions.width * 0.022),
      fill: '#65676b',
      fontFamily: 'Arial, sans-serif',
    })
    objects.push(publicLabel)

    contentText.set({ left: 14, top: headerHeight })
    objects.push(contentText)

    if (mediaImg) {
      mediaImg.set({
        left: (cardWidth - mediaImg.getScaledWidth()) / 2,
        top: headerHeight + 12 + textHeight + 8,
        originX: 'left',
        originY: 'top',
      })
      objects.push(mediaImg)
    }

    const footerBg = new fabric.Rect({
      width: cardWidth,
      height: footerHeight,
      fill: '#f0f2f5',
      left: 0,
      top: cardHeight - footerHeight,
      originX: 'left',
      originY: 'top',
    })
    objects.push(footerBg)

    const footerText = new fabric.Text('Dotknij, aby wyświetlić post', {
      left: cardWidth / 2,
      top: cardHeight - footerHeight / 2,
      originX: 'center',
      originY: 'center',
      fontSize: Math.max(11, storyDimensions.width * 0.024),
      fill: '#0866ff',
      fontWeight: 'bold',
      fontFamily: 'Arial, sans-serif',
    })
    objects.push(footerText)

    const group = new fabric.Group(objects, {
      id: `el_post_${Date.now()}`,
      elementType: 'post',
      postId: String(post.id),
      left: center.x,
      top: center.y,
      originX: 'center',
      originY: 'center',
    } as CustomFabricObject)

    applyCustomControls(group)
    fCanvas.add(group)
    fCanvas.setActiveObject(group)
    fCanvas.renderAll()

    background.value = ['rgba(24, 25, 26, 0.95)', 'rgba(50, 52, 54, 0.95)']
    applyFabricBackground()
  }

  const addReelSticker = async (
    fCanvas: fabric.Canvas,
    reel: ReelData,
    storyDimensions: StoryDimensions,
    center: { x: number; y: number },
    background: BackgroundSettings,
    applyFabricBackground: () => void,
  ) => {
    const cardWidth = storyDimensions.width * 0.55
    const objects: fabric.FabricObject[] = []
    let mediaHeight = cardWidth * 1.4

    try {
      const poster = await fabric.FabricImage.fromURL(
        getMediaUrl(reel.poster || reel.videoSrc),
        { crossOrigin: 'anonymous' },
      )
      poster.scaleToWidth(cardWidth)
      mediaHeight = poster.getScaledHeight() || mediaHeight
      poster.set({ left: 0, top: 0, originX: 'left', originY: 'top' })
      objects.push(poster)
    } catch {
      objects.push(
        new fabric.Rect({
          width: cardWidth,
          height: mediaHeight,
          fill: '#111',
          originX: 'left',
          originY: 'top',
        }),
      )
    }

    const label = new fabric.Text('Reel · Dotknij, aby otworzyć', {
      left: cardWidth / 2,
      top: mediaHeight + 18,
      originX: 'center',
      originY: 'center',
      fill: '#fff',
      fontSize: Math.max(12, storyDimensions.width * 0.028),
      fontWeight: 'bold',
    })

    const bg = new fabric.Rect({
      width: cardWidth,
      height: mediaHeight + 36,
      fill: 'rgba(0,0,0,0.35)',
      rx: 12,
      ry: 12,
      originX: 'left',
      originY: 'top',
    })

    const group = new fabric.Group([bg, ...objects, label], {
      id: `el_reel_${Date.now()}`,
      elementType: 'reel',
      postId: String(reel.id),
      left: center.x,
      top: center.y,
      originX: 'center',
      originY: 'center',
    } as CustomFabricObject)

    applyCustomControls(group)
    fCanvas.add(group)
    fCanvas.setActiveObject(group)
    background.value = ['rgba(0,0,0,0.95)', 'rgba(30,30,30,0.95)']
    applyFabricBackground()
  }

  return {
    getHotspotFromObject,
    addMusicPoster,
    addLinkSticker,
    addPostSticker,
    addReelSticker,
  }
}
