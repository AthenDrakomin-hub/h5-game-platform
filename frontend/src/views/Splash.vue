<template>
  <div class="splash-page">
    <div class="splash-glow"></div>
    <div class="splash-logo">
      <img src="/logo.svg" alt="NOVA" class="logo-img" />
      <h1 class="logo-text">NOVA</h1>
      <p class="logo-sub">新星娱乐</p>
    </div>
    <div class="splash-loading">
      <img src="/loading.svg" alt="loading" class="loading-icon" />
      <p class="loading-text">正在加载...</p>
    </div>
    <div class="splash-footer">
      <p>安全 · 公平 · 透明</p>
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
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #0A1628;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}
.splash-glow {
  position: absolute;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(0,212,170,0.12), transparent 70%);
  top: -150px;
  left: 50%;
  transform: translateX(-50%);
}
.splash-logo {
  text-align: center;
  margin-bottom: 80px;
  position: relative;
  z-index: 1;
}
.logo-img {
  width: 140px;
  height: auto;
  margin-bottom: 24px;
  animation: logoFadeIn 0.8s ease-out;
}
@keyframes logoFadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}
.logo-text {
  font-size: 42px;
  font-weight: 900;
  color: #FFFFFF;
  margin: 0 0 6px 0;
  letter-spacing: 12px;
  font-family: 'Arial Black', sans-serif;
}
.logo-sub {
  font-size: 16px;
  color: #00D4AA;
  margin: 0;
  letter-spacing: 8px;
}
.splash-loading {
  text-align: center;
  position: relative;
  z-index: 1;
}
.loading-icon {
  width: 40px;
  height: 40px;
  margin-bottom: 12px;
}
.loading-text {
  font-size: 13px;
  color: #6E7681;
  margin: 0;
}
.splash-footer {
  position: absolute;
  bottom: 40px;
  text-align: center;
}
.splash-footer p {
  font-size: 12px;
  color: #4A5568;
  margin: 0;
  letter-spacing: 4px;
}
</style>
