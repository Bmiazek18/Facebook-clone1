<script setup>
import { ref } from 'vue';

// --- DANE ---
const totalMembersCount = 64;

const admins = ref([
  {
    id: 100,
    name: 'Maciej Łasocha',
    role: 'Administrator',
    badge: 'Obiecujący współautor',
    subtext: 'PWSZ Biała Podlaska',
    avatar: 'https://via.placeholder.com/150'
  }
]);

const activeMembers = ref([
  {
    id: 1,
    name: 'Bartosz Miazek',
    subtext: 'Zespół Szkół nr 3 im. Władysława Stanisława Reymonta w Łukowie',
    points: 5,
    avatar: 'https://via.placeholder.com/150'
  }
]);

const friendsInGroup = ref([
  { id: 200, name: 'Kuba Trzaskowski', subtext: 'Łuków', avatar: 'https://via.placeholder.com/150' },
  { id: 201, name: 'Bartosz Zabielski', subtext: '', avatar: 'https://via.placeholder.com/150' },
  { id: 202, name: 'Paweł Sitkowski', subtext: 'AWF Biała Podlaska', avatar: 'https://via.placeholder.com/150' }
]);

const suggestedFriends = ref([
  {
    id: 10,
    name: 'Konrad Gąsiorowski',
    mutual: 37,
    mutualDetails: 'Krystian Wojda i Michał Buch',
    extraInfo: 'Pracuje w: Krajowa Administracja Skarbowa',
    avatar: 'https://via.placeholder.com/150'
  },
  {
    id: 11,
    name: 'Sebastian Ignaciuk',
    mutual: 47,
    mutualDetails: 'Marlena Droś-Klamka i Przemysław Wereszczyński',
    extraInfo: 'Łuków',
    avatar: 'https://via.placeholder.com/150'
  },
  {
    id: 12,
    name: 'Adam Buch',
    mutual: 20,
    mutualDetails: 'Michał Buch i Patryk Ptasznik',
    extraInfo: 'Akademia Rolnicza w Lublinie',
    avatar: 'https://via.placeholder.com/150'
  }
]);

const newMembers = ref([
  { id: 30, name: 'Maciek Kucharski', joined: 'około 5 mies. temu', action: null },
  { id: 31, name: 'Maciek Celiński', joined: 'około 5 mies. temu', action: 'message' },
  { id: 32, name: 'Tomek Kobojek', joined: 'około 5 mies. temu', action: 'message' },
  { id: 33, name: 'Kacper Wawryszewicz', joined: 'około 5 mies. temu', extra: 'Terespol', action: 'add' },
  { id: 34, name: 'Antek Nowicki', joined: 'około 5 mies. temu', extra: 'Wojcieszków', action: 'add' }
]);
</script>

