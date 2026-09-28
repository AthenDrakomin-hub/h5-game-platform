<template>
  <div class="home-page">
    <!-- 顶部Logo -->
    <div class="home-logo-bar">
      <img src="/assets/logo.png" alt="NOVA" class="logo-img" />
    </div>

    <!-- Banner轮播 -->
    <div class="home-banner">
      <van-swipe :autoplay="3000" indicator-color="#d4a84b" class="banner-swipe">
        <van-swipe-item v-for="(banner, i) in banners" :key="banner.id || i" @click="goLink(banner.link)">
          <div class="banner-slide" :style="banner.image ? '' : `background:${banner.bg || 'linear-gradient(135deg,#2a1f10,#1a130a)'}`">
            <img v-if="banner.image" :src="banner.image" :alt="banner.title" class="banner-img" />
            <template v-else>
              <div class="banner-title">{{ banner.title || '欢迎来到新星娱乐' }}</div>
              <div class="banner-sub">{{ banner.subtitle || '精彩游戏 等你来玩' }}</div>
            </template>
          </div>
        </van-swipe-item>
        <van-swipe-item v-if="banners.length === 0" v-for="i in 3" :key="'local-'+i">
          <div class="banner-slide">
            <img :src="`/assets/banners/banner${i}.png`" :alt="`banner${i}`" class="banner-img" />
          </div>
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 大奖记录2列卡片 -->
    <div class="jackpot-section">
      <div class="jackpot-header">
        <span class="jackpot-title-text">大奖记录</span>
        <span class="jackpot-more" @click="$router.push('/promo')">更多 ›</span>
      </div>
      <div class="jackpot-grid">
        <div v-for="(item, i) in jackpotRecords" :key="i" class="jackpot-card">
          <img :src="`/assets/games/game${(i % 6) + 1}.png`" :alt="item.user" class="jackpot-game-icon" />
          <div class="jackpot-card-info">
            <span class="jackpot-user">恭喜 {{ item.user }}</span>
            <span class="jackpot-card-amount">¥{{ item.amount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 娱乐城提示条 -->
    <div class="casino-notice">
      <span class="notice-icon">🎰</span>
      <span class="notice-text">欢迎来到娱乐城，精彩游戏等你来玩！</span>
    </div>

    <!-- 操作按钮行 -->
    <div class="action-row">
      <div class="auth-btns">
        <div class="auth-btn register" @click="openAuth('register')">极速注册</div>
        <div class="auth-btn login" @click="openAuth('login')">登入账号</div>
      </div>
      <div class="action-icons">
        <div class="action-icon" @click="goPage('/casino')">
          <GoldIcon name="gift" :size="22" />
          <span>试玩</span>
        </div>
        <div class="action-icon" @click="goPage('/user/recharge')">
          <GoldIcon name="coin" :size="22" />
          <span>存款</span>
        </div>
        <div class="action-icon" @click="goPage('/user/withdraw')">
          <GoldIcon name="bankcard" :size="22" />
          <span>取款</span>
        </div>
        <div class="action-icon" @click="goPage('/promo')">
          <GoldIcon name="star" :size="22" />
          <span>奖励</span>
        </div>
      </div>
    </div>

    <!-- 游戏分类+内容区 -->
    <div class="game-section">
      <!-- 左侧分类导航 -->
      <div class="category-nav">
        <div
          v-for="cat in categories"
          :key="cat.key"
          class="category-item"
          :class="{ active: activeCategory === cat.key }"
          @click="activeCategory = cat.key"
        >
          <GoldIcon :name="cat.icon" :size="22" />
          <span>{{ cat.name }}</span>
          <div v-if="cat.hot" class="hot-badge">HOT</div>
        </div>
      </div>

      <!-- 右侧游戏内容 -->
      <div class="game-content">
        <!-- Tab切换 -->
        <div class="game-tabs">
          <span class="game-tab active">热门</span>
          <span class="game-tab">最近游戏</span>
          <span class="game-tab">收藏</span>
        </div>

        <!-- 游戏网格 -->
        <div class="game-grid">
          <div
            v-for="game in displayGames"
            :key="game.id || game.name"
            class="game-card"
            @click="goGame(game)"
          >
            <div class="game-icon-wrap">
              <img v-if="game.icon" :src="game.icon" :alt="game.name" class="game-icon-img" />
              <img v-else :src="`/assets/games/game${(game.id % 6) + 1}.png`" :alt="game.name" class="game-icon-img" />
            </div>
            <span class="game-name">{{ game.name }}</span>
          </div>
        </div>

        <!-- 更多游戏按钮 -->
        <div class="more-games" @click="$router.push('/lottery')">
          <span>更多游戏</span>
          <GoldIcon name="arrow" :size="14" color="#888" />
        </div>
      </div>
    </div>

    <!-- 底部宣传区 -->
    <div class="promo-footer">
      <div class="promo-features">
        <div class="feature-item"><GoldIcon name="shield" :size="20" /><span>安全加密</span></div>
        <div class="feature-item"><GoldIcon name="lock" :size="20" /><span>隐私保护</span></div>
        <div class="feature-item"><GoldIcon name="bolt" :size="20" /><span>极速出入金</span></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { homeApi } from '@/api/home'
import { showToast } from 'vant'
import GoldIcon from '@/components/GoldIcon.vue'

const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const banners = ref([])
const gameEntries = ref([])
const activeCategory = ref('hot')

// 大奖记录模拟数据
const jackpotRecords = ref([
  { user: 'jm***17', amount: '22,995.0' },
  { user: 'lj***76', amount: '39,214.6' },
  { user: 'to***53', amount: '42,468.5' },
  { user: 'mx***40', amount: '6,655.4' },
  { user: 'va***68', amount: '29,635.4' },
  { user: 'tn***86', amount: '11,589.4' },
  { user: 'sa***82', amount: '20,640.0' },
])

// 游戏分类
const categories = ref([
  { key: 'hot', name: '热门', icon: 'flame', hot: true },
  { key: 'electronic', name: '电子', icon: 'gamepad' },
  { key: 'chess', name: '棋牌', icon: 'chess' },
  { key: 'live', name: '视讯', icon: 'video' },
  { key: 'fish', name: '捕鱼', icon: 'fish' },
  { key: 'sports', name: '体育', icon: 'trophy' },
  { key: 'lottery', name: '彩票', icon: 'chart' },
  { key: 'esports', name: '电竞', icon: 'bolt' },
])

// 默认游戏数据
const defaultGames = [
  { id: 1, name: '赏金船长', path: '/casino' },
  { id: 2, name: '麻将胡了', path: '/casino' },
  { id: 3, name: '少林足球', path: '/casino' },
  { id: 4, name: '麻将胡了2', path: '/casino' },
  { id: 5, name: '赏金女王', path: '/casino' },
  { id: 6, name: '赏金大对决', path: '/casino' },
  { id: 7, name: 'PG电子', path: '/casino' },
  { id: 8, name: 'CQ9电子', path: '/casino' },
  { id: 9, name: '极速赛车', path: '/lottery/pk10' },
  { id: 10, name: '时时彩', path: '/lottery/ssc' },
  { id: 11, name: '幸运28', path: '/lottery/pc28' },
  { id: 12, name: '六合彩', path: '/lottery/lhc' },
]

const displayGames = computed(() => {
  if (gameEntries.value.length > 0) return gameEntries.value
  return defaultGames
})

async function loadData() {
  try {
    const [bannerData, gameData] = await Promise.all([
      homeApi.getBanners().catch(() => []),
      homeApi.getGameEntries().catch(() => []),
    ])
    banners.value = bannerData || []
    gameEntries.value = gameData || []
  } catch (e) {
    // 静默
  }
}

function openAuth(type) {
  appStore.openAuthPopup(type)
}

function goPage(path) {
  if (!authStore.isLoggedIn) {
    appStore.openAuthPopup('login', path)
    return
  }
  router.push(path)
}

function goLink(link) {
  if (link) router.push(link)
}

function goGame(game) {
  if (game.path) {
    if (!authStore.isLoggedIn && game.requiresAuth !== false) {
      appStore.openAuthPopup('login', game.path)
      return
    }
    router.push(game.path)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
  padding-bottom: 70px;
}

/* 顶部Logo */
.home-logo-bar {
  text-align: center;
  padding: 10px 0 6px;
}
.logo-img {
  height: 36px;
  object-fit: contain;
}

/* Banner */
.home-banner {
  padding: 0 12px;
}
.banner-slide {
  height: 160px;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.banner-img {
  width: 100%;
  height: 160px;
  object-fit: cover;
}
.banner-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(180deg, #f0d080, #d4a84b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.banner-sub {
  font-size: 13px;
  color: #f2e0b8;
  margin-top: 8px;
}

/* 大奖记录2列卡片 */
.jackpot-section {
  margin: 12px;
}
.jackpot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 0 2px;
}
.jackpot-title-text {
  font-size: 14px;
  font-weight: 600;
  color: #f2e0b8;
}
.jackpot-more {
  font-size: 12px;
  color: #8a7a5a;
}
.jackpot-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.jackpot-card {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(rgba(255,255,255,0.08) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.7), rgba(11,10,8,0.5));
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 10px;
  padding: 8px 10px;
}
.jackpot-game-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}
.jackpot-card-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.jackpot-user {
  font-size: 11px;
  color: #b0a080;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.jackpot-card-amount {
  font-size: 13px;
  font-weight: 700;
  color: #f0d080;
}

/* 娱乐城提示条 */
.casino-notice {
  margin: 0 12px 12px;
  padding: 10px 14px;
  background: linear-gradient(90deg, rgba(212,168,75,0.15), rgba(212,168,75,0.05));
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.notice-icon {
  font-size: 16px;
}
.notice-text {
  font-size: 12px;
  color: #f2e0b8;
}

/* 操作按钮行 */
.action-row {
  margin: 0 12px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.auth-btns {
  display: flex;
  gap: 8px;
}
.auth-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.auth-btn.register {
  background: linear-gradient(180deg, #f0d080, #d4a84b);
  color: #3a2610;
}
.auth-btn.login {
  background: rgba(255,255,255,0.08);
  color: #f0e6d0;
  border: 1px solid rgba(255,255,255,0.12);
}
.action-icons {
  display: flex;
  gap: 12px;
}
.action-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}
.action-icon span {
  font-size: 10px;
  color: #b0a080;
}

/* 游戏分类区 */
.game-section {
  margin: 0 12px;
  display: flex;
  gap: 10px;
}
.category-nav {
  width: 64px;
  flex-shrink: 0;
  background: linear-gradient(rgba(255,255,255,0.06), rgba(255,255,255,0)),
              linear-gradient(145deg, rgba(30,24,18,0.6), rgba(10,9,8,0.4));
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.08);
  padding: 8px 0;
  overflow-y: auto;
}
.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 4px;
  cursor: pointer;
  position: relative;
}
.category-item span {
  font-size: 10px;
  color: #8a7a5a;
}
.category-item.active {
  background: rgba(212,168,75,0.1);
}
.category-item.active span {
  color: #f0d080;
}
.hot-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: #ee0a24;
  color: #fff;
  font-size: 8px;
  padding: 1px 4px;
  border-radius: 4px;
  font-weight: 700;
}

