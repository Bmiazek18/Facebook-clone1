<script setup lang="ts">
import { ref, computed, watch, nextTick, onBeforeUnmount, onMounted } from 'vue'

// Import Leaflet i jego styli (wymagane do poprawnego działania mapy)
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'

const emit = defineEmits(['close'])

type ViewState = 'main' | 'all-alerts' | 'logins' | 'login-info'
const currentView = ref<ViewState>('main')
const selectedLogin = ref<any>(null)

// Referencja do kontenera mapy i instancja Leaflet
const mapContainer = ref<HTMLElement | null>(null)
let mapInstance: L.Map | null = null

// --- DYNAMIC DATA FROM KEYCLOAK ---
const logins = ref<any[]>([])

const alerts = computed(() => {
  return logins.value.map((l, index) => {
    return {
      id: index + 1,
      title: `Nowe logowanie · ${l.title.replace('Facebook · ', '')}`,
      subtitle: `${l.keyRegistered || '8 min temu'} · ${l.location}`,
      icon: l.icon
    }
  })
})

const fetchLogins = async () => {
  try {
    const data = await $fetch<any[]>('/api/auth/sessions')
    logins.value = data
  } catch (err) {
    console.error('Failed to fetch sessions from Keycloak:', err)
  }
}

onMounted(() => {
  fetchLogins()
})

// --- FUNKCJE NAWIGACJI ---
function close() {
  emit('close')
}

function goBack() {
  if (currentView.value === 'login-info') {
    currentView.value = 'logins'
  } else {
    currentView.value = 'main'
  }
}

function openView(view: ViewState) {
  currentView.value = view
}

function openLoginInfo(login: any) {
  selectedLogin.value = login
  currentView.value = 'login-info'
}

const modalTitle = computed(() => {
  switch (currentView.value) {
    case 'all-alerts': return 'Wszystkie alerty zabezpieczeń'
    case 'logins': return 'Logowania'
    case 'login-info': return 'Informacje o logowaniu'
    default: return 'Alerty zabezpieczeń'
  }
})

// --- LEAFLET MAP LOGIC ---
function initMap() {
  if (!mapContainer.value || !selectedLogin.value) return

  const { lat, lng, title, location, subtitle } = selectedLogin.value
  const displayLocation = location || subtitle?.split(' · ')[1] || 'Nieznana lokalizacja'

  // Ustawienie mapy
  mapInstance = L.map(mapContainer.value, {
    zoomControl: false, // wyłączamy kontrolki zoomu, żeby mapa wyglądała na zintegrowaną
    attributionControl: false
  }).setView([lat, lng], 13)

  // Kafelki OpenStreetMap
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(mapInstance)

  // Niestandardowy marker z użyciem HTML-a z Tailwindem (odwzorowanie wcześniejszego Tooltipa)
  const customIcon = L.divIcon({
    className: 'bg-transparent border-none', // reset domyślnych styli markera
    html: `
      <div class="relative bg-white px-3 py-2 rounded-lg shadow-xl flex flex-col items-center w-max -translate-x-1/2 -translate-y-[110%] pointer-events-none">
        <span class="text-[14px] font-bold text-black font-sans">${title}</span>
        <span class="text-[12px] text-gray-500 font-sans">${displayLocation}</span>
        <div class="absolute -bottom-2 w-0 h-0 border-[6px] border-transparent border-t-white"></div>
      </div>
    `,
    iconSize: [0, 0] // Pozwalamy HTML-owi zajmować tyle miejsca ile potrzebuje
  })

  L.marker([lat, lng], { icon: customIcon }).addTo(mapInstance)
}

function destroyMap() {
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
}

// Obserwujemy zmianę widoku - kiedy wchodzimy w 'login-info', budujemy mapę
watch([currentView, selectedLogin], async ([newView, newLogin]) => {
  if (newView === 'login-info' && newLogin) {
    await nextTick() // czekamy na renderowanie DOM elementu mapContainer
    initMap()
  } else {
    destroyMap()
  }
})

