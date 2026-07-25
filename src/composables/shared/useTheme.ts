import { useColorMode } from '@vueuse/core'
import { computed } from 'vue'

const mode = useColorMode({
  selector: 'html',
  attribute: 'class',
  storageKey: 'theme',
  emitAuto: true, // Ważne: upewnij się, że to pasuje do skryptu w head
  modes: {
    dark: 'dark',
    light: '',
  },
})

export const useTheme = () => {
  return {
    mode,
    isDark: computed(() => mode.value === 'dark'),
  }
}