.game-content {
  flex: 1;
  min-width: 0;
}
.game-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
  padding: 0 4px;
}
.game-tab {
  font-size: 13px;
  color: #8a7a5a;
  cursor: pointer;
}
.game-tab.active {
  color: #f0d080;
  font-weight: 600;
  position: relative;
}
.game-tab.active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  right: 0;
  height: 2px;
  background: #d4a84b;
  border-radius: 1px;
}

.game-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.game-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: transform 0.2s;
}
.game-card:active {
  transform: scale(0.96);
}
.game-icon-wrap {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(145deg, rgba(40,30,20,0.8), rgba(20,15,10,0.6));
  border: 1px solid rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.game-card:active .game-icon-wrap {
  border-color: rgba(212,168,75,0.6);
  box-shadow: 0 0 12px rgba(212,168,75,0.3);
}
.game-icon-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.game-icon-placeholder {
  font-size: 24px;
  font-weight: 700;
  color: #d4a84b;
}
.game-name {
  font-size: 11px;
  color: #d0c4a8;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.more-games {
  margin-top: 12px;
  padding: 10px;
  text-align: center;
  background: rgba(255,255,255,0.04);
  border-radius: 8px;
  font-size: 12px;
  color: #8a7a5a;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* 底部宣传 */
.promo-footer {
  margin: 20px 12px;
  padding: 16px;
  background: linear-gradient(rgba(255,255,255,0.06), rgba(255,255,255,0)),
              linear-gradient(145deg, rgba(30,24,18,0.6), rgba(10,9,8,0.4));
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.08);
}
.promo-features {
  display: flex;
  justify-content: space-around;
}
.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.feature-item span {
  font-size: 11px;
  color: #8a7a5a;
}
</style>
