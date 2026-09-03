export const useSidebar = () => {
  const isExpanded = useState('sidebar_expanded', () => false)

  const toggleSidebar = () => {
    isExpanded.value = !isExpanded.value
  }

  return {
    isExpanded,
    toggleSidebar
  }
}
