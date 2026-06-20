<template>
  <div class="min-h-screen bg-theme-bg-secondary flex items-center justify-center p-4 font-sans text-[#1c1e21]">
    <div class="w-full max-w-150 p-4 sm:p-5">

      <div class="flex items-center justify-between mb-4">
        <button type="button" @click="goBack" class="text-[#606770] hover:bg-gray-100 p-2 rounded-full transition-colors focus:outline-none">
          <ArrowLeftIcon :size="20" />
        </button>
        <img src="https://upload.wikimedia.org/wikipedia/commons/7/7b/Meta_Platforms_Inc._logo.svg" alt="Meta" class="h-4" />
        <div class="w-9"></div>
      </div>

      <h1 class="text-[22px] sm:text-2xl font-bold mb-1.5 text-center sm:text-left">{{ t('auth.register.title') }}</h1>
      <p class="text-[14px] sm:text-[15px] text-[#606770] mb-5 text-center sm:text-left leading-snug">
        {{ t('auth.register.description') }}
      </p>

      <form @submit="onSubmit">
        <fieldset class="mb-3">
          <legend class="text-[15px] font-semibold mb-2 text-[#1c1e21]">Imię i nazwisko</legend>
          <div class="flex gap-2.5">
            <CustomInput
              id="firstName"
              :label="t('auth.register.firstName')"
              v-model="firstName"
              v-bind="firstNameAttrs"
              :error="!!errors.firstName"
            />
            <CustomInput
              id="lastName"
              :label="t('auth.register.lastName')"
              v-model="lastName"
              v-bind="lastNameAttrs"
              :error="!!errors.lastName"
            />
          </div>
          <div v-if="errors.firstName || errors.lastName" class="text-[#b0281c] text-[12px] mt-1.5 leading-tight">
            Imiona i nazwiska na Facebooku nie mogą być zbyt krótkie. <a href="#" class="text-[#1877f2] hover:underline font-medium">Dowiedz się więcej</a> na temat naszych zasad dotyczących imion i nazwisk.
          </div>
        </fieldset>

        <fieldset class="mb-3">
          <legend :class="['text-[15px] font-semibold mb-2 flex items-center gap-1', (errors.day || errors.month || errors.year) ? 'text-[#b0281c]' : 'text-[#1c1e21]']">
            {{ t('auth.register.birthdate') }} <HelpCircleOutlineIcon :size="16" class="text-[#606770] cursor-help" />
          </legend>
          <div class="flex gap-2.5">
            <div class="flex-1">
              <CustomDropdown
                v-model="day"
                v-bind="dayAttrs"
                :label="t('auth.register.day')"
                :options="dayOptions"
                :error="!!errors.day"
              />
            </div>
            <div class="flex-1">
              <CustomDropdown
                v-model="month"
                v-bind="monthAttrs"
                :label="t('auth.register.month')"
                :options="monthOptions"
                :error="!!errors.month"
              />
            </div>
            <div class="flex-[1.2]">
              <CustomDropdown
                v-model="year"
                v-bind="yearAttrs"
                :label="t('auth.register.year')"
                :options="yearOptions"
                :error="!!errors.year"
              />
            </div>
          </div>
        </fieldset>

        <fieldset class="mb-3">
          <legend :class="['text-[15px] font-semibold mb-2 flex items-center gap-1', errors.gender ? 'text-[#b0281c]' : 'text-[#1c1e21]']">
            Płeć
            <VDropdown placement="bottom-start" :distance="8" :skidding="10">
              <button type="button" class="focus:outline-none flex items-center justify-center">
                <HelpCircleOutlineIcon :size="16" class="text-[#606770] cursor-pointer hover:text-[#1c1e21] transition-colors" />
              </button>

              <template #popper>
                <div class="p-4 w-[340px] text-[13px] text-[#1c1e21] leading-snug bg-white rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.15)] border border-gray-100">
                  Później możesz zmienić widoczność informacji dotyczących płci w profilu. Wybierz opcję Niestandardowa, aby wybrać inną płeć lub nie podawać tych informacji.
                </div>
              </template>
            </VDropdown>
          </legend>

          <CustomDropdown
            v-model="gender"
            v-bind="genderAttrs"
            :label="t('auth.register.gender')"
            :options="genderOptions"
            :error="!!errors.gender"
          />
        </fieldset>

        <fieldset class="mb-3">
          <legend class="text-[15px] font-semibold mb-2 text-[#1c1e21]">{{ t('auth.register.emailOrPhone') }}</legend>
          <CustomInput
            id="emailOrPhone"
            :label="t('auth.register.emailOrPhone')"
            v-model="emailOrPhone"
            v-bind="emailOrPhoneAttrs"
            :error="!!errors.emailOrPhone"
          />
          <span v-if="errors.emailOrPhone" class="text-[#b0281c] text-[12px] mt-1.5 block">{{ errors.emailOrPhone }}</span>
          <p class="text-[12px] text-[#606770] mt-1.5 leading-normal">
            Możesz otrzymywać od nas powiadomienia. <a href="#" class="text-[#1877f2] hover:underline">Dowiedz się, dlaczego prosimy o dane kontaktowe</a>
          </p>
        </fieldset>

        <fieldset class="mb-5">
          <legend class="text-[15px] font-semibold mb-2 text-[#1c1e21]">Hasło</legend>
          <CustomInput
            id="password"
            :label="t('auth.register.password')"
            :type="showPassword ? 'text' : 'password'"
            v-model="password"
            v-bind="passwordAttrs"
            :error="!!errors.password"
          >
            <template #icon>
              <div class="absolute right-3 top-1/2 -translate-y-1/2 flex items-center gap-2.5 text-[#606770]">
                <KeyVariantIcon :size="18" />
                <button type="button" @click="showPassword = !showPassword" class="focus:outline-none hover:text-[#1c1e21] transition-colors">
                  <EyeOffIcon v-if="!showPassword" :size="18" />
                  <EyeIcon v-else :size="18" />
                </button>
              </div>
            </template>
          </CustomInput>
          <div v-if="errors.password" class="flex items-start gap-1.5 text-[#b0281c] mt-1.5">
            <AlertCircleOutlineIcon :size="14" class="shrink-0 mt-px" />
            <span class="text-[12px] leading-tight">{{ errors.password }}</span>
          </div>
        </fieldset>

        <div class="text-[11px] sm:text-[11.5px] text-[#606770] mb-6 leading-normal space-y-2.5">
          <p>
            Osoby korzystające z naszej usługi mogły przesłać Twoje dane kontaktowe do platformy Facebook. <a href="#" class="text-[#1877f2] hover:underline">Dowiedz się więcej</a>.
          </p>
          <p>
            Klikając przycisk Prześlij, akceptujesz utworzenie konta i <a href="#" class="text-[#1877f2] hover:underline">Regulamin</a> Facebooka. Informacje o tym, jak zbieramy, wykorzystujemy i udostępniamy Twoje dane, zawierają nasze <a href="#" class="text-[#1877f2] hover:underline">Zasady ochrony prywatności</a>. O wykorzystaniu plików cookie i podobnych technologii informują <a href="#" class="text-[#1877f2] hover:underline">Zasady dotyczące plików cookie</a>.
          </p>
          <p>
            <a href="#" class="text-[#1877f2] hover:underline font-medium">Zasady ochrony prywatności</a> opisują możliwe sposoby wykorzystywania informacji gromadzonych w ramach tworzenia konta. Tych informacji używamy na przykład do dostarczania, personalizowania i ulepszania naszych produktów, w tym reklam.
          </p>
        </div>

        <div class="flex flex-col gap-2.5 w-full">
          <AuthPrimaryButton type="submit" :disabled="isLoading">
            {{ isLoading ? 'Tworzenie konta...' : t('auth.register.submit') }}
          </AuthPrimaryButton>

          <button
            type="button"
            @click="router.push('/login')"
            class="w-full py-2.5 sm:py-3 px-4 rounded-full border border-[#bcc0c4] text-[#4b4f56] font-semibold text-[14px] sm:text-[15px] bg-white hover:bg-gray-50 active:bg-gray-100 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-200"
          >
            Mam już konto
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import { useRouter } from 'vue-router';
import { useToast } from 'vue-toastification';

