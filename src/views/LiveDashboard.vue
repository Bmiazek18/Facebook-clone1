<script setup lang="ts">
import { ref,  watch, onActivated } from 'vue';
import { useI18n } from 'vue-i18n';
import { useLiveStore } from '@/stores/live';
import { storeToRefs } from 'pinia';


import CameraOffIcon from 'vue-material-design-icons/CameraOff.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import EmoticonIcon from 'vue-material-design-icons/Emoticon.vue';
import AccountMultipleIcon from 'vue-material-design-icons/AccountMultiple.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import HeartIcon from 'vue-material-design-icons/Heart.vue';
import ArrowExpandAllIcon from 'vue-material-design-icons/ArrowExpandAll.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import ClipboardTextOutlineIcon from 'vue-material-design-icons/ClipboardTextOutline.vue';
import DeleteOutlineIcon from 'vue-material-design-icons/DeleteOutline.vue';
import CheckIcon from 'vue-material-design-icons/Check.vue';
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue';
import InformationOutlineIcon from 'vue-material-design-icons/InformationOutline.vue';
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue';
import EyeIcon from 'vue-material-design-icons/Eye.vue';


const { t } = useI18n();
const liveStore = useLiveStore();
const { activeStream } = storeToRefs(liveStore);

const videoElement = ref<HTMLVideoElement | null>(null);

const setVideo = async (stream: MediaStream | null) => {
    if (videoElement.value) {
        videoElement.value.srcObject = stream;
        if (stream) {
            try {
                await videoElement.value.play();
            } catch(e) { console.error(e); }
        }
    }
}

watch(activeStream, (newStream) => {
    setVideo(newStream);
}, { immediate: true });

onActivated(() => {
    setVideo(activeStream.value);
});






// Dane formularza
const title = ref('');
const description = ref('');
const shareToStory = ref(true);
const gameSearch = ref('');