<template>
  <div class="min-h-screen bg-theme-bg-secondary text-theme-text p-4 font-sans max-w-[680px] mx-auto">

    <section class="mb-6">
      <div class="flex items-center mb-4">
        <h2 class="text-[20px] font-bold">Członkowie</h2>
        <span class="text-[20px] font-bold text-theme-text-secondary ml-2">· {{ totalMembersCount }}</span>
      </div>
      <p class="text-[15px] text-theme-text-secondary mb-3">
        Tutaj będą widoczne nowe osoby i strony, które dołączą do grupy.
        <span class="text-theme-primary hover:underline cursor-pointer font-semibold">Dowiedz się więcej</span>
      </p>
      <div class="relative group">
        <span class="absolute inset-y-0 left-3 flex items-center text-theme-text-secondary">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
        </span>
        <input
          type="text"
          placeholder="Znajdź członka grupy"
          class="w-full bg-theme-bg-subtle hover:bg-theme-hover transition-colors rounded-full py-2 pl-10 pr-4 text-[15px] focus:outline-none placeholder-theme-text-secondary"
        />
      </div>
    </section>

    <section class="mb-6">
      <div v-for="member in activeMembers" :key="'top-'+member.id" class="flex items-center justify-between py-2 border-t border-theme-border pt-4">
        <div class="flex items-center space-x-3">
          <img :src="member.avatar" class="w-12 h-12 rounded-full object-cover" />
          <div>
            <div class="font-bold text-[15px] hover:underline cursor-pointer">{{ member.name }}</div>
            <div class="text-[13px] text-theme-text-secondary">{{ member.subtext }}</div>
          </div>
        </div>
        <button class="p-2 hover:bg-theme-hover rounded-full text-theme-text-secondary">
          <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20"><path d="M6 10a2 2 0 11-4 0 2 2 0 014 0zM12 10a2 2 0 11-4 0 2 2 0 014 0zM16 12a2 2 0 100-4 2 2 0 000 4z"/></svg>
        </button>
      </div>
    </section>

    <section class="mb-8">
      <h3 class="text-[17px] font-bold mb-4 text-theme-text">Administratorzy i moderatorzy · 1</h3>
      <div v-for="admin in admins" :key="admin.id" class="flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <img :src="admin.avatar" class="w-12 h-12 rounded-full border border-theme-border" />
          <div>
            <div class="font-bold text-[15px] hover:underline cursor-pointer">{{ admin.name }}</div>
            <div class="flex flex-wrap items-center gap-1 mt-0.5">
              <span class="bg-theme-primary-opacity-20 text-theme-primary text-[12px] px-1.5 py-0.5 rounded font-semibold">{{ admin.role }}</span>
              <span class="bg-theme-bg-subtle text-theme-text text-[12px] px-1.5 py-0.5 rounded flex items-center">
                <span class="text-[10px] mr-1">★</span> {{ admin.badge }}
              </span>
            </div>
            <div class="text-[13px] text-theme-text-secondary mt-1">{{ admin.subtext }}</div>
          </div>
        </div>
        <button class="bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text px-3 py-2 rounded-md font-semibold text-[14px] flex items-center whitespace-nowrap">
          <span class="mr-2">👤<span class="ml-[-4px] text-[10px]">×</span></span> Anuluj zaproszenie
        </button>
      </div>
    </section>

    <hr class="border-theme-border my-6" />

    <section class="mb-8">
      <h3 class="text-[17px] font-bold mb-1 text-theme-text">· 1 uczestnik z punktami za wkład w grupę</h3>
      <p class="text-[13px] text-theme-text-secondary mb-4">To są członkowie grupy, którzy zdobyli punkty za wkład w nią, z sortowaniem malejącym.</p>
      <div v-for="member in activeMembers" :key="'points-'+member.id" class="flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <img :src="member.avatar" class="w-12 h-12 rounded-full" />
          <div>
            <div class="font-bold text-[15px] hover:underline cursor-pointer">{{ member.name }}</div>
            <div class="text-[13px] text-theme-text-secondary">{{ member.points }} punktów</div>
          </div>
        </div>
        <button class="p-2 hover:bg-theme-hover rounded-md text-theme-text-secondary">
           <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path d="M6 10a2 2 0 11-4 0 2 2 0 014 0zM12 10a2 2 0 11-4 0 2 2 0 014 0zM16 12a2 2 0 100-4 2 2 0 000 4z"/></svg>
        </button>
      </div>
    </section>

    <hr class="border-theme-border my-6" />

    <section class="mb-8">
      <h3 class="text-[17px] font-bold mb-4 text-theme-text">Znajomi · 34</h3>
      <div class="space-y-4">
        <div v-for="friend in friendsInGroup" :key="friend.id" class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <img :src="friend.avatar" class="w-12 h-12 rounded-full" />
            <div>
              <div class="font-bold text-[15px] hover:underline cursor-pointer">{{ friend.name }}</div>
              <div v-if="friend.subtext" class="text-[13px] text-theme-text-secondary">{{ friend.subtext }}</div>
            </div>
          </div>
          <button class="bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text px-3 py-2 rounded-md font-semibold text-[14px] flex items-center transition">
             <span class="mr-2">💬</span> Wiadomość
          </button>
        </div>
      </div>
      <button class="w-full mt-6 bg-theme-bg-subtle-opacity-60 hover:bg-theme-hover-strong transition py-2 rounded-md font-semibold text-[15px] text-theme-text">
        Wyświetl wszystkich
      </button>
    </section>

    <hr class="border-theme-border my-6" />

    <section class="mb-8">
      <h3 class="text-[17px] font-bold mb-4 text-theme-text">Członkowie, których coś łączy</h3>
      <div class="space-y-6">
        <div v-for="person in suggestedFriends" :key="person.id" class="flex items-start justify-between">
          <div class="flex space-x-3">
            <img :src="person.avatar" class="w-14 h-14 rounded-full border border-theme-border" />
            <div class="max-w-[360px]">
              <div class="font-bold text-[15px] hover:underline cursor-pointer">{{ person.name }}</div>
              <div class="text-[13px] text-theme-text-secondary leading-tight mb-0.5">
                {{ person.mutual }} wspólnych znajomych, w tym {{ person.mutualDetails }}
              </div>
              <div class="text-[13px] text-theme-text-secondary italic">{{ person.extraInfo }}</div>
            </div>
          </div>
          <button class="bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text px-3 py-1.5 rounded-md font-semibold text-[14px] flex items-center shrink-0">
            <span class="mr-1.5 text-lg">👤<span class="ml-[-4px] text-[10px]">+</span></span> Dodaj znajomego
          </button>
        </div>
      </div>
      <button class="w-full mt-6 bg-theme-bg-subtle-opacity-60 hover:bg-theme-hover-strong transition py-2 rounded-md font-semibold text-[15px] text-theme-text">
        Wyświetl wszystkich
      </button>
    </section>

    <hr class="border-theme-border my-6" />

    <section class="mb-8">
      <h3 class="text-[17px] font-bold mb-1 text-theme-text">Nowi członkowie grupy</h3>
      <p class="text-[13px] text-theme-text-secondary mb-4 leading-snug">
        Ta lista obejmuje osoby, które dołączyły do grupy, a także osoby wyświetlające podgląd tej grupy. Każda zaproszona i zatwierdzona osoba może wyświetlać podgląd zawartości grupy.
      </p>

      <div class="space-y-4">
        <div v-for="member in newMembers" :key="member.id" class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <div v-if="!member.extra" class="w-12 h-12 rounded-full bg-theme-bg-subtle flex items-center justify-center overflow-hidden">
               <svg class="w-10 h-10 text-theme-text-secondary mt-2" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"></path></svg>
            </div>
            <img v-else src="https://via.placeholder.com/150" class="w-12 h-12 rounded-full object-cover" />

            <div>
              <div class="font-bold text-[15px] hover:underline cursor-pointer">{{ member.name }}</div>
              <div class="text-[13px] text-theme-text-secondary">Dołączono {{ member.joined }}</div>
              <div v-if="member.extra" class="text-[13px] text-theme-text-secondary">{{ member.extra }}</div>
            </div>
          </div>

          <div class="flex items-center">
            <button v-if="member.action === 'message'" class="bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text px-3 py-1.5 rounded-md font-semibold text-[14px] flex items-center">
              <span class="mr-2">💬</span> Wiadomość
            </button>
            <button v-else-if="member.action === 'add'" class="bg-theme-bg-subtle hover:bg-theme-hover-strong text-theme-text px-3 py-1.5 rounded-md font-semibold text-[14px] flex items-center">
              <span class="mr-1.5 text-lg leading-none">👤<span class="ml-[-4px] text-[10px]">+</span></span> Dodaj znajomego
            </button>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<style>
/* Opcjonalne: wygładzenie scrollbara w Dark Mode */
body {
  background-color: #18191a;
}
</style>
