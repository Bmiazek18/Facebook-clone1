<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import CustomHoursModal from '@/components/pages/modals/CustomHoursModal.vue'
import BaseModal from '~/components/common/BaseModal.vue'
import PageCreationStep0 from '@/components/pages/creation/PageCreationStep0.vue'
import PageCreationStep1 from '@/components/pages/creation/PageCreationStep1.vue'
import PageCreationStep2 from '@/components/pages/creation/PageCreationStep2.vue'
import PageCreationStep3 from '@/components/pages/creation/PageCreationStep3.vue'
import PageCreationStep4 from '@/components/pages/creation/PageCreationStep4.vue'
import PageCreationLivePreview from '@/components/pages/creation/PageCreationLivePreview.vue'
import { useAuthStore } from '@/stores/auth'
import type { PageForm, ViewMode } from '@/types/pageCreation'

const authStore = useAuthStore()

const viewMode = ref<ViewMode>('desktop')
const step = ref(0)
const isHoursModalOpen = ref(false)
const isSubmitting = ref(false)

const form = reactive<PageForm>({
  pageName: '',
  category: '',
  bio: '',
  website: '',
  phoneCode: 'US+1',
  phone: '',
  email: '',
  address: '',
  city: '',
  zip: '',
  hours: 'none',
  profileImage: null,
  coverImage: null,
  pageNotifications: true,
  promotionalEmails: false,
})

watch(() => form.hours, (newVal) => {
  if (newVal === 'selected') {
    isHoursModalOpen.value = true
  }
})

const closeCreation = () => {
  navigateTo('/pages')
}

// --- ZAKOŃCZENIE KREACJI STRONY ---
const finishPage = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true
  try {
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const ownerId = String(authStore.originalUserId || authStore.currentUserId)
    const payload = {
      ownerId,
      pageName: form.pageName,
      name: form.pageName,
      category: form.category,
      bio: form.bio,
      website: form.website,
      phoneCode: form.phoneCode,
      phone: form.phone,
      email: form.email,
      address: form.address,
      city: form.city,
      zip: form.zip,
      hours: form.hours,
      profileImage: form.profileImage,
      coverImage: form.coverImage,
      pageNotifications: form.pageNotifications,
      promotionalEmails: form.promotionalEmails,
    }

    let savedPage: any = null
    try {
      const res = await fetch(`${apiUrl}/api/pages`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': ownerId,
        },
        body: JSON.stringify(payload),
      })
      if (res.ok) {
        savedPage = await res.json()
      }
    } catch (networkErr) {
      console.warn('Network error while saving page to backend:', networkErr)
    }

    if (!savedPage) {
      const newId = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `page_${Date.now()}`
      savedPage = {
        id: newId,
        ownerId,
        name: form.pageName,
        category: form.category,
        bio: form.bio,
        website: form.website,
        phoneCode: form.phoneCode,
        phone: form.phone,
        email: form.email,
        address: form.address,
        city: form.city,
        zip: form.zip,
        hours: form.hours,
        avatar: form.profileImage || `https://i.pravatar.cc/150?u=${newId}`,
        cover: form.coverImage || `https://picsum.photos/seed/${newId}/1200/400`,
        pageNotifications: form.pageNotifications,
        promotionalEmails: form.promotionalEmails,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      }
    }

    authStore.addPage(savedPage)
    authStore.switchToPage(savedPage)
    await navigateTo(`/profile/${savedPage.id}`)
  } catch (err) {
    console.error('Error during finishPage:', err)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] text-[#050505] overflow-hidden antialiased font-sans">
    <!-- SIDEBAR -->
    <aside class="w-[360px] shrink-0 flex flex-col bg-white shadow-[2px_0_5px_rgba(0,0,0,0.05)] h-full z-20 relative border-r border-[#E5E5E5]">
      <!-- Górny pasek z ikoną X i logo FB -->
      <div class="h-[56px] flex items-center px-4 shrink-0 gap-3 border-b border-transparent">
        <div
          @click="closeCreation"
          class="w-10 h-10 flex items-center justify-center rounded-full bg-[#E4E6EB] hover:bg-[#D8DADF] cursor-pointer text-[#050505] transition-colors"
        >
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"></path>
          </svg>
        </div>
        <div class="w-10 h-10 text-[#1877F2]">
          <svg viewBox="0 0 36 36" width="40" height="40" fill="currentColor">
            <path d="M15 35.8C6.5 34.3 0 26.9 0 18 0 8.1 8.1 0 18 0s18 8.1 18 18c0 8.9-6.5 16.3-15 17.8l-1-.8h-4l-1 .8z" fill="#1877F2"></path>
            <path d="M21.1 18h-3.4v17.8c-1 .2-2 .3-3.1.3s-2.1-.1-3.1-.3V18h-2.5v-3.8h2.5v-2.3c0-2.8 1.4-4.5 4.7-4.5 1.3 0 2.8.2 2.8.2v3h-1.6c-1.5 0-2.1.8-2.1 1.9v1.7h3.6l-.5 3.8z" fill="#FFF"></path>
          </svg>
        </div>
      </div>

      <!-- KROKI KREACJI -->
      <PageCreationStep0
        v-if="step === 0"
        :form="form"
        @next-step="step = 1"
      />

      <PageCreationStep1
        v-else-if="step === 1"
        :form="form"
        @prev-step="step = 0"
        @next-step="step = 2"
      />

      <PageCreationStep2
        v-else-if="step === 2"
        :form="form"
        @prev-step="step = 1"
        @next-step="step = 3"
      />

      <PageCreationStep3
        v-else-if="step === 3"
        :form="form"
        @prev-step="step = 2"
        @next-step="step = 4"
      />

      <PageCreationStep4
        v-else-if="step === 4"
        :form="form"
        :is-submitting="isSubmitting"
        @prev-step="step = 3"
        @finish="finishPage"
      />
    </aside>

    <!-- GŁÓWNY WIDOK PODGLĄDU -->
    <PageCreationLivePreview
      :form="form"
      v-model:view-mode="viewMode"
    />
  </div>

  <!-- Modal -->
  <BaseModal
    v-if="isHoursModalOpen"
    @close="isHoursModalOpen = false"
  >
    <CustomHoursModal
      @close="isHoursModalOpen = false"
      @save="isHoursModalOpen = false"
    />
  </BaseModal>
</template>
