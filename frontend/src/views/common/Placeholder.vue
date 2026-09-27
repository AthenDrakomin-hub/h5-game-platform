<template>
  <div class="placeholder-page">
    <van-nav-bar :title="title" left-arrow @click-left="$router.back()" />
    <div class="placeholder-content">
      <div class="placeholder-icon">{{ icon }}</div>
      <h2 class="placeholder-title">{{ title }}</h2>
      <p class="placeholder-desc">{{ description }}</p>
      <van-button round type="primary" class="placeholder-btn" @click="$router.back()">
        返回上一页
      </van-button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '页面' },
  icon: { type: String, default: '🚧' },
  description: { type: String, default: '该功能正在开发中，敬请期待' }
})
</script>

<style scoped>
.placeholder-page {
  min-height: 100vh;
  background: #0d0d0d;
}
:deep(.van-nav-bar) {
  background: #1a1a1a;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f0f0f0 !important;
}
.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  text-align: center;
}
.placeholder-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.6;
}
.placeholder-title {
  font-size: 20px;
  color: #f0f0f0;
  margin: 0 0 12px 0;
}
.placeholder-desc {
  font-size: 14px;
  color: #888;
  margin: 0 0 30px 0;
}
.placeholder-btn {
  background: linear-gradient(135deg, #e8b860, #c99a3e) !important;
  border: none !important;
  color: #1a1a1a !important;
  font-weight: 600 !important;
  width: 200px;
}
</style>
