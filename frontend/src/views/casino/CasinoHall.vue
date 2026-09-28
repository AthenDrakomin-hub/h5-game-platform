<template>
  <div class="casino-hall">
    <van-nav-bar title="娱乐城" left-arrow @click-left="$router.back()">
      <template #right>
        <van-icon name="search" size="18" @click="showSearch = !showSearch" />
      </template>
    </van-nav-bar>

    <div class="casino-content">
      <!-- 热门游戏 -->
      <div class="section">
        <div class="section-title">
          <span class="title-bar"></span>
          <span class="title-text">热门游戏</span>
        </div>
        <div class="game-scroll">
          <div
            v-for="game in hotGames"
            :key="game.id"
            class="game-card-horizontal"
            @click="enterGame(game)"
          >
            <img :src="game.icon" :alt="game.name" class="game-thumb" />
            <span class="game-name-overlay">{{ game.name }}</span>
          </div>
        </div>
      </div>

      <!-- 平台列表 -->
      <div class="section">
        <div class="section-title">
          <span class="title-bar"></span>
          <span class="title-text">游戏平台</span>
        </div>
        <div class="platform-grid">
          <div
            v-for="platform in platforms"
            :key="platform.id"
            class="platform-card"
            :class="{ maintenance: platform.status === 'maintenance' }"
            @click="enterPlatform(platform)"
          >
            <div class="platform-icon">
              <img v-if="platform.icon" :src="platform.icon" :alt="platform.name" class="platform-icon-img" />
              <span v-else>{{ platform.icon }}</span>
            </div>
            <div class="platform-info">
              <span class="platform-name">{{ platform.name }}</span>
              <span class="platform-count">{{ platform.gameCount }}款游戏</span>
            </div>
            <span v-if="platform.status === 'maintenance'" class="maintenance-tag">维护</span>
          </div>
        </div>
      </div>

      <!-- 最新游戏 -->
      <div class="section">
        <div class="section-title">
          <span class="title-bar"></span>
          <span class="title-text">最新上线</span>
        </div>
        <div class="game-grid-2">
          <div
            v-for="game in newGames"
            :key="game.id"
            class="game-card-vertical"
            @click="enterGame(game)"
          >
            <img :src="game.icon" :alt="game.name" class="game-thumb-vertical" />
            <span class="game-name-v">{{ game.name }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { casinoApi } from '@/api/casino'
import { showFailToast } from 'vant'

const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const showSearch = ref(false)
const platforms = ref([])
const hotGames = ref([])
const newGames = ref([])

async function loadData() {
  try {
    const [p, h, n] = await Promise.all([
      casinoApi.getPlatforms().catch(() => []),
      casinoApi.getHotGames().catch(() => []),
      casinoApi.getNewGames().catch(() => [])
    ])
    platforms.value = p
    hotGames.value = h
    newGames.value = n
  } catch (e) {
    // 静默
  }
}

function enterPlatform(platform) {
  if (platform.status === 'maintenance') {
    showFailToast('该平台维护中')
    return
  }
  router.push({ path: '/casino-sub-games', query: { platform: platform.id, name: platform.name } })
}

function enterGame(game) {
  if (!authStore.isLoggedIn) {
    appStore.openAuthPopup('login', '/casino')
    return
  }
  router.push({ path: '/fs-game', query: { gameId: game.id, gameCode: game.gameCode, platform: game.venueCode, title: game.name } })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.casino-hall {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
  padding-bottom: 60px;
}
:deep(.van-nav-bar) {
  background: transparent !important;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left), :deep(.van-nav-bar__right) {
  color: #f2e0b8 !important;
}
.casino-content {
  padding: 12px;
}
.section {
  margin-bottom: 20px;
}
.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.title-bar {
  width: 3px;
  height: 14px;
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%);
  border-radius: 2px;
  margin-right: 8px;
}
.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #f2e0b8;
}
.game-scroll {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
}
.game-card-horizontal {
  flex-shrink: 0;
  width: 140px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  border: 1px solid rgba(255,255,255,0.1);
}
.game-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.game-name-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 6px 8px;
  background: linear-gradient(transparent, rgba(0,0,0,0.85));
  font-size: 11px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.platform-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.platform-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 12px;
  padding: 14px;
  border: 1px solid rgba(255,255,255,0.12);
  cursor: pointer;
  position: relative;
}
.platform-card:active {
  transform: scale(0.98);
  border-color: rgba(212,168,75,0.4);
}
.platform-card.maintenance {
  opacity: 0.5;
}
.platform-icon {
  font-size: 28px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.06);
  border-radius: 10px;
  flex-shrink: 0;
}
.platform-icon-img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}
.platform-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.platform-name {
  font-size: 15px;
  color: #f2e0b8;
  font-weight: 500;
}
.platform-count {
  font-size: 11px;
  color: #8a7a5a;
}
.maintenance-tag {
  font-size: 10px;
  color: #f0d080;
  border: 1px solid #ff976a;
  padding: 2px 6px;
  border-radius: 4px;
}
.game-grid-2 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.game-card-vertical {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #2a2a2a;
}
.game-thumb-vertical {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}
.game-name-v {
  display: block;
  padding: 6px 8px;
  font-size: 11px;
  color: #d0c4a8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}
</style>
