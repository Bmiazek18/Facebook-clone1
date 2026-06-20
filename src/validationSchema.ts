// src/validationSchema.ts
import { object, string } from 'zod';

const phoneRegex = /^([0-9]{7,15})$/;
// RegEx dla hasła: min. 1 litera, 1 cyfra, 1 znak specjalny, minimum 6 znaków
const passwordComplexityRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{6,}$/;

export const registrationSchema = object({
  firstName: string()
    .min(1, 'Imię jest wymagane')
    .min(2, 'Imię musi mieć co najmniej 2 znaki'),
  lastName: string()
    .min(1, 'Nazwisko jest wymagane')
    .min(2, 'Nazwisko musi mieć co najmniej 2 znaki'),

  emailOrPhone: string()
    .min(1, 'Numer telefonu komórkowego lub adres e-mail jest wymagany')
    .refine(value => {
      const isEmail = string().email().safeParse(value).success;
      const isPhone = phoneRegex.test(value);
      return isEmail || isPhone;
    }, 'Wprowadź poprawny numer telefonu komórkowego lub adres e-mail'),

  day: string().min(1, 'Wybierz dzień'),
  month: string().min(1, 'Wybierz miesiąc'),
  year: string().min(1, 'Wybierz rok'),
  gender: string().min(1, 'Wybierz płeć'),

  // ZAKTUALIZOWANE HASŁO
  password: string()
    .min(1, 'Hasło jest wymagane')
    .regex(
      passwordComplexityRegex,
      'Wprowadź kombinację co najmniej sześciu cyfr, liter i znaków interpunkcyjnych (takich jak ! i &).'
    ),
});
