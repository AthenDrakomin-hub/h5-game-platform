<template>
  <div class="not-found-page">
    <div class="nf-content">
      <div class="nf-code">404</div>
      <h1 class="nf-title">页面不存在</h1>
      <p class="nf-desc">您访问的页面可能已被移除或链接错误</p>
      <van-button round type="primary" class="nf-btn" @click="goHome">
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
  background: #0d0d0d;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nf-content {
  text-align: center;
  padding: 40px;
}
.nf-code {
  font-size: 96px;
  font-weight: 900;
  background: linear-gradient(135deg, #e8b860, #c99a3e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 16px;
}
.nf-title {
  font-size: 22px;
  color: #f0f0f0;
  margin: 0 0 10px 0;
}
.nf-desc {
  font-size: 14px;
  color: #888;
  margin: 0 0 30px 0;
}
.nf-btn {
  background: linear-gradient(135deg, #e8b860, #c99a3e) !important;
  border: none !important;
  color: #1a1a1a !important;
  font-weight: 600 !important;
  width: 180px;
}
</style>
