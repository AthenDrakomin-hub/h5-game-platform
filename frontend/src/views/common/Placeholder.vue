<template>
  <div class="placeholder-page">
    <van-nav-bar :title="title" left-arrow @click-left="$router.back()" />
    <div class="placeholder-content">
      <div class="placeholder-icon">{{ icon }}</div>
      <h2 class="placeholder-title">{{ title }}</h2>
      <p class="placeholder-desc">{{ description }}</p>
      <van-button type="primary" class="placeholder-btn" @click="$router.back()">
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
  background: var(--app-bg);
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), var(--card-bg);
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: var(--gold-1) !important;
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
  color: var(--gold-1);
  margin: 0 0 12px 0;
}
.placeholder-desc {
  font-size: 14px;
  color: var(--text-hint);
  margin: 0 0 30px 0;
}
.placeholder-btn {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%) !important;
  border: none !important;
  color: #3a2610 !important;
  font-weight: 600 !important;
  width: 200px;
}
</style>
