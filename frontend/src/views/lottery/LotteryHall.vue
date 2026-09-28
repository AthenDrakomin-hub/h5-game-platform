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
          <GoldIcon :name="cat.icon" :size="22" class="cat-icon" />
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </div>

      <!-- 游戏列表 -->
      <div class="game-list-area">
        <div v-if="loading" class="loading-wrap">
          <van-loading color="#f0d080">加载中...</van-loading>
        </div>
        <div v-else class="game-grid">
          <div
            v-for="game in games"
            :key="game.code"
            class="game-card"
            @click="goGame(game)"
          >
            <div class="game-icon">
              <img v-if="game.icon" :src="game.icon" :alt="game.name" class="game-icon-img" />
              <span v-else>{{ game.icon }}</span>
            </div>
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
import GoldIcon from '@/components/GoldIcon.vue'

const router = useRouter()

const categories = ref([
  { id: 'pk10', name: 'PK10', icon: 'bolt' },
  { id: 'ssc', name: '时时彩', icon: 'lottery' },
  { id: 'lhc', name: '六合彩', icon: 'trophy' },
  { id: 'pc28', name: '28彩', icon: 'gamepad' }
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
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
  display: flex;
  flex-direction: column;
}
:deep(.van-nav-bar) {
  background: transparent !important;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left), :deep(.van-nav-bar__right) {
  color: #f2e0b8 !important;
}
.hall-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.category-sidebar {
  width: 80px;
  background: rgba(0,0,0,0.2);
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
  background: linear-gradient(90deg, rgba(212,168,75,0.12), transparent);
  border-left-color: #d4a84b;
}
.cat-icon {
  font-size: 22px;
  margin-bottom: 4px;
}
.cat-name {
  font-size: 12px;
  color: #8a7a5a;
}
.category-item.active .cat-name {
  color: #f0d080;
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
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 12px;
  padding: 14px;
  border: 1px solid rgba(255,255,255,0.12);
  cursor: pointer;
}
.game-card:active {
  transform: scale(0.98);
  border-color: rgba(212,168,75,0.4);
}
.game-icon {
  font-size: 32px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.06);
  border-radius: 10px;
  flex-shrink: 0;
}
.game-icon-img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 8px;
}
.game-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.game-name {
  font-size: 15px;
  color: #f2e0b8;
  font-weight: 500;
}
.game-status {
  font-size: 11px;
  color: #f0d080;
}
.game-status.maintenance {
  color: #8a7a5a;
}
</style>
