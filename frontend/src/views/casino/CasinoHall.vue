<template>
  <div class="casino-hall">
    <van-nav-bar title="娱乐城" left-arrow @click-left="$router.back()">
      <template #right>
        <van-icon name="search" size="18" />
      </template>
    </van-nav-bar>

    <div class="casino-content">
      <!-- 热门游戏：横向滚动 -->
      <div class="section">
        <div class="section-header">
          <div class="section-bar"></div>
          <span class="section-title">热门游戏</span>
        </div>
        <div class="game-scroll">
          <div
            v-for="(game, i) in hotGames.length ? hotGames : fallbackHot"
            :key="'hot-'+i"
            class="game-card-h"
            @click="enterGame(game)"
          >
            <img :src="game.icon || `/assets/games/game${(i % 6) + 1}.webp`" :alt="game.name" class="game-thumb" />
            <span class="game-name-overlay">{{ game.name }}</span>
          </div>
        </div>
      </div>

      <!-- 平台列表：纵向卡片 -->
      <div class="section">
        <div class="section-header">
          <div class="section-bar"></div>
          <span class="section-title">游戏平台</span>
        </div>
        <div class="platform-grid">
          <div
            v-for="platform in platforms.length ? platforms : fallbackPlatforms"
            :key="platform.id"
            class="platform-card"
            :class="{ maintenance: platform.status === 'maintenance' }"
            @click="enterPlatform(platform)"
          >
            <div class="platform-icon">
              <img v-if="platform.icon" :src="platform.icon" :alt="platform.name" class="platform-icon-img" />
              <img v-else-if="platform.localIcon" :src="platform.localIcon" :alt="platform.name" class="platform-icon-img" />
              <span v-else>{{ platform.name.slice(0, 2) }}</span>
            </div>
            <div class="platform-info">
              <span class="platform-name">{{ platform.name }}</span>
              <span class="platform-count">{{ platform.gameCount || '—' }}款游戏</span>
            </div>
            <span v-if="platform.status === 'maintenance'" class="maintenance-tag">维护</span>
            <van-icon v-else name="arrow" class="platform-arrow" />
          </div>
        </div>
      </div>

      <!-- 最新游戏：3列宫格 -->
      <div class="section">
        <div class="section-header">
          <div class="section-bar"></div>
          <span class="section-title">最新上线</span>
        </div>
        <div class="game-grid">
          <div
            v-for="(game, i) in newGames.length ? newGames : fallbackNew"
            :key="'new-'+i"
            class="game-card"
            @click="enterGame(game)"
          >
            <div class="game-image">
              <img :src="game.icon || `/assets/games/game${(i % 6) + 1}.webp`" :alt="game.name" class="game-img" />
            </div>
            <div class="game-name">{{ game.name }}</div>
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

const platforms = ref([])
const hotGames = ref([])
const newGames = ref([])

// 本地兜底数据（API 未返回时用真实场馆图）
const fallbackPlatforms = [
  { id: 'pg', name: 'PG电子', localIcon: '/assets/venue/pg.jpg', gameCount: 100 },
  { id: 'cq9', name: 'CQ9电子', localIcon: '/assets/venue/cq9.jpg', gameCount: 80 },
  { id: 'jdb', name: 'JDB电子', localIcon: '/assets/venue/jdb.jpg', gameCount: 60 },
  { id: 'pp', name: 'PP电子', localIcon: '/assets/venue/pp.jpg', gameCount: 50 },
  { id: 'ag', name: 'AG真人', localIcon: '/assets/venue/ag.png', gameCount: 30 },
  { id: 'pt', name: 'PT电子', localIcon: '/assets/venue/pt.jpg', gameCount: 40 },
]
const fallbackHot = [
  { id: 1, name: '赏金船长' }, { id: 2, name: '麻将胡了' },
  { id: 3, name: '少林足球' }, { id: 4, name: '麻将胡了2' },
  { id: 5, name: '赏金女王' }, { id: 6, name: '赏金大对决' },
]
const fallbackNew = [
  { id: 7, name: '少林足球' }, { id: 8, name: '麻将胡了' },
  { id: 9, name: '赏金船长' }, { id: 10, name: '赏金女王' },
  { id: 11, name: '麻将胡了2' }, { id: 12, name: '赏金大对决' },
]

