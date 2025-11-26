import { useI18n } from 'vue-i18n'

export function useLanguage() {
  const i18n = useI18n()

  const changeLanguage = (locale: string) => {
    i18n.locale.value = locale
    localStorage.setItem('locale', locale)
  }

  const currentLanguage = () => {
    return i18n.locale.value
  }

  const availableLanguages = ['pl', 'en']

  return {
    changeLanguage,
    currentLanguage,
    availableLanguages,
    i18n
  }
}
