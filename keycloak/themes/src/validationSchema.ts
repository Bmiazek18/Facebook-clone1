import { z } from 'zod'

export const registrationSchema = z.object({
  firstName: z.string().min(2, { message: 'Imię jest za krótkie' }),
  lastName: z.string().min(2, { message: 'Nazwisko jest za krótkie' }),
  emailOrPhone: z.string().refine(
    (val) => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      const phoneRegex = /^[0-9+]{9,15}$/
      return emailRegex.test(val) || phoneRegex.test(val)
    },
    { message: 'Wprowadź poprawny e-mail lub numer telefonu' }
  ),
  day: z.string().min(1, { message: 'Wybierz dzień' }),
  month: z.string().min(1, { message: 'Wybierz miesiąc' }),
  year: z.string().min(1, { message: 'Wybierz rok' }),
  gender: z.string().min(1, { message: 'Wybierz płeć' }),
  password: z.string().min(6, { message: 'Hasło musi mieć co najmniej 6 znaków' })
})