import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';
import EyeIcon from 'vue-material-design-icons/Eye.vue';
import EyeOffIcon from 'vue-material-design-icons/EyeOff.vue';
import AlertCircleOutlineIcon from 'vue-material-design-icons/AlertCircleOutline.vue';
import HelpCircleOutlineIcon from 'vue-material-design-icons/HelpCircleOutline.vue';
import KeyVariantIcon from 'vue-material-design-icons/KeyVariant.vue';

import CustomInput from '@/components/common/CustomInput.vue';
import CustomDropdown from '@/components/common/CustomDropdown.vue';
import AuthPrimaryButton from '@/components/auth/AuthPrimaryButton.vue';

import { registrationSchema } from '@/validationSchema';

const router = useRouter();
const { t } = useI18n();
const toast = useToast();

const showPassword = ref(false);
const isLoading = ref(false);

const goBack = () => {
  router.back();
};

const genderOptions = [
  { id: 'female', title: 'Kobieta' },
  { id: 'male', title: 'Mężczyzna' },
  { id: 'custom', title: 'Niestandardowa' }
];

const dayOptions = Array.from({ length: 31 }, (_, i) => ({
  id: String(i + 1),
  title: String(i + 1)
}));

const monthNames = ['Styczeń', 'Luty', 'Marzec', 'Kwiecień', 'Maj', 'Czerwiec', 'Lipiec', 'Sierpień', 'Wrzesień', 'Październik', 'Listopad', 'Grudzień'];
const monthOptions = monthNames.map((m, i) => ({
  id: String(i + 1),
  title: m
}));

