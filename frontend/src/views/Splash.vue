<template>
  <div class="splash-page">
    <div class="splash-logo">
      <div class="logo-icon">🎮</div>
      <h1 class="logo-text">{{ siteName }}</h1>
      <p class="logo-slogan">{{ slogan }}</p>
    </div>
    <div class="splash-loading">
      <van-loading size="24px" color="#e8b860" vertical>
        正在加载...
      </van-loading>
    </div>
    <div class="splash-footer">
      <p>仅供技术学习演示使用</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const appStore = useAppStore()

const siteName = ref('H5 Clone')
const slogan = ref('官方直营 · 信誉首选')

onMounted(async () => {
  try {
    await appStore.loadConfig()
    if (appStore.siteConfig?.siteName) {
      siteName.value = appStore.siteConfig.siteName
    }
    if (appStore.siteConfig?.slogan) {
      slogan.value = appStore.siteConfig.slogan
    }
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
  background: linear-gradient(180deg, #1a1a2e 0%, #0d0d0d 100%);
  padding: 40px 20px;
}
.splash-logo {
  text-align: center;
  margin-bottom: 60px;
}
.logo-icon {
  font-size: 72px;
  margin-bottom: 20px;
  animation: bounce 2s ease-in-out infinite;
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
.logo-text {
  font-size: 28px;
  font-weight: 700;
  color: #e8b860;
  margin: 0 0 8px 0;
  letter-spacing: 2px;
}
.logo-slogan {
  font-size: 14px;
  color: #808080;
  margin: 0;
}
.splash-loading {
  margin-bottom: 40px;
}
.splash-footer {
  position: absolute;
  bottom: 30px;
  text-align: center;
}
.splash-footer p {
  font-size: 12px;
  color: #444;
  margin: 0;
}
</style>
