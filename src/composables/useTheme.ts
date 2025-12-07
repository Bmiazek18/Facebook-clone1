import { useDark, usePreferredDark } from '@vueuse/core'
import { computed } from 'vue'

const THEME_STORAGE_KEY = 'theme'

const isDark = useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: '',
  storageKey: THEME_STORAGE_KEY,
})

const preferredDark = usePreferredDark()

export const useTheme = () => {
  // 1. Określenie stanu motywu
  const themeState = computed<'light' | 'dark' | 'system'>(() => {
    const storedValue = typeof localStorage !== 'undefined' ? localStorage.getItem(THEME_STORAGE_KEY) : null
    console.log(storedValue)
    if (storedValue === 'dark') return 'dark'
    if (storedValue === 'light') return 'light'
    return 'system' // jeśli 'system' lub brak wartości
  })

  // 2. Określenie aktywnego motywu
  const activeTheme = computed<'light' | 'dark'>(() => {
    if (themeState.value === 'system') {
      return preferredDark.value ? 'dark' : 'light'
    }
    return themeState.value
  })

  // 3. Ustawianie motywu
  const setTheme = (theme: 'light' | 'dark' | 'system') => {
    if (theme === 'dark') {
      isDark.value = true
      localStorage.setItem(THEME_STORAGE_KEY, 'dark')
    } else if (theme === 'light') {
      isDark.value = false
      localStorage.setItem(THEME_STORAGE_KEY, 'light')
    } else if (theme === 'system') {
      // Zapisujemy 'system' w localStorage zamiast usuwać klucz
      localStorage.setItem(THEME_STORAGE_KEY, 'system')
      isDark.value = preferredDark.value
    }
  }

  // 4. Przełączanie motywu
  const toggleTheme = () => {
    if (themeState.value === 'system' || themeState.value === 'light') {
      setTheme('dark')
    } else {
      setTheme('light')
    }
  }

  return {
    isDark,
    themeState,
    activeTheme,
    toggleTheme,
    setTheme,
  }
}
