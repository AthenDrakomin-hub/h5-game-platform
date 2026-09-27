<template>
  <div class="details-page">
    <van-nav-bar title="投注明细" left-arrow @click-left="$router.back()" />
    <div class="details-content">
      <van-empty description="投注明细功能开发中" />
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.details-page {
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
