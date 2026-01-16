<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import { useEventsStore } from '@/stores/events';
import type { Event as EventType } from '@/data/events'; // Import Event interface with alias
import ImageWithGradient from '@/components/ImageWithGradient.vue';
import EventsSidebar from '@/components/events/EventsSidebar.vue';
// --- LEAFLET IMPORTS (Czysty Leaflet) ---
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Fix dla ikon (Standardowy problem w Vue/Vite)
import iconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png';
import iconUrl from 'leaflet/dist/images/marker-icon.png';
import shadowUrl from 'leaflet/dist/images/marker-shadow.png';

// Reset domyślnych ikon
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
});

// Import Icons (Material Design)
import CalendarMonthIcon from 'vue-material-design-icons/CalendarMonth.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import StarIcon from 'vue-material-design-icons/Star.vue';
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue';
import EmailIcon from 'vue-material-design-icons/Email.vue';
import ShareVariantIcon from 'vue-material-design-icons/ShareVariant.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import InformationIcon from 'vue-material-design-icons/Information.vue';

// --- KONFIGURACJA MAPY ---
const mapContainerRef = ref<HTMLElement | null>(null);
let mapInstance: L.Map | null = null;
const mapCenter = [54.371661, 18.619082]; // Gdańsk - default if no event location

// Definicja Czerwonej Ikony
const redIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

// --- LOGIKA "FACEBOOK DUAL STICKY" ---
const rightSectionRef = ref<HTMLElement | null>(null);
const stickyTop = ref(0); // Dynamiczna wartość CSS 'top'

// Konfiguracja marginesów
const HEADER_OFFSET = 56; // Wysokość nawigacji
const BOTTOM_OFFSET = 16;  // Margines od dołu ekranu

let lastScrollY = window.scrollY;

const updateStickyPosition = () => {
    if (!rightSectionRef.value) return;

    const currentScrollY = window.scrollY;
    const scrollDiff = currentScrollY - lastScrollY;

    const viewportHeight = window.innerHeight;
    const sidebarHeight = rightSectionRef.value.offsetHeight;

    if (sidebarHeight + HEADER_OFFSET + BOTTOM_OFFSET < viewportHeight) {
        stickyTop.value = HEADER_OFFSET;
        lastScrollY = currentScrollY;
        return;
    }

    let newTop = stickyTop.value - scrollDiff;

    const maxTop = HEADER_OFFSET;
    const minTop = viewportHeight - sidebarHeight - BOTTOM_OFFSET;

    if (newTop > maxTop) {
        newTop = maxTop;
    } else if (newTop < minTop) {
        newTop = minTop;
    }

    stickyTop.value = newTop;
    lastScrollY = currentScrollY;
};

let resizeObserver: ResizeObserver | null = null;

const route = useRoute();
const eventsStore = useEventsStore();

const event = ref<EventType | undefined>(undefined);

const eventId = computed(() => route.params.id as string);

onMounted(() => {
  event.value = eventsStore.getEventById(eventId.value);

    // Inicjalizacja pozycji
    stickyTop.value = HEADER_OFFSET;
    lastScrollY = window.scrollY;

    window.addEventListener('scroll', updateStickyPosition, { passive: true });
    window.addEventListener('resize', updateStickyPosition);

    if (rightSectionRef.value) {
        resizeObserver = new ResizeObserver(() => {
            updateStickyPosition();
        });
        resizeObserver.observe(rightSectionRef.value);
    }
  if (mapContainerRef.value && event.value && event.value.location) {
    // For now, using a fixed mapCenter. Geocoding would be needed for dynamic locations.
    mapInstance = L.map(mapContainerRef.value, {
      center: mapCenter,
      zoom: 15,
      zoomControl: false,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      touchZoom: false,
      boxZoom: false,
      keyboard: false,
      dragging: false
    });

    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
      maxZoom: 20
    }).addTo(mapInstance);

    L.marker(mapCenter, { icon: redIcon })
      .addTo(mapInstance)
      .bindTooltip(event.value.location, { direction: 'top', offset: [0, -40] });

    L.control.zoom({ position: 'bottomright' }).addTo(mapInstance);
  }
});

onUnmounted(() => {
    window.removeEventListener('scroll', updateStickyPosition);
    window.removeEventListener('resize', updateStickyPosition);
    if (resizeObserver) resizeObserver.disconnect();
  if (mapInstance) {
    mapInstance.remove();
    mapInstance = null;
  }
});

// --- Computed properties for event display ---
const formattedMonth = computed(() => {
  if (!event.value?.startDate) return '--';
  return new Date(event.value.startDate).toLocaleDateString('pl-PL', { month: 'short' }).toUpperCase();
});

const formattedDay = computed(() => {
  if (!event.value?.startDate) return '--';
  return new Date(event.value.startDate).getDate();
});

const formattedDateFull = computed(() => {
  if (!event.value?.startDate) return '--';
  return new Date(event.value.startDate).toLocaleDateString('pl-PL', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
});

const friends = [
  { name: 'Mikołaj Niedziela', avatar: 'https://i.pravatar.cc/150?u=1' },
  { name: 'Wojtek Piotrowski', avatar: 'https://i.pravatar.cc/150?u=2' },
  { name: 'Magda Chłopecka', avatar: 'https://i.pravatar.cc/150?u=3' },
];

const organizers = [
  { name: 'Technikalia', type: 'Wydarzenie', role: '92 minionych wydarzeń', logo: 'https://placehold.co/100x100/1e293b/FFF?text=T.26' },
  { name: 'Politechnika Gdańska', type: 'Szkoła wyższa', role: '361 minionych wydarzeń', logo: 'https://placehold.co/100x100/white/000?text=PG' },
];
</script>
