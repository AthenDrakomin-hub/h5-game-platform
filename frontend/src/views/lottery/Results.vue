<template>
  <div class="results-page">
    <van-nav-bar title="开奖结果" left-arrow @click-left="$router.back()">
      <template #right>
        <van-dropdown-menu>
          <van-dropdown-item v-model="activeGame" :options="gameOptions" @change="loadResults" />
        </van-dropdown-menu>
      </template>
    </van-nav-bar>

    <div class="results-content">
      <div v-if="loading" class="loading-wrap">
        <van-loading color="#f0d080">加载中...</van-loading>
      </div>
      <div v-else class="results-list">
        <div v-for="(item, idx) in results" :key="idx" class="result-card">
          <div class="result-header">
            <span class="result-issue">第 {{ item.issue }} 期</span>
            <span class="result-time">{{ formatTime(item.openTime) }}</span>
          </div>
          <div class="result-numbers">
            <span
              v-for="(num, ni) in getNumberArray(item.numbers)"
              :key="ni"
              class="result-ball"
              :class="{ special: isSpecialBall(item.numbers, ni) }"
            >
              {{ num }}
            </span>
          </div>
        </div>
      </div>
      <div v-if="!loading && results.length === 0" class="empty-state">
        <img src="/empty-state.svg" alt="empty" class="empty-img" />
        <p>暂无开奖记录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { lotteryApi } from '@/api/lottery'
import dayjs from 'dayjs'

const activeGame = ref('bjpk10')
const results = ref([])
const loading = ref(false)

const gameOptions = [
  { text: '北京PK10', value: 'bjpk10' },
  { text: '加拿大PK10', value: 'jndpk10' },
  { text: '重庆时时彩', value: 'cqssc' },
  { text: '香港六合彩', value: 'hk6' },
  { text: '加拿大28', value: 'pc28' }
]

function getNumberArray(numbers) {
  if (Array.isArray(numbers)) return numbers
  if (numbers?.red) return [...numbers.red, numbers.blue]
  return []
}

function isSpecialBall(numbers, index) {
  if (numbers?.blue) {
    return index === numbers.red.length
  }
  return false
}

function formatTime(time) {
  return dayjs(time).format('MM-DD HH:mm')
}

async function loadResults() {
  loading.value = true
  try {
    const data = await lotteryApi.getHistoryResults(activeGame.value, 1, 30)
    results.value = data.list || []
  } catch (e) {
    results.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadResults()
})
</script>

<style scoped>
.results-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f2e0b8 !important;
}
:deep(.van-dropdown-menu) {
  background: transparent;
  width: auto;
}
:deep(.van-dropdown-menu__bar) {
  background: transparent;
  height: 36px;
}
:deep(.van-dropdown-item__title) {
  color: #f0d080;
  font-size: 13px;
}
.results-content {
  padding: 12px;
}
.loading-wrap {
  padding: 40px;
  text-align: center;
}
.results-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.result-card {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 12px;
  padding: 14px;
  border: 1px solid rgba(255,255,255,0.12);
}
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.result-issue {
  font-size: 14px;
  color: #f0d080;
  font-weight: 600;
}
.result-time {
  font-size: 11px;
  color: #6a5a40;
}
.result-numbers {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.result-ball {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%);
  border-radius: 50%;
  font-size: 13px;
  color: #3a2610;
  font-weight: 700;
}
.result-ball.special {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%);
  color: #fff;
}
.empty-state {
  padding: 60px 20px;
  text-align: center;
}
.empty-img {
  width: 160px;
  height: auto;
  margin-bottom: 12px;
}
.empty-state p {
  color: #6a5a40;
  font-size: 14px;
}
</style>
