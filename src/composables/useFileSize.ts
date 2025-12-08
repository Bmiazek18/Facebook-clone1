
export function useFileSize(fileSize: number) {

  const size = fileSize
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(2)} MB`
  return `${(size / (1024 * 1024 * 1024)).toFixed(2)} GB`

}
