<template>
  <div class="trend-panel">
    <div class="trend-header">
      <span class="trend-title">走势图</span>
      <van-dropdown-menu>
        <van-dropdown-item v-model="trendType" :options="trendOptions" />
      </van-dropdown-menu>
    </div>
    <div class="trend-table-wrap" v-if="periods.length">
      <table class="trend-table">
        <thead>
          <tr>
            <th class="issue-col">期号</th>
            <th v-for="n in numberRange" :key="n">{{ n }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(period, idx) in periods" :key="idx">
            <td class="issue-col">{{ period.issue }}</td>
            <td
              v-for="n in numberRange"
              :key="n"
              :class="getCellClass(period.numbers, n)"
            >
              {{ isNumberHit(period.numbers, n) ? n : '' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else class="trend-loading">
      <van-loading color="#e8b860">加载中...</van-loading>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { lotteryApi } from '@/api/lottery'

const props = defineProps({
  gameCode: { type: String, required: true },
  category: { type: String, default: 'pk10' }
})

const periods = ref([])
const trendType = ref('basic')

const trendOptions = [
  { text: '基本走势', value: 'basic' },
  { text: '大小走势', value: 'bigsmall' },
  { text: '单双走势', value: 'oddeven' }
]

const numberRange = computed(() => {
  switch (props.category) {
    case 'pk10': return 10
    case 'ssc': return 10
    case 'pc28': return 10
    case 'lhc': return 49
    default: return 10
  }
})

function isNumberHit(numbers, n) {
  if (Array.isArray(numbers)) {
    return numbers.includes(n)
  }
  if (numbers?.red) {
    return numbers.red.includes(n) || numbers.blue === n
  }
  return false
}

function getCellClass(numbers, n) {
  if (isNumberHit(numbers, n)) {
    if (numbers?.blue === n) return 'cell-hit cell-blue'
    return 'cell-hit'
  }
  return 'cell-empty'
}

async function loadTrend() {
  try {
    const data = await lotteryApi.getTrendData(props.gameCode, trendType.value)
    periods.value = data.periods || []
  } catch (e) {
    periods.value = []
  }
}

onMounted(() => {
  loadTrend()
})
</script>

<style scoped>
.trend-panel {
  background: #1a1a1a;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
}
.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.trend-title {
  font-size: 15px;
  font-weight: 600;
  color: #f0f0f0;
}
:deep(.van-dropdown-menu) {
  background: transparent;
  width: auto;
}
:deep(.van-dropdown-menu__bar) {
  background: transparent;
  height: 32px;
}
:deep(.van-dropdown-item__title) {
  color: #e8b860;
  font-size: 13px;
}
.trend-table-wrap {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
.trend-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 11px;
}
.trend-table th {
  background: #2a2a2a;
  color: #888;
  font-weight: 500;
  padding: 6px 2px;
  text-align: center;
  border: 1px solid #333;
  min-width: 24px;
}
.trend-table td {
  padding: 6px 2px;
  text-align: center;
  border: 1px solid #2a2a2a;
  color: #ccc;
  min-width: 24px;
}
.issue-col {
  min-width: 70px !important;
  font-size: 10px;
  color: #666;
}
.cell-hit {
  background: linear-gradient(135deg, #e8b860, #c99a3e);
  color: #1a1a1a !important;
  font-weight: 600;
  border-radius: 50%;
}
.cell-blue {
  background: linear-gradient(135deg, #4a90d9, #357abd) !important;
  color: #fff !important;
}
.cell-empty {
  color: #333;
}
.trend-loading {
  padding: 30px;
  text-align: center;
}
</style>
