<template>
  <div class="lottery-hall">
    <van-nav-bar title="彩票大厅" left-arrow @click-left="$router.back()">
      <template #right>
        <van-icon name="chart-trending-o" size="20" @click="goResults" />
      </template>
    </van-nav-bar>

    <div class="hall-content">
      <!-- 分类侧栏 -->
      <div class="category-sidebar">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-item"
          :class="{ active: activeCategory === cat.id }"
          @click="activeCategory = cat.id"
        >
          <span class="cat-icon">{{ cat.icon }}</span>
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </div>

      <!-- 游戏列表 -->
      <div class="game-list-area">
        <div v-if="loading" class="loading-wrap">
          <van-loading color="#e8b860">加载中...</van-loading>
        </div>
        <div v-else class="game-grid">
          <div
            v-for="game in games"
            :key="game.code"
            class="game-card"
            @click="goGame(game)"
          >
            <div class="game-icon">{{ game.icon }}</div>
            <div class="game-info">
              <span class="game-name">{{ game.name }}</span>
              <span class="game-status" :class="game.status">{{ game.status === 'online' ? '正在开奖' : '维护中' }}</span>
            </div>
            <van-icon name="arrow" color="#666" size="14" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { lotteryApi } from '@/api/lottery'

const router = useRouter()

const categories = ref([
  { id: 'pk10', name: 'PK10', icon: '🏎️' },
  { id: 'ssc', name: '时时彩', icon: '🎰' },
  { id: 'lhc', name: '六合彩', icon: '🎱' },
  { id: 'pc28', name: '28彩', icon: '🎲' }
])

const activeCategory = ref('pk10')
const games = ref([])
const loading = ref(false)

async function loadGames() {
  loading.value = true
  try {
    const data = await lotteryApi.getGameList(activeCategory.value)
    games.value = data
  } catch (e) {
    games.value = []
  } finally {
    loading.value = false
  }
}

function goGame(game) {
  const pathMap = { pk10: '/pk10', ssc: '/ssc', lhc: '/lhc', pc28: '/pc28' }
  router.push(`${pathMap[activeCategory.value]}/${game.code}`)
}

function goResults() {
  router.push('/results')
}

watch(activeCategory, () => {
  loadGames()
})

onMounted(() => {
  loadGames()
})
</script>

<style scoped>
.lottery-hall {
  min-height: 100vh;
  background: #0d0d0d;
  display: flex;
  flex-direction: column;
}
:deep(.van-nav-bar) {
  background: #1a1a1a;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left), :deep(.van-nav-bar__right) {
  color: #f0f0f0 !important;
}
.hall-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.category-sidebar {
  width: 80px;
  background: #151515;
  flex-shrink: 0;
}
.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.2s;
}
.category-item.active {
  background: #0d0d0d;
  border-left-color: #e8b860;
}
.cat-icon {
  font-size: 22px;
  margin-bottom: 4px;
}
.cat-name {
  font-size: 12px;
  color: #888;
}
.category-item.active .cat-name {
  color: #e8b860;
  font-weight: 600;
}
.game-list-area {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}
.loading-wrap {
  padding: 40px;
  text-align: center;
}
.game-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.game-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #1a1a1a;
  border-radius: 12px;
  padding: 14px;
  border: 1px solid #2a2a2a;
  cursor: pointer;
}
.game-card:active {
  transform: scale(0.98);
}
.game-icon {
  font-size: 32px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #222;
  border-radius: 10px;
  flex-shrink: 0;
}
.game-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.game-name {
  font-size: 15px;
  color: #e0e0e0;
  font-weight: 500;
}
.game-status {
  font-size: 11px;
  color: #07c160;
}
.game-status.maintenance {
  color: #888;
}
</style>
