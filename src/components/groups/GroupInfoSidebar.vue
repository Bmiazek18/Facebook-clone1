<script setup lang="ts">
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { Group } from '@/types/Group';

// --- ICONS ---
import InformationIcon from 'vue-material-design-icons/Information.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';

const { t } = useI18n();

const props = defineProps<{
  groupDetails?: Group;
}>();

const isDescriptionExpanded = ref(false);

const truncatedDescription = computed(() => {
  const description = props.groupDetails?.description || '';
  const maxLength = 200;
  if (description.length > maxLength && !isDescriptionExpanded.value) {
    return description.substring(0, maxLength) + '...';
  }
  return description;
});
</script>

<template>
  <div class="rounded-lg shadow-sm p-4 bg-theme-bg-secondary border border-theme-border">
    <div class="flex justify-between items-center mb-3">
       <h2 class="text-[17px] font-bold text-theme-text">{{ t('groups.information') }}</h2>
    </div>

    <ul class="space-y-4">
      <li class="flex items-start gap-3">
        <div class="mt-1"><InformationIcon class="text-theme-text-secondary" :size="20" /></div>
        <div class="text-[15px] text-theme-text">
          {{ truncatedDescription }}
          <span
            v-if="(groupDetails?.description || '').length > 200"
            @click="isDescriptionExpanded = !isDescriptionExpanded"
            class="font-semibold cursor-pointer hover:underline ml-1 text-theme-primary"
          >
            {{ isDescriptionExpanded ? t('groups.hide') : t('groups.seeMore') }}
          </span>
        </div>
      </li>

      <li class="flex items-center gap-3">
        <div>
            <EarthIcon v-if="groupDetails?.privacy === 'public'" class="text-theme-text-secondary" :size="20" />
            <AccountGroupIcon v-else class="text-theme-text-secondary" :size="20" />
        </div>
        <div>
            <div class="text-[17px] font-semibold text-theme-text">
                {{ groupDetails?.privacy === 'public' ? t('groups.public') : t('groups.private') }}
            </div>
            <div class="text-[13px] text-theme-text-secondary">
                {{ groupDetails?.privacy === 'public' ? t('groups.publicDescription') : t('groups.privateDescription') }}
            </div>
        </div>
      </li>

      <li class="flex items-center gap-3">
        <div><AccountGroupIcon class="text-theme-text-secondary" :size="20" /></div>
        <div>
            <div class="text-[17px] font-semibold text-theme-text">
                {{ t('groups.visibility') }}
            </div>
            <div class="text-[13px] text-theme-text-secondary">
                {{ t('groups.visibleDescription') }}
            </div>
        </div>
      </li>
    </ul>
  </div>
</template>
