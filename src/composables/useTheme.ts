import { useDark } from '@vueuse/core'

// useDark handles: localStorage sync, DOM class toggle ('dark'), system preference fallback
const isDark = useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: '',
  storageKey: 'theme',
  onChanged: (isDarkMode) => {
    // Sync CSS variables when theme changes
    if (isDarkMode) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }
})

export const useTheme = () => {
  const toggleTheme = () => {
    isDark.value = !isDark.value
  }

  const setTheme = (theme: 'light' | 'dark') => {
    isDark.value = theme === 'dark'
  }

  const getTheme = () => {
    return isDark.value ? 'dark' : 'light'
  }

  return {
    isDark,
    toggleTheme,
    setTheme,
    getTheme,
  }
}
