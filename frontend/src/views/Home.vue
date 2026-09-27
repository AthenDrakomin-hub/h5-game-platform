<template>
  <div class="home-page">
    <!-- 顶部导航 -->
    <div class="home-header">
      <div class="header-left">
        <span class="header-logo">🎮</span>
        <span class="header-title">{{ siteName }}</span>
      </div>
      <div class="header-right">
        <van-icon name="chat-o" size="20" @click="goChat" />
      </div>
    </div>

    <!-- 公告滚动 -->
    <div v-if="notice" class="home-notice">
      <van-icon name="volume-o" color="#e8b860" />
      <van-notice-bar
        :text="notice.title + '：' + notice.content"
        color="#b0b0b0"
        background="transparent"
        scrollable
      />
    </div>

    <!-- Banner 轮播 -->
    <div class="home-banner">
      <van-swipe :autoplay="3000" indicator-color="#e8b860">
        <van-swipe-item v-for="banner in banners" :key="banner.id" @click="goLink(banner.link)">
          <img :src="banner.image" :alt="banner.title" class="banner-img" />
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 快捷入口 -->
    <div class="home-quick-nav">
      <div
        v-for="item in quickNavs"
        :key="item.id"
        class="quick-nav-item"
        @click="goQuickNav(item)"
      >
        <div class="quick-nav-icon">
          <img :src="item.icon" :alt="item.name" class="icon-img" />
        </div>
        <span class="quick-nav-name">{{ item.name }}</span>
      </div>
    </div>

    <!-- 游戏入口 -->
    <div class="home-section">
      <div class="section-title">
        <span class="title-bar"></span>
        <span class="title-text">热门游戏</span>
        <span class="title-more" @click="$router.push('/lottery')">更多 ›</span>
      </div>
      <div class="game-grid">
        <div
          v-for="game in gameEntries"
          :key="game.id"
          class="game-card"
          @click="goGame(game)"
        >
          <div class="game-icon">
            <img v-if="game.icon" :src="game.icon" :alt="game.name" class="game-icon-img" />
            <span v-else>{{ game.name }}</span>
          </div>
          <span class="game-name">{{ game.name }}</span>
        </div>
      </div>
    </div>

    <!-- 优惠活动 -->
    <div class="home-section">
      <div class="section-title">
        <span class="title-bar"></span>
        <span class="title-text">优惠活动</span>
        <span class="title-more" @click="$router.push('/promo')">更多 ›</span>
      </div>
      <div class="promo-list">
        <div
          v-for="promo in promos"
          :key="promo.id"
          class="promo-card"
          @click="goPromoDetail(promo.id)"
        >
          <img :src="promo.image" :alt="promo.title" class="promo-img" />
          <div class="promo-info">
            <span class="promo-title">{{ promo.title }}</span>
            <span class="promo-tag">立即参与</span>
          </div>
        </div>
      </div>
    </div>

    <div class="home-footer">
      <p>本平台仅供技术学习演示使用</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { homeApi } from '@/api/home'
import { promoApi } from '@/api/promo'
import { showToast } from 'vant'

const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const siteName = ref('H5 Clone')
const notice = ref(null)
const banners = ref([])
const quickNavs = ref([])
const gameEntries = ref([])
const promos = ref([])

async function loadData() {
  try {
    const [bannerData, navData, gameData, noticeData, promoData] = await Promise.all([
      homeApi.getBanners().catch(() => []),
      homeApi.getQuickNav().catch(() => []),
      homeApi.getGameEntries().catch(() => []),
      homeApi.getNotice().catch(() => null),
      promoApi.getList().catch(() => ({ list: [] }))
    ])
    banners.value = bannerData
    quickNavs.value = navData
    gameEntries.value = gameData
    notice.value = noticeData?.show ? noticeData : null
    promos.value = (promoData.list || []).slice(0, 3)
  } catch (e) {
    // 静默失败
  }
}

function goLink(link) {
  if (link) router.push(link)
}

function goChat() {
  router.push('/chat')
}

function goQuickNav(item) {
  if (item.requiresAuth && !authStore.isLoggedIn) {
    appStore.openAuthPopup('login', item.path)
    return
  }
  router.push(item.path)
}

function goGame(game) {
  router.push(game.path)
}

function goPromoDetail(id) {
  router.push(`/promo/detail/${id}`)
}

onMounted(() => {
  if (appStore.siteConfig?.siteName) {
    siteName.value = appStore.siteConfig.siteName
  }
  loadData()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #0d0d0d;
  padding-bottom: 60px;
}
.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #1a1a1a;
  position: sticky;
  top: 0;
  z-index: 10;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.header-logo {
  font-size: 22px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #e8b860;
}
.header-right {
  color: #b0b0b0;
}
.home-notice {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background: #1a1a1a;
  border-bottom: 1px solid #2a2a2a;
  gap: 8px;
}
.home-notice :deep(.van-notice-bar) {
  flex: 1;
  padding: 0;
}
.home-banner {
  padding: 12px 16px 0;
}
.banner-img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 12px;
}
.home-quick-nav {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 16px;
}
.quick-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 0;
  cursor: pointer;
}
.quick-nav-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2a2a2a, #1a1a1a);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #333;
}
.icon-img {
  width: 24px;
  height: 24px;
  object-fit: contain;
}
.quick-nav-name {
  font-size: 12px;
  color: #b0b0b0;
}
.home-section {
  padding: 0 16px 16px;
}
.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.title-bar {
  width: 3px;
  height: 14px;
  background: linear-gradient(180deg, #e8b860, #c99a3e);
  border-radius: 2px;
  margin-right: 8px;
}
.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #f0f0f0;
  flex: 1;
}
.title-more {
  font-size: 12px;
  color: #808080;
}
.game-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.game-card {
  background: #1a1a1a;
  border-radius: 12px;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  border: 1px solid #2a2a2a;
}
.game-card:active {
  transform: scale(0.98);
}
.game-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #222;
  border-radius: 10px;
  flex-shrink: 0;
}
.game-icon-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}
.game-name {
  font-size: 13px;
  color: #d0d0d0;
  font-weight: 500;
}
.promo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.promo-card {
  background: #1a1a1a;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #2a2a2a;
}
.promo-img {
  width: 100%;
  height: 100px;
  object-fit: cover;
}
.promo-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
}
.promo-title {
  font-size: 14px;
  color: #e0e0e0;
  font-weight: 500;
}
.promo-tag {
  font-size: 11px;
  color: #e8b860;
  border: 1px solid #e8b860;
  padding: 2px 8px;
  border-radius: 10px;
}
.home-footer {
  text-align: center;
  padding: 20px;
}
.home-footer p {
  font-size: 11px;
  color: #444;
  margin: 0;
}
</style>
