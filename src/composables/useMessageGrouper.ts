import { computed } from 'vue';
import type { Message } from '@/types/Message';

const MAX_TIME_DIFF_MS = 5 * 60 * 1000;

function isSameDay(t1: number, t2: number) {
    return new Date(t1).toDateString() === new Date(t2).toDateString();
}

export function useMessageGrouper(messagesList: globalThis.Ref<Message[]>) {
    const getDisplayTime = (index: number): string | null => {
        const currentMsg = messagesList.value[index];
        if (!currentMsg) return null;

        const prevMsg = messagesList.value[index - 1];
        const isFirst = index === 0 || !prevMsg;

        // Jeśli to pierwsza wiadomość LUB różnica czasu jest duża LUB to inny dzień
        if (isFirst ||
            (currentMsg.time - prevMsg.time >= MAX_TIME_DIFF_MS) ||
            !isSameDay(currentMsg.time, prevMsg.time)) {

            const dateStr = new Date(currentMsg.time);
            const timeStr = dateStr.toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' });

            if (isFirst || !isSameDay(currentMsg.time, prevMsg?.time ?? 0)) {
                const dayName = dateStr.toLocaleDateString('pl-PL', { weekday: 'short' }).replace('.', '');
                return `${dayName.charAt(0).toUpperCase() + dayName.slice(1)}, ${timeStr}`;
            }
            return timeStr;
        }
        return null;
    };

    const getMessagePositionInGroup = (index: number): 'single' | 'first' | 'middle' | 'last' => {
        const current = messagesList.value[index];
        const prev = messagesList.value[index - 1];
        const next = messagesList.value[index + 1];

        const isSameSenderPrev = prev && prev.sender === current.sender && (current.time - prev.time) < MAX_TIME_DIFF_MS;
        const isSameSenderNext = next && next.sender === current.sender && (next.time - current.time) < MAX_TIME_DIFF_MS;

        if (isSameSenderPrev && isSameSenderNext) return 'middle';
        if (isSameSenderPrev) return 'last';
        if (isSameSenderNext) return 'first';
        return 'single';
    };

    return {
        getDisplayTime,
        getMessagePositionInGroup
    };
}
