<script setup lang="ts">
import { useVisitorData } from '@fingerprint/vue'
import { onMounted } from 'vue'

const { getData } = useVisitorData({ immediate: false })

onMounted(async () => {
  try {
    const result = await getData()
    console.log('Dane Fingerprint pobrane:', result)

    if (result && result.visitor_id) {
      document.cookie = `visitorId=${result.visitor_id}; max-age=31536000; path=/; SameSite=Lax`
      console.log('Zapisano Fingerprint ID w cookie:', result.visitor_id)
    }
  } catch (error) {
    console.error('Błąd podczas pobierania danych Fingerprint:', error)
  }
})
</script>

<template>
  <div style="display: none"></div>
</template>
