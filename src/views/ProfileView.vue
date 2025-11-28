<script setup lang="ts">

import CreatePostBox from '@/components/CreatePostBox.vue';


import Camera from 'vue-material-design-icons/Camera.vue'
import ImageWithGradient from '@/components/ImageWithGradient.vue';

import Check from 'vue-material-design-icons/Check.vue'
import Message from 'vue-material-design-icons/Message.vue'

</script>

<template>

        <div class="w-full min-h-screen pb-20 bg-theme-bg">
            <div class="w-full bg-theme-bg-secondary">
              <ImageWithGradient class="rounded-b-xl"/>
                <div class="max-w-[1250px]  mx-auto pb-1">


                    <div id="ProfileInfo" class="flex md:flex-row flex-col items-center justify-between px-4">
                        <div class="flex md:flex-row flex-col gap-4 md:-mt-6 -mt-16 items-center">
                            <div class="relative">
                                <img
                                    class="rounded-full w-[165px] h-[165px] border-theme-border border-4"
src="https://picsum.photos/id/142/2000/320"
                                >
                                <button

                                    class="absolute right-0 top-[100px] bg-gray-200 hover:bg-gray-300 p-1.5 rounded-full cursor-pointer"
                                >
                                    <Camera :size="25"/>
                                </button>
                            </div>
                            <div class="md:mt-4 text-center md:text-left -mt-3">
                                <div class="text-[28px] text-theme-text font-extrabold pt-1">
                                   Wiktoria Szerszeń
                                </div>
                                <div class="text-[17px] font-bold text-theme-text-secondary mb-1.5 text-center md:text-left">525 znajomi · 79 wspólnych znajomych</div>
                                <div class="flex md:justify-start justify-center md:-ml-1">
                                    <img
                                        class="rounded-full -ml-1 z-[10] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/141/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[9] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/142/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[8] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/143/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[7] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/144/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[6] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/145/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[5] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/146/2000/320"
                                    >
                                    <img
                                        class="rounded-full -ml-3 z-[4] w-[40px] h-[40px] border-white border-2"
                                        src="https://picsum.photos/id/147/2000/320"
                                    >
                                </div>
                            </div>
                        </div>

                        <div class="flex items-center md:my-0 my-4 gap-2">
                             <a
                                class="
                                    flex
                                    justify-center
                                    items-baseline
                                    bg-gray-200
                                    hover:bg-gray-300
                                    rounded-lg
                                    cursor-pointer
                                "
                            >
                                <button class="flex items-center px-4 py-2 font-bold">
                                    <Check class="-ml-2 mr-1" :size="22"/> Znajomi
                                </button>
                            </a>
                             <a
                                class="
                                    flex
                                    justify-center
                                    items-baseline
                                    bg-blue-500
                                    hover:bg-blue-600
                                    rounded-lg
                                    cursor-pointer
                                    text-white
                                "
                            >
                                <button class="flex items-center px-4 py-2 font-bold">
                                    <Message class="-ml-2 mr-1" :size="22"/> {{ $t('profile.message') }}
                                </button>
                            </a>
                        </div>
                    </div>

                    <div class="flex items-center w-full border-t h-[50px] text-theme-text-secondary py-[4px]">
                                <button class="w-[85px]">
                                <div
                                class="
                                    flex
                                    items-center
                                    text-[15px]
                                    justify-center
                                    h-[45px]
                                    text-blue-500
                                    hover:bg-[#F2F2F2]
                                    w-full
                                    font-bold
                                    rounded-lg
                                    cursor-pointer
                                "
                            >
                                {{ $t('profile.tabs.posts') }}
                            </div>
                            <div class="border-b-4 border-b-blue-400 rounded-md"></div>
                        </button>
                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[100px] font-bold rounded-lg mx-1 cursor-pointer">
                            {{ $t('profile.tabs.info') }}
                        </button>
                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[85px] font-bold rounded-lg mx-1 cursor-pointer">
                            {{ $t('profile.tabs.friends') }}
                        </button>
                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[85px] font-bold rounded-lg mx-1 cursor-pointer">
                            {{ $t('profile.tabs.photos') }}
                        </button>
                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[110px] font-bold rounded-lg mx-1 cursor-pointer">
                            {{ $t('profile.tabs.checkins') }}
                        </button>
                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[70px] font-bold rounded-lg mx-1 cursor-pointer">
                            {{ $t('profile.tabs.sports') }}
                        </button>
                        <button class="flex items-center text-[15px] justify-center h-[48px] p-1 hover:bg-[#F2F2F2] w-[90px] font-bold rounded-lg mx-1 cursor-pointer">
                            {{ $t('common.more') }}
                        </button>
                    </div>
                </div>
            </div>

            <div class="flex-cols md:flex w-full max-w-[1250px] justify-between h-[calc(100%-56px)] md:px-0 px-2 mx-auto">

                <div id="LeftSection" class="w-full md:w-5/12 mt-4 mr-4">
                    <div class="bg-theme-bg-secondary p-3 rounded-lg shadow-lg">
                        <div class="font-extrabold pb-2 text-theme-text text-xl">Intro</div>
                        <div class="pb-5">
                            <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                                Add bio
                            </button>
                        </div>
                        <div class="pb-5">
                            <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                                Edit details
                            </button>
                        </div>
                        <div class="pb-5">
                            <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                                Add hobbies
                            </button>
                        </div>
                        <div>
                            <button class="w-full bg-gray-200 hover:bg-gray-300 rounded-lg p-2 font-bold">
                                Add feature
                            </button>
                        </div>
                    </div>

                    <div class="bg-theme-bg-secondary p-3 mt-4 rounded-lg shadow-lg">
                        <div class="font-extrabold pb-2 text-theme-text text-xl">Photos</div>
                        <div class="flex flex-wrap items-center justify-start w-full">


                        </div>
                    </div>
                </div>

                <div id="PostsSection" class="w-full md:w-7/12  overflow-auto">

                    <CreatePostBox
                :image="'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg'"
                :placeholder="'What\'s on your mind Wiktoria?'"
              />


                </div>

            </div>
        </div>

</template>