onBeforeUnmount(() => {
  destroyMap()
})
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4 font-sans text-black">
    <div class="w-full max-w-[480px] bg-white rounded-[20px] shadow-2xl flex flex-col overflow-hidden max-h-[90vh]">

      <!-- Nagłówek Modala -->
      <div class="relative flex items-center justify-center px-4 py-3.5 border-b border-gray-200 shrink-0">
        <button v-if="currentView !== 'main'" @click="goBack" class="absolute left-4 w-9 h-9 bg-gray-100 hover:bg-gray-200 rounded-full flex items-center justify-center transition-colors z-10">
          <svg class="w-5 h-5 text-gray-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h2 class="text-[17px] font-bold">{{ modalTitle }}</h2>
        <button @click="close" class="absolute right-4 w-9 h-9 bg-gray-100 hover:bg-gray-200 rounded-full flex items-center justify-center transition-colors z-10">
          <svg class="w-5 h-5 text-gray-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- ZAWARTOSĆ SCROLLOWANA -->
      <div class="overflow-y-auto flex-1 pb-4 relative">

        <template v-if="currentView === 'main'">
          <!-- ...kod widoku main (identyczny jak wcześniej)... -->
          <div class="px-5 py-4 text-[15px] text-gray-600 border-b border-gray-200 leading-snug">
            Możesz wylogować się ze wszystkich nierozpoznanych urządzeń i porównać klucze, gdy zostaną zmienione, aby zapewnić bezpieczeństwo swoich czatów.
            <a href="#" class="text-[#1A73E8] hover:underline font-medium">Dowiedz się więcej</a>
          </div>
          <button @click="openView('logins')" class="w-full flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors border-b border-gray-200">
            <span class="text-[16px] font-bold">Zobacz logowania</span>
            <svg class="w-5 h-5 text-black" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" /></svg>
          </button>
          <button class="w-full flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors border-b border-gray-200">
            <span class="text-[16px] font-bold">Zarządzaj alertami zabezpieczeń</span>
            <svg class="w-5 h-5 text-black" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" /></svg>
          </button>
          <div class="px-5 pt-4 pb-2 text-[15px] font-semibold text-gray-600">Nowe alerty zabezpieczeń</div>
          <div class="flex flex-col">
            <button v-for="alert in alerts.slice(0, 4)" :key="alert.id" class="w-full flex items-center gap-4 px-5 py-3 hover:bg-gray-50 transition-colors text-left">
              <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center shrink-0">
                <svg v-if="alert.icon === 'facebook'" class="w-6 h-6 text-black" fill="currentColor" viewBox="0 0 24 24"><path d="M14 13.5h2.5l1-4H14v-2c0-1.03 0-2 2-2h1.5V2.14c-.326-.043-1.557-.14-2.857-.14-2.89 0-4.643 1.745-4.643 4.93V9.5H8v4h2v9h4v-9z"/></svg>
                <svg v-else class="w-5 h-5 text-black" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.477 2 2 6.14 2 11.25c0 2.9 1.48 5.48 3.8 7.15V22l3.47-1.92c.87.24 1.79.37 2.73.37 5.523 0 10-4.14 10-9.25S17.523 2 12 2zm1.09 12.41l-2.8-3-5.46 3 5.97-6.32 2.8 3 5.46-3-5.97 6.32z"/></svg>
              </div>
              <div class="flex-1 flex flex-col overflow-hidden">
                <span class="text-[16px] font-bold truncate">{{ alert.title }}</span>
                <span class="text-[14px] text-gray-500 truncate">{{ alert.subtitle }}</span>
              </div>
              <svg class="w-6 h-6 text-black shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" /></svg>
            </button>
          </div>
          <div class="px-5 mt-2">
            <button @click="openView('all-alerts')" class="w-full flex items-center justify-between py-4 hover:bg-gray-50 transition-colors border-t border-gray-200">
              <span class="text-[16px] font-bold">Wyświetl wszystkie alerty zabezpieczeń</span>
              <svg class="w-5 h-5 text-black" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" /></svg>
            </button>
          </div>
        </template>

        <template v-else-if="currentView === 'all-alerts'">
          <!-- ...kod widoku all-alerts (identyczny jak wcześniej)... -->
          <div class="flex flex-col pt-2">
            <button v-for="alert in alerts" :key="'all-'+alert.id" class="w-full flex items-center gap-4 px-5 py-3 hover:bg-gray-50 transition-colors text-left">
              <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center shrink-0">
                <svg v-if="alert.icon === 'facebook'" class="w-6 h-6 text-black" fill="currentColor" viewBox="0 0 24 24"><path d="M14 13.5h2.5l1-4H14v-2c0-1.03 0-2 2-2h1.5V2.14c-.326-.043-1.557-.14-2.857-.14-2.89 0-4.643 1.745-4.643 4.93V9.5H8v4h2v9h4v-9z"/></svg>
                <svg v-else class="w-5 h-5 text-black" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.477 2 2 6.14 2 11.25c0 2.9 1.48 5.48 3.8 7.15V22l3.47-1.92c.87.24 1.79.37 2.73.37 5.523 0 10-4.14 10-9.25S17.523 2 12 2zm1.09 12.41l-2.8-3-5.46 3 5.97-6.32 2.8 3 5.46-3-5.97 6.32z"/></svg>
              </div>
              <div class="flex-1 flex flex-col overflow-hidden">
                <span class="text-[16px] font-bold truncate">{{ alert.title }}</span>
                <span class="text-[14px] text-gray-500 truncate">{{ alert.subtitle }}</span>
              </div>
              <svg class="w-6 h-6 text-black shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" /></svg>
            </button>
          </div>
          <p class="px-5 py-4 text-[14px] text-gray-500 text-center">Możesz sprawdzić do 100 alertów zabezpieczeń z ostatnich 30 dni.</p>
        </template>

        <template v-else-if="currentView === 'logins'">
          <!-- ...kod widoku logins (identyczny jak wcześniej)... -->
          <div class="px-5 py-4 text-[15px] text-gray-600 border-b border-gray-200 leading-snug">
            W ramach tych logowań mogą być wysyłane i odbierane w pełni zaszyfrowane wiadomości oraz połączenia. Przejdź do ustawień Facebooka, aby wyświetlić pełną listę
            <a href="#" class="text-[#1A73E8] hover:underline font-medium">lokalizacji zalogowań.</a>
          </div>
          <div class="flex flex-col pt-2">
            <button v-for="login in logins" :key="'login-'+login.id" @click="openLoginInfo(login)" class="w-full flex items-center gap-4 px-5 py-3 hover:bg-gray-50 transition-colors text-left">
              <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center shrink-0">
                <svg v-if="login.icon === 'facebook'" class="w-6 h-6 text-black" fill="currentColor" viewBox="0 0 24 24"><path d="M14 13.5h2.5l1-4H14v-2c0-1.03 0-2 2-2h1.5V2.14c-.326-.043-1.557-.14-2.857-.14-2.89 0-4.643 1.745-4.643 4.93V9.5H8v4h2v9h4v-9z"/></svg>
                <svg v-else class="w-5 h-5 text-black" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.477 2 2 6.14 2 11.25c0 2.9 1.48 5.48 3.8 7.15V22l3.47-1.92c.87.24 1.79.37 2.73.37 5.523 0 10-4.14 10-9.25S17.523 2 12 2zm1.09 12.41l-2.8-3-5.46 3 5.97-6.32 2.8 3 5.46-3-5.97 6.32z"/></svg>
              </div>
              <div class="flex-1 flex flex-col overflow-hidden">
                <span class="text-[16px] font-bold truncate">{{ login.title }}</span>
                <span class="text-[14px] text-gray-500 truncate mt-0.5">
                  <span v-if="login.isCurrent" class="text-green-600 font-medium">Ta sesja</span>
                  <span v-if="login.isCurrent"> · {{ login.location }}</span>
                  <span v-else>{{ login.subtitle }}</span>
                </span>
              </div>
              <svg class="w-6 h-6 text-black shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" /></svg>
            </button>
          </div>
        </template>

        <template v-else-if="currentView === 'login-info' && selectedLogin">
          <div class="px-5 pt-4 flex flex-col gap-5">

            <!-- PRAWDZIWA MAPA LEAFLET -->
            <div
              ref="mapContainer"
              class="w-full h-[220px] bg-gray-200 rounded-xl relative overflow-hidden flex items-center justify-center shadow-inner z-0"
            >
              <!-- Tutaj Leaflet wstawi Canvas mapy -->
            </div>

            <!-- Widziano -->
            <div>
              <p class="text-[15px] text-gray-600 mb-2">Widziano</p>
              <div class="bg-gray-100/80 px-4 py-3.5 rounded-[12px] text-[15px] font-medium text-black">
                {{ selectedLogin.seen || 'około minuty temu' }}
              </div>
            </div>

            <!-- Klucz -->
            <div class="pb-6">
              <p class="text-[15px] text-gray-600 mb-2">Klucz</p>
              <div class="bg-gray-100/80 px-4 py-3.5 rounded-[12px]">
                <p class="text-[14.5px] font-bold text-black tracking-wider leading-relaxed break-words font-mono">
                  {{ selectedLogin.key || '05 68 45 2E 48 73 D7 CC CE 4D 86 5D DB A5 0F 0D C3 B8 19 6A 16 AC 18 B8 72 E3 7F 44 4E B6 A7 F1 7F' }}
                </p>
                <div class="mt-2 text-[14px] text-gray-500">
                  Zarejestrowano:<br/>
                  {{ selectedLogin.keyRegistered || '11 min temu' }}
                </div>
              </div>
            </div>

          </div>
        </template>

      </div>
    </div>
  </div>
</template>

<style>
/* Reset dla Leafleta, by marker tworzony z divIcon był neutralny i nie dostawał białego kwadratu z borderem */
.leaflet-div-icon {
  background: transparent;
  border: none;
}
</style>
