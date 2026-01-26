import { type Ref } from 'vue';
import type { BaseTimelineItem } from '@/types/video-editor.types';


export function useTimelineDrag<T extends BaseTimelineItem>(
  trackRef: Ref<HTMLElement | null>,
  items: Ref<T[]>,
  totalDuration: Ref<number>,
  onSelect?: (item: T) => void
) {
  let draggedItem: T | null = null;
  
  let dragStartX = 0;
  let dragStartTime = 0;
  let dragStartEndTime = 0;

    const startMove = (item: T, event: MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();

    if (!trackRef.value) return;

    draggedItem = item;
    
    dragStartX = event.clientX;
    dragStartTime = item.startTime;
    dragStartEndTime = item.endTime;

    if (onSelect) onSelect(item);

    const handleMove = (e: MouseEvent) => {
      if (!trackRef.value || !draggedItem) return;

      const rect = trackRef.value.getBoundingClientRect();
      const deltaX = e.clientX - dragStartX;
      const deltaTime = (deltaX / rect.width) * totalDuration.value;

      const duration = dragStartEndTime - dragStartTime;
      let newStartTime = dragStartTime + deltaTime;
      let newEndTime = dragStartEndTime + deltaTime;

      // Constrain to timeline bounds
      if (newStartTime < 0) {
        newStartTime = 0;
        newEndTime = duration;
      }
      if (newEndTime > totalDuration.value) {
        newEndTime = totalDuration.value;
        newStartTime = totalDuration.value - duration;
      }

      draggedItem.startTime = Math.max(0, newStartTime);
      draggedItem.endTime = Math.min(totalDuration.value, newEndTime);
    };

    const stopMove = () => {
      document.removeEventListener('mousemove', handleMove);
      document.removeEventListener('mouseup', stopMove);
      draggedItem = null;
      
    };

    document.addEventListener('mousemove', handleMove);
    document.addEventListener('mouseup', stopMove);
  };


  const startResize = (item: T, side: 'left' | 'right', event: MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();

    if (!trackRef.value) return;

    draggedItem = item;
    
    dragStartX = event.clientX;
    dragStartTime = item.startTime;
    dragStartEndTime = item.endTime;

    if (onSelect) onSelect(item);

    const handleResize = (e: MouseEvent) => {
      if (!trackRef.value || !draggedItem) return;

      const rect = trackRef.value.getBoundingClientRect();
      const deltaX = e.clientX - dragStartX;
      const deltaTime = (deltaX / rect.width) * totalDuration.value;

      if (side === 'left') {
        let newStartTime = dragStartTime + deltaTime;
        // Min duration 0.1s
        if (newStartTime >= dragStartEndTime - 0.1) {
          newStartTime = dragStartEndTime - 0.1;
        }
        if (newStartTime < 0) newStartTime = 0;
        draggedItem.startTime = newStartTime;
      } else {
        let newEndTime = dragStartEndTime + deltaTime;
        // Min duration 0.1s
        if (newEndTime <= dragStartTime + 0.1) {
          newEndTime = dragStartTime + 0.1;
        }
        if (newEndTime > totalDuration.value) {
          newEndTime = totalDuration.value;
        }
        draggedItem.endTime = newEndTime;
      }
    };

    const stopResize = () => {
      document.removeEventListener('mousemove', handleResize);
      document.removeEventListener('mouseup', stopResize);
      draggedItem = null;
      
    };

    document.addEventListener('mousemove', handleResize);
    document.addEventListener('mouseup', stopResize);
  };

  return {
    startMove,
    startResize,
  };
}
