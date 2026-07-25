<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import CustomInput from '@/components/common/CustomInput.vue'
import CustomDropdown from '@/components/common/CustomDropdown.vue'
import AppCloseHeader from '@/layouts/AppCloseHeader.vue'
import { useAuthStore } from '@/stores/auth'

// --- IKONY ---
import Close from 'vue-material-design-icons/Close.vue'
import Monitor from 'vue-material-design-icons/Monitor.vue'
import Cellphone from 'vue-material-design-icons/Cellphone.vue'
import AccountCircle from 'vue-material-design-icons/AccountCircle.vue'
import ImageMultiple from 'vue-material-design-icons/ImageMultiple.vue'
import AccountTag from 'vue-material-design-icons/AccountTag.vue'
import EmoticonOutline from 'vue-material-design-icons/EmoticonOutline.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import Lock from 'vue-material-design-icons/Lock.vue'
import Eye from 'vue-material-design-icons/Eye.vue'
import EyeOff from 'vue-material-design-icons/EyeOff.vue'

// --- DANE I STAN ---
const groupName = ref('')
const privacy = ref('choose')
const visibility = ref('visible')
const inviteInput = ref('')
const invitedFriends = ref([])

// STAN PODGLĄDU
const isPreviewMobile = ref(false)

const privacyOptions = [
  {
    id: 'public',
    title: 'Publiczna',
    description: 'Każdy może sprawdzić listę członków grupy i zobaczyć ich posty.',
    icon: Earth,
  },
  {
    id: 'private',
    title: 'Prywatna',
    description: 'Tylko członkowie grupy mogą sprawdzić listę członków grupy i zobaczyć ich posty.',
    icon: Lock,
  },
]

const visibilityOptions = [
  { id: 'visible', title: 'Widoczna', description: 'Każdy może znaleźć tę grupę.', icon: Eye },
  {
    id: 'hidden',
    title: 'Ukryta',
    description: 'Tylko członkowie mogą znaleźć tę grupę.',
    icon: EyeOff,
  },
]

watch(privacy, (newVal) => {
  if (newVal === 'public') {
    visibility.value = 'visible'
  }
})

// --- COMPUTED ---
const previewName = computed(() => groupName.value.trim() || 'Nazwa grupy')

// POPRAWKA TUTAJ: Jeśli jest tekst (trim()) to kolor główny, jeśli nie (czyli placeholder) to secondary
const titleColorClass = computed(() =>
  groupName.value.trim() ? 'text-theme-text' : 'text-theme-text-secondary',
)

const subtitleText = computed(() => {
  if (privacy.value === 'public') return 'Grupa publiczna'
  if (privacy.value === 'private') return 'Grupa prywatna'
  return 'Prywatność grupy'
})
const isFormValid = computed(() => groupName.value.trim().length > 0 && privacy.value !== 'choose')

const auth = useAuthStore()
import { useGroupsStore } from '@/stores/groups'

const groupsStore = useGroupsStore()
const router = useRouter()

const userAvatar = auth.currentUser?.avatar || 'https://via.placeholder.com/40'
const coverIllustration =
  'https://img.freepik.com/free-vector/people-gardening-concept-illustration_114360-647.jpg?w=1380'

const addFriend = (name: string) => {
  if (!invitedFriends.value.includes(name)) invitedFriends.value.push(name)
}
const removeFriend = (name: string) => {
  invitedFriends.value = invitedFriends.value.filter((f) => f !== name)
}

const handleCreateGroup = () => {
  if (!isFormValid.value) return

  const newGroup = groupsStore.addGroup({
    name: groupName.value,
    privacy: privacy.value as 'public' | 'private',
    description:
      privacy.value === 'public'
        ? 'Każdy może sprawdzić listę członków grupy i zobaczyć ich posty.'
        : 'Tylko członkowie grupy mogą sprawdzić listę członków grupy i zobaczyć ich posty.',
    members: 1,
    image: coverIllustration,
    images: [coverIllustration],
  })

  router.push(`/groups/${newGroup.id}`)
}
</script>

