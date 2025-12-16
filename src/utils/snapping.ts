import type { StoryElement } from '@/types/StoryElement';

export type Guide = { type: 'vertical' | 'horizontal'; pos: number };

export interface SnapResult {
  snappedX: number;
  snappedY: number;
  guides: Guide[];
}

export interface SnapConfig {
  threshold: number;
  canvasWidth: number;
  canvasHeight: number;
}

/**
 * Calculates snap adjustments for dragging an element.
 * Supports center snapping and smart guides (edge/center alignment with other elements).
 */
export function calculateSnaps(
  elementToSnap: StoryElement,
  draggedX: number,
  draggedY: number,
  otherElements: StoryElement[],
  config: SnapConfig
): SnapResult {
  const guides: Guide[] = [];
  let snappedX = draggedX;
  let snappedY = draggedY;

  const elementWidth = elementToSnap.width || 100;
  const elementHeight = elementToSnap.height || 100;
  const elementCenterX = draggedX + elementWidth / 2;
  const elementCenterY = draggedY + elementHeight / 2;

  const centerX = config.canvasWidth / 2;
  const centerY = config.canvasHeight / 2;
  const threshold = config.threshold;

  // ===== CENTER SNAPPING =====
  // Horizontal center snap
  if (Math.abs(elementCenterX - centerX) < threshold) {
    snappedX = centerX - elementWidth / 2;
    // Guide line at the CENTER of the snapped element
    guides.push({ type: 'vertical', pos: snappedX + elementWidth / 2 });
  }
  // Vertical center snap
  if (Math.abs(elementCenterY - centerY) < threshold) {
    snappedY = centerY - elementHeight / 2;
    // Guide line at the CENTER of the snapped element
    guides.push({ type: 'horizontal', pos: snappedY + elementHeight / 2 });
  }

  // ===== SMART GUIDES: Compare with other elements =====
  for (const other of otherElements) {
    if (other.id === elementToSnap.id) continue;

    const otherWidth = other.width || 100;
    const otherHeight = other.height || 100;
    const otherLeft = other.x;
    const otherRight = other.x + otherWidth;
    const otherCenterX = otherLeft + otherWidth / 2;
    const otherTop = other.y;
    const otherBottom = other.y + otherHeight;
    const otherCenterY = otherTop + otherHeight / 2;

    const elementLeft = snappedX;
    const elementRight = snappedX + elementWidth;
    const elementTop = snappedY;
    const elementBottom = snappedY + elementHeight;

    // Snap to left edges
    if (Math.abs(elementLeft - otherLeft) < threshold) {
      snappedX = otherLeft;
      // Guide line at the left edge position
      guides.push({ type: 'vertical', pos: otherLeft });
    }
    // Snap to right edges
    if (Math.abs(elementRight - otherRight) < threshold) {
      snappedX = otherRight - elementWidth;
      // Guide line at the right edge position
      guides.push({ type: 'vertical', pos: otherRight });
    }
    // Snap to horizontal center
    if (Math.abs(elementCenterX - otherCenterX) < threshold) {
      snappedX = otherCenterX - elementWidth / 2;
      // Guide line at the CENTER where elements align
      guides.push({ type: 'vertical', pos: otherCenterX });
    }

    // Snap to top edges
    if (Math.abs(elementTop - otherTop) < threshold) {
      snappedY = otherTop;
      // Guide line at the top edge position
      guides.push({ type: 'horizontal', pos: otherTop });
    }
    // Snap to bottom edges
    if (Math.abs(elementBottom - otherBottom) < threshold) {
      snappedY = otherBottom - elementHeight;
      // Guide line at the bottom edge position
      guides.push({ type: 'horizontal', pos: otherBottom });
    }
    // Snap to vertical center
    if (Math.abs(elementCenterY - otherCenterY) < threshold) {
      snappedY = otherCenterY - elementHeight / 2;
      // Guide line at the CENTER where elements align
      guides.push({ type: 'horizontal', pos: otherCenterY });
    }
  }

  return { snappedX, snappedY, guides };
}
