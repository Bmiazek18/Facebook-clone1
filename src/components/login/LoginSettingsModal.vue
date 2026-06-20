<template>
  <Teleport to="body">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60">
      <div class="absolute inset-0" @click="close"></div>
      <div class="login-settings-dialog relative bg-white w-full p-8 shadow-2xl z-10 animate-fade-in-up">
        <button @click="close" class="absolute top-6 right-6 text-slate-800 hover:bg-slate-100 p-2 rounded-full transition-colors">
          <CloseIcon :size="28" />
        </button>

        <h2 class="text-[26px] font-semibold text-slate-900 mb-8 mt-2 tracking-tight">Usuń profile z tej przeglądarki</h2>

        <div class="flex items-center justify-between border border-slate-200 rounded-xl p-4 mb-6">
          <div class="flex items-center gap-4">
            <div class="login-settings-avatar rounded-full overflow-hidden border border-slate-100">
              <img :src="userProfile.avatar" :alt="userProfile.name" class="w-full h-full object-cover" />
            </div>
            <div class="flex flex-col text-left">
              <span class="text-[17px] font-medium text-slate-900">{{ userProfile.name }}</span>
              <span class="text-[15px] text-slate-500">Facebook</span>
            </div>
          </div>
          <button class="px-6 py-2 border border-slate-300 rounded-full font-medium text-slate-900 bg-white hover:bg-slate-50 transition-colors text-[15px]">Usuń</button>
        </div>

          <p class="text-[15px] text-slate-500 text-left">
          <a href="#" class="text-primary font-semibold hover:underline">Dowiedz się więcej</a>
          na temat tego, dlaczego widzisz tutaj profile i co oznacza ich usunięcie.
        </p>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import CloseIcon from 'vue-material-design-icons/Close.vue';

interface UserProfile {
  name: string;
  avatar: string;
}

defineProps<{
  modelValue: boolean;
  userProfile: UserProfile;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
}>();

const close = () => emit('update:modelValue', false);
</script>

<style scoped>
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up { animation: fade-in-up 0.2s ease-out forwards; }

.login-settings-dialog {
  max-width: 550px;
  border-radius: 24px;
}

.login-settings-avatar {
  width: 50px;
  height: 50px;
}
</style>
