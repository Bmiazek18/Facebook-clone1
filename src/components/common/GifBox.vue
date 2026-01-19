<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { renderGrid } from '@giphy/js-components';
import { GiphyFetch } from '@giphy/js-fetch-api';

useI18n()

type DebounceFunction = (...args: unknown[]) => void;

const debounce = (func: DebounceFunction, wait: number): DebounceFunction => {
    let timeout: ReturnType<typeof setTimeout> | null = null;
    return (...args: unknown[]) => {
        const later = () => {
            timeout = null;
            func(...args);
        };
        if (timeout !== null) {
            clearTimeout(timeout);
        }
        timeout = setTimeout(later, wait);
    };
};

interface Emits {
    (e: 'handleGifSelection', url: string): void;
}

const emit = defineEmits<Emits>();

const gifs = ref<HTMLElement | null>(null);
const searchTerm = ref<string>('');
const grid = ref<{ remove: () => void } | null>(null);
const previewGifUrl = ref<string | null>(null);

const gf = new GiphyFetch(import.meta.env.VITE_GIPHY_KEY as string);

onMounted(() => {
    if (gifs.value) {
        grid.value = makeGrid(gifs.value);
    }
});

const fetchGifs = (offset: number) => {
    if (searchTerm.value) {
        return gf.search(searchTerm.value, { offset, limit: 25 });
    }
    return gf.trending({ offset, limit: 25 });
};

const makeGrid = (targetEl: HTMLElement): { remove: () => void } => {
    const render = () => {
        return renderGrid(
            {
                width: 226,
                fetchGifs,
                columns: 2,
                gutter: 6,
                noLink: true,
                hideAttribution: true,
                onGifClick,
            },
            targetEl
        );
    };

    const remove = render();

    return {
        remove: () => {
            remove();
        },
    };
};

interface IGif {
    images: {
        fixed_height: {
            url: string;
        };
    };
}

const onGifClick = (gif: IGif, e: MouseEvent) => {
    e.preventDefault();
    const url = gif.images.fixed_height.url;

    previewGifUrl.value = url;

    emit('handleGifSelection', url);

    searchTerm.value = '';
    clearGridAndFetchGifs();
};

const handleGifSearch = debounce(() => {
    previewGifUrl.value = null;
    clearGridAndFetchGifs();
}, 500);

const handleTrendingClick = (): void => {
    searchTerm.value = '';
    previewGifUrl.value = null;
    clearGridAndFetchGifs();
};

const clearGridAndFetchGifs = (): void => {
    if (grid.value) {
        grid.value.remove();
    }

    if (gifs.value) {
        grid.value = makeGrid(gifs.value);
    }
};
</script>

<template>
    <div class="flex flex-col bottom-18 items-center justify-center w-[280px] h-[350px] bg-white shadow-lg rounded-2xl border p-4">

        <div v-if="previewGifUrl" class="w-full mb-3 p-2 bg-indigo-50 border border-indigo-200 rounded-lg flex flex-col items-center">
            <span class="text-xs text-indigo-700 mb-1">{{ $t('post.gifSelected') }}</span>
            <img :src="previewGifUrl" alt="Selected GIF" class="max-h-20 w-auto rounded" />
            <button
                @click="previewGifUrl = null"
                class="mt-1 text-xs text-red-500 hover:text-red-700">
                {{ $t('common.delete') }}
            </button>
        </div>

        <div class="flex items-center justify-between w-full" :class="{ 'mt-2': !previewGifUrl }">
            <input
                type="text"
                v-model="searchTerm"
                @input="handleGifSearch"
                class="w-full text-xl p-2 border border-gray-300 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition duration-150 ease-in-out rounded-xl"
                placeholder="Search gif"
            />
            <span
                @click="handleTrendingClick"
                class="ml-2 text-xl p-2 bg-white border border-gray-300 text-gray-700 flex items-center justify-center rounded-xl hover:bg-gray-100 active:bg-gray-200 cursor-pointer transition duration-150 ease-in-out"
                title="View Trending Gifs"
            >
                🔥
            </span>
        </div>

        <div v-if="!previewGifUrl" class="flex flex-wrap items-center justify-center w-full h-full overflow-y-auto mt-2">
            <div ref="gifs"/>
        </div>

        <div v-else class="flex items-center justify-center w-full h-full mt-2 text-gray-500">
            Wybór gotowy.
        </div>
    </div>
</template>

<style scoped>
.overflow-y-auto::-webkit-scrollbar {
    width: 6px;
}
.overflow-y-auto::-webkit-scrollbar-thumb {
    background-color: #cbd5e1;
    border-radius: 10px;
}
.overflow-y-auto::-webkit-scrollbar-track {
    background-color: #f8fafc;
}
</style>
