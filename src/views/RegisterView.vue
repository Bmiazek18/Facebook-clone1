<template>
  <div class="min-h-screen bg-[#f0f2f5] flex flex-col font-sans items-center">

    <div class="pt-8 pb-4">
      <h1 class="text-[#1877f2] text-[56px] font-bold tracking-tighter h-[60px] flex items-end cursor-pointer">
        facebook
      </h1>
    </div>

    <div class="bg-white rounded-lg shadow-xl w-[432px] mb-10">
      <div class="px-4 py-3 border-b border-[#dadde1] text-center relative">
        <h2 class="text-[25px] font-bold text-[#1c1e21] leading-8">Create a new account</h2>
        <p class="text-[#606770] text-[15px] leading-6">It's quick and easy.</p>
        <button class="absolute top-3 right-4 text-[#606770] hover:text-[#1c1e21]">
           <CloseIcon :size="24" />
        </button>
      </div>

      <div class="p-4">
        <form @submit.prevent="handleSignup" class="flex flex-col gap-3">

          <div class="flex gap-3">
            <div class="w-1/2 relative">
              <input
                v-model="form.firstName"
                type="text"
                placeholder="First name"
                :class="inputClass('firstName')"
                @input="clearError('firstName')"
              />
              <AlertCircleIcon v-if="errors.firstName" class="absolute right-2 top-2.5 text-red-500" :size="20" />
            </div>

            <div class="w-1/2 relative">
              <input
                v-model="form.surname"
                type="text"
                placeholder="Surname"
                :class="inputClass('surname')"
                @input="clearError('surname')"
              />
               <AlertCircleIcon v-if="errors.surname" class="absolute right-2 top-2.5 text-red-500" :size="20" />
            </div>
          </div>

          <div class="mt-1">
            <div class="flex items-center gap-1 mb-1">
              <span class="text-[12px] text-[#606770] font-normal">Date of birth</span>
              <HelpCircleIcon :size="12" class="text-[#606770] cursor-pointer" />
            </div>
            <div class="flex gap-3">
              <select v-model="form.day" class="w-1/3 border border-[#ccd0d5] rounded-[4px] px-2 py-1.5 text-[15px] bg-white focus:outline-none focus:border-[#1877f2]">
                <option v-for="d in 31" :key="d" :value="d">{{ d }}</option>
              </select>
              <select v-model="form.month" class="w-1/3 border border-[#ccd0d5] rounded-[4px] px-2 py-1.5 text-[15px] bg-white focus:outline-none focus:border-[#1877f2]">
                <option v-for="(m, i) in months" :key="m" :value="i">{{ m }}</option>
              </select>
              <select v-model="form.year" class="w-1/3 border border-[#ccd0d5] rounded-[4px] px-2 py-1.5 text-[15px] bg-white focus:outline-none focus:border-[#1877f2]">
                <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
              </select>
            </div>
          </div>

          <div class="mt-1">
            <div class="flex items-center gap-1 mb-1">
              <span class="text-[12px] text-[#606770] font-normal" :class="{'text-red-500': errors.gender}">Gender</span>
              <HelpCircleIcon :size="12" class="text-[#606770] cursor-pointer" />
              <AlertCircleIcon v-if="errors.gender" :size="14" class="text-red-500 ml-1" />
            </div>
            <div class="flex gap-3">
              <label class="w-1/3 border rounded-[4px] px-2 py-1.5 bg-white flex justify-between items-center cursor-pointer hover:bg-gray-50"
                 :class="errors.gender ? 'border-red-500' : 'border-[#ccd0d5]'">
                <span class="text-[15px] text-[#1c1e21]">Female</span>
                <input type="radio" name="gender" value="female" v-model="form.gender" @change="clearError('gender')" class="border-gray-300 focus:ring-0" />
              </label>

              <label class="w-1/3 border rounded-[4px] px-2 py-1.5 bg-white flex justify-between items-center cursor-pointer hover:bg-gray-50"
                 :class="errors.gender ? 'border-red-500' : 'border-[#ccd0d5]'">
                <span class="text-[15px] text-[#1c1e21]">Male</span>
                <input type="radio" name="gender" value="male" v-model="form.gender" @change="clearError('gender')" class="border-gray-300 focus:ring-0" />
              </label>

              <label class="w-1/3 border rounded-[4px] px-2 py-1.5 bg-white flex justify-between items-center cursor-pointer hover:bg-gray-50"
                 :class="errors.gender ? 'border-red-500' : 'border-[#ccd0d5]'">
                <span class="text-[15px] text-[#1c1e21]">Custom</span>
                <input type="radio" name="gender" value="custom" v-model="form.gender" @change="clearError('gender')" class="border-gray-300 focus:ring-0" />
              </label>
            </div>
             <p v-if="errors.gender" class="text-xs text-red-500 mt-1">Please choose a gender.</p>
          </div>

          <div class="mt-0 relative">
             <input
              v-model="form.contact"
              type="text"
              placeholder="Mobile number or email address"
              :class="inputClass('contact')"
              @input="clearError('contact')"
            />
            <AlertCircleIcon v-if="errors.contact" class="absolute right-2 top-2.5 text-red-500" :size="20" />
          </div>

          <div class="relative">
             <input
              v-model="form.password"
              type="password"
              placeholder="New password"
              :class="inputClass('password')"
              @input="clearError('password')"
            />
            <AlertCircleIcon v-if="errors.password" class="absolute right-2 top-2.5 text-red-500" :size="20" />
            <p v-if="errors.password" class="text-xs text-red-500 mt-1">Min. 6 characters required.</p>
          </div>

          <div class="text-[11px] text-[#777] leading-[15px] mt-1 mb-2">
            People who use our service may have uploaded your contact information to Facebook. <a href="#" class="text-[#385898] hover:underline">Learn more</a>.
            <br><br>
            By clicking Sign Up, you agree to our <a href="#" class="text-[#385898] hover:underline">Terms</a>...
          </div>

          <div class="flex flex-col items-center gap-4">
            <button
              type="submit"
              class="bg-[#00a400] hover:bg-[#36a420] text-white text-[18px] font-bold px-16 py-1.5 rounded-[6px] shadow-sm transition-colors min-w-[194px] h-[36px] flex items-center justify-center"
            >
              Sign Up
            </button>
            <a href="#" class="text-[#1877f2] text-[15px] hover:underline">Already have an account?</a>
          </div>

        </form>
      </div>
    </div>

    <footer class="bg-white w-full text-[#737373] text-xs pt-5 pb-5 mt-auto">
      <div class="max-w-[980px] mx-auto px-4 text-center">
        Meta © 2025
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import HelpCircleIcon from 'vue-material-design-icons/HelpCircle.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import AlertCircleIcon from 'vue-material-design-icons/AlertCircle.vue'; // Nowa ikona błędu

