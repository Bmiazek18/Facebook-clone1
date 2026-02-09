import { formatDistanceToNow } from 'date-fns';
import { pl } from 'date-fns/locale';

export function formatTimeAgo(timestamp: number | Date, now: number = Date.now()): string {
  const date = typeof timestamp === 'number' ? new Date(timestamp) : timestamp;
  return formatDistanceToNow(date, { addSuffix: true, locale: pl, includeSeconds: true, now });
}