<template>
  <div class="flex h-screen bg-theme-bg   text-theme-text overflow-hidden">
    <aside
      class="w-[360px] bg-theme-bg-secondary flex flex-col shadow-theme-shadow z-20 shrink-0 h-full border-r border-theme-border"
      @click.stop
    >
      <AppCloseHeader class="shadow-sm pb-2" />
      <div class="flex-1 overflow-y-auto px-4 py-5 custom-scrollbar relative">
        <div class="text-[13px] text-theme-text-secondary font-medium">Grupy</div>
        <h1 class="text-[24px] font-bold leading-tight mb-4">Utwórz grupę</h1>

        <div class="flex items-center mb-6 p-1">
          <img :src="userAvatar" class="w-9 h-9 rounded-full mr-3 border border-theme-border" />
          <div class="leading-tight">
            <div class="font-semibold text-[15px]">Bartosz Miazek</div>
            <div class="text-[13px] text-theme-text-secondary">Administrator</div>
          </div>
        </div>

        <div class="mb-4">
          <CustomInput v-model="groupName" label="Nazwa grupy" id="groupNameInput" />
        </div>

        <div class="mb-4 relative">
          <CustomDropdown
            v-model="privacy"
            :options="privacyOptions"
            label="Wybierz ustawienie prywatności"
          />
        </div>

        <div
          v-if="privacy === 'private'"
          class="mb-4 text-[12px] text-theme-text-secondary px-1 leading-snug"
        >
          Tylko członkowie grupy mogą sprawdzić listę członków grupy i zobaczyć ich posty. Status
          grupy możesz później zmienić na publiczną.
          <span class="text-theme-primary cursor-pointer hover:underline"
            >Dowiedz się więcej...</span
          >
        </div>

        <div v-if="privacy === 'private'" class="mb-4 relative">
          <CustomDropdown v-model="visibility" :options="visibilityOptions" label="Widoczność" />
        </div>

        <div class="mb-2">
          <div
            class="w-full min-h-[56px] px-2 py-1 border border-theme-border rounded-lg focus-within:border-theme-primary focus-within:ring-1 focus-within:ring-theme-primary hover:border-theme-border-hover flex flex-wrap items-center gap-1 bg-theme-bg-secondary"
          >
            <div
              v-for="friend in invitedFriends"
              :key="friend"
              class="bg-theme-blue-light text-theme-primary px-2 py-1 rounded-[4px] flex items-center text-[14px] font-semibold"
            >
              <img :src="userAvatar" class="w-5 h-5 rounded-full mr-1.5" />
              {{ friend }}
              <Close
                :size="16"
                class="ml-1 cursor-pointer hover:text-theme-primary-hover"
                @click="removeFriend(friend)"
              />
            </div>
            <input
              type="text"
              v-model="inviteInput"
              placeholder="Zaproś znajomych (opcjonalne)"
              class="flex-1 h-[40px] px-2 outline-none text-[15px] min-w-[120px] bg-transparent"
            />
          </div>
        </div>
        <p class="text-[12px] text-theme-text-secondary px-1 leading-snug">
          Propozycje:
          <span
            @click="addFriend('Mateusz Bieniek')"
            class="text-theme-primary font-semibold cursor-pointer hover:underline"
            >Mateusz Bieniek</span
          >...
        </p>
      </div>

      <div
        class="p-4 border-t border-theme-border shadow-theme-shadow-sm bg-theme-bg-secondary z-10"
      >
        <button
          @click="handleCreateGroup"
          class="w-full h-9 rounded-md font-semibold text-[15px] flex items-center justify-center transition-colors"
          :class="
            isFormValid
              ? 'bg-theme-primary text-white hover:bg-theme-primary-hover'
              : 'bg-theme-bg-tertiary text-theme-text-placeholder cursor-not-allowed'
          "
          :disabled="!isFormValid"
        >
          Utwórz
        </button>
      </div>
    </aside>

    <main class="flex-1 flex justify-center items-start pt-8 pb-8 px-8 overflow-y-auto bg-theme-bg">
      <div
        :class="[
          'bg-theme-bg-secondary shadow-md border border-theme-border overflow-hidden rounded-lg flex flex-col mx-auto select-none',
          isPreviewMobile ? 'w-[575px] h-[812px]  ' : 'w-full max-w-[980px] ',
        ]"
      >
        <div
          class="flex justify-between items-center px-4 h-[60px] border-b border-theme-border bg-theme-bg-secondary shrink-0"
        >
          <span class="font-bold text-[16px] text-theme-text">
            {{ isPreviewMobile ? 'Podgląd na urządzeniu mobilnym' : 'Podgląd na komputerze' }}
          </span>
          <div class="flex items-center gap-2">
            <button
              @click="isPreviewMobile = false"
              :class="
                !isPreviewMobile
                  ? 'text-theme-primary bg-theme-blue-light'
                  : 'text-theme-text-secondary hover:bg-theme-bg-hover'
              "
              class="p-2 rounded-full"
            >
              <Monitor :size="24" />
            </button>
            <button
              @click="isPreviewMobile = true"
              :class="
                isPreviewMobile
                  ? 'text-theme-primary bg-theme-blue-light'
                  : 'text-theme-text-secondary hover:bg-theme-bg-hover'
              "
              class="p-2 rounded-full"
            >
              <Cellphone :size="24" />
            </button>
          </div>
        </div>

        <div
          class="flex-1 flex flex-col overflow-y-auto custom-scrollbar bg-theme-bg-secondary cursor-default"
        >
          <div :class="isPreviewMobile ? 'p-0' : 'p-4'">
            <div
              :class="[
                'bg-theme-bg overflow-hidden relative',
                isPreviewMobile ? '' : 'rounded-lg aspect-[2.5/1]',
                isPreviewMobile ? 'aspect-[2/1]' : '',
              ]"
            >
              <img
                :src="coverIllustration"
                class="w-full h-full object-cover filter grayscale opacity-90 drag-none"
                style="-webkit-user-drag: none"
              />
            </div>
          </div>

          <div :class="isPreviewMobile ? 'px-4 pt-3 pb-0' : 'px-8 pt-4 pb-0'">
            <h1
              :class="[titleColorClass, isPreviewMobile ? 'text-[22px]' : 'text-[28px]']"
              class="font-bold mb-1 leading-snug break-words"
            >
              {{ previewName }}
            </h1>

            <div class="text-theme-text-secondary text-[15px] font-medium flex items-center mt-1">
              <span class="flex items-center">
                <Lock v-if="privacy === 'private'" :size="14" class="mr-1.5" />
                <Earth v-else-if="privacy === 'public'" :size="14" class="mr-1.5" />
                <span>{{ subtitleText }}</span>
              </span>
              <span class="mx-1.5">·</span>
              <span class="font-semibold text-theme-text-secondary">1 członek</span>
            </div>

            <div
              class="flex mt-4 border-t border-theme-border overflow-x-auto no-scrollbar justify-between md:justify-start"
            >
              <div
                :class="isPreviewMobile ? 'px-2 py-3 text-[14px]' : 'px-4 py-4 text-[15px]'"
                class="text-theme-text-secondary mx-1 my-1 font-semibold whitespace-nowrap"
              >
                Informacje
              </div>
              <div
                :class="isPreviewMobile ? 'px-2 py-3 text-[14px]' : 'px-4 py-4 text-[15px]'"
                class="text-theme-text-secondary font-semibold mx-1 my-1 whitespace-nowrap"
              >
                Posty
              </div>
              <div
                :class="isPreviewMobile ? 'px-2 py-3 text-[14px]' : 'px-4 py-4 text-[15px]'"
                class="text-theme-text-secondary font-semibold mx-1 my-1 whitespace-nowrap"
              >
                Członkowie
              </div>
              <div
                :class="isPreviewMobile ? 'px-2 py-3 text-[14px]' : 'px-4 py-4 text-[15px]'"
                class="text-theme-text-secondary font-semibold mx-1 my-1 whitespace-nowrap"
              >
                Wydarzenia
              </div>
            </div>
          </div>

          <div
            :class="[
              'bg-theme-bg p-4 flex gap-4 overflow-y-auto custom-scrollbar flex-1',
              isPreviewMobile ? 'flex-col' : 'flex-row items-start',
            ]"
          >
            <div class="flex-1 shrink-0">
              <div
                class="bg-theme-bg-secondary rounded-lg shadow-sm border border-theme-border px-4 py-3 grayscale opacity-50"
              >
                <div class="flex items-center gap-2 mb-3 grayscale">
                  <AccountCircle :size="40" class="text-theme-bg-tertiary" />
                  <div
                    class="bg-theme-bg-tertiary rounded-full flex-1 px-4 py-2 text-theme-text-placeholder text-[15px] text-left truncate"
                  >
                    Co słychać?
                  </div>
                </div>

                <div class="border-t border-theme-border pt-2 flex justify-between">
                  <div
                    class="flex items-center justify-center flex-1 py-2 rounded-md px-1 opacity-60 grayscale"
                  >
                    <ImageMultiple class="text-theme-text-secondary mr-2 shrink-0" :size="24" />
                    <span class="text-theme-text-secondary font-semibold text-[14px] truncate"
                      >Zdjęcie/film</span
                    >
                  </div>

                  <div
                    class="flex items-center justify-center flex-1 py-2 rounded-md px-1 opacity-60 grayscale"
                  >
                    <AccountTag class="text-theme-text-secondary mr-2 shrink-0" :size="24" />
                    <span class="text-theme-text-secondary font-semibold text-[14px] truncate"
                      >Oznacz osoby</span
                    >
                  </div>

                  <div
                    class="flex items-center justify-center flex-1 py-2 rounded-md px-1 opacity-60 grayscale"
                  >
                    <EmoticonOutline class="text-theme-text-secondary mr-2 shrink-0" :size="24" />
                    <span class="text-theme-text-secondary font-semibold text-[14px] truncate"
                      >Nastrój/akt...</span
                    >
                  </div>
                </div>
              </div>
            </div>

            <div :class="isPreviewMobile ? 'w-full order-last' : 'w-[360px] shrink-0'">
              <div
                class="bg-theme-bg-secondary rounded-lg shadow-sm border border-theme-border p-4"
              >
                <h3 class="font-bold text-[17px] text-theme-text mb-3">Informacje</h3>

                <div v-if="privacy !== 'choose'" class="space-y-4">
                  <div class="flex items-start">
                    <Lock
                      v-if="privacy === 'private'"
                      :size="20"
                      class="text-theme-text mr-3 mt-0.5 shrink-0"
                    />
                    <Earth v-else :size="20" class="text-theme-text mr-3 mt-0.5 shrink-0" />
                    <div>
                      <div class="font-semibold text-[15px] text-theme-text mb-0.5">
                        {{ privacy === 'private' ? 'Prywatna' : 'Publiczna' }}
                      </div>
                      <div class="text-[13px] text-theme-text leading-snug">
                        {{
                          privacy === 'private'
                            ? 'Tylko członkowie grupy mogą sprawdzić listę członków grupy i zobaczyć ich posty.'
                            : 'Każdy może zobaczyć, kto należy do grupy i co w niej publikuje.'
                        }}
                      </div>
                    </div>
                  </div>
                  <div class="flex items-start">
                    <Eye
                      v-if="visibility === 'visible'"
                      :size="20"
                      class="text-theme-text mr-3 mt-0.5 shrink-0"
                    />
                    <EyeOff v-else :size="20" class="text-theme-text mr-3 mt-0.5 shrink-0" />
                    <div>
                      <div class="font-semibold text-[15px] text-theme-text mb-0.5">
                        {{ visibility === 'visible' ? 'Widoczna' : 'Ukryta' }}
                      </div>
                      <div class="text-[13px] text-theme-text leading-snug">
                        {{
                          visibility === 'visible'
                            ? 'Każdy może znaleźć tę grupę.'
                            : 'Tylko członkowie mogą znaleźć tę grupę.'
                        }}
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="text-theme-text-secondary text-[13px]">
                  Wybierz ustawienia prywatności, aby zobaczyć podgląd informacji.
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div class="fixed bottom-6 right-6 z-50">
      <button
        class="bg-theme-bg-secondary p-3 rounded-full shadow-lg border border-theme-border hover:bg-theme-bg-hover transition"
      >
        <Pencil :size="20" class="text-theme-text" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.drag-none {
  -webkit-user-drag: none;
  user-drag: none;
}
</style>
