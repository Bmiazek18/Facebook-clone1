import React, { useState } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import toast, { Toaster } from 'react-hot-toast'
import { ArrowLeft, Eye, EyeOff, AlertCircle, HelpCircle, KeyRound } from 'lucide-react'

import CustomInput from '~/components/CustomInput'
import CustomDropdown from '~/components/CustomDropdown'
import AuthButton from '~/components/AuthButton'
import { registrationSchema } from '~/validationSchema'
import { z } from 'zod'

type RegistrationFormData = z.infer<typeof registrationSchema>

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [showGenderTooltip, setShowGenderTooltip] = useState(false)

  const genderOptions = [
    { id: 'female', title: 'Kobieta' },
    { id: 'male', title: 'Mężczyzna' },
    { id: 'custom', title: 'Niestandardowa' }
  ]

  const dayOptions = Array.from({ length: 31 }, (_, i) => ({
    id: String(i + 1),
    title: String(i + 1)
  }))

  const monthNames = ['Styczeń', 'Luty', 'Marzec', 'Kwiecień', 'Maj', 'Czerwiec', 'Lipiec', 'Sierpień', 'Wrzesień', 'Październik', 'Listopad', 'Grudzień']
  const monthOptions = monthNames.map((m, i) => ({
    id: String(i + 1),
    title: m
  }))

  const currentYear = new Date().getFullYear()
  const yearOptions = Array.from({ length: 100 }, (_, i) => ({
    id: String(currentYear - i),
    title: String(currentYear - i)
  }))

  const {
    control,
    handleSubmit,
    formState: { errors }
  } = useForm<RegistrationFormData>({
    resolver: zodResolver(registrationSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      emailOrPhone: '',
      day: '',
      month: '',
      year: '',
      gender: '',
      password: ''
    }
  })

  const onSubmit = async (values: RegistrationFormData) => {
    try {
      setIsLoading(true)

      const payload = {
        firstName: values.firstName,
        lastName: values.lastName,
        birthDay: parseInt(values.day, 10),
        birthMonth: parseInt(values.month, 10),
        birthYear: parseInt(values.year, 10),
        gender: values.gender,
        emailOrPhone: values.emailOrPhone,
        password: values.password
      }

      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(payload)
      })

      if (!response.ok) {
        const errorData = await response.text()
        throw new Error(errorData || 'Wystąpił błąd podczas rejestracji')
      }

      window.location.href = '/confirmemail'
    } catch (error: any) {
      console.error('Błąd zapytania:', error)
      toast.error(error.message || 'Nie udało się połączyć z serwerem.')
    } finally {
      setIsLoading(false)
    }
  }

  const goBack = () => {
    navigate(-1)
  }

  return (
    <div className="min-h-screen bg-theme-bg-secondary flex items-center justify-center p-4 font-sans text-[#1c1e21]">
      <Toaster position="top-right" />
      <div className="w-full max-w-[600px] p-4 sm:p-5">
        <div className="flex items-center justify-between mb-4">
          <button
            type="button"
            className="text-[#606770] hover:bg-gray-100 p-2 rounded-full transition-colors focus:outline-none cursor-pointer"
            onClick={goBack}
          >
            <ArrowLeft size={20} />
          </button>
          <img
            src="https://upload.wikimedia.org/wikipedia/commons/7/7b/Meta_Platforms_Inc._logo.svg"
            alt="Meta"
            className="h-4"
          />
          <div className="w-9" />
        </div>

        <h1 className="text-[22px] sm:text-2xl font-bold mb-1.5 text-center sm:text-left">
          {t('auth.register.title')}
        </h1>
        <p className="text-[14px] sm:text-[15px] text-[#606770] mb-5 text-center sm:text-left leading-snug">
          {t('auth.register.description')}
        </p>

        <form onSubmit={handleSubmit(onSubmit)}>
          <fieldset className="mb-3">
            <legend className="text-[15px] font-semibold mb-2 text-[#1c1e21] text-left">
              Imię i nazwisko
            </legend>
            <div className="flex gap-2.5">
              <div className="flex-1">
                <Controller
                  name="firstName"
                  control={control}
                  render={({ field }) => (
                    <CustomInput
                      id="firstName"
                      value={field.value}
                      onChange={field.onChange}
                      label={t('auth.register.firstName')}
                      error={!!errors.firstName}
                    />
                  )}
                />
              </div>
              <div className="flex-1">
                <Controller
                  name="lastName"
                  control={control}
                  render={({ field }) => (
                    <CustomInput
                      id="lastName"
                      value={field.value}
                      onChange={field.onChange}
                      label={t('auth.register.lastName')}
                      error={!!errors.lastName}
                    />
                  )}
                />
              </div>
            </div>
            {(errors.firstName || errors.lastName) && (
              <div className="text-[#b0281c] text-[12px] mt-1.5 leading-tight text-left">
                Imiona i nazwiska na Facebooku nie mogą być zbyt krótkie.{' '}
                <a href="#" className="text-[#1877f2] hover:underline font-medium">
                  Dowiedz się więcej
                </a>{' '}
                na temat naszych zasad dotyczących imion i nazwisk.
              </div>
            )}
          </fieldset>

          <fieldset className="mb-3">
            <legend
              className={`text-[15px] font-semibold mb-2 flex items-center gap-1 text-left ${
                errors.day || errors.month || errors.year ? 'text-[#b0281c]' : 'text-[#1c1e21]'
              }`}
            >
              {t('auth.register.birthdate')}{' '}
              <HelpCircle size={16} className="text-[#606770] cursor-help" />
            </legend>
            <div className="flex gap-2.5">
              <div className="flex-1">
                <Controller
                  name="day"
                  control={control}
                  render={({ field }) => (
                    <CustomDropdown
                      value={field.value}
                      onChange={field.onChange}
                      label={t('auth.register.day')}
                      options={dayOptions}
                      error={!!errors.day}
                    />
                  )}
                />
              </div>
              <div className="flex-1">
                <Controller
                  name="month"
                  control={control}
                  render={({ field }) => (
                    <CustomDropdown
                      value={field.value}
                      onChange={field.onChange}
                      label={t('auth.register.month')}
                      options={monthOptions}
                      error={!!errors.month}
                    />
                  )}
                />
              </div>
              <div className="flex-[1.2]">
                <Controller
                  name="year"
                  control={control}
                  render={({ field }) => (
                    <CustomDropdown
                      value={field.value}
                      onChange={field.onChange}
                      label={t('auth.register.year')}
                      options={yearOptions}
                      error={!!errors.year}
                    />
                  )}
                />
              </div>
            </div>
          </fieldset>

          <fieldset className="mb-3">
            <legend
              className={`text-[15px] font-semibold mb-2 flex items-center gap-1 text-left ${
                errors.gender ? 'text-[#b0281c]' : 'text-[#1c1e21]'
              }`}
            >
              Płeć
              <div className="relative inline-block">
                <button
                  type="button"
                  onClick={() => setShowGenderTooltip(!showGenderTooltip)}
                  className="focus:outline-none flex items-center justify-center cursor-pointer"
                >
                  <HelpCircle
                    size={16}
                    className="text-[#606770] hover:text-[#1c1e21] transition-colors"
                  />
                </button>
                {showGenderTooltip && (
                  <div className="absolute left-0 top-6 z-50 p-4 w-[340px] text-[13px] text-[#1c1e21] leading-snug bg-white rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.15)] border border-gray-100 text-left">
                    Później możesz zmienić widoczność informacji dotyczących płci w profilu. Wybierz
                    opcję Niestandardowa, aby wybrać inną płeć lub nie podawać tych informacji.
                  </div>
                )}
              </div>
            </legend>

            <Controller
              name="gender"
              control={control}
              render={({ field }) => (
                <CustomDropdown
                  value={field.value}
                  onChange={field.onChange}
                  label={t('auth.register.gender')}
                  options={genderOptions}
                  error={!!errors.gender}
                />
              )}
            />
          </fieldset>

          <fieldset className="mb-3">
            <legend className="text-[15px] font-semibold mb-2 text-[#1c1e21] text-left">
              {t('auth.register.emailOrPhone')}
            </legend>
            <Controller
              name="emailOrPhone"
              control={control}
              render={({ field }) => (
                <CustomInput
                  id="emailOrPhone"
                  value={field.value}
                  onChange={field.onChange}
                  label={t('auth.register.emailOrPhone')}
                  error={!!errors.emailOrPhone}
                />
              )}
            />
            {errors.emailOrPhone && (
              <span className="text-[#b0281c] text-[12px] mt-1.5 block text-left">
                {errors.emailOrPhone.message}
              </span>
            )}
            <p className="text-[12px] text-[#606770] mt-1.5 leading-normal text-left">
              Możesz otrzymywać od nas powiadomienia.{' '}
              <a href="#" className="text-[#1877f2] hover:underline">
                Dowiedz się, dlaczego prosimy o dane kontaktowe
              </a>
            </p>
          </fieldset>

          <fieldset className="mb-5">
            <legend className="text-[15px] font-semibold mb-2 text-[#1c1e21] text-left">
              Hasło
            </legend>
            <Controller
              name="password"
              control={control}
              render={({ field }) => (
                <CustomInput
                  id="password"
                  value={field.value}
                  onChange={field.onChange}
                  label={t('auth.register.password')}
                  type={showPassword ? 'text' : 'password'}
                  error={!!errors.password}
                  icon={
                    <div className="flex items-center gap-2.5 text-[#606770]">
                      <KeyRound size={18} />
                      <button
                        type="button"
                        className="focus:outline-none hover:text-[#1c1e21] transition-colors cursor-pointer"
                        onClick={() => setShowPassword(!showPassword)}
                      >
                        {showPassword ? <Eye size={18} /> : <EyeOff size={18} />}
                      </button>
                    </div>
                  }
                />
              )}
            />
            {errors.password && (
              <div className="flex items-start gap-1.5 text-[#b0281c] mt-1.5 text-left">
                <AlertCircle size={14} className="shrink-0 mt-px" />
                <span className="text-[12px] leading-tight">{errors.password.message}</span>
              </div>
            )}
          </fieldset>

          <div className="text-[11px] sm:text-[11.5px] text-[#606770] mb-6 leading-normal space-y-2.5 text-left">
            <p>
              Osoby korzystające z naszej usługi mogły przesłać Twoje dane kontaktowe do platformy
              Facebook.{' '}
              <a href="#" className="text-[#1877f2] hover:underline">
                Dowiedz się więcej
              </a>
              .
            </p>
            <p>
              Klikając przycisk Prześlij, akceptujesz utworzenie konta i{' '}
              <a href="#" className="text-[#1877f2] hover:underline">
                Regulamin
              </a>{' '}
              Facebooka. Informacje o tym, jak zbieramy, wykorzystujemy i udostępniamy Twoje dane,
              zawierają nasze{' '}
              <a href="#" className="text-[#1877f2] hover:underline">
                Zasady ochrony prywatności
              </a>
              . O wykorzystaniu plików cookie i podobnych technologii informują{' '}
              <a href="#" className="text-[#1877f2] hover:underline">
                Zasady dotyczące plików cookie
              </a>
              .
            </p>
            <p>
              <a href="#" className="text-[#1877f2] hover:underline font-medium">
                Zasady ochrony prywatności
              </a>{' '}
              opisują możliwe sposoby wykorzystywania informacji gromadzonych w ramach tworzenia konta.
              Tych informacji używamy na przykład do dostarczania, personalizowania i ulepszania
              naszych produktów, w tym reklam.
            </p>
          </div>

          <div className="flex flex-col gap-2.5 w-full">
            <AuthButton type="submit" disabled={isLoading}>
              {isLoading ? 'Tworzenie konta...' : t('auth.register.submit')}
            </AuthButton>

            <button
              type="button"
              className="w-full py-2.5 sm:py-3 px-4 rounded-full border border-[#bcc0c4] text-[#4b4f56] font-semibold text-[14px] sm:text-[15px] bg-white hover:bg-gray-50 active:bg-gray-100 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-200 cursor-pointer"
              onClick={() => navigate('/')}
            >
              Mam już konto
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default RegisterPage
