<template>
  <div class="custom-tabbar">
    <div
      class="tabbar-item"
      :class="{ active: active === 0 }"
      @click="goTab('/')"
    >
      <GoldIcon name="home" :size="22" />
      <span class="tabbar-text">首页</span>
    </div>
    <div
      class="tabbar-item"
      :class="{ active: active === 1 }"
      @click="goTab('/lottery')"
    >
      <GoldIcon name="lottery" :size="22" />
      <span class="tabbar-text">彩票</span>
    </div>
    <div
      class="tabbar-item"
      :class="{ active: active === 2 }"
      @click="goTab('/casino')"
    >
      <GoldIcon name="casino" :size="22" />
      <span class="tabbar-text">娱乐城</span>
    </div>
    <div
      class="tabbar-item"
      :class="{ active: active === 3 }"
      @click="goTab('/promo')"
    >
      <GoldIcon name="coupon" :size="22" />
      <span class="tabbar-text">优惠</span>
    </div>
    <div
      class="tabbar-item"
      :class="{ active: active === 4 }"
      @click="goTab('/user/center')"
    >
      <GoldIcon name="user" :size="22" />
      <span class="tabbar-text">我的</span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import GoldIcon from '@/components/GoldIcon.vue'

const route = useRoute()
const router = useRouter()
const active = ref(0)

const pathMap = {
  '/': 0,
  '/lottery': 1,
  '/casino': 2,
  '/promo': 3,
  '/user/center': 4
}

watch(() => route.path, (path) => {
  active.value = pathMap[path] ?? 0
}, { immediate: true })

function goTab(path) {
  if (route.path !== path) {
    router.push(path)
  }
}
</script>

<style scoped>
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: linear-gradient(180deg, #1a130a 0%, #0d0a06 100%);
  border-top: 1px solid rgba(255,255,255,0.08);
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
}
.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 0 6px;
  cursor: pointer;
  opacity: 0.5;
  transition: all 0.2s;
  position: relative;
}
.tabbar-item.active {
  opacity: 1;
}
.tabbar-item.active::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 2px;
  background: linear-gradient(90deg, #f0d080, #d4a84b);
  border-radius: 0 0 2px 2px;
}
.tabbar-item.active :deep(.gold-icon) {
  filter: drop-shadow(0 0 8px rgba(240,208,128,0.6));
  transform: translateY(-1px);
}
.tabbar-text {
  font-size: 10px;
  color: #8a7a5a;
  margin-top: 3px;
}
.tabbar-item.active .tabbar-text {
  color: #f0d080;
  font-weight: 600;
}
</style>
