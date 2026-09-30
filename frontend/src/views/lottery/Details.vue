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
  background: linear-gradient(180deg, var(--app-bg-2) 0%, var(--app-bg) 30%, var(--app-bg) 100%);
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: var(--gold-1) !important;
}
:deep(.van-empty__description) {
  color: #6a5a40;
}
</style>
