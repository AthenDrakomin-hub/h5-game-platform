<template>
  <div class="splash-page">
    <img src="/splash.svg" alt="NOVA" class="splash-svg" />
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
  background: #0A1628;
}
.splash-svg {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
