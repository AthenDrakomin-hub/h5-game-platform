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
  background: #0d0d0d;
}
:deep(.van-nav-bar) {
  background: #1a1a1a;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f0f0f0 !important;
}
:deep(.van-empty__description) {
  color: #666;
}
</style>
