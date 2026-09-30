<template>
  <div class="not-found-page">
    <div class="nf-content">
      <img src="/404.svg" alt="404" class="nf-img" />
      <h1 class="nf-title">页面走丢了</h1>
      <p class="nf-desc">您访问的页面可能已被移除或链接错误</p>
      <van-button type="primary" class="nf-btn" @click="goHome">
        返回首页
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.replace('/')
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  background: var(--app-bg);
  display: flex;
  align-items: center;
  justify-content: center;
}
.nf-content {
  text-align: center;
  padding: 40px;
}
.nf-img {
  width: 280px;
  height: auto;
  margin-bottom: 20px;
}
.nf-title {
  font-size: 22px;
  color: var(--gold-1);
  margin: 0 0 10px 0;
}
.nf-desc {
  font-size: 14px;
  color: var(--text-hint);
  margin: 0 0 30px 0;
}
.nf-btn {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%) !important;
  border: none !important;
  color: #3a2610 !important;
  font-weight: 600 !important;
  width: 180px;
  border-radius: 10px !important;
}
</style>
