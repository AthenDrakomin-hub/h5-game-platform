<template>
  <div class="sub-games-page">
    <van-nav-bar :title="platformName" left-arrow @click-left="$router.back()" />
    <div class="sub-games-content">
      <div v-if="loading" class="loading-wrap">
        <van-loading color="#f0d080">加载中...</van-loading>
      </div>
      <div v-else class="games-grid">
        <div
          v-for="game in games"
          :key="game.id"
          class="game-item"
          @click="enterGame(game)"
        >
          <img :src="game.icon" :alt="game.name" class="game-icon" />
          <span class="game-name">{{ game.name }}</span>
          <span v-if="game.isHot" class="hot-tag">HOT</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { casinoApi } from '@/api/casino'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const platformName = ref(route.query.name || '游戏列表')
const games = ref([])
const loading = ref(false)

async function loadGames() {
  loading.value = true
  try {
    const data = await casinoApi.getGames(route.query.platform || 'ag')
    games.value = data
  } catch (e) {
    games.value = []
  } finally {
    loading.value = false
  }
}

function enterGame(game) {
  if (!authStore.isLoggedIn) {
    appStore.openAuthPopup('login', route.fullPath)
    return
  }
  router.push({ path: '/fs-game', query: { gameId: game.id, gameCode: game.gameCode, platform: game.venueCode, title: game.name } })
}

onMounted(() => {
  loadGames()
})
</script>

<style scoped>
.sub-games-page {
  min-height: 100vh;
  background: var(--app-bg);
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), var(--card-bg);
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: var(--gold-1) !important;
}
.sub-games-content {
  padding: 12px;
}
.loading-wrap {
  padding: 40px;
  text-align: center;
}
.games-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.game-item {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), var(--card-bg);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 1px solid rgba(255,255,255,0.12);
}
.game-item:active {
  transform: scale(0.96);
}
.game-icon {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}
.game-name {
  display: block;
  padding: 6px 4px;
  font-size: 11px;
  color: var(--gold-1);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.hot-tag {
  position: absolute;
  top: 4px;
  right: 4px;
  font-size: 9px;
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%);
  color: #3a2610;
  padding: 1px 4px;
  border-radius: 3px;
  font-weight: 700;
}
</style>
