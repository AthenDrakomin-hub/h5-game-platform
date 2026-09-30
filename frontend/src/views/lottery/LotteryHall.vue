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
    games.value = data?.length ? data : getDefaultGames(activeCategory.value)
  } catch (e) {
    games.value = getDefaultGames(activeCategory.value)
  } finally {
    loading.value = false
  }
}

function getDefaultGames(cat) {
  const map = {
    pk10: [
      { code: 'pk10-1', name: '北京赛车PK10', icon: '🏁', status: 'online' },
      { code: 'pk10-2', name: '幸运飞艇PK10', icon: '🚀', status: 'online' },
      { code: 'pk10-3', name: '澳洲幸运10', icon: '🦘', status: 'online' },
    ],
    ssc: [
      { code: 'ssc-1', name: '重庆时时彩', icon: '🎲', status: 'online' },
      { code: 'ssc-2', name: '新疆时时彩', icon: '🃏', status: 'online' },
      { code: 'ssc-3', name: '天津时时彩', icon: '⭐', status: 'maintenance' },
    ],
    lhc: [
      { code: 'lhc-1', name: '香港六合彩', icon: '🐎', status: 'online' },
      { code: 'lhc-2', name: '澳门六合彩', icon: '🎯', status: 'online' },
    ],
    pc28: [
      { code: 'pc28-1', name: 'PC28加拿大', icon: '🍀', status: 'online' },
      { code: 'pc28-2', name: 'PC28新加坡', icon: '🌴', status: 'online' },
    ],
  }
  return map[cat] || map.pk10
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
  background: var(--app-bg);
  display: flex;
  flex-direction: column;
}
:deep(.van-nav-bar) {
  background: transparent !important;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left), :deep(.van-nav-bar__right) {
  color: var(--gold-1) !important;
}
.hall-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.category-sidebar {
  width: 100px;
  background: var(--card-bg);
  flex-shrink: 0;
}
.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  cursor: pointer;
  border-right: 3px solid transparent;
  transition: all 0.2s;
}
.category-item.active {
  background: linear-gradient(90deg, rgba(212,168,75,0.15), transparent);
  border-right-color: var(--gold-3);
}
.cat-icon {
  font-size: 22px;
  margin-bottom: 4px;
}
.cat-name {
  font-size: 12px;
  color: var(--text-hint);
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
  background: var(--card-bg);
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
  color: var(--gold-1);
  font-weight: 500;
}
.game-status {
  font-size: 11px;
  color: #f0d080;
}
.game-status.maintenance {
  color: var(--text-hint);
}
</style>
