import * as fabric from 'fabric'
import type { Ref } from 'vue'

export interface CustomFabricObject extends fabric.FabricObject {
  id?: string
  elementType?: 'image' | 'text' | 'link' | 'post' | 'reel'
  musicTitle?: string
  musicArtist?: string
  musicStyle?: string
  linkUrl?: string
  postId?: string
  mentionedUserId?: string
}

const deleteIconSvg =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='black' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cline x1='18' y1='6' x2='6' y2='18'%3E%3C/line%3E%3Cline x1='6' y1='6' x2='18' y2='18'%3E%3C/line%3E%3C/svg%3E"
const deleteImg = new Image()
deleteImg.src = deleteIconSvg

const rotateIconSvg =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='23 4 23 10 17 10'%3E%3C/polyline%3E%3Cpath d='M20.49 15a9 9 0 1 1-2.12-9.36L23 10'%3E%3C/path%3E%3C/svg%3E"
const rotateImg = new Image()
rotateImg.src = rotateIconSvg

export function useStoryFabricControls(
  audioPlayer?: HTMLAudioElement,
  selectedMusicUrl?: Ref<string | null>,
) {
  const applyCustomControls = (obj: CustomFabricObject) => {
    obj.set({
      transparentCorners: false,
      cornerColor: '#ffffff',
      cornerStrokeColor: '#9ca3af',
      borderColor: '#ffffff',
      cornerSize: 12,
      padding: 6,
      cornerStyle: 'circle',
      borderDashArray: null,
    })

    const customControls = { ...obj.controls }

    delete customControls.ml
    delete customControls.mr
    delete customControls.mt
    delete customControls.mb

    customControls.tl = new fabric.Control({
      x: -0.5,
      y: -0.5,
      offsetX: -16,
      offsetY: -16,
      cursorStyle: 'pointer',
      mouseUpHandler: (_eventData, transform) => {
        const target = transform.target as CustomFabricObject
        const canvas = target.canvas
        if (target.musicTitle && audioPlayer?.src) {
          audioPlayer.pause()
          if (selectedMusicUrl) selectedMusicUrl.value = null
        }
        canvas?.remove(target)
        canvas?.discardActiveObject()
        canvas?.requestRenderAll()
        return true
      },
      render: (ctx, left, top, _styleOverride, fabricObject) => {
        if (
          (fabricObject as CustomFabricObject).elementType === 'image' &&
          !(fabricObject as CustomFabricObject).musicTitle
        )
          return
        const size = 28
        ctx.save()
        ctx.translate(left, top)
        ctx.beginPath()
        ctx.arc(0, 0, size / 2, 0, Math.PI * 2)
        ctx.fillStyle = 'white'
        ctx.shadowColor = 'rgba(0,0,0,0.15)'
        ctx.shadowBlur = 6
        ctx.fill()
        ctx.lineWidth = 1
        ctx.strokeStyle = '#e5e7eb'
        ctx.stroke()
        ctx.shadowColor = 'transparent'
        ctx.drawImage(deleteImg, -8, -8, 16, 16)
        ctx.restore()
      },
      cornerSize: 28,
    })

    customControls.mtr = new fabric.Control({
      x: 0.5,
      y: -0.5,
      offsetX: 28,
      offsetY: -28,
      actionHandler: fabric.controlsUtils.rotationWithSnapping,
      cursorStyle: 'crosshair',
      actionName: 'rotate',
      render: (ctx, left, top, _styleOverride, fabricObject) => {
        if (
          (fabricObject as CustomFabricObject).elementType === 'image' &&
          !(fabricObject as CustomFabricObject).musicTitle
        )
          return
        const size = 24
        ctx.save()
        ctx.translate(left, top)
        ctx.shadowColor = 'rgba(0,0,0,0.4)'
        ctx.shadowBlur = 4
        ctx.drawImage(rotateImg, -size / 2, -size / 2, size, size)
        ctx.restore()
      },
      cornerSize: 32,
      withConnection: false,
    })

    obj.controls = customControls
  }

  return {
    applyCustomControls,
  }
}