const currentYear = new Date().getFullYear();
const yearOptions = Array.from({ length: 100 }, (_, i) => ({
  id: String(currentYear - i),
  title: String(currentYear - i)
}));

const { errors, defineField, handleSubmit } = useForm({
  validationSchema: toTypedSchema(registrationSchema),
});

const [firstName, firstNameAttrs] = defineField('firstName');
const [lastName, lastNameAttrs] = defineField('lastName');
const [emailOrPhone, emailOrPhoneAttrs] = defineField('emailOrPhone');
const [day, dayAttrs] = defineField('day');
const [month, monthAttrs] = defineField('month');
const [year, yearAttrs] = defineField('year');
const [gender, genderAttrs] = defineField('gender');
const [password, passwordAttrs] = defineField('password');

const onSubmit = handleSubmit(async (values) => {
  try {
    isLoading.value = true;

    const payload = {
      firstName: values.firstName,
      lastName: values.lastName,
      birthDay: parseInt(values.day as string, 10),
      birthMonth: parseInt(values.month as string, 10),
      birthYear: parseInt(values.year as string, 10),
      gender: values.gender,
      emailOrPhone: values.emailOrPhone,
      password: values.password
    };



// Wewnątrz funkcji onSubmit:
const response = await fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  credentials: 'include' ,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',

  },
  body: JSON.stringify(payload)
});

    if (!response.ok) {
      const errorData = await response.text();
      throw new Error(errorData || 'Wystąpił błąd podczas rejestracji');
    }

    router.push('/confirmemail');

  } catch (error: any) {
    console.error('Błąd zapytania:', error);
    toast.error(error.message || 'Nie udało się połączyć z serwerem.');
  } finally {
    isLoading.value = false;
  }
});
</script>

<style>
.v-popper__inner {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
}
.v-popper__arrow-outer {
  display: none !important;
}
</style>
