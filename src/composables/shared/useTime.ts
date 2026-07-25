import { ref } from 'vue'

const now = ref(new Date())

let started = false
let timer: ReturnType<typeof setTimeout> | null = null
let interval: ReturnType<typeof setInterval> | null = null

function start() {
  if (started) return
  started = true

  const sync = () => {
    const current = new Date()
    const msUntilNextMinute =
      (60 - current.getSeconds()) * 1000 - current.getMilliseconds()

    timer = setTimeout(() => {
      now.value = new Date()

      interval = setInterval(() => {
        now.value = new Date()
      }, 60000)
    }, msUntilNextMinute + 50)
  }

  sync()
}

export function useNow() {
  start()
  return now
}
