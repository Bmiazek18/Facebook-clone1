import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import enTranslation from './i18n/locales/en.json'
import plTranslation from './i18n/locales/pl.json'

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: enTranslation },
      pl: { translation: plTranslation }
    },
    fallbackLng: 'pl',
    interpolation: {
      escapeValue: false
    }
  })

export default i18n
