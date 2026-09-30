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
      @click="goTab('/promo')"
    >
      <GoldIcon name="coupon" :size="22" />
      <span class="tabbar-text">优惠</span>
    </div>
    <div
      class="tabbar-item"
      :class="{ active: active === 2 }"
      @click="goLogin"
    >
      <GoldIcon name="user" :size="22" />
      <span class="tabbar-text">{{ isLoggedIn ? '账户' : '登录' }}</span>
    </div>
    <div
      class="tabbar-item"
      :class="{ active: active === 3 }"
      @click="goTab('/chat')"
    >
      <GoldIcon name="headset" :size="22" />
      <span class="tabbar-text">客服</span>
    </div>
    <div
      class="tabbar-item"
      :class="{ active: active === 4 }"
      @click="goTab('/user/center')"
    >
      <GoldIcon name="crown" :size="22" />
      <span class="tabbar-text">会员</span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import GoldIcon from '@/components/GoldIcon.vue'

const route = useRoute()
const router = useRouter()
const active = ref(0)

const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

const pathMap = {
  '/': 0,
  '/promo': 1,
  '/login': 2,
  '/chat': 3,
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

function goLogin() {
  if (isLoggedIn.value) {
    router.push('/user/center')
  } else {
    router.push('/login')
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
  height: 65px;
  padding-top: 4px;
  background: #0d0a06;
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
  font-size: 11px;
  color: rgba(231, 212, 174, 0.68);
  margin-top: 3px;
}
.tabbar-item.active .tabbar-text {
  color: rgb(242, 224, 184);
  font-weight: 600;
}
</style>
