<template>
  <div class="home-page">
    <!-- 顶部 Header：左空、中 logo、右空（Telegram 内顶部安全区） -->
    <div class="top-header">
      <div class="header-left"></div>
      <img src="/assets/common/logo.png" alt="NOVA" class="logo-center" />
      <div class="header-right"></div>
    </div>

    <!-- Banner 轮播 -->
    <div class="banner-section">
      <van-swipe :autoplay="3000" indicator-color="#d4a84b" class="banner-swipe">
        <van-swipe-item v-for="(banner, i) in banners" :key="banner.id || i" @click="goLink(banner.link)">
          <div class="banner-slide" :style="banner.image ? '' : `background:${banner.bg || 'linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6))'}`">
            <img v-if="banner.image" :src="banner.image" :alt="banner.title" class="banner-img" />
            <template v-else>
              <div class="banner-title">{{ banner.title || '欢迎来到新星娱乐' }}</div>
              <div class="banner-sub">{{ banner.subtitle || '精彩游戏 等你来玩' }}</div>
            </template>
          </div>
        </van-swipe-item>
        <van-swipe-item v-if="banners.length === 0" v-for="i in 5" :key="'local-'+i">
          <div class="banner-slide">
            <img :src="`/assets/banners/banner${i}.png`" :alt="`banner${i}`" class="banner-img" />
          </div>
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 大奖双行跑马灯 -->
    <div class="big-prize-section">
      <div class="big-prize-title">
        <span class="diamond">◆</span>
        <span>大奖记录</span>
        <span class="diamond">◆</span>
      </div>
      <div class="big-prize-marquee">
        <div class="big-prize-track big-prize-track--r1">
          <div v-for="(item, i) in marqueeRow1" :key="'r1-'+i" class="big-prize-card">
            <div class="big-prize-text">
              <span class="big-prize-user">恭喜{{ item.user }}</span>
              <span class="big-prize-amt">赢得 {{ item.amount }}</span>
            </div>
          </div>
        </div>
        <div class="big-prize-track big-prize-track--r2">
          <div v-for="(item, i) in marqueeRow2" :key="'r2-'+i" class="big-prize-card">
            <div class="big-prize-text">
              <span class="big-prize-user">恭喜{{ item.user }}</span>
              <span class="big-prize-amt">赢得 {{ item.amount }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 游戏区：左侧 sidebar + 右侧 section 流 -->
    <div class="game-layout">
      <!-- 左侧分类 sidebar（100 宽） -->
      <div class="category-sidebar">
        <div
          v-for="cat in categories"
          :key="cat.key"
          class="sidebar-item"
          :class="{ active: activeCategory === cat.key }"
          @click="onCategoryClick(cat)"
        >
          {{ cat.name }}
        </div>
      </div>

      <!-- 右侧 section 流 -->
      <div class="section-stream">
        <!-- 热门 section -->
        <div class="section-block section-hot">
          <div class="section-header">
            <div class="section-bar"></div>
            <span class="section-title">热门游戏</span>
            <div class="section-tabs-row">
              <span class="section-tab active">热门</span>
              <span class="section-tab">最近</span>
              <span class="section-tab">收藏</span>
            </div>
          </div>
          <div class="game-grid">
            <div
              v-for="game in displayGames.slice(0, 6)"
              :key="game.id || game.name"
              class="game-card"
              @click="goGame(game)"
            >
              <div class="game-image">
                <img v-if="game.icon" :src="game.icon" :alt="game.name" class="game-img" />
                <img v-else :src="`/assets/games/game${(game.id % 6) + 1}.webp`" :alt="game.name" class="game-img" />
              </div>
              <div class="game-name">{{ game.name }}</div>
            </div>
          </div>
        </div>

        <!-- 电子 section -->
        <div class="section-block section-slot">
          <div class="section-header">
            <div class="section-bar"></div>
            <span class="section-title">电子游戏</span>
          </div>
          <div class="game-grid">
            <div
              v-for="g in electronicGames"
              :key="g.id"
              class="game-card"
              @click="goPage('/casino')"
            >
              <div class="game-image">
                <img :src="`/assets/games/game${(g.id % 6) + 1}.webp`" :alt="g.name" class="game-img" />
              </div>
              <div class="game-name">{{ g.name }}</div>
            </div>
          </div>
        </div>

        <!-- 棋牌 section -->
        <div class="section-block section-chess">
          <div class="section-header">
            <div class="section-bar"></div>
            <span class="section-title">棋牌游戏</span>
          </div>
          <div class="game-grid">
            <div
              v-for="g in chessGames"
              :key="g.id"
              class="game-card"
              @click="goPage('/casino')"
            >
              <div class="game-image">
                <img :src="`/assets/games/game${(g.id % 6) + 1}.webp`" :alt="g.name" class="game-img" />
              </div>
              <div class="game-name">{{ g.name }}</div>
            </div>
          </div>
        </div>
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

const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const banners = ref([])
const gameEntries = ref([])
const activeCategory = ref('hot')

// 大奖跑马灯数据（双行）
const jackpotRecords = ref([
  { user: 'td***13', amount: '33,068.2' },
  { user: 'pe***67', amount: '41,225.3' },
  { user: 'pw***76', amount: '36,566.3' },
  { user: 'px***50', amount: '36,326.3' },
  { user: 'ry***51', amount: '41,877.5' },
  { user: 'ht***99', amount: '42,925.2' },
  { user: 'xx***37', amount: '39,007.8' },
  { user: 'jo***50', amount: '23,764.3' },
])
// 复制一份实现无缝滚动
const marqueeRow1 = computed(() => [...jackpotRecords.value, ...jackpotRecords.value])
const marqueeRow2 = computed(() => [...jackpotRecords.value].reverse().concat([...jackpotRecords.value].reverse()))

const categories = ref([
  { key: 'hot', name: '热门' },
  { key: 'electronic', name: '电子' },
  { key: 'chess', name: '棋牌' },
  { key: 'live', name: '视讯' },
  { key: 'fish', name: '捕鱼' },
  { key: 'sports', name: '体育' },
  { key: 'lottery', name: '彩票' },
  { key: 'esports', name: '电竞' },
])

const defaultGames = [
  { id: 1, name: '赏金船长', path: '/casino' },
  { id: 2, name: '麻将胡了', path: '/casino' },
  { id: 3, name: '少林足球', path: '/casino' },
  { id: 4, name: '麻将胡了2', path: '/casino' },
  { id: 5, name: '赏金女王', path: '/casino' },
  { id: 6, name: '赏金大对决', path: '/casino' },
]
const electronicGames = ref([
  { id: 7, name: 'PG电子' },
  { id: 8, name: 'CQ9电子' },
  { id: 9, name: 'PP电子' },
  { id: 10, name: 'JDB电子' },
  { id: 11, name: 'BNG电子' },
  { id: 12, name: 'FASTSPIN' },
])
const chessGames = ref([
  { id: 13, name: '龙虎斗' },
  { id: 14, name: '百家乐' },
  { id: 15, name: '三公' },
  { id: 16, name: '牛牛' },
  { id: 17, name: '德州扑克' },
  { id: 18, name: '斗地主' },
])

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
  } catch (e) { /* 静默 */ }
}

