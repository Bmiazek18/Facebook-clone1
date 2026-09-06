<script setup lang="ts">
import { defineProps, computed, ref } from 'vue'
import type { Event as EventType } from '@/types/Event'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import EarthIcon from 'vue-material-design-icons/Earth.vue'
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue'

const props = defineProps<{
  eventDetails: EventType | undefined
}>()

const isDescriptionExpanded = ref(false)

const truncatedDescription = computed(() => {
  const description = props.eventDetails?.description || ''
  const maxLength = 200
  if (description.length > maxLength && !isDescriptionExpanded.value) {
    return description.substring(0, maxLength) + '...'
  }
  return description
})
</script>

<template>
  <div class="bg-theme-bg-secondary rounded-lg shadow-sm p-4">
    <h2 class="text-xl font-bold mb-4">{{ $t('createEvent.details') }}</h2>
    <ul class="space-y-4 text-theme-text">
      <li class="flex items-start gap-3">
        <AccountGroupIcon class="text-theme-text-secondary mt-1" />
        <span>{{ $t('events.eventdetailsResponses0Uzytkownikow') }}</span>
      </li>
      <li class="flex items-start gap-3">
        <AccountGroupIcon class="text-theme-text-secondary mt-1" />
        <div>{{ $t('post.eventDateFallback') }}<span class="font-semibold">{{ eventDetails?.hosts?.[0] || 'Organizator' }}</span
          ><span v-if="eventDetails?.hosts?.[1]"
            >, <span class="font-semibold">{{ eventDetails.hosts[1] }}</span></span
          ><span v-if="eventDetails?.hosts?.[2]">{{ $t('birthday.and') }}<span class="font-semibold">{{ eventDetails.hosts[2] }}</span></span
          >
        </div>
      </li>
      <li class="flex items-start gap-3">
        <MapMarkerIcon class="text-theme-text-secondary mt-1" />
        <div>
          {{ eventDetails?.locationName || eventDetails?.location || 'Brak informacji' }}
          <div class="text-sm text-theme-text-secondary">{{ eventDetails?.address || '' }}</div>
        </div>
      </li>
      <li class="flex items-start gap-3">
        <EarthIcon class="text-theme-text-secondary mt-1" />
        <div>
          {{
            eventDetails?.privacy === 'public'
              ? 'Publiczne · Każdy na Facebooku i poza nim'
              : 'Prywatne'
          }}
        </div>
      </li>
    </ul>
    <div class="mt-6 text-sm text-theme-text leading-relaxed">
      {{ truncatedDescription }}
      <span
        v-if="(eventDetails?.description || '').length > 200"
        @click="isDescriptionExpanded = !isDescriptionExpanded"
        class="font-semibold text-theme-primary cursor-pointer hover:underline ml-1"
      >
        {{ isDescriptionExpanded ? '... Ukryj' : '... Czytaj więcej' }}
      </span>
    </div>
    <div class="mt-4">
      <span
        class="inline-block bg-theme-bg-subtle hover:bg-theme-hover rounded-full px-3 py-1 text-sm font-semibold text-theme-text mr-2 mb-2 cursor-pointer"
        >{{ $t('events.gdansk') }}</span
      >
    </div>
  </div>
</template>
