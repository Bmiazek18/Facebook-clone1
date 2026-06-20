<template>
  <section class="login-hero relative flex flex-col justify-center items-center p-6 lg:p-0 bg-white">
    <div class="absolute top-8 left-8 lg:top-10 lg:left-12 z-20">
      <FacebookIcon :size="48" class="text-[#1877F2]" />
    </div>

    <div class="absolute bottom-12 left-8 lg:bottom-20 lg:left-16 z-20 text-left">
      <h1 class="text-4xl md:text-5xl lg:text-[56px] font-bold tracking-tight text-slate-900 leading-[1.15]">
        Przeglądaj,<br />
        to, co <span class="text-[#1877F2]">lubisz.</span>
      </h1>
    </div>

    <div class="login-hero-canvas relative shrink-0 select-none">
      <div class="absolute inset-0 rounded-4xl overflow-hidden shadow-sm border border-slate-100 bg-slate-200 z-10">
        <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80" alt="Main User" class="w-full h-full object-cover" />
        <div class="absolute bottom-16 left-1/2 -translate-x-1/2 flex items-center gap-2 z-20">
          <div class="w-16 h-2 bg-white/40 border border-white rounded-full"></div>
          <div class="w-4 h-4 border-2 border-white rounded-full"></div>
          <div class="w-4 h-4 border-2 border-white rounded-full"></div>
        </div>
      </div>

      <div class="absolute top-10 -right-6 lg:-right-10 bg-[#7C3AED] text-white text-[13px] font-semibold px-3 py-1.5 rounded-full flex items-center gap-1.5 shadow-xl z-20">
        <ClockOutlineIcon :size="16" /> 16:45
      </div>

      <div class="login-hero-card absolute top-16 -left-16 lg:-left-24 rounded-3xl bg-white p-2 shadow-2xl border border-slate-100 z-20">
        <div class="relative w-full h-full rounded-xl overflow-hidden">
          <img src="https://images.unsplash.com/photo-1463936575829-25148e1db1b8?w=300&auto=format&fit=crop&q=80" alt="Plant" class="w-full h-full object-cover" />
          <div class="absolute top-2 left-2 bg-black/40 text-white p-1 rounded-lg backdrop-blur-sm">
            <HomeIcon :size="14" />
          </div>
        </div>
      </div>

      <div class="absolute -top-4 left-0 lg:-left-4 text-5xl transform -rotate-12 drop-shadow-lg z-20 animate-bounce-slow">😆</div>

      <div class="absolute bottom-28 -right-6 lg:-right-8 bg-[#FF246A] text-white p-3 rounded-full shadow-2xl z-20 transform scale-110">
        <HeartIcon :size="28" />
      </div>

      <div class="absolute bottom-6 -left-12 lg:-left-20 w-44 lg:w-48 bg-white rounded-2xl shadow-2xl p-2 border border-slate-100 z-0">
        <div class="relative w-full h-28 lg:h-32 rounded-xl overflow-hidden mb-3">
          <img src="https://images.unsplash.com/photo-1564982009877-1c650907536a?w=300&auto=format&fit=crop&q=80" alt="Skatepark" class="w-full h-full object-cover" />
          <div class="absolute top-2 left-2 bg-[#1877F2] text-white p-1 rounded-md">
            <StarIcon :size="14" />
          </div>
        </div>
        <div class="px-2 pb-2 space-y-2">
          <div class="h-1.5 bg-slate-100 rounded-full w-full"></div>
          <div class="h-1.5 bg-slate-100 rounded-full w-2/3"></div>
          <div class="h-1.5 bg-slate-100 rounded-full w-4/5"></div>
        </div>
      </div>

      <div class="login-hero-avatar-wrap absolute -bottom-10 left-1/2 -translate-x-1/2 rounded-full border-4 border-[#1877F2] bg-white shadow-2xl z-30">
        <img src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80" alt="Profile" class="w-full h-full object-cover rounded-full" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import FacebookIcon from 'vue-material-design-icons/Facebook.vue';
import ClockOutlineIcon from 'vue-material-design-icons/ClockOutline.vue';
import HomeIcon from 'vue-material-design-icons/Home.vue';
import StarIcon from 'vue-material-design-icons/Star.vue';
import HeartIcon from 'vue-material-design-icons/Heart.vue';
</script>

<style scoped>
@keyframes bounce-slow {
  0%, 100% { transform: translateY(0) rotate(-12deg); }
  50% { transform: translateY(-8px) rotate(-12deg); }
}
.animate-bounce-slow { animation: bounce-slow 3s infinite ease-in-out; }

.login-hero {
  min-height: 600px;
}

.login-hero-canvas {
  width: 280px;
  height: 400px;
  margin-top: -80px;
}

@media (min-width: 1024px) {
  .login-hero-canvas {
    width: 320px;
    height: 460px;
    margin-top: 0;
  }
}

.login-hero-card {
  width: 144px;
  height: 192px;
}

@media (min-width: 1024px) {
  .login-hero-card {
    width: 160px;
    height: 224px;
  }
}

.login-hero-avatar-wrap {
  width: 96px;
  height: 96px;
  padding: 2px;
}

@media (min-width: 1024px) {
  .login-hero-avatar-wrap {
    width: 112px;
    height: 112px;
  }
}
</style>