// Dane ankiet
const pollQuestion = ref('');
const pollOption1 = ref('');
const pollOption2 = ref('');
</script>
<template>
    <div class="flex-1 p-6 overflow-y-auto custom-scrollbar bg-theme-bg">
        <div class="flex gap-5 min-w-[850px] mx-auto max-w-[1200px]">

            <div class="flex-1 flex flex-col gap-5">

                <div class="bg-theme-bg-secondary rounded-lg shadow-sm border border-theme-border">
                    <div class="px-4 py-3 border-b border-theme-border flex justify-between items-center">
                        <h2 class="font-bold text-[17px] text-theme-text">{{ t('createLive.video') }}</h2>
                        <ChevronDownIcon class="text-theme-text-secondary cursor-pointer" :size="24" />
                    </div>
                    <div class="p-4">
                        <div class="relative w-full aspect-video bg-black rounded-lg flex flex-col items-center justify-center text-white mb-4 overflow-hidden">
                            <video v-if="activeStream" ref="videoElement" autoplay muted playsinline class="w-full h-full object-contain"></video>
                            <div v-else>
                                <CameraOffIcon :size="48" class="text-theme-text-secondary mb-3" />
                                <p class="font-bold text-[15px] text-theme-text text-center px-4 leading-snug">{{ t('createLive.cameraNotSupported') }}</p>
                            </div>
                        </div>
                        <div class="flex justify-between items-center border border-theme-border rounded-md px-4 py-2.5 bg-theme-bg-secondary hover:bg-theme-hover cursor-pointer transition-colors mb-4">
                            <span class="text-[15px] font-semibold text-theme-text">{{ t('createLive.expandVideoPreview') }}</span>
                            <ArrowExpandAllIcon :size="20" class="text-theme-text-secondary" />
                        </div>
                        <div class="flex items-center gap-2 text-theme-text font-semibold text-[15px] cursor-pointer hover:bg-theme-hover p-2 rounded-md -ml-2 w-max transition-colors">
                            <div class="bg-theme-bg-tertiary p-1.5 rounded-full">
                                    <ClipboardTextOutlineIcon :size="20"/>
                            </div>
                            {{ t('createLive.eventLogs') }}
                        </div>
                    </div>
                </div>

                <div class="bg-theme-bg-secondary rounded-lg shadow-sm border border-theme-border">
                        <div class="px-4 py-3 border-b border-theme-border flex justify-between items-center">
                        <h2 class="font-bold text-[17px] text-theme-text">{{ t('createLive.liveActivity') }}</h2>
                        <DotsHorizontalIcon class="text-theme-text-secondary cursor-pointer" :size="24" />
                    </div>
                    <div class="p-5">
                        <div class="flex gap-4 mb-6">
                            <div class="flex-1 bg-theme-bg-tertiary rounded-lg p-4 relative">
                                <div class="flex justify-between items-start mb-1">
                                    <span class="text-[13px] font-semibold text-theme-text-secondary">{{ t('createLive.currentViewers') }}</span>
                                    <InformationOutlineIcon :size="16" class="text-theme-text-secondary cursor-help" />
                                </div>
                                <div class="text-2xl font-bold text-theme-text">--</div>
                            </div>
                                <div class="flex-1 bg-theme-bg-tertiary rounded-lg p-4 relative">
                                <div class="flex justify-between items-start mb-1">
                                    <span class="text-[13px] font-semibold text-theme-text-secondary">{{ t('createLive.currentComments') }}</span>
                                    <InformationOutlineIcon :size="16" class="text-theme-text-secondary cursor-help" />
                                </div>
                                <div class="text-2xl font-bold text-theme-text">--</div>
                            </div>
                        </div>

                        <div class="relative h-32 border-b border-theme-border flex items-end mb-6">
                            <div class="w-full h-px bg-theme-bg-subtle absolute top-0"></div>
                            <div class="w-full h-px bg-theme-bg-subtle absolute top-1/3"></div>
                            <div class="w-full h-px bg-theme-bg-subtle absolute top-2/3"></div>

                            <div class="absolute left-0 -bottom-6 text-[11px] text-theme-text-secondary font-medium">0</div>
                            <div class="absolute right-0 -bottom-6 text-[11px] text-theme-text-secondary font-medium">0</div>
                            <div class="w-full text-center -bottom-6 absolute text-[11px] text-theme-text-secondary font-medium">00:00</div>
                        </div>

                        <div class="flex gap-6 mt-8">
                            <div class="flex items-center gap-2">
                                <div class="w-3 h-3 rounded-[2px] bg-theme-primary"></div>
                                <span class="text-[13px] font-medium text-theme-text">{{ t('createLive.viewers') }}</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <div class="w-3 h-3 rounded-[2px] bg-blue-300 dark:bg-blue-800"></div>
                                <span class="text-[13px] font-medium text-theme-text">{{ t('createLive.comments') }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                        <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                        <h2 class="font-bold text-[17px]">{{ t('createLive.manageOnSecondDevice') }}</h2>
                        <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                    </div>
                    <div class="p-5">
                        <p class="text-[13px] text-[#050505] mb-4 leading-snug">{{ t('createLive.manageOnSecondDeviceDescription') }}</p>
                        <div class="flex bg-[#F0F2F5] rounded-md overflow-hidden border border-transparent hover:border-[#CED0D4] transition-colors h-10">
                            <div class="flex-1 px-3 flex items-center text-[13px] text-[#050505] truncate select-all bg-[#F0F2F5]">
                                https://www.facebook.com/live/producer/1584...
                            </div>
                            <button class="bg-[#E4E6EB] hover:bg-[#D8DADF] px-5 font-semibold text-[13px] text-[#050505] transition-colors border-l border-[#CED0D4]">{{ t('createLive.copy') }}</button>
                        </div>
                    </div>
                </div>

            </div>

            <div class="flex-1 flex flex-col gap-5">

                <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                        <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                        <h2 class="font-bold text-[17px]">{{ t('createLive.polls') }}</h2>
                        <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                    </div>
                    <div class="p-5">
                        <div class="text-[15px] font-semibold mb-3">{{ t('createLive.createPoll') }}</div>
                        <label class="block text-[13px] font-semibold text-[#050505] mb-1.5">{{ t('createLive.question') }}</label>
                        <input v-model="pollQuestion" type="text" :placeholder="t('createLive.question')" class="block w-full bg-[#F0F2F5] border-transparent rounded-md px-3 py-3 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] mb-4 hover:bg-[#E4E6EB] transition-colors">

                        <label class="block text-[13px] font-semibold text-[#050505] mb-1.5">{{ t('createLive.options') }}</label>
                        <div class="space-y-2.5 mb-4">
                            <div class="flex items-center gap-2">
                                <div class="relative flex-1">
                                    <input v-model="pollOption1" type="text" :placeholder="t('createLive.option')" class="block w-full bg-[#F0F2F5] border-transparent rounded-md px-3 py-3 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] hover:bg-[#E4E6EB] transition-colors">
                                    <CheckIcon class="absolute right-3 top-3 text-[#BCC0C4]" :size="20"/>
                                </div>
                                <div class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-[#F2F2F2] cursor-pointer transition-colors">
                                    <DeleteOutlineIcon class="text-[#65676B]" :size="24" />
                                </div>
                            </div>
                                <div class="flex items-center gap-2">
                                <div class="relative flex-1">
                                    <input v-model="pollOption2" type="text" :placeholder="t('createLive.option')" class="block w-full bg-[#F0F2F5] border-transparent rounded-md px-3 py-3 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] hover:bg-[#E4E6EB] transition-colors">
                                    <CheckIcon class="absolute right-3 top-3 text-[#BCC0C4]" :size="20"/>
                                </div>
                                <div class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-[#F2F2F2] cursor-pointer transition-colors">
                                    <DeleteOutlineIcon class="text-[#65676B]" :size="24" />
                                </div>
                            </div>
                        </div>

                        <div class="text-[#1877F2] text-[15px] font-semibold cursor-pointer hover:bg-[#F2F2F2] mb-5 inline-flex items-center justify-center border-[2px] border-dashed border-[#CED0D4] rounded-full px-4 py-1.5 w-full text-center transition-colors">
                            {{ t('createLive.addOption') }}
                        </div>

                        <div class="flex gap-3 mb-3">
                            <button class="flex-1 bg-[#E4E6EB] text-[#BCC0C4] py-2.5 rounded-md font-semibold text-[15px] cursor-not-allowed">{{ t('createLive.clear') }}</button>
                            <button class="flex-1 bg-[#E4E6EB] text-[#BCC0C4] py-2.5 rounded-md font-semibold text-[15px] cursor-not-allowed">{{ t('createLive.save') }}</button>
                        </div>
                        <button class="w-full bg-[#E7F3FF] text-[#1877F2] py-2.5 rounded-md font-semibold text-[15px] hover:bg-[#DBE7F2] transition-colors">{{ t('createLive.createPoll') }}</button>
                    </div>
                </div>

                    <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                        <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                        <h2 class="font-bold text-[17px]">{{ t('createLive.postInfo') }}</h2>
                        <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                    </div>
                    <div class="p-5 space-y-4">
                        <input v-model="title" type="text" :placeholder="t('createLive.titleOptional')" class="block w-full border border-theme-border rounded-md px-4 py-3.5 text-[15px] placeholder-theme-text-secondary focus:border-theme-primary focus:ring-1 focus:ring-theme-primary outline-none hover:border-theme-border-hover transition-colors bg-theme-bg-tertiary">

                        <div class="relative">
                            <textarea v-model="description" rows="3" :placeholder="t('createLive.writeSomethingAboutLive')" class="block w-full border border-theme-border rounded-md px-4 py-3.5 text-[15px] placeholder-theme-text-secondary focus:border-theme-primary focus:ring-1 focus:ring-theme-primary outline-none resize-none pb-12 hover:border-theme-border-hover transition-colors bg-theme-bg-tertiary"></textarea>
                            <EmoticonIcon class="absolute top-3 right-3 text-theme-text-secondary cursor-pointer hover:text-theme-text" :size="24"/>
                                <div class="absolute bottom-3 right-3 flex gap-3 items-center">
                                    <AccountMultipleIcon class="text-[#42B72A] cursor-pointer hover:brightness-95" :size="24" />
                                    <MapMarkerIcon class="text-[#F02849] cursor-pointer hover:brightness-95" :size="24" />
                                    <EmoticonIcon class="text-[#F7B928] cursor-pointer hover:brightness-95" :size="24" />
                                    <div class="w-6 h-6 bg-[#E11D48] rounded-full flex items-center justify-center text-white cursor-pointer hover:brightness-95 shadow-sm"><HeartIcon :size="14" /></div>
                                </div>
                        </div>
                        <div>
                            <label class="block text-[13px] font-semibold text-[#050505] mb-1.5">{{ t('createLive.game') }}</label>
                            <div class="relative group">
                                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#65676B] group-focus-within:text-[#1877F2]">
                                    <MagnifyIcon :size="22"/>
                                </div>
                                <input v-model="gameSearch" type="text" :placeholder="t('createLive.tagGame')" class="block w-full bg-[#F0F2F5] border-transparent rounded-md pl-11 pr-3 py-3 text-[15px] placeholder-[#65676B] focus:border-[#1877F2] focus:ring-1 focus:ring-[#1877F2] hover:bg-[#E4E6EB] transition-colors">
                            </div>
                        </div>
                    </div>
                </div>

                <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4] h-72">
                        <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                        <h2 class="font-bold text-[17px]">{{ t('createLive.alerts') }}</h2>
                        <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                    </div>
                    <div class="p-5 flex items-center justify-center h-full">
                        <span class="text-[#65676B] text-[15px]">{{ t('createLive.noNewAlerts') }}</span>
                    </div>
                </div>

                    <div class="bg-white rounded-lg shadow-sm border border-[#CED0D4]">
                        <div class="px-4 py-3 border-b border-[#E4E6EB] flex justify-between items-center">
                        <h2 class="font-bold text-[17px]">{{ t('createLive.previewLink') }}</h2>
                        <DotsHorizontalIcon class="text-[#65676B] cursor-pointer" :size="24" />
                    </div>
                    <div class="p-5">
                        <p class="text-[13px] text-[#65676B] mb-3">{{ t('createLive.previewLinkDescription') }}</p>
                        <div class="flex items-center bg-[#F0F2F5] rounded-md p-2 border border-transparent hover:border-[#CED0D4] transition-colors cursor-pointer group">
                            <div class="bg-[#E4E6EB] p-2 rounded-full mr-3 text-[#65676B] group-hover:bg-[#D8DADF] transition-colors">
                                    <LinkVariantIcon :size="20"/>
                            </div>
                            <div class="flex-1 truncate text-[13px] text-[#65676B] mr-2 leading-snug font-medium">
                                https://www.facebook.com/live/producer/1584...
                            </div>
                            <button class="text-[#1877F2] font-semibold text-[15px] hover:underline px-2">{{ t('createLive.viewPost') }}</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