// --- Helpers ---
const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const currentYear = new Date().getFullYear();
const years = computed(() => {
  const y = [];
  for (let i = currentYear; i >= 1905; i--) y.push(i);
  return y;
});

// --- State ---
const form = ref({
  firstName: '',
  surname: '',
  day: 12,
  month: 11,
  year: 2025,
  gender: '',
  contact: '',
  password: ''
});

// Stan błędów - przechowuje true dla każdego pola, które ma błąd
const errors = ref<Record<string, boolean>>({
  firstName: false,
  surname: false,
  gender: false,
  contact: false,
  password: false
});

// --- Logic ---

// Helper do dynamicznych klas CSS dla inputów
const inputClass = (field: string) => {
  const base = "w-full bg-[#f5f6f7] border rounded-[5px] px-3 py-2 text-[15px] placeholder-[#8d949e] focus:outline-none";
  if (errors.value[field]) {
    // Styl błędu (czerwona ramka)
    return `${base} border-red-500 ring-1 ring-red-500`;
  }
  // Normalny styl
  return `${base} border-[#ccd0d5] focus:border-[#1877f2] focus:ring-1 focus:ring-[#1877f2]`;
};

// Funkcja czyszcząca błąd podczas pisania
const clearError = (field: string) => {
  errors.value[field] = false;
};

// Główna funkcja walidacji
const validateForm = () => {
  let isValid = true;

  // Reset
  Object.keys(errors.value).forEach(key => errors.value[key] = false);

  if (!form.value.firstName.trim()) {
    errors.value.firstName = true;
    isValid = false;
  }

  if (!form.value.surname.trim()) {
    errors.value.surname = true;
    isValid = false;
  }

  // Walidacja kontaktu (czy nie puste)
  if (!form.value.contact.trim()) {
    errors.value.contact = true;
    isValid = false;
  }
  // Opcjonalnie: Możesz tu dodać Regex dla email/telefonu
  // else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.contact)) { ... }

  if (!form.value.password || form.value.password.length < 6) {
    errors.value.password = true;
    isValid = false;
  }

  if (!form.value.gender) {
    errors.value.gender = true;
    isValid = false;
  }

  return isValid;
};

const handleSignup = () => {
  if (validateForm()) {
    console.log('Validation Passed! Registering user:', form.value);
    alert(`Success! Welcome ${form.value.firstName}`);
    // Tutaj logika API
  } else {
    console.log('Validation Failed');
    // Efekt "shake" formularza lub focus na pierwsze błędne pole można dodać tutaj
  }
};
</script>
