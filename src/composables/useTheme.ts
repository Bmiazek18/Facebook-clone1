import { useColorMode } from '@vueuse/core'

export const useTheme = () => {
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

  return {
    mode ,
    isDark: mode == 'dark' // Zwracamy bezpośrednio WritableComputedRef
  }
}
