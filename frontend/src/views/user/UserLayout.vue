<template>
  <div class="user-layout">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--app-bg-2) 0%, var(--app-bg) 30%, var(--app-bg) 100%);
}
</style>