async function loadData() {
  try {
    const [p, h, n] = await Promise.all([
      casinoApi.getPlatforms().catch(() => []),
      casinoApi.getHotGames().catch(() => []),
      casinoApi.getNewGames().catch(() => [])
    ])
    platforms.value = p || []
    hotGames.value = h || []
    newGames.value = n || []
  } catch (e) { /* 用兜底 */ }
}

function enterPlatform(platform) {
  if (platform.status === 'maintenance') { showFailToast('该平台维护中'); return }
  router.push({ path: '/casino-sub-games', query: { platform: platform.id, name: platform.name } })
}

function enterGame(game) {
  if (!authStore.isLoggedIn) { appStore.openAuthPopup('login', '/casino'); return }
  router.push({ path: '/fs-game', query: { gameId: game.id, gameCode: game.gameCode, platform: game.venueCode, title: game.name } })
}

onMounted(loadData)
</script>

<style scoped>
.casino-hall {
  min-height: 100vh;
  background: var(--app-bg);
  padding-top: var(--safe-top);
  padding-bottom: calc(60px + var(--safe-bottom));
}
:deep(.van-nav-bar) { background: transparent !important; }
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left), :deep(.van-nav-bar__right) { color: var(--gold-1) !important; }

.casino-content { padding: 12px; }
.section { margin-bottom: 20px; }

/* section 头：金色竖条 + 标题 */
.section-header {
  display: flex; align-items: center; gap: 8px; margin-bottom: 12px;
}
.section-bar {
  width: 4px; height: 18px; border-radius: 999px; background: var(--gold-bar);
}
.section-title {
  font-size: 15px; font-weight: 500; color: var(--gold-1);
}

/* 热门游戏横向滚动 */
.game-scroll { display: flex; gap: 10px; overflow-x: auto; padding-bottom: 4px; }
.game-card-h {
  flex-shrink: 0; width: 140px; height: 80px;
  border-radius: 12px; overflow: hidden; position: relative; cursor: pointer;
}
.game-thumb { width: 100%; height: 100%; object-fit: cover; }
.game-name-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  padding: 6px 8px;
  background: linear-gradient(transparent, rgba(0,0,0,0.85));
  font-size: 11px; color: #fff;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

/* 平台列表 */
.platform-grid { display: flex; flex-direction: column; gap: 10px; }
.platform-card {
  display: flex; align-items: center; gap: 12px;
  background: var(--card-bg);
  border-radius: 12px; padding: 14px;
  border: 1px solid var(--card-border);
  cursor: pointer; position: relative;
}
.platform-card:active { transform: scale(0.98); border-color: rgba(212,168,75,0.4); }
.platform-card.maintenance { opacity: 0.5; }
.platform-icon {
  width: 48px; height: 48px;
  display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.06);
  border-radius: 10px; flex-shrink: 0; overflow: hidden;
}
.platform-icon-img { width: 100%; height: 100%; object-fit: cover; }
.platform-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.platform-name { font-size: 15px; color: var(--gold-1); font-weight: 500; }
.platform-count { font-size: 11px; color: var(--text-hint); }
.maintenance-tag {
  font-size: 10px; color: #ff976a; border: 1px solid #ff976a;
  padding: 2px 6px; border-radius: 4px;
}
.platform-arrow { color: var(--text-hint); font-size: 16px; }

/* 最新游戏 3列 */
.game-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.game-card { cursor: pointer; }
.game-card:active { transform: scale(0.96); }
.game-image {
  width: 100%; aspect-ratio: 1; border-radius: 12px; overflow: hidden;
  background: var(--card-bg);
}
.game-img { width: 100%; height: 100%; object-fit: cover; }
.game-name {
  font-size: 12px; color: var(--gold-1); text-align: center;
  margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
</style>
