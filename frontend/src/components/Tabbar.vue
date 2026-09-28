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
  border-top: 1px solid rgba(212, 168, 75, 0.15);
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
}
.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6px 0 4px;
  cursor: pointer;
  opacity: 0.5;
  transition: opacity 0.2s;
}
.tabbar-item.active {
  opacity: 1;
}
.tabbar-text {
  font-size: 10px;
  color: #6a5a40;
  margin-top: 2px;
}
.tabbar-item.active .tabbar-text {
  color: #d4a84b;
}
</style>
