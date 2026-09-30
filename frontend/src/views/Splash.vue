<template>
  <div class="splash-page">
    <div class="splash-content">
      <img src="/assets/logo.png" alt="NOVA" class="splash-logo" />
      <div class="splash-loading">
        <div class="loading-dot"></div>
        <div class="loading-dot"></div>
        <div class="loading-dot"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const appStore = useAppStore()

onMounted(async () => {
  try {
    await appStore.loadConfig()
  } catch (e) {
    // 静默失败
  }
  setTimeout(() => {
    router.replace('/')
  }, 1500)
})
</script>

<style scoped>
.splash-page {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(180deg, var(--app-bg-2) 0%, var(--app-bg) 50%, var(--app-bg) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.splash-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
}
.splash-logo {
  width: 180px;
  height: auto;
  object-fit: contain;
  animation: logoFadeIn 0.8s ease-out;
}
.splash-loading {
  display: flex;
  gap: 8px;
}
.loading-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(180deg, #f0d080, #d4a84b);
  animation: dotBounce 1.2s ease-in-out infinite;
}
.loading-dot:nth-child(2) {
  animation-delay: 0.15s;
}
.loading-dot:nth-child(3) {
  animation-delay: 0.3s;
}
@keyframes logoFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
@keyframes dotBounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}
</style>
