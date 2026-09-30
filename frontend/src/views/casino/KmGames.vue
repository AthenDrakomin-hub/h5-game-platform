<template>
  <div class="km-games-page">
    <van-nav-bar title="KM棋牌" left-arrow @click-left="$router.back()" />
    <div class="km-games-content">
      <van-empty description="KM棋牌游戏列表" />
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.km-games-page {
  min-height: 100vh;
  background: var(--app-bg);
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), var(--card-bg);
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: var(--gold-1) !important;
}
:deep(.van-empty__description) {
  color: #6a5a40;
}
</style>