function goPage(path) {
  if (!authStore.isLoggedIn) {
    appStore.openAuthPopup('login', path)
    return
  }
  router.push(path)
}

function onCategoryClick(cat) {
  if (cat.key === 'lottery') {
    router.push('/lottery')
    return
  }
  if (['live','electronic','chess','fish'].includes(cat.key)) {
    router.push('/casino')
    return
  }
  activeCategory.value = cat.key
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

onMounted(() => { loadData() })
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #000;
  padding-top: env(safe-area-inset-top, 0px);
  padding-bottom: calc(70px + env(safe-area-inset-bottom, 0px));
}

/* ===== Header：53px, padding 0 10px 7px, flex 三栏 ===== */
.top-header {
  height: 53px;
  padding: 0 10px 7px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left, .header-right { width: 130px; }
.logo-center {
  height: 44px;
  width: 130px;
  object-fit: contain;
  margin: 0 auto;
}

/* ===== Banner：406×166 ===== */
.banner-section {
  margin: 0 12px;
}
.banner-swipe {
  border-radius: 12px;
  overflow: hidden;
}
.banner-slide {
  height: 166px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.banner-img {
  width: 100%;
  height: 166px;
  object-fit: cover;
}
.banner-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(180deg, #f0d080, #d4a84b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.banner-sub { font-size: 13px; color: #f2e0b8; margin-top: 8px; }

/* ===== 大奖双行跑马灯 ===== */
.big-prize-section {
  margin: 12px 0 0;
}
.big-prize-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 19px;
  font-size: 13px;
  font-weight: 500;
  color: rgba(244, 226, 189, 0.9);
}
.big-prize-title .diamond {
  color: #d4a84b;
  font-size: 10px;
}
.big-prize-marquee {
  height: 56px;
  border-radius: 14px;
  overflow: hidden;
  position: relative;
  background: rgba(255, 255, 255, 0.03);
}
.big-prize-track {
  display: inline-flex;
  gap: 8px;
  padding: 4px 0;
  white-space: nowrap;
  animation: marquee 30s linear infinite;
}
.big-prize-track--r2 {
  animation-direction: reverse;
  animation-duration: 35s;
}
@keyframes marquee {
  from { transform: translateX(0); }
  to   { transform: translateX(-50%); }
}
.big-prize-card {
  flex: 0 0 auto;
  width: 200px;
  height: 24px;
  padding: 4px 12px;
  border-radius: 16px;
  background: linear-gradient(145deg, rgba(212,168,75,0.12), rgba(212,168,75,0.04));
  display: flex;
  align-items: center;
}
.big-prize-text {
  display: flex;
  gap: 8px;
  align-items: baseline;
  width: 100%;
}
.big-prize-user {
  font-size: 12px;
  color: rgba(247, 235, 209, 0.9);
}
.big-prize-amt {
  font-size: 14px;
  font-weight: 500;
  color: rgb(229, 201, 139);
}

/* ===== 游戏区：sidebar + section 流 ===== */
.game-layout {
  display: flex;
  margin-top: 8px;
}

/* sidebar 100 宽, item 94×43, 16px 白色 */
.category-sidebar {
  width: 100px;
  flex-shrink: 0;
  padding: 10px 0 12px 6px;
  overflow-y: auto;
}
.sidebar-item {
  width: 94px;
  height: 43px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #fff;
  cursor: pointer;
  border-radius: 8px 0 0 8px;
}
.sidebar-item.active {
  background: linear-gradient(90deg, rgba(212,168,75,0.25), rgba(212,168,75,0.05));
  color: #f0d080;
  font-weight: 600;
  position: relative;
}
.sidebar-item.active::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  background: #d4a84b;
  border-radius: 2px;
}

/* section 流 */
.section-stream {
  flex: 1;
  min-width: 0;
  padding-right: 6px;
}
.section-block {
  padding: 12px 12px 10px 6px;
}
.section-header {
  display: flex;
  align-items: center;
  height: 28px;
  margin-bottom: 10px;
  gap: 8px;
}
.section-bar {
  width: 4px;
  height: 18px;
  border-radius: 999px;
  background: linear-gradient(180deg, #f0d080, #d4a84b);
}
.section-title {
  font-size: 15px;
  font-weight: 500;
  color: rgb(245, 230, 198);
  flex-shrink: 0;
}
.section-tabs-row {
  margin-left: auto;
  display: flex;
  gap: 12px;
}
.section-tab {
  font-size: 13px;
  color: rgba(239, 214, 156, 0.6);
  padding-bottom: 7px;
  cursor: pointer;
}
.section-tab.active {
  color: rgb(239, 214, 156);
  font-weight: 600;
  border-bottom: 2px solid #d4a84b;
}

/* game-grid 3 列, card 97×97 radius 12 */
.game-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.game-card {
  cursor: pointer;
}
.game-card:active {
  transform: scale(0.96);
}
.game-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(145deg, rgba(40,30,20,0.8), rgba(20,15,10,0.6));
}
.game-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.game-name {
  font-size: 12px;
  color: rgb(245, 230, 198);
  text-align: center;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
