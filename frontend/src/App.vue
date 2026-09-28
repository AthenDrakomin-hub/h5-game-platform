<template>
  <div class="app-root">
    <router-view v-slot="{ Component }">
      <transition :name="transitionName" mode="out-in">
        <keep-alive :include="cachedViews">
          <component :is="Component" :key="$route.path" />
        </keep-alive>
      </transition>
    </router-view>

    <!-- 全局登录弹窗 -->
    <van-popup
      v-model:show="appStore.showAuthPopup"
      position="center"
      round
      :style="{ width: '92%', maxWidth: '380px', borderRadius: '20px' }"
      :close-on-click-overlay="true"
      teleport="#wap-shell"
    >
      <Login
        v-if="appStore.showAuthPopup"
        popup-mode
        :default-tab="appStore.authDefaultTab"
        @close="appStore.closeAuthPopup"
      />
    </van-popup>

    <!-- 底部导航 -->
    <Tabbar v-if="showTabbar" />
  </div>
</template>

<script setup>
import { computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import Tabbar from '@/components/Tabbar.vue'
import Login from '@/views/Login.vue'
import {
  isTelegram,
  showBackButton,
  hideBackButton,
  onThemeChange,
  applyTelegramTheme,
  haptic
} from '@/utils/telegram'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const cachedViews = ['Home', 'Casino', 'Lottery', 'Promo', 'UserCenter']

const transitionName = computed(() => {
  const direction = sessionStorage.getItem('tabbar_slide_direction')
  sessionStorage.removeItem('tabbar_slide_direction')
  return direction === 'left' || direction === 'right' ? `tab-slide-${direction}` : 'fade'
})

const showTabbar = computed(() => {
  if (route.meta?.hideTabbar) return false
  const noTabbarPaths = ['/login', '/register', '/chat']
  if (noTabbarPaths.some(p => route.path.startsWith(p))) return false
  if (appStore.tabbarOverlayDepth > 0) return false
  return true
})

// 全局登录拦截
const AUTH_FREE_SELECTORS = [
  '.login-page',
  '.van-popup',
  '.van-overlay',
  '[data-auth-free-action="trial"]'
]

const PUBLIC_PATHS = ['/login', '/register']

function isAuthFreeTarget(target) {
  if (!target || typeof target.closest !== 'function') return false
  return AUTH_FREE_SELECTORS.some(sel => target.closest(sel))
}

function handleGlobalClick(event) {
  if (authStore.isLoggedIn) return
  if (appStore.showAuthPopup) return
  if (PUBLIC_PATHS.some(p => route.path.startsWith(p))) return
  if (isAuthFreeTarget(event.target)) return
  event.preventDefault()
  event.stopPropagation()
  event.stopImmediatePropagation?.()
  appStore.openAuthPopup('register', route.fullPath)
}

/* ===== Telegram 适配 ===== */

// 需要显示 Telegram BackButton 的页面（非 Tabbar 主页面）
const TG_BACK_PATTERNS = [
  '/lottery/', '/casino/', '/promo/', '/user/', '/chat', '/sponsor'
]

function updateTgBackButton() {
  if (!isTelegram) return
  const isMainTab = ['/', '/home', '/casino', '/lottery', '/promo', '/user'].includes(route.path)
  const needsBack = TG_BACK_PATTERNS.some(p => route.path.startsWith(p)) && !isMainTab
  if (needsBack) {
    showBackButton(() => {
      haptic.selection()
      if (window.history.length > 1) router.back()
      else router.push('/')
    })
  } else {
    hideBackButton()
  }
}

let offThemeChange = null

async function setupTelegram() {
  if (!isTelegram) return
  // 主题变化监听
  offThemeChange = onThemeChange((params) => {
    applyTelegramTheme(params)
  })
  // Telegram 环境自动登录
  if (!authStore.isLoggedIn) {
    await authStore.telegramLogin()
  }
}

onMounted(() => {
  appStore.loadConfig()
  document.addEventListener('click', handleGlobalClick, true)
  setupTelegram()
  updateTgBackButton()
})

onUnmounted(() => {
  document.removeEventListener('click', handleGlobalClick, true)
  if (offThemeChange) offThemeChange()
  if (isTelegram) hideBackButton()
})

watch(() => route.fullPath, () => {
  if (route.meta?.title) {
    document.title = route.meta.title
  }
  updateTgBackButton()
})
</script>

<style scoped>
.app-root {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
}
</style>
